package com.example.data.remote.provider

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Aggregator that coordinates Primary and Backup providers.
 * Enforces caching, health checks, quota preservation, and automatic failover.
 */
class GoldPriceAggregator(
    private val primaryProvider: IGoldPriceProvider,
    private val backupProvider: IGoldPriceProvider,
    private val cacheDurationMs: Long = 30_000L // 30-second cache window
) {
    private val mutex = Mutex()
    private var cachedPrices: ProviderGoldPrices? = null
    private var lastFetchTimeMs: Long = 0L

    suspend fun getPrices(forceRefresh: Boolean = false): ProviderGoldPrices = mutex.withLock {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()

            // 1. Return cached prices if within the validity window and not forced
            if (!forceRefresh && cachedPrices != null && (now - lastFetchTimeMs) < cacheDurationMs) {
                return@withContext cachedPrices!!
            }

            // 2. Attempt Primary Provider
            try {
                val primaryResult = primaryProvider.getEgyptianGoldPrices(forceRefresh)
                if (primaryResult.k21.buy > 0.0 && primaryResult.k24.buy > 0.0) {
                    cachedPrices = primaryResult
                    lastFetchTimeMs = now
                    return@withContext primaryResult
                }
            } catch (e: Exception) {
                Log.w("GoldPriceAggregator", "Primary provider failed: ${e.message}. Falling back to backup provider.")
            }

            // 3. Fallback to Backup Provider
            try {
                val backupResult = backupProvider.getEgyptianGoldPrices(forceRefresh)
                if (backupResult.k21.buy > 0.0) {
                    cachedPrices = backupResult
                    lastFetchTimeMs = now
                    return@withContext backupResult
                }
            } catch (e: Exception) {
                Log.e("GoldPriceAggregator", "Backup provider failed: ${e.message}")
            }

            // 4. Return stale cache if available or last known
            cachedPrices?.let { return@withContext it.copy(isLive = false) }

            // Final fallback
            backupProvider.getEgyptianGoldPrices(forceRefresh = true)
        }
    }

    fun getLastCachedPrices(): ProviderGoldPrices? = cachedPrices
}
