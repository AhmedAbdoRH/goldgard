package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.data.local.GoldDatabase
import com.example.data.local.MarketSettingsAuditLogEntity
import com.example.data.local.SettingsEntity
import com.example.data.local.TransactionEntity
import com.example.data.remote.GoldApiService
import com.example.data.remote.InternalGoldPriceBackend
import com.example.data.remote.LivePriceResponse
import com.example.data.remote.provider.GoldApiHybridProvider
import com.example.util.GoldBullionPricingEngine
import com.example.data.remote.provider.GoldAPIProvider
import com.example.data.remote.provider.GoldPriceProvider
import com.example.data.remote.provider.ManualAdminProvider
import com.example.data.remote.provider.MetalpriceAPIProvider
import com.example.data.remote.provider.GoldPriceLiveComProvider
import com.example.data.remote.provider.GoogleSheetsPriceProvider
import com.example.data.remote.provider.EgyptianGoldLiveProvider
import com.example.data.remote.provider.IGoldPriceProvider
import com.example.data.remote.provider.PrimaryGoldPriceProvider
import com.example.data.remote.provider.BackupGoldPriceProvider
import com.example.data.remote.provider.GoldPriceAggregator
import com.example.model.Country
import com.example.model.GoldPrice
import com.example.model.GoldPriceResponse
import com.example.model.GoldSettings
import com.example.model.KaratSpread
import com.example.model.MarketAdminSettings
import com.example.model.PricePair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GoldRepository(private val context: Context) {

    private val db = GoldDatabase.getDatabase(context)
    private val transactionDao = db.transactionDao()
    private val settingsDao = db.settingsDao()
    private val auditLogDao = db.auditLogDao()

    @Volatile
    private var cachedMarketSettings: MarketAdminSettings = MarketAdminSettings()

    // Providers
    val goldApiHybridProvider = GoldApiHybridProvider(
        context = context,
        getSd = { cachedMarketSettings.saghaUsdRate },
        getK = { cachedMarketSettings.calibrationK },
        onOfficialUsdUpdated = { usd ->
            cachedMarketSettings = cachedMarketSettings.copy(cachedUsdRate = usd)
        }
    )

    val egyptianLiveProvider = EgyptianGoldLiveProvider(
        settingsProvider = { cachedMarketSettings },
        onSaveSettings = { saveMarketAdminSettings(it, "النظام", "تحديث سعر الصرف") },
        cacheWriter = { saveResponseToCache(it) },
        cacheReader = { loadResponseFromCache() }
    )

    val goldPriceLiveProvider = GoldPriceLiveComProvider(
        settingsProvider = { cachedMarketSettings },
        cacheWriter = { saveResponseToCache(it) },
        cacheReader = { loadResponseFromCache() }
    )

    val googleSheetsProvider = GoogleSheetsPriceProvider(
        settingsProvider = { cachedMarketSettings },
        sheetUrlProvider = { cachedMarketSettings.googleSheetUrl },
        cacheWriter = { saveResponseToCache(it) },
        cacheReader = { loadResponseFromCache() }
    )

    private val goldAPIProvider = GoldAPIProvider(
        settingsProvider = { cachedMarketSettings },
        cacheWriter = { saveResponseToCache(it) },
        cacheReader = { loadResponseFromCache() }
    )

    private val manualAdminProvider = ManualAdminProvider(
        settingsProvider = { cachedMarketSettings }
    )

    private val metalpriceAPIProvider = MetalpriceAPIProvider(
        settingsProvider = { cachedMarketSettings },
        goldAPIProvider = goldAPIProvider
    )

    private fun getActiveProvider(): GoldPriceProvider {
        return when (cachedMarketSettings.providerType) {
            "ManualAdmin" -> manualAdminProvider
            else -> egyptianLiveProvider // EgyptianLiveEngine is the ONLY and primary default
        }
    }

    // Independent multi-provider aggregator with failover & caching
    val primaryProvider: IGoldPriceProvider = PrimaryGoldPriceProvider(goldPriceLiveProvider)
    val backupProvider: IGoldPriceProvider = BackupGoldPriceProvider(
        secondaryProvider = googleSheetsProvider,
        benchmark21Buy = 6385.0,
        benchmark21Sell = 6355.0
    )
    val priceAggregator: GoldPriceAggregator = GoldPriceAggregator(
        primaryProvider = primaryProvider,
        backupProvider = backupProvider,
        cacheDurationMs = 30_000L
    )

    private val internalBackend = InternalGoldPriceBackend(
        provider = object : GoldPriceProvider {
            override val providerName: String get() = getActiveProvider().providerName
            override val providerType: String get() = getActiveProvider().providerType
            override suspend fun getLivePrice(forceRefresh: Boolean): GoldPriceResponse = getActiveProvider().getLivePrice(forceRefresh)
            override suspend fun getCachedPrice(): GoldPriceResponse? = getActiveProvider().getCachedPrice()
        }
    )

    private val apiService = GoldApiService(internalBackend)

    // Network connectivity monitoring flow
    val isOnlineFlow: Flow<Boolean> = callbackFlow {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        fun checkCurrent(): Boolean {
            val network = connectivityManager.activeNetwork ?: return false
            val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        trySend(checkCurrent())

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet =
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                trySend(hasInternet)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)

        awaitClose {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            } catch (_: Exception) {}
        }
    }.distinctUntilChanged()

    suspend fun getLiveGoldPrices(country: Country = Country.EGYPT, forceRefresh: Boolean = false): LivePriceResponse = withContext(Dispatchers.IO) {
        // Ensure settings are loaded into memory
        loadMarketAdminSettings()

        try {
            val liveResponse = apiService.getLiveGoldPrices(country, forceRefresh)
            if (liveResponse.prices.isNotEmpty()) {
                return@withContext liveResponse
            }
        } catch (_: Exception) {
            // Fallback handled in provider
        }

        // Cached fallback
        val cached = loadPricesFromCache(country)
        if (cached != null && cached.prices.isNotEmpty()) {
            return@withContext cached
        }

        // Benchmark fallback
        val defaultPrices = calculatePricesFromBenchmark(country)
        LivePriceResponse(
            prices = defaultPrices,
            sourceTitle = "تقديري (بدون اتصال)",
            isLive = false,
            country = country
        )
    }

    suspend fun getLiveGoldPriceResponse(forceRefresh: Boolean = false): GoldPriceResponse = withContext(Dispatchers.IO) {
        loadMarketAdminSettings()
        val result = goldApiHybridProvider.getLivePrice()
        if (result.isSuccess) {
            val resp = result.getOrThrow()
            saveResponseToCache(resp)
            resp
        } else {
            val cached = loadResponseFromCache()
            if (cached != null) {
                cached.copy(status = "connection_lost")
            } else {
                val fallback = GoldBullionPricingEngine.calculateAll(
                    xau = 4361.0,
                    sd = cachedMarketSettings.saghaUsdRate,
                    k = cachedMarketSettings.calibrationK,
                    officialUsd = cachedMarketSettings.cachedUsdRate
                )
                GoldBullionPricingEngine.toResponse(
                    calculated = fallback,
                    status = "connection_lost"
                )
            }
        }
    }

    private suspend fun saveResponseToCache(resp: GoldPriceResponse) {
        try {
            settingsDao.setSetting(SettingsEntity("cache_resp_source", resp.source))
            settingsDao.setSetting(SettingsEntity("cache_resp_time", resp.timestamp.toString()))
            settingsDao.setSetting(SettingsEntity("cache_resp_ounce", resp.ouncePrice.toString()))
            settingsDao.setSetting(SettingsEntity("cache_resp_ask", resp.ounceAsk.toString()))
            settingsDao.setSetting(SettingsEntity("cache_resp_bid", resp.ounceBid.toString()))
            settingsDao.setSetting(SettingsEntity("cache_resp_change", resp.change.toString()))
            settingsDao.setSetting(SettingsEntity("cache_resp_change_pct", resp.changePercent.toString()))
            listOf(
                24 to resp.gram24,
                22 to resp.gram22,
                21 to resp.gram21,
                18 to resp.gram18,
                14 to resp.gram14
            ).forEach { (karat, pair) ->
                settingsDao.setSetting(SettingsEntity("cache_EG_${karat}_buy", pair.buy.toString()))
                settingsDao.setSetting(SettingsEntity("cache_EG_${karat}_sell", pair.sell.toString()))
            }
        } catch (_: Exception) {}
    }

    private suspend fun loadResponseFromCache(): GoldPriceResponse? {
        return try {
            val source = settingsDao.getSettingDirect("cache_resp_source") ?: return null
            val timeStr = settingsDao.getSettingDirect("cache_resp_time")
            val timestamp = timeStr?.toLongOrNull() ?: System.currentTimeMillis()
            val ounce = settingsDao.getSettingDirect("cache_resp_ounce")?.toDoubleOrNull() ?: 0.0
            val ask = settingsDao.getSettingDirect("cache_resp_ask")?.toDoubleOrNull() ?: ounce
            val bid = settingsDao.getSettingDirect("cache_resp_bid")?.toDoubleOrNull() ?: ounce
            val change = settingsDao.getSettingDirect("cache_resp_change")?.toDoubleOrNull() ?: 0.0
            val changePct = settingsDao.getSettingDirect("cache_resp_change_pct")?.toDoubleOrNull() ?: 0.0

            suspend fun pair(karat: Int): PricePair {
                val b = settingsDao.getSettingDirect("cache_EG_${karat}_buy")?.toDoubleOrNull() ?: 0.0
                val s = settingsDao.getSettingDirect("cache_EG_${karat}_sell")?.toDoubleOrNull() ?: 0.0
                return PricePair(b, s)
            }

            val p24 = pair(24)
            val p22 = pair(22)
            val p21 = pair(21)
            val p18 = pair(18)
            val p14 = pair(14)

            if (p21.buy <= 0.0) return null

            val ageSec = (System.currentTimeMillis() - timestamp).coerceAtLeast(0L) / 1000L
            val isStale = ageSec > cachedMarketSettings.staleAfterSeconds
            val status = if (isStale) "stale" else "cached"

            GoldPriceResponse(
                source = source,
                sourceType = "local_cache",
                metal = "XAU",
                currency = "EGP",
                timestamp = timestamp,
                datetime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date(timestamp)),
                lastUpdated = GoldPriceProvider.formatTimestampToDisplay(timestamp),
                lastChecked = GoldPriceProvider.formatTimestampToDisplay(System.currentTimeMillis()),
                status = status,
                dataAgeSeconds = ageSec,
                ouncePrice = ounce,
                ounceAsk = ask,
                ounceBid = bid,
                change = change,
                changePercent = changePct,
                gram24 = p24,
                gram22 = p22,
                gram21 = p21,
                gram18 = p18,
                gram14 = p14,
                spread = KaratSpread(p24.spread, p22.spread, p21.spread, p18.spread, p14.spread),
                isStale = isStale,
                error = if (isStale) "تحذير: السعر قديم نسبيًا." else "آخر سعر محفوظ، وقد لا يكون السعر الحالي."
            )
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun loadPricesFromCache(country: Country): LivePriceResponse? {
        return try {
            val resp = loadResponseFromCache() ?: return null
            LivePriceResponse(
                prices = resp.toGoldPriceList(),
                sourceTitle = "محفوظ محلياً (${resp.lastUpdated})",
                isLive = false,
                country = country,
                timestamp = resp.timestamp,
                rawResponse = resp
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun calculatePricesFromBenchmark(country: Country): List<GoldPrice> {
        val karats = listOf(24, 22, 21, 18, 14)
        return karats.map { karat ->
            val ratio = karat / 21.0
            val buyPrice = Math.round(country.defaultBenchmark21Buy * ratio)
            val sellPrice = Math.round(country.defaultBenchmark21Sell * ratio)
            GoldPrice(
                karat = karat,
                buyPrice = buyPrice.toDouble(),
                sellPrice = sellPrice.toDouble(),
                change24h = 0.0
            )
        }
    }

    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByType(type)

    suspend fun saveTransaction(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar", "EG"))
        val dateStr = dateFormat.format(Date(transaction.timestamp))
        val enriched = transaction.copy(formattedDate = dateStr)
        transactionDao.insertTransaction(enriched)
    }

    suspend fun deleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(id)
    }

    suspend fun updateTransactionNote(id: Long, note: String) = withContext(Dispatchers.IO) {
        transactionDao.updateTransactionNote(id, note)
    }

    suspend fun clearAllTransactions() = withContext(Dispatchers.IO) {
        transactionDao.clearAll()
    }

    // App User Settings
    suspend fun loadSettings(): GoldSettings = withContext(Dispatchers.IO) {
        val countryCode = settingsDao.getSettingDirect("selected_country") ?: "EG"
        val country = Country.fromCode(countryCode)
        val buyMaking = settingsDao.getSettingDirect("buy_making")?.toDoubleOrNull() ?: 7.0
        val sellDeduction = settingsDao.getSettingDirect("sell_deduction")?.toDoubleOrNull() ?: 2.0
        val stampFee = settingsDao.getSettingDirect("stamp_fee")?.toDoubleOrNull() ?: country.defaultStampFee
        val defaultDamaged = settingsDao.getSettingDirect("sell_damaged")?.toDoubleOrNull() ?: 0.0
        val defaultSagha = settingsDao.getSettingDirect("sagha_usd_rate")?.toDoubleOrNull() ?: 51.70
        GoldSettings(
            selectedCountry = country,
            defaultBuyMakingPercent = buyMaking,
            defaultSellDeductionPercent = sellDeduction,
            defaultStampFeePerGram = stampFee,
            defaultDamagedPercent = defaultDamaged,
            defaultSaghaUsd = defaultSagha
        )
    }

    suspend fun saveSettings(settings: GoldSettings) = withContext(Dispatchers.IO) {
        settingsDao.setSetting(SettingsEntity("selected_country", settings.selectedCountry.code))
        settingsDao.setSetting(SettingsEntity("buy_making", settings.defaultBuyMakingPercent.toString()))
        settingsDao.setSetting(SettingsEntity("sell_deduction", settings.defaultSellDeductionPercent.toString()))
        settingsDao.setSetting(SettingsEntity("stamp_fee", settings.defaultStampFeePerGram.toString()))
        settingsDao.setSetting(SettingsEntity("sell_damaged", settings.defaultDamagedPercent.toString()))
        settingsDao.setSetting(SettingsEntity("sagha_usd_rate", settings.defaultSaghaUsd.toString()))
    }

    // Market Admin Settings & Audit Log
    val allAuditLogs: Flow<List<MarketSettingsAuditLogEntity>> = auditLogDao.getAllAuditLogs()

    suspend fun loadMarketAdminSettings(): MarketAdminSettings = withContext(Dispatchers.IO) {
        val buyPremium = settingsDao.getSettingDirect("admin_buy_premium")?.toDoubleOrNull() ?: 1.5
        val sellDiscount = settingsDao.getSettingDirect("admin_sell_discount")?.toDoubleOrNull() ?: 0.74
        val fixedAdj = settingsDao.getSettingDirect("admin_fixed_adj")?.toDoubleOrNull() ?: 0.0
        val roundStep = settingsDao.getSettingDirect("admin_round_step")?.toDoubleOrNull() ?: 1.0
        val staleSec = settingsDao.getSettingDirect("admin_stale_sec")?.toLongOrNull() ?: 60L
        val refreshSec = settingsDao.getSettingDirect("admin_refresh_sec")?.toLongOrNull() ?: 30L
        val cacheEnabled = settingsDao.getSettingDirect("admin_cache_enabled")?.toBooleanStrictOrNull() ?: true
        val maxChange = settingsDao.getSettingDirect("admin_max_change")?.toDoubleOrNull() ?: 5.0
        val provider = settingsDao.getSettingDirect("admin_provider") ?: "EgyptianLiveEngine"
        val sheetUrl = settingsDao.getSettingDirect("admin_google_sheet_url")?.takeIf { it.isNotBlank() }
            ?: ""
        val hourlySync = settingsDao.getSettingDirect("admin_sheet_hourly_sync")?.toBooleanStrictOrNull() ?: false
        val sheetBuy21 = settingsDao.getSettingDirect("admin_sheet_buy21")?.toDoubleOrNull() ?: 6185.0
        val sheetSell21 = settingsDao.getSettingDirect("admin_sheet_sell21")?.toDoubleOrNull() ?: 6235.0
        val sheetSyncTime = settingsDao.getSettingDirect("admin_sheet_sync_time") ?: "2026-09-14 11:00"

        val sellFactor = settingsDao.getSettingDirect("market_sell_factor")?.toDoubleOrNull() ?: 0.9972
        val buyFactor = settingsDao.getSettingDirect("market_buy_factor")?.toDoubleOrNull() ?: 1.0019
        val bullionMargin = settingsDao.getSettingDirect("bullion_margin")?.toDoubleOrNull() ?: 0.0
        val manual21Buy = settingsDao.getSettingDirect("manual_benchmark_21_buy")?.toDoubleOrNull() ?: 0.0
        val manual21Sell = settingsDao.getSettingDirect("manual_benchmark_21_sell")?.toDoubleOrNull() ?: 0.0
        val isManualActive = settingsDao.getSettingDirect("manual_calib_active")?.toBooleanStrictOrNull() ?: false
        val soundAlert = settingsDao.getSettingDirect("sound_alert_enabled")?.toBooleanStrictOrNull() ?: true
        val vibration = settingsDao.getSettingDirect("vibration_enabled")?.toBooleanStrictOrNull() ?: true
        val cachedUsd = settingsDao.getSettingDirect("cached_usd_rate")?.toDoubleOrNull() ?: 52.30
        val cachedUsdBuy = settingsDao.getSettingDirect("cached_usd_buy_rate")?.toDoubleOrNull() ?: 52.30
        val cachedUsdSell = settingsDao.getSettingDirect("cached_usd_sell_rate")?.toDoubleOrNull() ?: 52.20
        val saghaUsd = settingsDao.getSettingDirect("sagha_usd_rate")?.toDoubleOrNull() ?: 51.70
        val cachedUsdTime = settingsDao.getSettingDirect("cached_usd_time")?.toLongOrNull() ?: 0L
        val cachedUsdDate = settingsDao.getSettingDirect("cached_usd_date") ?: ""
        val calibK = settingsDao.getSettingDirect("calibration_k")?.toDoubleOrNull() ?: GoldBullionPricingEngine.DEFAULT_K
        val calibDate = settingsDao.getSettingDirect("last_calibration_date") ?: ""

        val loaded = MarketAdminSettings(
            customerBuyPremiumPercent = buyPremium,
            dealerBuyDiscountPercent = sellDiscount,
            fixedAdjustmentEGP = fixedAdj,
            roundingStep = roundStep,
            staleAfterSeconds = staleSec,
            refreshIntervalSeconds = refreshSec,
            cacheEnabled = cacheEnabled,
            maxAcceptableChangePercent = maxChange,
            providerType = provider,
            googleSheetUrl = sheetUrl,
            googleSheetHourlySyncEnabled = hourlySync,
            lastGoogleSheetBuy21 = sheetBuy21,
            lastGoogleSheetSell21 = sheetSell21,
            lastGoogleSheetSyncTime = sheetSyncTime,
            marketSellFactor = sellFactor,
            marketBuyFactor = buyFactor,
            bullionMarginPerGram = bullionMargin,
            manualBenchmark21Buy = manual21Buy,
            manualBenchmark21Sell = manual21Sell,
            isManualCalibrationActive = isManualActive,
            soundAlertEnabled = soundAlert,
            vibrationEnabled = vibration,
            cachedUsdRate = cachedUsd,
            cachedUsdBuyRate = cachedUsdBuy,
            cachedUsdSellRate = cachedUsdSell,
            saghaUsdRate = saghaUsd,
            cachedUsdRateTimestamp = cachedUsdTime,
            cachedUsdRateDate = cachedUsdDate,
            calibrationK = calibK,
            lastCalibrationDate = calibDate
        )
        cachedMarketSettings = loaded
        loaded
    }

    suspend fun calibrateWithEntered21(enteredBuy21: Double): Pair<Double, String> = withContext(Dispatchers.IO) {
        val current = loadMarketAdminSettings()
        val xau = goldApiHybridProvider.getLastKnownXau().takeIf { it > 100.0 } ?: 4379.0
        val sd = current.saghaUsdRate
        val newK = GoldBullionPricingEngine.calibrateK(enteredBuy21, xau, sd)

        val sdf = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar", "EG"))
        val calibDate = sdf.format(Date())

        settingsDao.setSetting(SettingsEntity("calibration_k", newK.toString()))
        settingsDao.setSetting(SettingsEntity("last_calibration_date", calibDate))

        cachedMarketSettings = current.copy(
            calibrationK = newK,
            lastCalibrationDate = calibDate
        )
        Pair(newK, calibDate)
    }

    suspend fun resetCalibrationK(): Double = withContext(Dispatchers.IO) {
        val current = loadMarketAdminSettings()
        val defaultK = GoldBullionPricingEngine.DEFAULT_K
        val calibDate = "الافتراضي (0.9996)"

        settingsDao.setSetting(SettingsEntity("calibration_k", defaultK.toString()))
        settingsDao.setSetting(SettingsEntity("last_calibration_date", calibDate))

        cachedMarketSettings = current.copy(
            calibrationK = defaultK,
            lastCalibrationDate = calibDate
        )
        defaultK
    }

    suspend fun updateSaghaDollar(newSd: Double) = withContext(Dispatchers.IO) {
        val current = loadMarketAdminSettings()
        settingsDao.setSetting(SettingsEntity("sagha_usd_rate", newSd.toString()))
        cachedMarketSettings = current.copy(saghaUsdRate = newSd)
    }

    suspend fun calibrateMarketFactors(
        localSell21: Double,
        changedBy: String = "المشرف"
    ): Pair<Double, Double> = withContext(Dispatchers.IO) {
        val currentSettings = loadMarketAdminSettings()
        // Compute raw 21: (XAU * EGP / 31.1035) * (21/24)
        val usdRate = if (currentSettings.cachedUsdRate > 0) currentSettings.cachedUsdRate else 51.378
        val defaultXau = 4296.4
        val raw24 = (defaultXau * usdRate) / 31.1035
        val raw21 = raw24 * (21.0 / 24.0)

        val newSellFactor = Math.round((localSell21 / raw21) * 10000.0) / 10000.0
        val newBuyFactor = Math.round((newSellFactor * (0.996 / 1.004)) * 10000.0) / 10000.0

        val updated = currentSettings.copy(
            marketSellFactor = newSellFactor,
            marketBuyFactor = newBuyFactor
        )
        saveMarketAdminSettings(
            updated,
            changedBy,
            "معايرة تلقائية من سعر بيع محلي عيار 21: $localSell21 ج.م (معامل بيع: $newSellFactor, شراء: $newBuyFactor)"
        )
        Pair(newSellFactor, newBuyFactor)
    }

    suspend fun applyExplicitPrices(
        buy21: Double,
        sell21: Double,
        sourceName: String = "gold-price-live.com (المصدر الأساسي المباشر)",
        timeStr: String = "2026-09-13 11:15"
    ): GoldPriceResponse = withContext(Dispatchers.IO) {
        val current = loadMarketAdminSettings()
        val updated = current.copy(
            lastGoogleSheetBuy21 = buy21,
            lastGoogleSheetSell21 = sell21,
            manualBenchmark21Buy = buy21,
            manualBenchmark21Sell = sell21,
            lastGoogleSheetSyncTime = timeStr,
            providerType = "GoldPriceLive"
        )
        saveMarketAdminSettings(updated, "المستخدم", "تحديث عيار 21 من gold-price-live.com: شراء $buy21 | بيع $sell21")
        goldPriceLiveProvider.setExplicitPrices(buy21, sell21, timeStr)
        val response = googleSheetsProvider.applyDirectPrices(
            buy21 = buy21,
            sell21 = sell21,
            sourceDesc = sourceName,
            timeStr = timeStr
        )
        saveResponseToCache(response)
        response
    }

    suspend fun saveMarketAdminSettings(
        newSettings: MarketAdminSettings,
        changedBy: String,
        reason: String
    ) = withContext(Dispatchers.IO) {
        val old = loadMarketAdminSettings()
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar", "EG"))
        val formattedDate = sdf.format(Date(now))

        suspend fun recordAudit(name: String, oldVal: Any, newVal: Any) {
            if (oldVal.toString() != newVal.toString()) {
                auditLogDao.insertLog(
                    MarketSettingsAuditLogEntity(
                        settingName = name,
                        oldValue = oldVal.toString(),
                        newValue = newVal.toString(),
                        changedBy = changedBy.ifBlank { "المشرف" },
                        reason = reason.ifBlank { "تحديث هوامش التسعير" },
                        timestamp = now,
                        formattedDate = formattedDate
                    )
                )
            }
        }

        recordAudit("معامل سعر البيع المصري (marketSellFactor)", old.marketSellFactor, newSettings.marketSellFactor)
        recordAudit("معامل سعر الشراء المصري (marketBuyFactor)", old.marketBuyFactor, newSettings.marketBuyFactor)
        recordAudit("نسبة هامش البيع للعميل (customerBuyPremiumPercent)", old.customerBuyPremiumPercent, newSettings.customerBuyPremiumPercent)
        recordAudit("نسبة خصم الشراء من العميل (dealerBuyDiscountPercent)", old.dealerBuyDiscountPercent, newSettings.dealerBuyDiscountPercent)
        recordAudit("التعديل الثابت بالجنيه (fixedAdjustmentEGP)", old.fixedAdjustmentEGP, newSettings.fixedAdjustmentEGP)
        recordAudit("خطوة التقريب (roundingStep)", old.roundingStep, newSettings.roundingStep)
        recordAudit("عمر السعر قبل التحذير بالثواني (staleAfterSeconds)", old.staleAfterSeconds, newSettings.staleAfterSeconds)
        recordAudit("فترة التحديث التلقائي بالثواني (refreshIntervalSeconds)", old.refreshIntervalSeconds, newSettings.refreshIntervalSeconds)
        recordAudit("مزود البيانات (providerType)", old.providerType, newSettings.providerType)
        recordAudit("التنبيه الصوتي (soundAlertEnabled)", old.soundAlertEnabled, newSettings.soundAlertEnabled)

        settingsDao.setSetting(SettingsEntity("market_sell_factor", newSettings.marketSellFactor.toString()))
        settingsDao.setSetting(SettingsEntity("market_buy_factor", newSettings.marketBuyFactor.toString()))
        settingsDao.setSetting(SettingsEntity("bullion_margin", newSettings.bullionMarginPerGram.toString()))
        settingsDao.setSetting(SettingsEntity("manual_benchmark_21_buy", newSettings.manualBenchmark21Buy.toString()))
        settingsDao.setSetting(SettingsEntity("manual_benchmark_21_sell", newSettings.manualBenchmark21Sell.toString()))
        settingsDao.setSetting(SettingsEntity("manual_calib_active", newSettings.isManualCalibrationActive.toString()))
        settingsDao.setSetting(SettingsEntity("sound_alert_enabled", newSettings.soundAlertEnabled.toString()))
        settingsDao.setSetting(SettingsEntity("vibration_enabled", newSettings.vibrationEnabled.toString()))
        settingsDao.setSetting(SettingsEntity("cached_usd_rate", newSettings.cachedUsdRate.toString()))
        settingsDao.setSetting(SettingsEntity("cached_usd_buy_rate", newSettings.cachedUsdBuyRate.toString()))
        settingsDao.setSetting(SettingsEntity("cached_usd_sell_rate", newSettings.cachedUsdSellRate.toString()))
        settingsDao.setSetting(SettingsEntity("sagha_usd_rate", newSettings.saghaUsdRate.toString()))
        settingsDao.setSetting(SettingsEntity("cached_usd_time", newSettings.cachedUsdRateTimestamp.toString()))
        settingsDao.setSetting(SettingsEntity("cached_usd_date", newSettings.cachedUsdRateDate))

        settingsDao.setSetting(SettingsEntity("admin_buy_premium", newSettings.customerBuyPremiumPercent.toString()))
        settingsDao.setSetting(SettingsEntity("admin_sell_discount", newSettings.dealerBuyDiscountPercent.toString()))
        settingsDao.setSetting(SettingsEntity("admin_fixed_adj", newSettings.fixedAdjustmentEGP.toString()))
        settingsDao.setSetting(SettingsEntity("admin_round_step", newSettings.roundingStep.toString()))
        settingsDao.setSetting(SettingsEntity("admin_stale_sec", newSettings.staleAfterSeconds.toString()))
        settingsDao.setSetting(SettingsEntity("admin_refresh_sec", newSettings.refreshIntervalSeconds.toString()))
        settingsDao.setSetting(SettingsEntity("admin_cache_enabled", newSettings.cacheEnabled.toString()))
        settingsDao.setSetting(SettingsEntity("admin_max_change", newSettings.maxAcceptableChangePercent.toString()))
        settingsDao.setSetting(SettingsEntity("admin_provider", newSettings.providerType))
        settingsDao.setSetting(SettingsEntity("admin_google_sheet_url", newSettings.googleSheetUrl))
        settingsDao.setSetting(SettingsEntity("admin_sheet_hourly_sync", newSettings.googleSheetHourlySyncEnabled.toString()))
        settingsDao.setSetting(SettingsEntity("admin_sheet_buy21", newSettings.lastGoogleSheetBuy21.toString()))
        settingsDao.setSetting(SettingsEntity("admin_sheet_sell21", newSettings.lastGoogleSheetSell21.toString()))
        settingsDao.setSetting(SettingsEntity("admin_sheet_sync_time", newSettings.lastGoogleSheetSyncTime))

        cachedMarketSettings = newSettings
    }
}
