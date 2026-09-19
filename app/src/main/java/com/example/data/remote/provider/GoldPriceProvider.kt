package com.example.data.remote.provider

import com.example.model.GoldPriceResponse
import com.example.model.MarketAdminSettings
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Independent Gold Price Provider Interface.
 * Defines the contract for all licensed gold price providers.
 */
interface GoldPriceProvider {
    val providerName: String
    val providerType: String

    /**
     * Fetches real-time live gold price with full market calculations.
     * @param forceRefresh when true, bypasses short-term cache window and queries remote API.
     */
    suspend fun getLivePrice(forceRefresh: Boolean = false): GoldPriceResponse

    /**
     * Retrieves the last successfully cached price response.
     */
    suspend fun getCachedPrice(): GoldPriceResponse?

    /**
     * Calculates the raw spot price for a given karat from 24K spot price.
     * Formula: gramKaratSpotPrice = gram24SpotPrice * karat / 24.0
     */
    fun calculateKaratPrice(gram24SpotPrice: Double, karat: Int): Double {
        if (karat !in 1..24 || gram24SpotPrice <= 0.0) return 0.0
        return gram24SpotPrice * (karat.toDouble() / 24.0)
    }

    /**
     * Calculates Customer Buy Price (سعر البيع للعميل: السعر الذي يدفعه العميل عند شراء الذهب من الصائغ).
     * Formula: spotPrice * (1 + customerBuyPremiumPercent / 100) + fixedAdjustmentEGP
     * Maintains full double-precision prior to final rounding.
     */
    fun calculateCustomerBuyPrice(spotPrice: Double, settings: MarketAdminSettings): Double {
        if (spotPrice <= 0.0) return 0.0
        val premiumMultiplier = 1.0 + (settings.customerBuyPremiumPercent / 100.0)
        return (spotPrice * premiumMultiplier) + settings.fixedAdjustmentEGP
    }

    /**
     * Calculates Dealer Buy Price (سعر الشراء من العميل: السعر الذي يدفعه الصائغ للعميل عند بيع الذهب له).
     * Formula: customerBuyPrice * (1 - dealerBuyDiscountPercent / 100)
     * Maintains full double-precision prior to final rounding.
     */
    fun calculateDealerBuyPrice(customerBuyPrice: Double, settings: MarketAdminSettings): Double {
        if (customerBuyPrice <= 0.0) return 0.0
        val discountMultiplier = 1.0 - (settings.dealerBuyDiscountPercent / 100.0)
        return customerBuyPrice * discountMultiplier
    }

    /**
     * Rounds price according to market admin roundingStep (e.g. nearest 1 EGP).
     */
    fun roundToStep(value: Double, step: Double): Double {
        if (step <= 0.0) return value
        val bdVal = BigDecimal.valueOf(value)
        val bdStep = BigDecimal.valueOf(step)
        val divided = bdVal.divide(bdStep, 0, RoundingMode.HALF_UP)
        return divided.multiply(bdStep).toDouble()
    }

    /**
     * Evaluates data status based on age and stale threshold.
     * Returns: "live", "cached", "stale", "error"
     */
    fun getPriceStatus(timestamp: Long, staleAfterSeconds: Long, hasError: Boolean): String {
        if (hasError) return "error"
        val ageSeconds = (System.currentTimeMillis() - timestamp).coerceAtLeast(0L) / 1000L
        return if (ageSeconds > staleAfterSeconds) "stale" else "live"
    }

    /**
     * Validates input response integrity.
     */
    fun validatePriceResponse(
        ouncePrice: Double,
        currency: String,
        metal: String,
        timestamp: Long
    ): Boolean {
        if (ouncePrice <= 0.0 || ouncePrice.isNaN() || ouncePrice.isInfinite()) return false
        if (!currency.equals("EGP", ignoreCase = true)) return false
        if (!metal.equals("XAU", ignoreCase = true)) return false
        if (timestamp <= 0L) return false
        return true
    }

    /**
     * Troy Ounce to Gram constant: 1 Troy Ounce = 31.1034768 grams.
     */
    companion object {
        const val TROY_OUNCE_TO_GRAMS = 31.1034768

        fun formatTimestampToDisplay(timestamp: Long): String {
            val date = Date(timestamp)
            val sdf = SimpleDateFormat("hh:mm:ss a - yyyy/MM/dd", Locale("ar", "EG"))
            return sdf.format(date)
        }
    }
}
