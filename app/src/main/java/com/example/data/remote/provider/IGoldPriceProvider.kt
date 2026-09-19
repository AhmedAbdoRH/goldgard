package com.example.data.remote.provider

import com.example.model.PricePair

/**
 * Standardized data transfer object for provider prices.
 * Returns buy/sell prices for Egyptian gold karats (24K, 22K, 21K, 18K, 14K),
 * source identification, update timestamp, and live connectivity status.
 */
data class ProviderGoldPrices(
    val k24: PricePair,
    val k22: PricePair,
    val k21: PricePair,
    val k18: PricePair,
    val k14: PricePair,
    val source: String,
    val updatedAt: Long,
    val isLive: Boolean,
    val errorMessage: String? = null
) {
    fun getPairForKarat(karat: Int): PricePair = when (karat) {
        24 -> k24
        22 -> k22
        21 -> k21
        18 -> k18
        14 -> k14
        else -> k21
    }
}

/**
 * IGoldPriceProvider interface.
 * Exposes a method that returns current Egyptian gold prices across all standard karats.
 */
interface IGoldPriceProvider {
    val providerName: String
    val isAvailable: Boolean

    suspend fun getEgyptianGoldPrices(forceRefresh: Boolean = false): ProviderGoldPrices
}
