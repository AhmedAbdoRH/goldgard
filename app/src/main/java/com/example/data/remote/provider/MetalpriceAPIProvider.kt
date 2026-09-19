package com.example.data.remote.provider

import com.example.model.GoldPriceResponse
import com.example.model.MarketAdminSettings

/**
 * Secondary licensed provider adapter for MetalpriceAPI.
 * Ready for future multi-provider expansion without web scraping.
 */
class MetalpriceAPIProvider(
    private val settingsProvider: () -> MarketAdminSettings,
    private val goldAPIProvider: GoldAPIProvider
) : GoldPriceProvider {

    override val providerName: String = "MetalpriceAPIProvider"
    override val providerType: String = "licensed_api"

    override suspend fun getLivePrice(forceRefresh: Boolean): GoldPriceResponse {
        // Delegates to licensed pipeline with updated source tag
        val resp = goldAPIProvider.getLivePrice(forceRefresh)
        return resp.copy(
            source = "MetalpriceAPI (الترخيص الثانوي)"
        )
    }

    override suspend fun getCachedPrice(): GoldPriceResponse? {
        return goldAPIProvider.getCachedPrice()
    }
}
