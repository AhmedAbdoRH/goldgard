package com.example.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class PriceDirection {
    UP, DOWN, STABLE
}

enum class Country(
    val code: String,
    val nameAr: String,
    val flag: String,
    val currencySymbol: String,
    val currencyNameAr: String,
    val currencyCode: String,
    val isaghaEndpoint: String,
    val defaultBenchmark21Buy: Double,
    val defaultBenchmark21Sell: Double,
    val defaultStampFee: Double,
    val decimalPlaces: Int
) {
    EGYPT(
        code = "EG",
        nameAr = "مصر",
        flag = "🇪🇬",
        currencySymbol = "ج.م",
        currencyNameAr = "جنيه مصري",
        currencyCode = "EGP",
        isaghaEndpoint = "https://market.isagha.com/prices",
        defaultBenchmark21Buy = 6325.0,
        defaultBenchmark21Sell = 6275.0,
        defaultStampFee = 10.0,
        decimalPlaces = 0
    );

    fun formatPrice(amount: Double): String {
        if (decimalPlaces == 0) {
            return Math.round(amount).toString()
        }
        val pattern = when (decimalPlaces) {
            2 -> "#,##0.00"
            3 -> "#,##0.000"
            else -> "#,##0.##"
        }
        return DecimalFormat(pattern, DecimalFormatSymbols(Locale.US)).format(amount)
    }

    fun formatWithCurrency(amount: Double): String {
        return "${formatPrice(amount)} $currencySymbol"
    }

    companion object {
        fun fromCode(code: String?): Country {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: EGYPT
        }
    }
}

data class GoldPrice(
    val karat: Int,
    val buyPrice: Double,   // سعر الشراء (المستهلك يشتري من المحل)
    val sellPrice: Double,  // سعر البيع (المستهلك يبيع للمحل)
    val change24h: Double = 0.0
)

data class GoldSettings(
    val selectedCountry: Country = Country.EGYPT,
    val defaultBuyMakingPercent: Double = 5.0,
    val defaultSellDeductionPercent: Double = 2.0,
    val defaultStampFeePerGram: Double = 10.0,
    val defaultDamagedPercent: Double = 0.0,
    val defaultSaghaUsd: Double = 51.7
)

data class BuyCalculationResult(
    val rawGoldPrice: Double = 0.0,
    val makingTotal: Double = 0.0,
    val makingPerGram: Double = 0.0,
    val stampTotal: Double = 0.0,
    val extraFees: Double = 0.0,
    val totalFairPrice: Double = 0.0,
    val shopPrice: Double = 0.0,
    val difference: Double = 0.0,
    val differencePercent: Double = 0.0,
    val comparisonStatus: ComparisonStatus = ComparisonStatus.NONE
)

data class SellCalculationResult(
    val currentGramPrice: Double = 0.0,
    val deductionPercent: Double = 0.0,
    val deductionPerGram: Double = 0.0,
    val damagedPercent: Double = 0.0,
    val damagedValue: Double = 0.0,
    val netGramPrice: Double = 0.0,
    val weight: Double = 0.0,
    val netWeight: Double = 0.0,
    val stonesWeight: Double = 0.0,
    val deductedStones: Boolean = true,
    val expectedTotalPayout: Double = 0.0,
    val shopOffer: Double = 0.0,
    val difference: Double = 0.0,
    val differencePercent: Double = 0.0,
    val comparisonStatus: ComparisonStatus = ComparisonStatus.NONE
)

enum class ComparisonStatus {
    NONE,
    MORE_EXPENSIVE, // المحل أغلى في الشراء / أو عارض أقل في البيع
    CHEAPER,        // المحل أرخص في الشراء / أو عارض أعلى في البيع
    EQUAL           // مطابق
}

enum class ScreenType {
    HOME,
    PRICES,
    BUY,
    SELL,
    SETTINGS,
    HISTORY,
    ZAKAT,
    TIPS
}

enum class ZakatStatus {
    NONE,
    BELOW_NISAB,              // 🔴 الذهب أقل من النصاب
    HAWL_NOT_MET,             // 🟡 لم يكتمل الحول
    EXEMPT_PERSONAL_JEWELRY,  // ⚪ معفي (حُلي زينة شخصية عند جمهور الفقهاء)
    ZAKAT_DUE                 // 🟢 تجب الزكاة
}

