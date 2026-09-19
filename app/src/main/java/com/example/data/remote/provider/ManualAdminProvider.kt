package com.example.data.remote.provider

import com.example.model.GoldPriceResponse
import com.example.model.KaratSpread
import com.example.model.MarketAdminSettings
import com.example.model.PricePair
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manual Admin Price Provider.
 * Allows administrator to manually set benchmark market prices in case of emergency
 * market suspensions or offline events.
 */
class ManualAdminProvider(
    private val settingsProvider: () -> MarketAdminSettings,
    private var manualOuncePrice: Double = 224833.0
) : GoldPriceProvider {

    override val providerName: String = "ManualAdminProvider"
    override val providerType: String = "manual_admin"

    fun setManualOuncePrice(price: Double) {
        if (price > 0) {
            manualOuncePrice = price
        }
    }

    override suspend fun getLivePrice(forceRefresh: Boolean): GoldPriceResponse {
        val settings = settingsProvider()
        val now = System.currentTimeMillis()

        val gram24Spot = if (settings.manualGram24Price > 0.0) {
            settings.manualGram24Price
        } else {
            manualOuncePrice / GoldPriceProvider.TROY_OUNCE_TO_GRAMS
        }
        val gram22Spot = calculateKaratPrice(gram24Spot, 22)
        val gram21Spot = calculateKaratPrice(gram24Spot, 21)
        val gram18Spot = calculateKaratPrice(gram24Spot, 18)
        val gram14Spot = calculateKaratPrice(gram24Spot, 14)

        fun pair(spot: Double): PricePair {
            val buy = roundToStep(calculateCustomerBuyPrice(spot, settings), settings.roundingStep)
            val sell = roundToStep(calculateDealerBuyPrice(buy, settings), settings.roundingStep)
            return PricePair(buy = buy, sell = sell)
        }

        val p24 = pair(gram24Spot)
        val p22 = pair(gram22Spot)
        val p21 = pair(gram21Spot)
        val p18 = pair(gram18Spot)
        val p14 = pair(gram14Spot)

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

        return GoldPriceResponse(
            source = "لوحة تحكم المشرف (يدوي)",
            sourceType = "manual_admin",
            currency = "EGP",
            timestamp = now,
            datetime = sdf.format(Date(now)),
            lastUpdated = GoldPriceProvider.formatTimestampToDisplay(now),
            status = "live",
            dataAgeSeconds = 0L,
            ouncePrice = roundToStep(manualOuncePrice, settings.roundingStep),
            gram24 = p24,
            gram22 = p22,
            gram21 = p21,
            gram18 = p18,
            gram14 = p14,
            spread = KaratSpread(
                k24 = p24.spread,
                k22 = p22.spread,
                k21 = p21.spread,
                k18 = p18.spread,
                k14 = p14.spread
            ),
            isStale = false,
            error = null
        )
    }

    override suspend fun getCachedPrice(): GoldPriceResponse = getLivePrice()
}
