package com.example

import com.example.util.GoldBullionPricingEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testGoldBullionPricingEngine_karat21() {
    val pair = GoldBullionPricingEngine.calculatePair(mid = 6340.0)
    // Buy = round(6340 * 1.0024) = 6355
    // Sell = round(6340 * 0.9976) = 6325
    assertEquals(6355.0, pair.buy, 0.01)
    assertEquals(6325.0, pair.sell, 0.01)
    assertTrue("Buy must be greater than Sell", pair.buy > pair.sell)
    val gap = pair.buy - pair.sell
    assertEquals(30.0, gap, 0.01)
  }

  @Test
  fun testGoldBullionPricingEngine_karat24() {
    val mid24 = GoldBullionPricingEngine.deriveMid(p21 = 6340.0, karat = 24)
    assertEquals(7246.0, mid24, 0.01)
    val pair = GoldBullionPricingEngine.calculatePair(mid = mid24)
    // Buy = round(7246 * 1.0024) = 7263
    // Sell = round(7246 * 0.9976) = 7229
    assertEquals(7263.0, pair.buy, 0.01)
    assertEquals(7229.0, pair.sell, 0.01)
    assertTrue("Buy must be greater than Sell", pair.buy > pair.sell)
  }

  @Test
  fun testGoldBullionPricingEngine_karat18() {
    val mid18 = GoldBullionPricingEngine.deriveMid(p21 = 6340.0, karat = 18)
    assertEquals(5434.0, mid18, 0.01)
    val pair = GoldBullionPricingEngine.calculatePair(mid = mid18)
    // Buy = round(5434 * 1.0024) = 5447
    // Sell = round(5434 * 0.9976) = 5421
    assertEquals(5447.0, pair.buy, 0.01)
    assertEquals(5421.0, pair.sell, 0.01)
    assertTrue("Buy must be greater than Sell", pair.buy > pair.sell)
  }

  @Test
  fun testGoldBullionPricingEngine_usd() {
    val usdPair = GoldBullionPricingEngine.calculateUsdRates(usdMid = 52.25)
    // Buy = 52.30, Sell = 52.20
    assertEquals(52.30, usdPair.first, 0.001)
    assertEquals(52.20, usdPair.second, 0.001)
    assertTrue("USD Buy must be greater than Sell", usdPair.first > usdPair.second)
  }

  @Test
  fun testGoldBullionPricingEngine_zakatCalculation() {
    val fullResponse = GoldBullionPricingEngine.buildGoldPriceResponse(
      p21 = 6340.0,
      usdMid = 52.25
    )
    val sell24 = fullResponse.gram24.sell
    assertEquals(7229.0, sell24, 0.01)
    val nisabThresholdMoney = 85.0 * sell24
    assertEquals(614465.0, nisabThresholdMoney, 0.01)

    // 100 grams of karat 21
    val sell21 = fullResponse.gram21.sell
    assertEquals(6325.0, sell21, 0.01)
    val userValue = 100.0 * sell21
    assertEquals(632500.0, userValue, 0.01)
    assertTrue("User value must exceed nisab", userValue >= nisabThresholdMoney)

    val zakatMoney = userValue * 0.025
    assertEquals(15812.5, zakatMoney, 0.01)
  }
}