data class ZakatCalculationResult(
    val karat: Int = 21,
    val totalWeight: Double = 0.0,
    val pureGoldEquivalent: Double = 0.0,
    val nisabThreshold: Double = 85.0,
    val differenceToNisab: Double = 0.0,
    val gramPrice: Double = 0.0,
    val totalGoldValue: Double = 0.0,
    val isHawlMet: Boolean = true,
    val isPersonalJewelry: Boolean = false,
    val payWaraa: Boolean = false,
    val zakatAmountMoney: Double = 0.0,
    val zakatAmountGrams: Double = 0.0,
    val zakatAmountPureGrams: Double = 0.0,
    val status: ZakatStatus = ZakatStatus.NONE,
    val reasonMessage: String = "",
    val nisabEgpValue: Double = 0.0,
    val sellPrice24k: Double = 0.0,
    val priceSourceNote: String = "إدخال يدوي بواسطة المستخدم"
) {
    val gramPriceUsed: Double get() = gramPrice
}

/**
 * High-precision model representing price pair for buy and sell.
 * Note on strict terminology:
 * buy: سعر البيع للعميل (السعر الذي يدفعه العميل عند شراء الذهب من الصائغ)
 * sell: سعر الشراء من العميل (السعر الذي يدفعه الصائغ للعميل عند بيع الذهب له)
 */
data class PricePair(
    val buy: Double = 0.0,  // سعر البيع للعميل (العميل يشتري من الصائغ)
    val sell: Double = 0.0  // سعر الشراء من العميل (الصائغ يشتري من العميل)
) {
    val spread: Double get() = (buy - sell).coerceAtLeast(0.0)
}

data class KaratSpread(
    val k24: Double = 0.0,
    val k22: Double = 0.0,
    val k21: Double = 0.0,
    val k18: Double = 0.0,
    val k14: Double = 0.0
)

data class BullionItem(
    val weightGrams: Double,
    val label: String,
    val buyPrice: Double,
    val sellPrice: Double
)

data class GoldPriceResponse(
    val source: String = "مصدر لحظي عالمي",
    val sourceType: String = "live_global_formula",
    val metal: String = "XAU",
    val currency: String = "EGP",
    val timestamp: Long = System.currentTimeMillis(),
    val datetime: String = "",
    val lastUpdated: String = "",
    val lastChecked: String = "",
    val status: String = "live", // "live", "unchanged", "cached", "stale", "unavailable", "anomaly"
    val dataAgeSeconds: Long = 0L,
    val ouncePrice: Double = 0.0,
    val ounceAsk: Double = 0.0,
    val ounceBid: Double = 0.0,
    val prevClosePrice: Double = 0.0,
    val change: Double = 0.0,
    val changePercent: Double = 0.0,
    val pricePerUnit: Double = 0.0,
    val meltPricePerGram: Double = 0.0,
    val exchange: String = "LBMA",
    val gram24: PricePair = PricePair(),
    val gram22: PricePair = PricePair(),
    val gram21: PricePair = PricePair(),
    val gram18: PricePair = PricePair(),
    val gram14: PricePair = PricePair(),
    val spread: KaratSpread = KaratSpread(),
    val isStale: Boolean = false,
    val error: String? = null,
    val usdToEgpRate: Double = 52.30,
    val usdBuyRate: Double = 52.30,
    val usdSellRate: Double = 52.20,
    val saghaUsdRate: Double = 51.70,
    val goldPoundPrice: PricePair = PricePair(buy = gram21.buy * 8.0, sell = gram21.sell * 8.0),
    val isManualCalibration: Boolean = false,
    val bullionMarginPerGram: Double = 0.0,
    val manualP21: Double = 6340.0,
    val manualUsdMid: Double = 52.25
) {
    val ouncePriceUsd: Double get() = ouncePrice
    val lastUpdatedLocal: String get() = lastUpdated
    val price21: Double get() = gram21.sell

    /**
     * Ounce in EGP: Gram 24 * 31.1035
     */
    val ouncePriceEgp: PricePair get() = PricePair(
        buy = gram24.buy * 31.1035,
        sell = gram24.sell * 31.1035
    )

    /**
     * Standard Bullions (عيار 24) in weights: 1, 2.5, 5, 10, 20, 50, 100, 250 grams
     * Price = gram24 * weight + (bullionMargin * weight)
     */
    fun getStandardBullions(): List<BullionItem> {
        val weights = listOf(
            1.0 to "سبيكة 1 جرام",
            2.5 to "سبيكة 2.5 جرام",
            5.0 to "سبيكة 5 جرام",
            10.0 to "سبيكة 10 جرام",
            20.0 to "سبيكة 20 جرام",
            50.0 to "سبيكة 50 جرام",
            100.0 to "سبيكة 100 جرام",
            250.0 to "سبيكة 250 جرام"
        )
        return weights.map { (w, lbl) ->
            val buy = (gram24.buy * w) + (bullionMarginPerGram * w)
            val sell = (gram24.sell * w)
            BullionItem(
                weightGrams = w,
                label = lbl,
                buyPrice = buy,
                sellPrice = sell
            )
        }
    }

    fun getPairForKarat(karat: Int): PricePair = when (karat) {
        24 -> gram24
        22 -> gram22
        21 -> gram21
        18 -> gram18
        14 -> gram14
        else -> gram21
    }

    fun toGoldPriceList(): List<GoldPrice> {
        return listOf(
            GoldPrice(karat = 24, buyPrice = gram24.buy, sellPrice = gram24.sell, change24h = change),
            GoldPrice(karat = 22, buyPrice = gram22.buy, sellPrice = gram22.sell, change24h = change * 22.0 / 24.0),
            GoldPrice(karat = 21, buyPrice = gram21.buy, sellPrice = gram21.sell, change24h = change * 21.0 / 24.0),
            GoldPrice(karat = 18, buyPrice = gram18.buy, sellPrice = gram18.sell, change24h = change * 18.0 / 24.0),
            GoldPrice(karat = 14, buyPrice = gram14.buy, sellPrice = gram14.sell, change24h = change * 14.0 / 24.0)
        )
    }
}

