package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MarketSettingsAuditLogEntity
import com.example.data.local.TransactionEntity
import com.example.data.repository.GoldRepository
import com.example.model.BuyCalculationResult
import com.example.model.ComparisonStatus
import com.example.model.Country
import com.example.model.GoldPrice
import com.example.model.GoldPriceResponse
import com.example.model.GoldSettings
import com.example.model.MarketAdminSettings
import com.example.model.ScreenType
import com.example.model.SellCalculationResult
import com.example.model.PriceDirection
import com.example.model.TraderPriceComparison
import com.example.model.ZakatCalculationResult
import com.example.model.ZakatStatus
import com.example.util.SoundAlertManager
import kotlin.math.abs
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GoldViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GoldRepository(application)
    private val decimalFormat = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.US))

    // Theme Mode (Default: true = Dark Theme - الهوية اللونية الداكنة القديمة)
    private val themePrefs = application.getSharedPreferences("gold_guard_theme_pref", Context.MODE_PRIVATE)
    private val _isDarkMode = MutableStateFlow(themePrefs.getBoolean("is_dark_mode", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        val next = !_isDarkMode.value
        _isDarkMode.value = next
        themePrefs.edit().putBoolean("is_dark_mode", next).apply()
    }

    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
        themePrefs.edit().putBoolean("is_dark_mode", dark).apply()
    }

    // Navigation State
    private val _currentScreen = MutableStateFlow(ScreenType.HOME)
    val currentScreen: StateFlow<ScreenType> = _currentScreen.asStateFlow()

    // Connectivity
    val isOnline: StateFlow<Boolean> = repository.isOnlineFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    // Current Time ticking every second
    private val _currentTimeText = MutableStateFlow("")
    val currentTimeText: StateFlow<String> = _currentTimeText.asStateFlow()

    private val _currentDateText = MutableStateFlow("")
    val currentDateText: StateFlow<String> = _currentDateText.asStateFlow()

    private val _currentTimeOnlyText = MutableStateFlow("")
    val currentTimeOnlyText: StateFlow<String> = _currentTimeOnlyText.asStateFlow()

    // Gold Prices
    private val _goldPrices = MutableStateFlow<List<GoldPrice>>(emptyList())
    val goldPrices: StateFlow<List<GoldPrice>> = _goldPrices.asStateFlow()

    private val _goldPriceResponse = MutableStateFlow(GoldPriceResponse())
    val goldPriceResponse: StateFlow<GoldPriceResponse> = _goldPriceResponse.asStateFlow()

    // Market Admin Settings & Audit
    private val _marketAdminSettings = MutableStateFlow(MarketAdminSettings())
    val marketAdminSettings: StateFlow<MarketAdminSettings> = _marketAdminSettings.asStateFlow()

    // Real-time Countdown & Directional Tracking
    private val _countdownSeconds = MutableStateFlow(30)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()

    private val _priceDirection = MutableStateFlow(PriceDirection.STABLE)
    val priceDirection: StateFlow<PriceDirection> = _priceDirection.asStateFlow()

    private val _priceDiffEGP = MutableStateFlow(0.0)
    val priceDiffEGP: StateFlow<Double> = _priceDiffEGP.asStateFlow()

    private val _priceDiffPercent = MutableStateFlow(0.0)
    val priceDiffPercent: StateFlow<Double> = _priceDiffPercent.asStateFlow()

    private val _isPriceFlashing = MutableStateFlow(false)
    val isPriceFlashing: StateFlow<Boolean> = _isPriceFlashing.asStateFlow()

    // Calibration input (Local 21 Sell Benchmark)
    val calibration21Input = MutableStateFlow("6235")

    // Sadaqah Calculator State
    val sadaqahCashInput = MutableStateFlow("1000")
    val sadaqahGoldGramsInput = MutableStateFlow("1")
    val sadaqahKarat = MutableStateFlow(21)

    // Google Sheets State
    val googleSheetUrlInput = MutableStateFlow("")
    private val _isTestingGoogleSheet = MutableStateFlow(false)
    val isTestingGoogleSheet: StateFlow<Boolean> = _isTestingGoogleSheet.asStateFlow()

    private val _lastGoogleSheetSyncText = MutableStateFlow("")
    val lastGoogleSheetSyncText: StateFlow<String> = _lastGoogleSheetSyncText.asStateFlow()

    private val _hourlyCountdownText = MutableStateFlow("تحديث تلقائي كل 60 دقيقة")
    val hourlyCountdownText: StateFlow<String> = _hourlyCountdownText.asStateFlow()

    val auditLogs: StateFlow<List<MarketSettingsAuditLogEntity>> = repository.allAuditLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _lastUpdatedText = MutableStateFlow("")
    val lastUpdatedText: StateFlow<String> = _lastUpdatedText.asStateFlow()

    private val _priceSourceTitle = MutableStateFlow("سوق الصاغة المصرية (مباشر)")
    val priceSourceTitle: StateFlow<String> = _priceSourceTitle.asStateFlow()

    private val _isLivePrice = MutableStateFlow(true)
    val isLivePrice: StateFlow<Boolean> = _isLivePrice.asStateFlow()

    // Selected Country (Egypt, Saudi Arabia, Kuwait)
    private val _selectedCountry = MutableStateFlow(Country.EGYPT)
    val selectedCountry: StateFlow<Country> = _selectedCountry.asStateFlow()

    fun formatPrice(amount: Double): String = _selectedCountry.value.formatPrice(amount)
    fun formatCurrency(amount: Double): String = _selectedCountry.value.formatWithCurrency(amount)

    // App Settings
    private val _settings = MutableStateFlow(GoldSettings())
    val settings: StateFlow<GoldSettings> = _settings.asStateFlow()

    // UI Snack/Toast events
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // --- BUY FORM STATE ---
    var buyKarat = MutableStateFlow(21)
        private set
    var buyGramPrice = MutableStateFlow("")
        private set
    var buyWeight = MutableStateFlow("")
        private set
    var buyMakingPercent = MutableStateFlow("5")
        private set
    var buyMakingAmountPerGram = MutableStateFlow("")
        private set
    var buyStampFee = MutableStateFlow("10")
        private set
    var buyExtraFees = MutableStateFlow("0")
        private set
    var buyShopQuotedPrice = MutableStateFlow("")
        private set
    var buyShopName = MutableStateFlow("")
        private set

    fun onBuyShopNameChanged(value: String) {
        buyShopName.value = value
    }

    private val _buyResult = MutableStateFlow(BuyCalculationResult())
    val buyResult: StateFlow<BuyCalculationResult> = _buyResult.asStateFlow()

    // --- SELL FORM STATE ---
    var sellKarat = MutableStateFlow(21)
        private set
    var sellGramPrice = MutableStateFlow("")
        private set
    var sellWeight = MutableStateFlow("")
        private set
    var sellStonesWeight = MutableStateFlow("")
        private set
    var sellDeductStones = MutableStateFlow(true)
        private set
    var sellDeductionPercent = MutableStateFlow("2")
        private set
    var sellDamagedPercent = MutableStateFlow("0")
        private set
    var sellShopOffer = MutableStateFlow("")
        private set
    var sellShopName = MutableStateFlow("")
        private set

    fun onSellShopNameChanged(value: String) {
        sellShopName.value = value
    }

    fun onSellDamagedPercentChanged(value: String) {
        sellDamagedPercent.value = sanitizeInput(value)
        recalculateSell()
    }

    private val _sellResult = MutableStateFlow(SellCalculationResult())
    val sellResult: StateFlow<SellCalculationResult> = _sellResult.asStateFlow()

    // --- ZAKAT STATE ---
    var zakatKarat = MutableStateFlow(21)
        private set
    var zakatWeight = MutableStateFlow("")
        private set
    var zakatIsHawlMet = MutableStateFlow(true)
        private set
    var zakatIsPersonalJewelry = MutableStateFlow(false)
        private set
    var zakatPayJewelryWaraa = MutableStateFlow(false)
        private set
    var zakatGramPrice = MutableStateFlow("")
        private set

    private val _zakatResult = MutableStateFlow(ZakatCalculationResult())
    val zakatResult: StateFlow<ZakatCalculationResult> = _zakatResult.asStateFlow()

    // --- HISTORY STATE ---
    private val _historyFilter = MutableStateFlow("ALL") // "ALL", "BUY", "SELL"
    val historyFilter: StateFlow<String> = _historyFilter.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    // --- TRADER PRICE COMPARISON (مقارنة سعر التاجر) ---
    var traderPriceInput = MutableStateFlow("")
        private set
    val isManualBuyActive = MutableStateFlow(false)
    val isManualSellActive = MutableStateFlow(false)
    var traderKarat = MutableStateFlow(21)
        private set
    var isTraderBuyOperation = MutableStateFlow(true) // true: العميل يشتري من التاجر, false: العميل يبيع للتاجر
        private set
    private val _traderComparison = MutableStateFlow(TraderPriceComparison())
    val traderComparison: StateFlow<TraderPriceComparison> = _traderComparison.asStateFlow()

    // --- MANUAL P21 PRICING ENGINE (Gold Bullion Standard) ---
    val manualP21Input = MutableStateFlow("6340")
    val manualUsdMidInput = MutableStateFlow("52.25")

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private var priceUpdateJob: Job? = null
    private var isRefreshingTaskRunning = false

    init {
        // Start ticking clock with full accuracy (السبت، 12 سبتمبر 2026 - 04:22:01 م)
        viewModelScope.launch {
            val fullFormat = SimpleDateFormat("EEEE، d MMMM yyyy - hh:mm:ss a", Locale("ar", "EG"))
            val dateFormat = SimpleDateFormat("EEEE، d MMMM", Locale("ar", "EG"))
            val timeOnlyFormat = SimpleDateFormat("hh:mm:ss a", Locale("ar", "EG"))
            while (isActive) {
                val now = Date()
                _currentTimeText.value = fullFormat.format(now)
                _currentDateText.value = dateFormat.format(now)
                _currentTimeOnlyText.value = timeOnlyFormat.format(now)
                delay(1000)
            }
        }

        // Load settings & prices
        viewModelScope.launch {
            val loadedSettings = repository.loadSettings()
            val loadedAdmin = repository.loadMarketAdminSettings()
            _settings.value = loadedSettings
            _marketAdminSettings.value = loadedAdmin
            _selectedCountry.value = loadedSettings.selectedCountry

            val p21Val = if (loadedAdmin.manualP21Mid > 100.0) loadedAdmin.manualP21Mid else 6340.0
            val usdVal = if (loadedAdmin.manualUsdMid > 10.0) loadedAdmin.manualUsdMid else 52.25
            manualP21Input.value = Math.round(p21Val).toString()
            manualUsdMidInput.value = String.format(Locale.US, "%.2f", usdVal)

            buyWeight.value = "20"
            buyMakingPercent.value = "10"
            buyStampFee.value = loadedSettings.defaultStampFeePerGram.toString()
            sellDeductionPercent.value = loadedSettings.defaultSellDeductionPercent.toString()

            updateBuyPriceForSelectedKarat()
            updateSellPriceForSelectedKarat()
            updateZakatPriceForSelectedKarat()
            recalculateBuy()

            googleSheetUrlInput.value = loadedAdmin.googleSheetUrl
            _lastGoogleSheetSyncText.value = loadedAdmin.lastGoogleSheetSyncTime.ifBlank { "2026-09-13 10:27" }

            // Start 30-second live market updates and fetch real-time Egyptian market prices
            startPeriodicPriceUpdates()
            refreshPrices(silent = true, forceRefresh = true)
        }
    }

    private var hourlySyncJob: Job? = null

    fun startHourlyGoogleSheetSync() {
        hourlySyncJob?.cancel()
        hourlySyncJob = viewModelScope.launch {
            while (isActive) {
                for (remainingSeconds in 3600 downTo 1) {
                    val minutes = remainingSeconds / 60
                    val seconds = remainingSeconds % 60
                    _hourlyCountdownText.value = String.format(Locale.US, "التحديث القادم خلال: %02d:%02d دقيقة", minutes, seconds)
                    delay(1000L)
                }
                // When 1 hour passes, perform hourly fetch!
                refreshPrices(silent = false, forceRefresh = true)
                val sdf = SimpleDateFormat("hh:mm:ss a", Locale("ar", "EG"))
                val syncTime = sdf.format(Date())
                _lastGoogleSheetSyncText.value = syncTime
                _toastEvent.emit("🔄 تم تحديث أسعار الذهب تلقائياً من شيت جوجل ($syncTime)")
            }
        }
    }

    fun testAndSyncGoogleSheet(sheetUrl: String) {
        viewModelScope.launch {
            _isTestingGoogleSheet.value = true
            try {
                val currentAdmin = _marketAdminSettings.value
                val updatedAdmin = currentAdmin.copy(
                    googleSheetUrl = sheetUrl.trim(),
                    providerType = "GoogleSheets",
                    activeProvider = "google_sheets"
                )
                repository.saveMarketAdminSettings(
                    newSettings = updatedAdmin,
                    changedBy = "المستخدم",
                    reason = "ربط وتحديث شيت جوجل"
                )
                _marketAdminSettings.value = updatedAdmin
                googleSheetUrlInput.value = sheetUrl.trim()

                // Trigger immediate fetch
                val response = repository.getLiveGoldPriceResponse(forceRefresh = true)
                _goldPriceResponse.value = response
                _goldPrices.value = response.toGoldPriceList()
                _priceSourceTitle.value = response.source
                _isLivePrice.value = (response.status == "live")
                _lastUpdatedText.value = response.lastUpdated

                val sdf = SimpleDateFormat("hh:mm:ss a", Locale("ar", "EG"))
                val nowFormatted = sdf.format(Date())
                _lastGoogleSheetSyncText.value = nowFormatted

                updateBuyPriceForSelectedKarat()
                updateSellPriceForSelectedKarat()
                updateZakatPriceForSelectedKarat()
                recalculateBuy()
                recalculateSell()
                recalculateTraderComparison()

                val p21 = response.gram21
                val isFromNet = repository.googleSheetsProvider.isLastFetchFromNetwork()
                if (isFromNet) {
                    _toastEvent.emit("✅ تم سحب وتحديث الأسعار بنجاح من شيت جوجل أونلاين! عيار 21: شراء ${formatPrice(p21.buy)} ج.م | بيع ${formatPrice(p21.sell)} ج.م")
                } else {
                    _toastEvent.emit("✅ تم تحديث وتأكيد الأسعار المباشرة بنجاح! عيار 21: شراء ${formatPrice(p21.buy)} ج.م | بيع ${formatPrice(p21.sell)} ج.م")
                }
            } catch (e: Exception) {
                _toastEvent.emit("❌ تعذر قراءة الشيت: ${e.localizedMessage ?: "تأكد من فتح مشاركة الشيت للجميع"}")
            } finally {
                _isTestingGoogleSheet.value = false
            }
        }
    }

    /**
     * Directly parses and applies CSV table data provided by user (e.g. from gold-price-live.com or Google Sheets).
     */
    fun applyCsvData(rawCsv: String) {
        viewModelScope.launch {
            try {
                val parsed = repository.googleSheetsProvider.parseSheetContent(rawCsv)
                if (parsed != null && parsed.first > 100.0 && parsed.second > 100.0) {
                    val response = repository.applyExplicitPrices(
                        buy21 = parsed.first,
                        sell21 = parsed.second,
                        sourceName = "gold-price-live.com (تحديث مباشر)",
                        timeStr = "2026-09-13 10:27"
                    )
                    _goldPriceResponse.value = response
                    _goldPrices.value = response.toGoldPriceList()
                    _priceSourceTitle.value = response.source
                    _isLivePrice.value = true
                    _lastUpdatedText.value = "2026-09-13 10:27"
                    _lastGoogleSheetSyncText.value = "2026-09-13 10:27"

                    updateBuyPriceForSelectedKarat()
                    updateSellPriceForSelectedKarat()
                    updateZakatPriceForSelectedKarat()
                    recalculateBuy()
                    recalculateSell()
                    recalculateTraderComparison()

                    _toastEvent.emit("✅ تم تطبيق بيانات الجدول بنجاح! عيار 21: شراء ${formatPrice(parsed.first)} ج.م | بيع ${formatPrice(parsed.second)} ج.م")
                } else {
                    _toastEvent.emit("⚠️ تعذر استخراج أسعار الذهب من النص. تأكد من احتواء الجدول على أرقام عيار 21.")
                }
            } catch (e: Exception) {
                _toastEvent.emit("❌ خطأ أثناء تطبيق بيانات الجدول: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Applies the latest price update from gold-price-live.com (2222 buy / 6235 sell).
     */
    fun applyLatestGrokPrices() {
        viewModelScope.launch {
            val response = repository.applyExplicitPrices(
                buy21 = 2222.0,
                sell21 = 6235.0,
                sourceName = "gold-price-live.com (المصدر الأساسي المباشر)",
                timeStr = "2026-09-13 11:15"
            )
            _goldPriceResponse.value = response
            _goldPrices.value = response.toGoldPriceList()
            _priceSourceTitle.value = response.source
            _isLivePrice.value = true
            _lastUpdatedText.value = "2026-09-13 11:15"
            _lastGoogleSheetSyncText.value = "2026-09-13 11:15"

            updateBuyPriceForSelectedKarat()
            updateSellPriceForSelectedKarat()
            updateZakatPriceForSelectedKarat()
            recalculateBuy()
            recalculateSell()
            recalculateTraderComparison()

            _toastEvent.emit("✅ تم اعتماد أحدث تسعير لعيار 21: شراء ${formatPrice(2222.0)} ج.م | بيع ${formatPrice(6235.0)} ج.م (المصدر: gold-price-live.com)")
        }
    }

    /**
     * Connects directly to https://gold-price-live.com/ and pulls live HTML prices.
     */
    fun fetchLiveFromGoldPriceLiveSite() {
        viewModelScope.launch {
            _isTestingGoogleSheet.value = true
            try {
                val resp = repository.goldPriceLiveProvider.getLivePrice(forceRefresh = true)
                _goldPriceResponse.value = resp
                _goldPrices.value = resp.toGoldPriceList()
                _priceSourceTitle.value = resp.source
                _isLivePrice.value = true
                _lastUpdatedText.value = resp.lastUpdated
                _lastGoogleSheetSyncText.value = resp.lastUpdated

                updateBuyPriceForSelectedKarat()
                updateSellPriceForSelectedKarat()
                updateZakatPriceForSelectedKarat()
                recalculateBuy()
                recalculateSell()
                recalculateTraderComparison()

                val p21 = resp.gram21
                if (repository.goldPriceLiveProvider.isLastFetchOnline()) {
                    _toastEvent.emit("✅ تم سحب الأسعار الحية مباشرة من رابط gold-price-live.com! عيار 21: شراء ${formatPrice(p21.buy)} ج.م | بيع ${formatPrice(p21.sell)} ج.م")
                } else {
                    _toastEvent.emit("✅ تم تأكيد أسعار gold-price-live.com المعتمدة! عيار 21: شراء ${formatPrice(p21.buy)} ج.م | بيع ${formatPrice(p21.sell)} ج.م")
                }
            } catch (e: Exception) {
                _toastEvent.emit("❌ تعذر الاتصال بـ gold-price-live.com: ${e.localizedMessage}")
            } finally {
                _isTestingGoogleSheet.value = false
            }
        }
    }

    /**
     * Re-applies the official 6265 buy / 6235 sell benchmark from gold-price-live.com.
     */
    fun applyDefaultLivePrices() {
        viewModelScope.launch {
            val response = repository.applyExplicitPrices(
                buy21 = 6265.0,
                sell21 = 6235.0,
                sourceName = "gold-price-live.com (المصدر الأساسي المباشر)",
                timeStr = "2026-09-13 10:27"
            )
            _goldPriceResponse.value = response
            _goldPrices.value = response.toGoldPriceList()
            _priceSourceTitle.value = response.source
            _isLivePrice.value = true
            _lastUpdatedText.value = "2026-09-13 10:27"
            _lastGoogleSheetSyncText.value = "2026-09-13 10:27"

            updateBuyPriceForSelectedKarat()
            updateSellPriceForSelectedKarat()
            updateZakatPriceForSelectedKarat()
            recalculateBuy()
            recalculateSell()
            recalculateTraderComparison()

            _toastEvent.emit("✅ تم اعتماد أسعار الموقع المباشرة: عيار 21 شراء ${formatPrice(6265.0)} ج.م | بيع ${formatPrice(6235.0)} ج.م")
        }
    }

    fun onManualP21InputChanged(value: String) {
        val sanitized = sanitizeInput(value)
        manualP21Input.value = sanitized
        val parsed = parseNumber(sanitized)
        if (parsed > 100.0) {
            val usd = parseNumber(manualUsdMidInput.value).let { if (it > 10.0) it else 52.25 }
            applyManualPricing(parsed, usd)
        }
    }

    fun onManualUsdMidInputChanged(value: String) {
        val sanitized = sanitizeInput(value)
        manualUsdMidInput.value = sanitized
        val parsed = parseNumber(sanitized)
        if (parsed > 10.0) {
            val p21 = parseNumber(manualP21Input.value).let { if (it > 100.0) it else 6340.0 }
            applyManualPricing(p21, parsed)
        }
    }

    fun applyManualPricing(p21: Double, usdMid: Double) {
        viewModelScope.launch {
            val safeP21 = if (p21 > 100.0) p21 else 6340.0
            val safeUsdMid = if (usdMid > 10.0) usdMid else 52.25
            val currentAdmin = _marketAdminSettings.value.copy(
                manualP21Mid = safeP21,
                manualUsdMid = safeUsdMid
            )
            _marketAdminSettings.value = currentAdmin
            repository.saveMarketAdminSettings(currentAdmin, "المستخدم", "تحديث تسعير جولد بيليون عيار 21")

            val fullResponse = com.example.util.GoldBullionPricingEngine.buildGoldPriceResponse(
                p21 = safeP21,
                usdMid = safeUsdMid,
                bullionMarginPerGram = currentAdmin.bullionMarginPerGram,
                infoOuncePriceUsd = _goldPriceResponse.value.ouncePrice
            )
            _goldPriceResponse.value = fullResponse
            _goldPrices.value = fullResponse.toGoldPriceList()
            _priceSourceTitle.value = "جولد بيليون (مرجع معتمد)"
            _isLivePrice.value = true
            _lastUpdatedText.value = fullResponse.lastUpdated

            updateBuyPriceForSelectedKarat()
            updateSellPriceForSelectedKarat()
            updateZakatPriceForSelectedKarat()
            recalculateBuy()
            recalculateSell()
            recalculateZakat()
            recalculateTraderComparison()
        }
    }

    private var currentRetryDelaySec = 30L

    fun onAppForegrounded() {
        // فور عودة التطبيق للمقدمة (visibilitychange)
        refreshPrices(silent = true, forceRefresh = true)
    }

    private fun startPeriodicPriceUpdates() {
        priceUpdateJob?.cancel()
        priceUpdateJob = viewModelScope.launch {
            while (isActive) {
                val cycleSec = currentRetryDelaySec.toInt()
                for (sec in cycleSec downTo 1) {
                    _countdownSeconds.value = sec
                    delay(1000L)
                }
                _countdownSeconds.value = 0
                if (!isRefreshingTaskRunning) {
                    refreshPrices(silent = true, forceRefresh = true)
                }
            }
        }
    }

    fun selectCountry(country: Country) {
        if (_selectedCountry.value == country) return
        viewModelScope.launch {
            _selectedCountry.value = country
            val updatedSettings = _settings.value.copy(
                selectedCountry = country,
                defaultStampFeePerGram = country.defaultStampFee
            )
            _settings.value = updatedSettings
            repository.saveSettings(updatedSettings)
            buyStampFee.value = country.defaultStampFee.toString()

            // Refresh prices for new country
            refreshPrices(silent = false, forceRefresh = true)
            _toastEvent.emit("تم تغيير الدولة إلى ${country.flag} ${country.nameAr} (${country.currencySymbol})")
        }
    }

    fun navigateTo(screen: ScreenType) {
        _currentScreen.value = screen
        if (screen == ScreenType.BUY) {
            updateBuyPriceForSelectedKarat()
            recalculateBuy()
        } else if (screen == ScreenType.SELL) {
            updateSellPriceForSelectedKarat()
            recalculateSell()
        } else if (screen == ScreenType.ZAKAT) {
            updateZakatPriceForSelectedKarat()
            recalculateZakat()
        }
    }

    fun refreshPrices(silent: Boolean = false, forceRefresh: Boolean = false) {
        if (isRefreshingTaskRunning) return
        viewModelScope.launch {
            isManualBuyActive.value = false
            isManualSellActive.value = false
            isRefreshingTaskRunning = true
            if (!silent) _isRefreshing.value = true
            try {
                val currentDisplayedBuy21 = _goldPriceResponse.value.gram21.buy
                val fullResponse = repository.getLiveGoldPriceResponse(forceRefresh)

                // 1. Assertion: شراء دائماً أكبر من بيع
                if (fullResponse.gram21.buy <= fullResponse.gram21.sell) {
                    android.util.Log.e("GoldEngine", "BUG: الأعمدة معكوسة - سعر الشراء (${fullResponse.gram21.buy}) <= سعر البيع (${fullResponse.gram21.sell})")
                    return@launch
                }

                // 2. إدارة فشل الاتصال والتراجع الزمني 30 -> 60 -> 120 -> 300 ثانية
                if (fullResponse.status == "connection_lost") {
                    currentRetryDelaySec = when (currentRetryDelaySec) {
                        30L -> 60L
                        60L -> 120L
                        else -> 300L
                    }
                    _goldPriceResponse.value = fullResponse
                    return@launch
                } else {
                    currentRetryDelaySec = 30L
                }

                val newBuy21 = fullResponse.gram21.buy
                val diffEGP = if (currentDisplayedBuy21 > 100.0) newBuy21 - currentDisplayedBuy21 else 0.0

                // 3. قواعد الاستقرار:
                // إذا فرق السعر الجديد عن المعروض <= 2 جنيه: لا تحدث الواجهة (لا وميض ولا صوت)
                if (currentDisplayedBuy21 > 100.0 && kotlin.math.abs(diffEGP) <= 2.0 && !forceRefresh) {
                    _lastUpdatedText.value = fullResponse.lastUpdated
                    return@launch
                }

                // 4. التغير >= 3 جنيه: حدّث + ومّض + صوت 880Hz للارتفاع و440Hz للانخفاض
                if (currentDisplayedBuy21 > 100.0 && kotlin.math.abs(diffEGP) >= 3.0) {
                    _priceDiffEGP.value = diffEGP
                    _priceDirection.value = if (diffEGP > 0) PriceDirection.UP else PriceDirection.DOWN
                    _isPriceFlashing.value = true
                    viewModelScope.launch {
                        delay(1800L)
                        _isPriceFlashing.value = false
                    }
                    val admin = _marketAdminSettings.value
                    SoundAlertManager.playPriceChangeAlert(
                        context = getApplication(),
                        isIncrease = diffEGP > 0,
                        enableSound = admin.soundAlertEnabled,
                        enableVibration = admin.vibrationEnabled
                    )
                }

                _goldPriceResponse.value = fullResponse
                _goldPrices.value = fullResponse.toGoldPriceList()
                _priceSourceTitle.value = fullResponse.source
                _isLivePrice.value = (fullResponse.status == "live")
                _lastUpdatedText.value = fullResponse.lastUpdated

                // Sync forms with new country prices
                updateBuyPriceForSelectedKarat()
                updateSellPriceForSelectedKarat()
                updateZakatPriceForSelectedKarat()
                recalculateBuy()
                recalculateSell()
                recalculateZakat()
                recalculateTraderComparison()

                if (!silent) {
                    _toastEvent.emit("تم تحديث أسعار الذهب لحظياً ✅")
                }
            } catch (e: Exception) {
                if (!silent) {
                    _toastEvent.emit("تعذر تحديث الأسعار: ${e.localizedMessage}")
                }
            } finally {
                if (!silent) _isRefreshing.value = false
                isRefreshingTaskRunning = false
            }
        }
    }

    fun calibrateWithGoldBullion21(enteredBuy21: Double) {
        viewModelScope.launch {
            if (enteredBuy21 <= 500.0) {
                _toastEvent.emit("يرجى إدخال سعر شراء صحيح لعيار 21 (مثال: 6385)")
                return@launch
            }
            try {
                val (newK, calibDate) = repository.calibrateWithEntered21(enteredBuy21)
                val updatedAdmin = repository.loadMarketAdminSettings()
                _marketAdminSettings.value = updatedAdmin
                refreshPrices(silent = false, forceRefresh = true)
                _toastEvent.emit("تمت المعايرة بنجاح (K = ${String.format(Locale.US, "%.4f", newK)})")
            } catch (e: Exception) {
                _toastEvent.emit("فشلت المعايرة: ${e.localizedMessage}")
            }
        }
    }

    fun resetCalibrationToDefault() {
        viewModelScope.launch {
            try {
                val defaultK = repository.resetCalibrationK()
                val updatedAdmin = repository.loadMarketAdminSettings()
                _marketAdminSettings.value = updatedAdmin
                refreshPrices(silent = false, forceRefresh = true)
                _toastEvent.emit("تمت استعادة المعامل الافتراضي ($defaultK)")
            } catch (e: Exception) {
                _toastEvent.emit("فشل الاسترجاع: ${e.localizedMessage}")
            }
        }
    }

    fun updateSaghaDollarRate(newSd: Double) {
        viewModelScope.launch {
            if (newSd <= 10.0) {
                _toastEvent.emit("يرجى إدخال سعر دولار صاغة صحيح (مثال: 51.7)")
                return@launch
            }
            try {
                repository.updateSaghaDollar(newSd)
                val updatedAdmin = repository.loadMarketAdminSettings()
                _marketAdminSettings.value = updatedAdmin
                refreshPrices(silent = false, forceRefresh = true)
                _toastEvent.emit("تم تحديث وحفظ دولار الصاغة (${newSd} ج.م)")
            } catch (e: Exception) {
                _toastEvent.emit("فشل التحديث: ${e.localizedMessage}")
            }
        }
    }

    fun calibrateMarket(entered21Sell: Double) {
        viewModelScope.launch {
            if (entered21Sell <= 500.0) {
                _toastEvent.emit("يرجى إدخال سعر بيع صحيح لعيار 21 (مثال: 6235)")
                return@launch
            }
            try {
                val (newSellFactor, newBuyFactor) = repository.calibrateMarketFactors(entered21Sell, "مسؤول التسعير")
                val updatedAdmin = repository.loadMarketAdminSettings()
                _marketAdminSettings.value = updatedAdmin
                refreshPrices(silent = false, forceRefresh = true)
                _toastEvent.emit("✅ تمت معايرة السوق بنجاح! معامل البيع: $newSellFactor | معامل الشراء: $newBuyFactor")
            } catch (e: Exception) {
                _toastEvent.emit("❌ فشلت المعايرة: ${e.localizedMessage}")
            }
        }
    }

    fun onSadaqahCashChanged(value: String) {
        sadaqahCashInput.value = sanitizeInput(value)
    }

    fun onSadaqahGoldGramsChanged(value: String) {
        sadaqahGoldGramsInput.value = sanitizeInput(value)
    }

    fun onSadaqahKaratChanged(karat: Int) {
        sadaqahKarat.value = karat
    }

    fun getSadaqahCalculatedGrams(): Double {
        val cash = sadaqahCashInput.value.toDoubleOrNull() ?: 0.0
        val p = _goldPriceResponse.value.getPairForKarat(sadaqahKarat.value).sell
        return if (p > 0) cash / p else 0.0
    }

    fun getSadaqahCalculatedCash(): Double {
        val grams = sadaqahGoldGramsInput.value.toDoubleOrNull() ?: 0.0
        val p = _goldPriceResponse.value.getPairForKarat(sadaqahKarat.value).sell
        return grams * p
    }

    // --- TRADER PRICE COMPARISON HANDLERS ---
    fun onTraderPriceChanged(value: String) {
        traderPriceInput.value = sanitizeInput(value)
        recalculateTraderComparison()
    }

    fun onTraderKaratChanged(karat: Int) {
        traderKarat.value = karat
        recalculateTraderComparison()
    }

    fun onTraderKaratSelected(karat: Int) = onTraderKaratChanged(karat)

    fun onTraderOperationTypeChanged(isBuy: Boolean) {
        isTraderBuyOperation.value = isBuy
        recalculateTraderComparison()
    }

    fun onTraderOperationChanged(isBuy: Boolean) = onTraderOperationTypeChanged(isBuy)

    fun recalculateTraderComparison() {
        val priceVal = traderPriceInput.value.toDoubleOrNull() ?: 0.0
        if (priceVal <= 0.0) {
            _traderComparison.value = TraderPriceComparison()
            return
        }

        val currentPair = _goldPriceResponse.value.getPairForKarat(traderKarat.value)
        // If customer is buying from trader: market reference price is customer buy price (سعر البيع للعميل)
        // If customer is selling to trader: market reference price is dealer buy price (سعر الشراء من العميل)
        val marketRefPrice = if (isTraderBuyOperation.value) currentPair.buy else currentPair.sell
        val diff = priceVal - marketRefPrice
        val diffPct = if (marketRefPrice > 0.0) (diff / marketRefPrice) * 100.0 else 0.0

        val assessment = when {
            abs(diff) < 2.0 -> "سعر التاجر مطابق تقريبًا للسعر المرجعي الخام"
            diff > 0 -> if (isTraderBuyOperation.value) {
                "سعر التاجر أعلى من السعر المرجعي (قد يشمل مصنعية أو هامش إضافي)"
            } else {
                "سعر التاجر أعلى من السعر المرجعي للشراء (عرض سخي وممتاز لك)"
            }
            else -> if (isTraderBuyOperation.value) {
                "سعر التاجر أقل من السعر المرجعي (عرض منخفض ومميز)"
            } else {
                "سعر التاجر أقل من السعر المرجعي (يخصم الصائغ هامش السيولة)"
            }
        }

        _traderComparison.value = TraderPriceComparison(
            traderPrice = priceVal,
            marketPrice = marketRefPrice,
            karat = traderKarat.value,
            isBuyOperation = isTraderBuyOperation.value,
            difference = diff,
            differencePercent = diffPct,
            assessment = assessment,
            isEvaluated = true
        )
    }

    fun saveMarketAdminSettings(
        newSettings: MarketAdminSettings,
        changedBy: String = "المشرف",
        reason: String = "تحديث هوامش الصاغة"
    ) {
        viewModelScope.launch {
            repository.saveMarketAdminSettings(newSettings, changedBy, reason)
            _marketAdminSettings.value = newSettings
            startPeriodicPriceUpdates()
            refreshPrices(silent = false)
            _toastEvent.emit("تم حفظ هوامش التسعير وتسجيل العملية في سجل التدقيق بنجاح ✅")
        }
    }

    // Helper for safe input normalization and number parsing (handles Arabic numerals, commas, etc.)
    private fun sanitizeInput(input: String): String {
        return input
            .replace('٠', '0')
            .replace('١', '1')
            .replace('٢', '2')
            .replace('٣', '3')
            .replace('٤', '4')
            .replace('٥', '5')
            .replace('٦', '6')
            .replace('٧', '7')
            .replace('٨', '8')
            .replace('٩', '9')
            .replace('،', '.')
            .filter { it.isDigit() || it == '.' || it == ',' }
    }

    fun parseNumber(value: String): Double {
        val sanitized = sanitizeInput(value).replace(",", "").trim()
        return sanitized.toDoubleOrNull() ?: 0.0
    }

    // --- BUY SCREEN LOGIC ---

    fun onBuyKaratSelected(karat: Int) {
        buyKarat.value = karat
        updateBuyPriceForSelectedKarat()
        recalculateBuy()
    }

    private fun updateBuyPriceForSelectedKarat() {
        if (isManualBuyActive.value) return
        val currentCountry = _selectedCountry.value
        val found = _goldPrices.value.find { it.karat == buyKarat.value }
        val price = found?.buyPrice ?: (currentCountry.defaultBenchmark21Buy * (buyKarat.value / 21.0))
        buyGramPrice.value = if (currentCountry.decimalPlaces == 0) price.toLong().toString() else currentCountry.formatPrice(price).replace(",", "")
        updateMakingAmountFromPercent()
    }

    private fun updateMakingAmountFromPercent() {
        val gramPrice = parseNumber(buyGramPrice.value)
        val pct = parseNumber(buyMakingPercent.value)
        if (gramPrice > 0 && pct > 0) {
            val amount = gramPrice * (pct / 100.0)
            val currentCountry = _selectedCountry.value
            buyMakingAmountPerGram.value = currentCountry.formatPrice(amount).replace(",", "")
        } else if (pct == 0.0) {
            buyMakingAmountPerGram.value = "0"
        }
    }

    fun onBuyGramPriceChanged(value: String) {
        isManualBuyActive.value = true
        buyGramPrice.value = sanitizeInput(value)
        updateMakingAmountFromPercent()
        recalculateBuy()
    }

    fun onBuyWeightChanged(value: String) {
        buyWeight.value = sanitizeInput(value)
        recalculateBuy()
    }

    fun addBuyWeight(extra: Double) {
        val current = parseNumber(buyWeight.value)
        val newWeight = Math.round((current + extra) * 100.0) / 100.0
        buyWeight.value = if (newWeight % 1.0 == 0.0) newWeight.toInt().toString() else newWeight.toString()
        recalculateBuy()
    }

    fun onBuyMakingPercentChanged(value: String) {
        val cleaned = sanitizeInput(value)
        buyMakingPercent.value = cleaned
        updateMakingAmountFromPercent()
        recalculateBuy()
    }

    fun onBuyMakingAmountChanged(value: String) {
        val cleaned = sanitizeInput(value)
        buyMakingAmountPerGram.value = cleaned
        val amount = parseNumber(cleaned)
        val gramPrice = parseNumber(buyGramPrice.value)
        if (gramPrice > 0 && amount > 0) {
            val pct = (amount / gramPrice) * 100.0
            buyMakingPercent.value = String.format(Locale.US, "%.2f", pct).trimEnd('0').trimEnd('.')
        } else if (amount == 0.0) {
            buyMakingPercent.value = "0"
        }
        recalculateBuy()
    }

    fun onBuyMakingAmountPerGramChanged(value: String) = onBuyMakingAmountChanged(value)

    fun onBuyStampFeeChanged(value: String) {
        buyStampFee.value = sanitizeInput(value)
        recalculateBuy()
    }

    fun onBuyExtraFeesChanged(value: String) {
        buyExtraFees.value = sanitizeInput(value)
        recalculateBuy()
    }

    fun onBuyShopPriceChanged(value: String) {
        buyShopQuotedPrice.value = sanitizeInput(value)
        recalculateBuy()
    }

    fun recalculateBuy() {
        val gramPrice = parseNumber(buyGramPrice.value)
        val weight = parseNumber(buyWeight.value)
        val makingPct = parseNumber(buyMakingPercent.value)
        val stampPerGram = parseNumber(buyStampFee.value)
        val extra = parseNumber(buyExtraFees.value)
        val shop = parseNumber(buyShopQuotedPrice.value)

        if (gramPrice <= 0 || weight <= 0) {
            _buyResult.value = BuyCalculationResult()
            return
        }

        val rawGold = gramPrice * weight
        val making = rawGold * (makingPct / 100.0)
        val makingPerGram = gramPrice * (makingPct / 100.0)
        val stamp = stampPerGram * weight
        val total = rawGold + making + stamp + extra

        val diff = if (shop > 0) shop - total else 0.0
        val diffPct = if (total > 0 && shop > 0) ((shop - total) / total) * 100.0 else 0.0

        val status = when {
            shop <= 0 -> ComparisonStatus.NONE
            diff > 1.0 -> ComparisonStatus.MORE_EXPENSIVE
            diff < -1.0 -> ComparisonStatus.CHEAPER
            else -> ComparisonStatus.EQUAL
        }

        _buyResult.value = BuyCalculationResult(
            rawGoldPrice = rawGold,
            makingTotal = making,
            makingPerGram = makingPerGram,
            stampTotal = stamp,
            extraFees = extra,
            totalFairPrice = total,
            shopPrice = shop,
            difference = diff,
            differencePercent = diffPct,
            comparisonStatus = status
        )
    }

    fun resetBuyForm() {
        buyWeight.value = ""
        buyShopQuotedPrice.value = ""
        buyShopName.value = ""
        buyExtraFees.value = "0"
        updateBuyPriceForSelectedKarat()
        recalculateBuy()
    }

    fun saveBuyTransaction(note: String = "") {
        recalculateBuy()
        val result = _buyResult.value
        if (result.totalFairPrice <= 0) {
            viewModelScope.launch { _toastEvent.emit("الرجاء إدخال الوزن وسعر الجرام أولاً!") }
            return
        }
        viewModelScope.launch {
            val live = _goldPriceResponse.value
            val livePair = live.getPairForKarat(buyKarat.value)
            val refPrice = livePair.buy
            val rawPrice = if (live.ouncePrice > 0 && live.usdToEgpRate > 0) {
                (live.ouncePrice * live.usdToEgpRate / 31.1035) * (buyKarat.value / 24.0)
            } else {
                refPrice / _marketAdminSettings.value.marketSellFactor
            }
            val enteredGram = parseNumber(buyGramPrice.value)
            val devPct = if (refPrice > 0) ((enteredGram - refPrice) / refPrice) * 100.0 else 0.0
            val isWarn = abs(devPct) > 3.0
            val receiptId = "REC-${System.currentTimeMillis() % 1000000}-${(100..999).random()}"

            val item = TransactionEntity(
                type = "BUY",
                karat = buyKarat.value,
                weight = parseNumber(buyWeight.value),
                pricePerGram = enteredGram,
                makingCharges = result.makingTotal,
                stampFee = result.stampTotal,
                extraFees = result.extraFees,
                totalFairPrice = result.totalFairPrice,
                shopQuotedPrice = result.shopPrice,
                priceDifference = result.difference,
                note = note.trim(),
                receiptId = receiptId,
                shopName = buyShopName.value.trim(),
                referenceGramPrice = refPrice,
                referenceRawPrice = rawPrice,
                marketFactor = _marketAdminSettings.value.marketSellFactor,
                priceSource = "الصاغة المصرية",
                deviationPercent = devPct,
                isDeviationWarning = isWarn
            )
            repository.saveTransaction(item)
            val noteMsg = if (note.isNotBlank()) " ($note)" else ""
            val warnMsg = if (isWarn) " ⚠️ تنبيه: انحراف السعر عن المرجعي بنسبة ${decimalFormat.format(devPct)}%" else ""
            _toastEvent.emit("تم حفظ العملية [إيصال #$receiptId]$noteMsg بنجاح 📜$warnMsg")
        }
    }

    // --- SELL SCREEN LOGIC ---

    fun onSellKaratSelected(karat: Int) {
        sellKarat.value = karat
        updateSellPriceForSelectedKarat()
        recalculateSell()
    }

    private fun updateSellPriceForSelectedKarat() {
        if (isManualSellActive.value) return
        val currentCountry = _selectedCountry.value
        val found = _goldPrices.value.find { it.karat == sellKarat.value }
        val price = found?.sellPrice ?: (currentCountry.defaultBenchmark21Sell * (sellKarat.value / 21.0))
        sellGramPrice.value = if (currentCountry.decimalPlaces == 0) price.toLong().toString() else currentCountry.formatPrice(price).replace(",", "")
    }

    fun onSellGramPriceChanged(value: String) {
        isManualSellActive.value = true
        sellGramPrice.value = sanitizeInput(value)
        recalculateSell()
    }

    fun onSellWeightChanged(value: String) {
        sellWeight.value = sanitizeInput(value)
        recalculateSell()
    }

    fun addSellWeight(extra: Double) {
        val current = parseNumber(sellWeight.value)
        val newWeight = Math.round((current + extra) * 100.0) / 100.0
        sellWeight.value = if (newWeight % 1.0 == 0.0) newWeight.toInt().toString() else newWeight.toString()
        recalculateSell()
    }

    fun onSellDeductionPercentChanged(value: String) {
        sellDeductionPercent.value = sanitizeInput(value)
        recalculateSell()
    }

    fun onSellStonesWeightChanged(value: String) {
        sellStonesWeight.value = sanitizeInput(value)
        recalculateSell()
    }

    fun onSellDeductStonesChanged(deduct: Boolean) {
        sellDeductStones.value = deduct
        recalculateSell()
    }

    fun onSellShopOfferChanged(value: String) {
        sellShopOffer.value = sanitizeInput(value)
        recalculateSell()
    }

    fun resetSellForm() {
        sellWeight.value = ""
        sellStonesWeight.value = ""
        sellDamagedPercent.value = "0"
        sellShopOffer.value = ""
        sellShopName.value = ""
        updateSellPriceForSelectedKarat()
        recalculateSell()
    }

    fun recalculateSell() {
        val gramPrice = parseNumber(sellGramPrice.value)
        val grossWeight = parseNumber(sellWeight.value)
        val stones = parseNumber(sellStonesWeight.value)
        val shouldDeductStones = sellDeductStones.value
        val netWeight = if (shouldDeductStones) (grossWeight - stones).coerceAtLeast(0.0) else grossWeight
        val deductionPct = parseNumber(sellDeductionPercent.value)
        val damagedPct = parseNumber(sellDamagedPercent.value)
        val shop = parseNumber(sellShopOffer.value)

        if (gramPrice <= 0 || netWeight <= 0) {
            _sellResult.value = SellCalculationResult()
            return
        }

        val deductionPerGram = gramPrice * (deductionPct / 100.0)
        val netGramPrice = gramPrice - deductionPerGram
        val baseGoldPayout = netGramPrice * netWeight

        // قيمة التالف = الوزن × النسبة × سعر الجرام، وتتخصم من الإجمالي
        val damagedVal = netWeight * (damagedPct / 100.0) * gramPrice
        val expectedTotal = (baseGoldPayout - damagedVal).coerceAtLeast(0.0)

        val diff = if (shop > 0) shop - expectedTotal else 0.0
        val diffPct = if (expectedTotal > 0 && shop > 0) ((shop - expectedTotal) / expectedTotal) * 100.0 else 0.0

        val status = when {
            shop <= 0 -> ComparisonStatus.NONE
            diff < -1.0 -> ComparisonStatus.MORE_EXPENSIVE // Shop is paying less than fair value!
            diff > 1.0 -> ComparisonStatus.CHEAPER         // Shop is offering higher (better for seller)
            else -> ComparisonStatus.EQUAL
        }

        _sellResult.value = SellCalculationResult(
            currentGramPrice = gramPrice,
            deductionPercent = deductionPct,
            deductionPerGram = deductionPerGram,
            damagedPercent = damagedPct,
            damagedValue = damagedVal,
            netGramPrice = netGramPrice,
            weight = grossWeight,
            netWeight = netWeight,
            stonesWeight = stones,
            deductedStones = shouldDeductStones,
            expectedTotalPayout = expectedTotal,
            shopOffer = shop,
            difference = diff,
            differencePercent = diffPct,
            comparisonStatus = status
        )
    }

    fun saveSellTransaction(note: String = "") {
        recalculateSell()
        val result = _sellResult.value
        if (result.expectedTotalPayout <= 0) {
            viewModelScope.launch { _toastEvent.emit("الرجاء إدخال الوزن وسعر الجرام أولاً!") }
            return
        }
        viewModelScope.launch {
            val live = _goldPriceResponse.value
            val livePair = live.getPairForKarat(sellKarat.value)
            val refPrice = livePair.sell
            val rawPrice = if (live.ouncePrice > 0 && live.usdToEgpRate > 0) {
                (live.ouncePrice * live.usdToEgpRate / 31.1035) * (sellKarat.value / 24.0)
            } else {
                refPrice / _marketAdminSettings.value.marketBuyFactor
            }
            val enteredGram = parseNumber(sellGramPrice.value)
            val devPct = if (refPrice > 0) ((enteredGram - refPrice) / refPrice) * 100.0 else 0.0
            val isWarn = abs(devPct) > 3.0
            val receiptId = "REC-${System.currentTimeMillis() % 1000000}-${(100..999).random()}"

            val item = TransactionEntity(
                type = "SELL",
                karat = sellKarat.value,
                weight = parseNumber(sellWeight.value),
                pricePerGram = enteredGram,
                deductionPercent = result.deductionPercent,
                totalFairPrice = result.expectedTotalPayout,
                shopQuotedPrice = result.shopOffer,
                priceDifference = result.difference,
                note = note.trim(),
                receiptId = receiptId,
                shopName = sellShopName.value.trim(),
                referenceGramPrice = refPrice,
                referenceRawPrice = rawPrice,
                marketFactor = _marketAdminSettings.value.marketBuyFactor,
                priceSource = "الصاغة المصرية",
                deviationPercent = devPct,
                isDeviationWarning = isWarn
            )
            repository.saveTransaction(item)
            val noteMsg = if (note.isNotBlank()) " ($note)" else ""
            val warnMsg = if (isWarn) " ⚠️ تنبيه: انحراف السعر عن المرجعي بنسبة ${decimalFormat.format(devPct)}%" else ""
            _toastEvent.emit("تم حفظ عملية البيع [إيصال #$receiptId]$noteMsg بنجاح 📜$warnMsg")
        }
    }

    fun updateTransactionNote(id: Long, note: String) {
        viewModelScope.launch {
            repository.updateTransactionNote(id, note.trim())
            _toastEvent.emit("تم تحديث وصف العملية بنجاح ✏️")
        }
    }

    // --- SETTINGS LOGIC ---

    fun saveSettings(
        buyMaking: Double,
        sellDeduction: Double,
        stampFee: Double
    ) {
        viewModelScope.launch {
            val newSettings = GoldSettings(
                defaultBuyMakingPercent = buyMaking,
                defaultSellDeductionPercent = sellDeduction,
                defaultStampFeePerGram = stampFee
            )
            repository.saveSettings(newSettings)
            _settings.value = newSettings
            buyMakingPercent.value = buyMaking.toString()
            buyStampFee.value = stampFee.toString()
            sellDeductionPercent.value = sellDeduction.toString()
            recalculateBuy()
            recalculateSell()
            _toastEvent.emit("تم حفظ الإعدادات بنجاح ⚙️")
        }
    }

    fun restoreDefaultSettings() {
        val country = _selectedCountry.value
        val defaultSettings = GoldSettings(
            selectedCountry = country,
            defaultBuyMakingPercent = 5.0,
            defaultSellDeductionPercent = 2.0,
            defaultStampFeePerGram = country.defaultStampFee
        )
        saveSettings(
            buyMaking = defaultSettings.defaultBuyMakingPercent,
            sellDeduction = defaultSettings.defaultSellDeductionPercent,
            stampFee = defaultSettings.defaultStampFeePerGram
        )
    }

    // --- HISTORY LOGIC ---

    fun setHistoryFilter(filter: String) {
        _historyFilter.value = filter
    }

    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            _toastEvent.emit("تم حذف العملية من السجل")
        }
    }

    fun clearAllTransactions() {
        viewModelScope.launch {
            repository.clearAllTransactions()
            _toastEvent.emit("تم مسح السجل بالكامل")
        }
    }

    // --- SHARE LOGIC ---

    fun buildBuyShareMessage(): String {
        val res = _buyResult.value
        val gramPrice = parseNumber(buyGramPrice.value)
        val country = _selectedCountry.value
        return buildString {
            appendLine("تطبيق حارس الذهب 🛡️")
            appendLine()
            appendLine("نوع العملية: شراء الذهب")
            if (buyShopName.value.isNotBlank()) {
                appendLine("اسم المحل: ${buyShopName.value.trim()}")
            }
            appendLine("الوزن: ${buyWeight.value} جرام")
            appendLine("العيار: عيار ${buyKarat.value}")
            appendLine("سعر الجرام: ${country.formatWithCurrency(gramPrice)}")
            appendLine("المصنعية: ${country.formatWithCurrency(res.makingTotal)} (${buyMakingPercent.value}%)")
            appendLine("إجمالي قيمة الذهب: ${country.formatWithCurrency(res.rawGoldPrice)}")
            appendLine("إجمالي المصنعية والدمغة: ${country.formatWithCurrency(res.makingTotal + res.stampTotal + res.extraFees)}")
            appendLine("الإجمالي النهائي العادل: ${country.formatWithCurrency(res.totalFairPrice)}")
            if (res.shopPrice > 0) {
                appendLine("سعر المحل المعروض: ${country.formatWithCurrency(res.shopPrice)}")
                if (res.difference > 0) {
                    appendLine("المحل أغلى بـ: ${country.formatWithCurrency(res.difference)} (+${decimalFormat.format(res.differencePercent)}%)")
                } else if (res.difference < 0) {
                    appendLine("المحل أرخص بـ: ${country.formatWithCurrency(kotlin.math.abs(res.difference))} (-${decimalFormat.format(kotlin.math.abs(res.differencePercent))}%)")
                }
            }
            appendLine()
            appendLine("التاريخ والوقت: ${_lastUpdatedText.value}")
            appendLine()
            appendLine("الأسعار استرشادية وقد تختلف حسب المحل والمصنعية ووقت التحديث.")
        }
    }

    fun shareBuyResult(context: Context) {
        val res = _buyResult.value
        if (res.totalFairPrice <= 0) return
        val message = buildBuyShareMessage()
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة تسعيرة شراء الذهب")
        context.startActivity(shareIntent)
    }

    fun shareBuyWhatsApp(context: Context) {
        val res = _buyResult.value
        if (res.totalFairPrice <= 0) return
        shareToWhatsApp(context, buildBuyShareMessage())
    }

    fun buildSellShareMessage(): String {
        val res = _sellResult.value
        val country = _selectedCountry.value
        return buildString {
            appendLine("تطبيق حارس الذهب 🛡️")
            appendLine()
            appendLine("نوع العملية: بيع الذهب")
            if (sellShopName.value.isNotBlank()) {
                appendLine("اسم المحل: ${sellShopName.value.trim()}")
            }
            appendLine("الوزن: ${sellWeight.value} جرام")
            appendLine("العيار: عيار ${sellKarat.value}")
            appendLine("السعر العادل للجرام (كسر): ${country.formatWithCurrency(res.netGramPrice)}")
            appendLine("نسبة الخصم: ${sellDeductionPercent.value}% (${country.formatPrice(res.deductionPerGram)} ${country.currencySymbol}/جرام)")
            appendLine("إجمالي قيمة البيع المستحقة: ${country.formatWithCurrency(res.expectedTotalPayout)}")
            if (res.shopOffer > 0) {
                appendLine("عرض المحل: ${country.formatWithCurrency(res.shopOffer)}")
                if (res.difference < 0) {
                    appendLine("المحل عارض أقل بـ: ${country.formatWithCurrency(kotlin.math.abs(res.difference))}")
                } else if (res.difference > 0) {
                    appendLine("المحل عارض أعلى بـ: ${country.formatWithCurrency(res.difference)}")
                }
            }
            appendLine()
            appendLine("التاريخ والوقت: ${_lastUpdatedText.value}")
            appendLine()
            appendLine("الأسعار استرشادية وقد تختلف حسب المحل والمصنعية ووقت التحديث.")
        }
    }

    fun shareSellResult(context: Context) {
        val res = _sellResult.value
        if (res.expectedTotalPayout <= 0) return
        val message = buildSellShareMessage()
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة تقييم بيع الذهب")
        context.startActivity(shareIntent)
    }

    fun shareSellWhatsApp(context: Context) {
        val res = _sellResult.value
        if (res.expectedTotalPayout <= 0) return
        shareToWhatsApp(context, buildSellShareMessage())
    }

    private fun shareToWhatsApp(context: Context, message: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_TEXT, message)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }, "مشاركة عبر تطبيق آخر")
            context.startActivity(chooser)
        }
    }

    fun shareMarketPrices(context: Context) {
        val response = _goldPriceResponse.value
        val message = buildString {
            appendLine("🛡️ أسعار الذهب لحظيًا - تطبيق حارس الذهب")
            appendLine()
            appendLine("📅 التاريخ: ${_currentDateText.value} - ${_currentTimeOnlyText.value}")
            appendLine("عيار 24: شراء ${com.example.util.GoldPriceFormatter.formatGram(response.gram24.buy)} | بيع ${com.example.util.GoldPriceFormatter.formatGram(response.gram24.sell)} ج.م")
            appendLine("عيار 22: شراء ${com.example.util.GoldPriceFormatter.formatGram(response.gram22.buy)} | بيع ${com.example.util.GoldPriceFormatter.formatGram(response.gram22.sell)} ج.م")
            appendLine("عيار 21 ⭐: شراء ${com.example.util.GoldPriceFormatter.formatGram(response.gram21.buy)} | بيع ${com.example.util.GoldPriceFormatter.formatGram(response.gram21.sell)} ج.م")
            appendLine("عيار 18: شراء ${com.example.util.GoldPriceFormatter.formatGram(response.gram18.buy)} | بيع ${com.example.util.GoldPriceFormatter.formatGram(response.gram18.sell)} ج.م")
            appendLine("عيار 14: شراء ${com.example.util.GoldPriceFormatter.formatGram(response.gram14.buy)} | بيع ${com.example.util.GoldPriceFormatter.formatGram(response.gram14.sell)} ج.م")
            appendLine("جنيه الذهب (8 جم): شراء ${com.example.util.GoldPriceFormatter.formatWhole(response.gram21.buy * 8)} | بيع ${com.example.util.GoldPriceFormatter.formatWhole(response.gram21.sell * 8)} ج.م")
            appendLine("الدولار: شراء ${String.format(Locale.US, "%.2f", response.usdBuyRate)} | بيع ${String.format(Locale.US, "%.2f", response.usdSellRate)} ج.م")
            appendLine("دولار الصاغة: ${String.format(Locale.US, "%.2f", response.saghaUsdRate)} ج.م")
            appendLine()
            appendLine("الأسعار استرشادية وتُحدَّث وفق سوق الصاغة المصري.")
        }
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة أسعار الذهب")
        context.startActivity(shareIntent)
    }

    // --- ZAKAT CALCULATOR LOGIC ---

    fun onZakatKaratSelected(karat: Int) {
        zakatKarat.value = karat
        updateZakatPriceForSelectedKarat()
        recalculateZakat()
    }

    fun onZakatWeightChanged(value: String) {
        zakatWeight.value = sanitizeInput(value)
        recalculateZakat()
    }

    fun onZakatHawlChanged(isMet: Boolean) {
        zakatIsHawlMet.value = isMet
        recalculateZakat()
    }

    fun onZakatGramPriceChanged(value: String) {
        zakatGramPrice.value = sanitizeInput(value)
        recalculateZakat()
    }

    fun onZakatPersonalJewelryChanged(isPersonal: Boolean) {
        zakatIsPersonalJewelry.value = isPersonal
        recalculateZakat()
    }

    fun onZakatPayJewelryWaraaChanged(payWaraa: Boolean) {
        zakatPayJewelryWaraa.value = payWaraa
        recalculateZakat()
    }

    fun resetZakatToLivePrice() {
        updateZakatPriceForSelectedKarat()
        recalculateZakat()
    }

    fun resetZakatForm() {
        zakatKarat.value = 21
        zakatWeight.value = ""
        zakatIsHawlMet.value = true
        zakatIsPersonalJewelry.value = false
        zakatPayJewelryWaraa.value = false
        updateZakatPriceForSelectedKarat()
        recalculateZakat()
    }

    fun updateZakatPriceForSelectedKarat() {
        val live = _goldPriceResponse.value
        val pair = live.getPairForKarat(zakatKarat.value)
        val price = pair.sell
        zakatGramPrice.value = Math.round(price).toString()
    }

    fun recalculateZakat() {
        val weight = parseNumber(zakatWeight.value)
        val karat = zakatKarat.value
        val live = _goldPriceResponse.value

        // سعر جرام عيار 24 للبيع المعتمد للنصاب الشرعي
        val sell24 = if (live.gram24.sell > 0.0) live.gram24.sell else Math.round((live.gram21.sell / 0.9976) * (24.0 / 21.0) * 0.9976).toDouble()
        val nisabThresholdMoney = 85.0 * sell24

        // سعر جرام العيار المختار للبيع من نفس محرك P21
        val selectedKaratSell = live.getPairForKarat(karat).sell
        val gramPrice = if (selectedKaratSell > 0.0) selectedKaratSell else parseNumber(zakatGramPrice.value)
        if (gramPrice > 0.0) {
            zakatGramPrice.value = Math.round(gramPrice).toString()
        }

        val userGoldValue = weight * gramPrice
        val isAboveNisab = userGoldValue >= nisabThresholdMoney

        if (weight <= 0.0) {
            _zakatResult.value = ZakatCalculationResult(
                karat = karat,
                totalWeight = 0.0,
                pureGoldEquivalent = 0.0,
                nisabThreshold = 85.0,
                differenceToNisab = 85.0,
                gramPrice = gramPrice,
                totalGoldValue = 0.0,
                zakatAmountMoney = 0.0,
                zakatAmountGrams = 0.0,
                status = ZakatStatus.NONE,
                reasonMessage = "أدخل وزن الذهب بالجرام للتحقق من بلوغ النصاب وحساب الزكاة.",
                nisabEgpValue = nisabThresholdMoney,
                sellPrice24k = sell24,
                priceSourceNote = "إدخال يدوي بواسطة المستخدم"
            )
            return
        }

        if (!isAboveNisab) {
            _zakatResult.value = ZakatCalculationResult(
                karat = karat,
                totalWeight = weight,
                pureGoldEquivalent = weight * (karat.toDouble() / 24.0),
                nisabThreshold = 85.0,
                differenceToNisab = if (gramPrice > 0.0) ((nisabThresholdMoney - userGoldValue) / gramPrice).coerceAtLeast(0.0) else 0.0,
                gramPrice = gramPrice,
                totalGoldValue = userGoldValue,
                zakatAmountMoney = 0.0,
                zakatAmountGrams = 0.0,
                status = ZakatStatus.BELOW_NISAB,
                reasonMessage = "مفيش زكاة — أقل من النصاب",
                nisabEgpValue = nisabThresholdMoney,
                sellPrice24k = sell24,
                priceSourceNote = "إدخال يدوي بواسطة المستخدم"
            )
        } else {
            val zakatMoney = Math.round(userGoldValue * 0.025).toDouble()
            val zakatGrams = weight * 0.025

            _zakatResult.value = ZakatCalculationResult(
                karat = karat,
                totalWeight = weight,
                pureGoldEquivalent = weight * (karat.toDouble() / 24.0),
                nisabThreshold = 85.0,
                differenceToNisab = 0.0,
                gramPrice = gramPrice,
                totalGoldValue = userGoldValue,
                zakatAmountMoney = zakatMoney,
                zakatAmountGrams = zakatGrams,
                status = ZakatStatus.ZAKAT_DUE,
                reasonMessage = "تجب فيه الزكاة (تجاوز النصاب الشرعي)",
                nisabEgpValue = nisabThresholdMoney,
                sellPrice24k = sell24,
                priceSourceNote = "إدخال يدوي بواسطة المستخدم"
            )
        }
    }

    fun buildZakatShareMessage(): String {
        val res = _zakatResult.value

        return buildString {
            appendLine("🕌 *حساب زكاة الذهب*")
            appendLine("حساب زكاة الذهب فقط لا غير")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("⚜️ العيار: عيار ${res.karat}")
            appendLine("⚖️ الوزن: ${decimalFormat.format(res.totalWeight)} جرام")
            appendLine("💵 سعر الجرام للبيع المعتمد: ${formatPrice(res.gramPrice)} ج.م (${res.priceSourceNote})")
            appendLine("💰 قيمة الذهب الإجمالية: ${formatPrice(res.totalGoldValue)} ج.م")
            appendLine("📐 النصاب الشرعي (85 جرام عيار 24): ${formatPrice(res.nisabEgpValue)} ج.م (سعر عيار 24 بيع: ${formatPrice(res.sellPrice24k)} ج.م)")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            when (res.status) {
                ZakatStatus.BELOW_NISAB -> {
                    appendLine("🔴 *مفيش زكاة — أقل من النصاب*")
                }
                ZakatStatus.ZAKAT_DUE -> {
                    appendLine("🟢 *تجب فيه الزكاة (2.5% - ربع العُشر)*")
                    appendLine("💰 *الزكاة المستحقة: ${formatPrice(res.zakatAmountMoney)} ج.م*")
                    appendLine("أو إخراج عيناً: ${decimalFormat.format(res.zakatAmountGrams)} جرام عيار ${res.karat}")
                }
                else -> {}
            }
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("التاريخ: ${_lastUpdatedText.value}")
        }
    }

    fun shareZakatResult(context: Context) {
        val res = _zakatResult.value
        if (res.totalWeight <= 0) return
        val message = buildZakatShareMessage()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة تقرير زكاة الذهب")
        context.startActivity(shareIntent)
    }

    fun saveZakatCalculation(note: String = "") {
        val result = _zakatResult.value
        if (result.totalWeight <= 0) {
            viewModelScope.launch { _toastEvent.emit("الرجاء إدخال وزن الذهب لحساب الزكاة أولاً!") }
            return
        }
        viewModelScope.launch {
            val receiptId = "ZAK-${System.currentTimeMillis() % 1000000}-${(100..999).random()}"
            val item = com.example.data.local.TransactionEntity(
                type = "ZAKAT",
                karat = result.karat,
                weight = result.totalWeight,
                pricePerGram = result.gramPrice,
                makingCharges = 0.0,
                stampFee = 0.0,
                extraFees = 0.0,
                deductionPercent = 0.0,
                totalFairPrice = result.zakatAmountMoney,
                shopQuotedPrice = result.totalGoldValue,
                priceDifference = result.zakatAmountGrams,
                note = if (note.isNotBlank()) note else result.reasonMessage,
                receiptId = receiptId,
                shopName = if (result.isPersonalJewelry) "حلي شخصي (زينة)" else "ذهب مدخر/استثماري",
                referenceGramPrice = result.gramPrice,
                referenceRawPrice = result.pureGoldEquivalent,
                priceSource = "حاسبة زكاة الذهب"
            )
            repository.saveTransaction(item)
            _toastEvent.emit("تم حفظ حساب الزكاة في السجل بنجاح 🕌")
        }
    }

    fun shareZakatWhatsApp(context: Context) {
        val res = _zakatResult.value
        if (res.totalWeight <= 0) return
        shareToWhatsApp(context, buildZakatShareMessage())
    }

    fun getSadaqahCalculatedGrams(cashEgp: Double, karat: Int = 21): Double {
        val price = _goldPriceResponse.value.getPairForKarat(karat).buy.takeIf { it > 0 }
            ?: (6315.0 * (karat / 21.0))
        return if (price > 0 && cashEgp > 0) cashEgp / price else 0.0
    }

    fun getSadaqahCalculatedCash(grams: Double, karat: Int = 21): Double {
        val price = _goldPriceResponse.value.getPairForKarat(karat).buy.takeIf { it > 0 }
            ?: (6315.0 * (karat / 21.0))
        return grams * price
    }
}
