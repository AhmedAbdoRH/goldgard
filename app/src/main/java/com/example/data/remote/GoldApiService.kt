package com.example.data.remote

import com.example.data.remote.provider.GoldPriceProvider
import com.example.model.Country
import com.example.model.GoldPrice
import com.example.model.GoldPriceResponse

data class LivePriceResponse(
    val prices: List<GoldPrice>,
    val sourceTitle: String,
    val isLive: Boolean,
    val country: Country = Country.EGYPT,
    val timestamp: Long = System.currentTimeMillis(),
    val rawResponse: GoldPriceResponse? = null
)

/**
 * Service bridge for backward compatibility and provider routing.
 * Strictly uses licensed API provider architecture (GoldAPI.io) without web scraping.
 */
class GoldApiService(
    private val backend: InternalGoldPriceBackend
) {
    suspend fun getLiveGoldPrices(country: Country = Country.EGYPT, forceRefresh: Boolean = false): LivePriceResponse {
        val result = backend.handleGetGoldPrices(forceRefresh)
        val prices = result.toGoldPriceList()
        return LivePriceResponse(
            prices = prices,
            sourceTitle = result.source,
            isLive = result.status == "live",
            country = country,
            timestamp = result.timestamp,
            rawResponse = result
        )
    }
}