data class TraderPriceComparison(
    val traderPrice: Double = 0.0,
    val marketPrice: Double = 0.0,
    val karat: Int = 21,
    val isBuyOperation: Boolean = true, // true = العميل يشتري من التاجر، false = العميل يبيع للتاجر
    val difference: Double = 0.0,
    val differencePercent: Double = 0.0,
    val assessment: String = "",
    val isEvaluated: Boolean = false
)

data class MarketAdminSettings(
    val customerBuyPremiumPercent: Double = 0.0,
    val dealerBuyDiscountPercent: Double = 0.0,
    val customerBuyAdjustmentPercent: Double = 0.0,
    val dealerBuyAdjustmentPercent: Double = 0.0,
    val fixedAdjustmentEGP: Double = 0.0,
    val roundingStep: Double = 1.0,
    val staleAfterSeconds: Long = 3600L,
    val refreshIntervalSeconds: Long = 30L,
    val requestTimeoutSeconds: Long = 5L,
    val maxRetries: Int = 2,
    val cacheEnabled: Boolean = true,
    val maxAcceptableChangePercent: Double = 5.0,
    val providerType: String = "EgyptianLiveEngine",
    val manualGram24Price: Double = 7160.0,
    val manualBenchmark21Buy: Double = 0.0,
    val manualBenchmark21Sell: Double = 0.0,
    val isManualCalibrationActive: Boolean = false,
    val activeProvider: String = "egyptian_live_engine",
    val roundingMode: String = "NEAREST_1",
    val googleSheetUrl: String = "",
    val googleSheetHourlySyncEnabled: Boolean = false,
    val googleSheetUpdateIntervalMinutes: Int = 60,
    val lastGoogleSheetBuy21: Double = 6246.0,
    val lastGoogleSheetSell21: Double = 6208.5,
    val lastGoogleSheetSyncTime: String = "2026-09-15 15:00",
    val marketSellFactor: Double = 0.9972,
    val marketBuyFactor: Double = 1.0019,
    val bullionMarginPerGram: Double = 0.0,
    val soundAlertEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val cachedUsdRate: Double = 52.30,
    val cachedUsdBuyRate: Double = 52.30,
    val cachedUsdSellRate: Double = 52.20,
    val saghaUsdRate: Double = 51.70,
    val cachedUsdRateTimestamp: Long = 0L,
    val cachedUsdRateDate: String = "",
    val manualP21Mid: Double = 6340.0,
    val manualUsdMid: Double = 52.25,
    val calibrationK: Double = 0.9996,
    val lastCalibrationDate: String = ""
)

