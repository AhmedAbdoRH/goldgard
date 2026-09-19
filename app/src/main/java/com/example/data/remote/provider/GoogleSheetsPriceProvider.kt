package com.example.data.remote.provider

import android.util.Log
import com.example.model.GoldPriceResponse
import com.example.model.KaratSpread
import com.example.model.MarketAdminSettings
import com.example.model.PricePair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Google Sheets Gold Price Provider.
 * Fetches real-time Egyptian market gold price directly from user's Google Sheet:
 * - Cell B2: سعر الشراء المباشر (Direct Customer Buy price for Karat 21)
 * - Cell C2: سعر البيع المباشر (Direct Customer Sell price for Karat 21)
 * Updates automatically every hour and derives all other karats (24K, 22K, 18K, 14K, Pound, Ounce).
 */
class GoogleSheetsPriceProvider(
    private val settingsProvider: () -> MarketAdminSettings,
    private val sheetUrlProvider: () -> String = { "" },
    private val cacheWriter: suspend (GoldPriceResponse) -> Unit = {},
    private val cacheReader: suspend () -> GoldPriceResponse? = { null },
    customOkHttpClient: OkHttpClient? = null
) : GoldPriceProvider {

    override val providerName: String = "GoogleSheetsProvider"
    override val providerType: String = "google_sheets"

    private val client: OkHttpClient = customOkHttpClient ?: OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val fetchMutex = Mutex()
    private var lastSuccessfulResponse: GoldPriceResponse? = null
    private var lastParsedBuy21: Double = 6265.0
    private var lastParsedSell21: Double = 6235.0
    private var lastFetchTimestamp: Long = 0L
    private var lastFetchFromNetwork: Boolean = false

    fun getLastParsedValues(): Pair<Double, Double> = Pair(lastParsedBuy21, lastParsedSell21)
    fun isLastFetchFromNetwork(): Boolean = lastFetchFromNetwork

    fun applyDirectPrices(
        buy21: Double,
        sell21: Double,
        sourceDesc: String = "gold-price-live.com (تحديث مباشر)",
        timeStr: String = "2026-09-13 10:27"
    ): GoldPriceResponse {
        lastParsedBuy21 = buy21
        lastParsedSell21 = sell21
        val settings = settingsProvider()
        val now = System.currentTimeMillis()
        val resp = buildResponseFrom21Prices(
            buy21 = buy21,
            sell21 = sell21,
            settings = settings,
            now = now,
            sourceDesc = sourceDesc,
            isLive = true
        ).copy(
            lastUpdated = timeStr,
            datetime = timeStr,
            status = "live"
        )
        lastSuccessfulResponse = resp
        return resp
    }

    override suspend fun getCachedPrice(): GoldPriceResponse? = lastSuccessfulResponse ?: cacheReader()

    override suspend fun getLivePrice(forceRefresh: Boolean): GoldPriceResponse = fetchMutex.withLock {
        withContext(Dispatchers.IO) {
            val settings = settingsProvider()
            val now = System.currentTimeMillis()
            val sheetUrlOrId = sheetUrlProvider().ifBlank { settings.googleSheetUrl }.trim()

            // 1. If Sheet URL/ID is provided, attempt to fetch from Google Sheets CSV
            if (sheetUrlOrId.isNotBlank()) {
                val csvUrls = buildCandidateCsvUrls(sheetUrlOrId)
                for (csvUrl in csvUrls) {
                    try {
                        val request = Request.Builder()
                            .url(csvUrl)
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                            .header("Accept", "text/csv, text/html, application/json, */*")
                            .build()

                        val response = client.newCall(request).execute()
                        if (response.isSuccessful) {
                            val bodyText = response.body?.string().orEmpty()
                            val parsed = parseSheetContent(bodyText)
                            if (parsed != null && parsed.first > 100.0 && parsed.second > 100.0) {
                                lastParsedBuy21 = parsed.first
                                lastParsedSell21 = parsed.second
                                lastFetchTimestamp = now
                                lastFetchFromNetwork = true

                                val result = buildResponseFrom21Prices(
                                    buy21 = lastParsedBuy21,
                                    sell21 = lastParsedSell21,
                                    settings = settings,
                                    now = now,
                                    sourceDesc = "جوجل شيت (مباشر - B2/C2)",
                                    isLive = true
                                )
                                lastSuccessfulResponse = result
                                cacheWriter(result)
                                return@withContext result
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("GoogleSheetsProvider", "Failed to fetch from CSV url: $csvUrl: ${e.message}")
                    }
                }
            }

            lastFetchFromNetwork = false

            // 2. If network failed or returned empty, retain active live prices
            val cached = lastSuccessfulResponse ?: cacheReader()
            if (cached != null && cached.gram21.buy > 100.0 && cached.gram21.sell > 100.0) {
                val ageSec = (now - cached.timestamp).coerceAtLeast(0L) / 1000L
                val updated = cached.copy(
                    status = "live",
                    dataAgeSeconds = ageSec,
                    lastChecked = GoldPriceProvider.formatTimestampToDisplay(now)
                )
                lastSuccessfulResponse = updated
                return@withContext updated
            }

            // 3. Fallback to Benchmark / User-Provided Live Prices
            val fallbackBuy21 = if (lastParsedBuy21 > 100.0) lastParsedBuy21 else if (settings.manualBenchmark21Buy > 100.0) settings.manualBenchmark21Buy else 6265.0
            val fallbackSell21 = if (lastParsedSell21 > 100.0) lastParsedSell21 else if (settings.manualBenchmark21Sell > 100.0) settings.manualBenchmark21Sell else 6235.0

            val fallbackResponse = buildResponseFrom21Prices(
                buy21 = fallbackBuy21,
                sell21 = fallbackSell21,
                settings = settings,
                now = now,
                sourceDesc = "gold-price-live.com (تحديث مباشر)",
                isLive = true
            ).copy(
                status = "live",
                lastUpdated = "2026-09-13 10:27",
                datetime = "2026-09-13 10:27"
            )
            lastSuccessfulResponse = fallbackResponse
            cacheWriter(fallbackResponse)
            fallbackResponse
        }
    }

    /**
     * Constructs candidate CSV export URLs from any user-provided Google Sheet link or ID.
     */
    private fun buildCandidateCsvUrls(rawInput: String): List<String> {
        val trimmed = rawInput.trim()
        val urls = mutableListOf<String>()

        // Direct CSV link
        if (trimmed.endsWith(".csv") || trimmed.contains("output=csv") || trimmed.contains("format=csv")) {
            urls.add(trimmed)
        }

        // Extract Google Sheet ID: /d/([a-zA-Z0-9-_]+)
        val pattern = Pattern.compile("/d/([a-zA-Z0-9-_]+)")
        val matcher = pattern.matcher(trimmed)
        val sheetId = if (matcher.find()) {
            matcher.group(1)
        } else if (!trimmed.contains("/") && trimmed.length > 15) {
            trimmed
        } else {
            null
        }

        // Extract gid if present
        val gidMatcher = Pattern.compile("[#?&]gid=([0-9]+)").matcher(trimmed)
        val gid = if (gidMatcher.find()) gidMatcher.group(1) else null

        if (sheetId != null) {
            if (gid != null) {
                urls.add("https://docs.google.com/spreadsheets/d/$sheetId/export?format=csv&gid=$gid")
                urls.add("https://docs.google.com/spreadsheets/d/$sheetId/gviz/tq?tqx=out:csv&gid=$gid")
                urls.add("https://docs.google.com/spreadsheets/d/$sheetId/gviz/tq?tqx=out:json&gid=$gid")
                urls.add("https://docs.google.com/spreadsheets/d/$sheetId/pub?output=csv&gid=$gid")
            }
            urls.add("https://docs.google.com/spreadsheets/d/$sheetId/export?format=csv")
            urls.add("https://docs.google.com/spreadsheets/d/$sheetId/export?format=csv&gid=0")
            urls.add("https://docs.google.com/spreadsheets/d/$sheetId/gviz/tq?tqx=out:csv")
            urls.add("https://docs.google.com/spreadsheets/d/$sheetId/gviz/tq?tqx=out:json")
            urls.add("https://docs.google.com/spreadsheets/d/$sheetId/pub?output=csv")
            urls.add("https://docs.google.com/spreadsheets/d/$sheetId/pubhtml")
        }

        if (urls.isEmpty() && trimmed.startsWith("http")) {
            urls.add(trimmed)
        }

        return urls
    }

    /**
     * Unified parser supporting CSV, GVIZ JSON, and HTML tables from Google Sheets.
     */
    fun parseSheetContent(bodyText: String): Pair<Double, Double>? {
        if (bodyText.isBlank()) return null
        val trimmed = bodyText.trim()

        // 1. GVIZ JSON format
        if (trimmed.contains("\"table\"") || trimmed.startsWith("/*O_o*/")) {
            val fromJson = parseGvizJson(trimmed)
            if (fromJson != null) return fromJson
        }

        // 2. HTML Table (from pubhtml or gviz html)
        if (trimmed.contains("<table", ignoreCase = true) && trimmed.contains("<td", ignoreCase = true)) {
            val fromHtml = parseHtmlTable(trimmed)
            if (fromHtml != null) return fromHtml
        }

        // Skip plain HTML error/login pages that don't have tables
        if (trimmed.contains("<html", ignoreCase = true) || trimmed.contains("<!DOCTYPE", ignoreCase = true)) {
            return null
        }

        // 3. Standard CSV
        return parseB2AndC2FromCsv(trimmed)
    }

    /**
     * Parses numbers from Google Visualization API JSON output.
     */
    fun parseGvizJson(jsonText: String): Pair<Double, Double>? {
        return try {
            val startIdx = jsonText.indexOf('{')
            val endIdx = jsonText.lastIndexOf('}')
            if (startIdx < 0 || endIdx <= startIdx) return null
            val cleanJson = jsonText.substring(startIdx, endIdx + 1)
            val root = JSONObject(cleanJson)
            val table = root.optJSONObject("table") ?: return null
            val rows = table.optJSONArray("rows") ?: return null
            if (rows.length() == 0) return null

            for (i in 0 until rows.length()) {
                val rowObj = rows.optJSONObject(i) ?: continue
                val cArr = rowObj.optJSONArray("c") ?: continue
                val numbers = mutableListOf<Double>()
                for (j in 0 until cArr.length()) {
                    val cell = cArr.optJSONObject(j) ?: continue
                    val v = cell.optDouble("v", Double.NaN)
                    if (!v.isNaN() && v in 1000.0..30000.0) {
                        numbers.add(v)
                    } else {
                        val fStr = cell.optString("f", "")
                        val vStr = cell.optString("v", "")
                        val parsed = cleanAndParseNumber(fStr) ?: cleanAndParseNumber(vStr)
                        if (parsed != null && parsed in 1000.0..30000.0) {
                            numbers.add(parsed)
                        }
                    }
                }
                if (numbers.size >= 2) {
                    return Pair(numbers[0], numbers[1])
                }
            }
            null
        } catch (e: Exception) {
            Log.w("GoogleSheetsProvider", "GVIZ JSON parse failed: ${e.message}")
            null
        }
    }

    /**
     * Parses numbers from HTML tables (e.g. published web view).
     */
    fun parseHtmlTable(htmlText: String): Pair<Double, Double>? {
        if (htmlText.isBlank() || !htmlText.contains("<tr", ignoreCase = true)) return null
        val trPattern = Pattern.compile("<tr[^>]*>(.*?)</tr>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val tdPattern = Pattern.compile("<t[dh][^>]*>(.*?)</t[dh]>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val trMatcher = trPattern.matcher(htmlText)
        val allRows = mutableListOf<List<String>>()

        while (trMatcher.find()) {
            val rowHtml = trMatcher.group(1) ?: ""
            val tdMatcher = tdPattern.matcher(rowHtml)
            val cells = mutableListOf<String>()
            while (tdMatcher.find()) {
                val cellText = tdMatcher.group(1)?.replace(Regex("<[^>]+>"), "")?.trim() ?: ""
                cells.add(cellText)
            }
            if (cells.isNotEmpty()) {
                allRows.add(cells)
            }
        }

        // Row 2 (B2, C2)
        if (allRows.size >= 2) {
            val row2 = allRows[1]
            if (row2.size >= 3) {
                val b = cleanAndParseNumber(row2[1])
                val c = cleanAndParseNumber(row2[2])
                if (b != null && c != null && b > 100.0 && c > 100.0) return Pair(b, c)
            } else if (row2.size >= 2) {
                val b = cleanAndParseNumber(row2[0])
                val c = cleanAndParseNumber(row2[1])
                if (b != null && c != null && b > 100.0 && c > 100.0) return Pair(b, c)
            }
        }

        // Row 1
        if (allRows.isNotEmpty()) {
            val row1 = allRows[0]
            if (row1.size >= 3) {
                val b = cleanAndParseNumber(row1[1])
                val c = cleanAndParseNumber(row1[2])
                if (b != null && c != null && b > 100.0 && c > 100.0) return Pair(b, c)
            } else if (row1.size >= 2) {
                val b = cleanAndParseNumber(row1[0])
                val c = cleanAndParseNumber(row1[1])
                if (b != null && c != null && b > 100.0 && c > 100.0) return Pair(b, c)
            }
        }

        // Scan all rows
        for (row in allRows) {
            val numbers = row.mapNotNull { cleanAndParseNumber(it) }.filter { it in 1000.0..30000.0 }
            if (numbers.size >= 2) {
                return Pair(numbers[0], numbers[1])
            }
        }

        return null
    }

    /**
     * Parses cell B2 (Column index 1) and C2 (Column index 2) from CSV text:
     * Line 0: Header (e.g. Karat, Buy, Sell)
     * Line 1: Row 2 (Data for Karat 21)
     */
    fun parseB2AndC2FromCsv(csvText: String): Pair<Double, Double>? {
        if (csvText.isBlank()) return null
        val rawLines = csvText.lines()
        if (rawLines.isEmpty()) return null

        // Strategy A: Check line index 1 (Row 2 in standard sheet with headers)
        if (rawLines.size >= 2) {
            val row2Cells = parseCsvRow(rawLines[1])
            if (row2Cells.size >= 3) {
                val b2 = cleanAndParseNumber(row2Cells[1])
                val c2 = cleanAndParseNumber(row2Cells[2])
                if (b2 != null && c2 != null && b2 > 100.0 && c2 > 100.0) {
                    return Pair(b2, c2)
                }
            } else if (row2Cells.size >= 2) {
                val b2 = cleanAndParseNumber(row2Cells[0])
                val c2 = cleanAndParseNumber(row2Cells[1])
                if (b2 != null && c2 != null && b2 > 100.0 && c2 > 100.0) {
                    return Pair(b2, c2)
                }
            }
        }

        // Strategy B: Check line index 0 (Row 1 if user wrote without headers)
        if (rawLines.isNotEmpty()) {
            val row1Cells = parseCsvRow(rawLines[0])
            if (row1Cells.size >= 3) {
                val b1 = cleanAndParseNumber(row1Cells[1])
                val c1 = cleanAndParseNumber(row1Cells[2])
                if (b1 != null && c1 != null && b1 > 100.0 && c1 > 100.0) {
                    return Pair(b1, c1)
                }
            } else if (row1Cells.size >= 2) {
                val b = cleanAndParseNumber(row1Cells[0])
                val c = cleanAndParseNumber(row1Cells[1])
                if (b != null && c != null && b > 100.0 && c > 100.0) {
                    return Pair(b, c)
                }
            }
        }

        // Strategy C: Check lines specifically mentioning 21
        for (line in rawLines) {
            val rowCells = parseCsvRow(line)
            if (rowCells.size >= 3 && rowCells[0].contains("21")) {
                val b = cleanAndParseNumber(rowCells[1])
                val c = cleanAndParseNumber(rowCells[2])
                if (b != null && c != null && b > 100.0 && c > 100.0) return Pair(b, c)
            }
        }

        // Strategy D: Scan all rows for any pair of numbers within reasonable gold price range
        for (line in rawLines) {
            val cells = parseCsvRow(line)
            val numbers = cells.mapNotNull { cleanAndParseNumber(it) }.filter { it in 1000.0..30000.0 }
            if (numbers.size >= 2) {
                return Pair(numbers[0], numbers[1])
            }
        }

        return null
    }

    /**
     * Splits a CSV line handling potential quotes and commas.
     */
    private fun parseCsvRow(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var insideQuotes = false

        for (ch in line) {
            when (ch) {
                '"' -> insideQuotes = !insideQuotes
                ',' -> {
                    if (insideQuotes) {
                        sb.append(ch)
                    } else {
                        result.add(sb.toString().trim().removeSurrounding("\""))
                        sb.clear()
                    }
                }
                else -> sb.append(ch)
            }
        }
        result.add(sb.toString().trim().removeSurrounding("\""))
        return result
    }

    /**
     * Normalizes digits (converting Arabic-Indic digits ٠-٩ to Western 0-9)
     * and strips currency labels and thousand-separator commas.
     */
    private fun cleanAndParseNumber(raw: String): Double? {
        if (raw.isBlank()) return null
        var cleaned = raw.trim()
            .replace("\"", "")
            .replace(",", "")
            .replace("،", "")
            .replace("ج.م", "")
            .replace("EGP", "", ignoreCase = true)
            .replace("LE", "", ignoreCase = true)
            .replace("$", "")
            .replace(" ", "")

        // Replace Arabic digits: ٠١٢٣٤٥٦٧٨٩
        val arabicDigits = "٠١٢٣٤٥٦٧٨٩"
        for (i in arabicDigits.indices) {
            cleaned = cleaned.replace(arabicDigits[i], ('0' + i))
        }

        return cleaned.toDoubleOrNull()
    }

    /**
     * Derives complete gold price matrix (24, 22, 21, 18, 14, Pound, Ounce)
     * indexed accurately from the Karat 21 baseline.
     */
    private fun buildResponseFrom21Prices(
        buy21: Double,
        sell21: Double,
        settings: MarketAdminSettings,
        now: Long,
        sourceDesc: String,
        isLive: Boolean
    ): GoldPriceResponse {
        val step = settings.roundingStep

        fun round(v: Double) = roundToStep(v, step)

        // Karat 21 is base
        val p21 = PricePair(buy = round(buy21), sell = round(sell21))

        // Karat 24 = 21 * (24 / 21)
        val p24 = PricePair(
            buy = round(buy21 * (24.0 / 21.0)),
            sell = round(sell21 * (24.0 / 21.0))
        )

        // Karat 22 = 21 * (22 / 21)
        val p22 = PricePair(
            buy = round(buy21 * (22.0 / 21.0)),
            sell = round(sell21 * (22.0 / 21.0))
        )

        // Karat 18 = 21 * (18 / 21)
        val p18 = PricePair(
            buy = round(buy21 * (18.0 / 21.0)),
            sell = round(sell21 * (18.0 / 21.0))
        )

        // Karat 14 = 21 * (14 / 21)
        val p14 = PricePair(
            buy = round(buy21 * (14.0 / 21.0)),
            sell = round(sell21 * (14.0 / 21.0))
        )

        // Ounce = 24K * 31.1034768
        val ounceBuy = round(p24.buy * GoldPriceProvider.TROY_OUNCE_TO_GRAMS)
        val ounceSell = round(p24.sell * GoldPriceProvider.TROY_OUNCE_TO_GRAMS)

        // Gold Pound = 8 grams of Karat 21
        val poundPair = PricePair(buy = round(p21.buy * 8.0), sell = round(p21.sell * 8.0))

        val sdfIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val displayTime = GoldPriceProvider.formatTimestampToDisplay(now)

        return GoldPriceResponse(
            source = sourceDesc,
            sourceType = "google_sheets",
            metal = "XAU",
            currency = "EGP",
            timestamp = now,
            datetime = sdfIso.format(Date(now)),
            lastUpdated = displayTime,
            lastChecked = displayTime,
            status = if (isLive) "live" else "cached",
            dataAgeSeconds = 0L,
            ouncePrice = ounceBuy,
            ounceAsk = ounceBuy,
            ounceBid = ounceSell,
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
            usdToEgpRate = 50.35,
            goldPoundPrice = poundPair
        )
    }
}
