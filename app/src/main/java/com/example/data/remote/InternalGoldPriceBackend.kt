package com.example.data.remote

import com.example.data.remote.provider.GoldPriceProvider
import com.example.model.GoldPriceResponse
import org.json.JSONObject

/**
 * Internal Backend Service Interface for GET /api/gold-prices.
 * Provides structured JSON and model payloads conforming strictly to the GoldAPI.io unified schema.
 */
class InternalGoldPriceBackend(
    private val provider: GoldPriceProvider
) {

    suspend fun handleGetGoldPrices(forceRefresh: Boolean = false): GoldPriceResponse {
        return provider.getLivePrice(forceRefresh)
    }

    suspend fun getGoldPricesAsJson(forceRefresh: Boolean = false): String {
        val data = handleGetGoldPrices(forceRefresh)
        val json = JSONObject()
        json.put("source", data.source)
        json.put("metal", data.metal)
        json.put("currency", data.currency)
        json.put("status", data.status)
        json.put("timestamp", data.timestamp)
        json.put("datetime", data.datetime)
        json.put("lastUpdated", data.lastUpdated)
        json.put("lastChecked", data.lastChecked)
        json.put("ouncePrice", data.ouncePrice)
        json.put("ounceAsk", data.ounceAsk)
        json.put("ounceBid", data.ounceBid)
        json.put("gram24", data.gram24.buy)
        json.put("gram22", data.gram22.buy)
        json.put("gram21", data.gram21.buy)
        json.put("gram18", data.gram18.buy)
        json.put("gram14", data.gram14.buy)
        json.put("change", data.change)
        json.put("changePercent", data.changePercent)
        json.put("dataAgeSeconds", data.dataAgeSeconds)
        json.put("isStale", data.isStale)
        json.put("error", data.error ?: JSONObject.NULL)

        // Detailed Buy/Sell pairs
        val pairs = JSONObject()
        fun pairJson(buy: Double, sell: Double): JSONObject {
            return JSONObject().apply {
                put("buy", buy)
                put("sell", sell)
                put("spread", (buy - sell).coerceAtLeast(0.0))
            }
        }
        pairs.put("24", pairJson(data.gram24.buy, data.gram24.sell))
        pairs.put("22", pairJson(data.gram22.buy, data.gram22.sell))
        pairs.put("21", pairJson(data.gram21.buy, data.gram21.sell))
        pairs.put("18", pairJson(data.gram18.buy, data.gram18.sell))
        pairs.put("14", pairJson(data.gram14.buy, data.gram14.sell))
        json.put("karats", pairs)

        return json.toString(2)
    }
}

