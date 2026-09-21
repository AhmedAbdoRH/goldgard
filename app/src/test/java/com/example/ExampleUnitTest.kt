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
  fun testGoldBullionPricingEngine_formulas() {
    val xau = 3000.0
    val sd = 51.7
    val k = 1.0

    // raw21 = 3000 * 51.7 / 31.1035 * (21 / 24) = 4986.577... * 0.875 = 4363.255
    val raw21 = GoldBullionPricingEngine.calculateRaw(xau, sd, 21)
    assertTrue("Raw 21 must be positive", raw21 > 4000.0)

    val pair21 = GoldBullionPricingEngine.calculateKaratPair(raw21, k)
    assertTrue("Buy must be greater than Sell", pair21.buy > pair21.sell)
    assertEquals(pair21.buy, Math.round(raw21 * k * 1.0019).toDouble(), 0.01)
    assertEquals(pair21.sell, Math.round(raw21 * k * 0.9972).toDouble(), 0.01)
  }

  @Test
  fun testGoldBullionPricingEngine_calibration() {
    val xau = 3000.0
    val sd = 51.7
    val entered21 = 6385.0

    val calculatedK = GoldBullionPricingEngine.calibrateK(entered21, xau, sd)
    assertTrue("K must be positive", calculatedK > 0.5)

    // With this K, calculated buy for 21 must be exactly entered21
    val raw21 = GoldBullionPricingEngine.calculateRaw(xau, sd, 21)
    val pair21 = GoldBullionPricingEngine.calculateKaratPair(raw21, calculatedK)
    assertEquals(entered21, pair21.buy, 1.0)
  }

  @Test
  fun testGoldBullionPricingEngine_goldPound() {
    val prices = GoldBullionPricingEngine.calculateAll(xau = 3000.0, sd = 51.7, k = 1.0)
    assertEquals((prices.gram21.buy.toLong() * 8).toDouble(), prices.goldPound.buy, 0.01)
    assertEquals((prices.gram21.sell.toLong() * 8).toDouble(), prices.goldPound.sell, 0.01)
    assertTrue("Pound Buy must be greater than Pound Sell", prices.goldPound.buy > prices.goldPound.sell)
  }

  @Test
  fun testGoldBullionPricingEngine_zakatCalculation() {
    val weight = 100.0
    val karat = 21
    val sell24 = 7000.0
    val sell21 = 6125.0

    val zakat = GoldBullionPricingEngine.calculateZakat(
      weight = weight,
      karat = karat,
      sell24Price = sell24,
      chosenKaratSellPrice = sell21
    )

    val expectedTotal = 100.0 * 6125.0 // 612,500
    val expectedNisab = 85.0 * 7000.0 // 595,000
    assertEquals(expectedTotal, zakat.totalValue, 0.01)
    assertEquals(expectedNisab, zakat.nisabValue, 0.01)
    assertTrue("Zakat should be due as totalValue > nisab", zakat.isZakatDue)
    assertEquals(Math.round(expectedTotal * 0.025), zakat.zakatAmount)
  }
}
