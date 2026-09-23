package com.example

import com.example.goldfeed.GoldPriceCalculator
import com.example.goldfeed.PriceValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * اختبارات التحقق من نظام تسعير Gold Bullion والمعايير المعتمدة لـ "حارس الذهب".
 */
class GoldPriceFeedTest {

    @Test
    fun testGoldBullionReferenceMatching() {
        val inputs = GoldPriceCalculator.RawFeedInputs(
            ounceBuyUsd = 4285.46,
            ounceSellUsd = 4284.96,
            dollarBuyEgp = 51.55,
            dollarSellEgp = 51.45,
            saghaDollarBuyEgp = 51.60,
            sourceUpdatedAt = System.currentTimeMillis()
        )

        val calibration = GoldPriceCalculator.CalibrationConfig(
            buyAdjustmentEgp = 0.0,
            sellAdjustmentEgp = 0.0,
            buyRounding = GoldPriceCalculator.RoundingMode.FLOOR,
            sellRounding = GoldPriceCalculator.RoundingMode.FLOOR,
            goldPoundRounding = GoldPriceCalculator.RoundingMode.FLOOR
        )

        val snapshot = GoldPriceCalculator.calculate(inputs, calibration)

        // 1. عيار 24: شراء مرجعي 7109، بيع مرجعي 7086
        assertEquals(7109.0, snapshot.gram24.buyPrice, 0.0)
        assertTrue(kotlin.math.abs(snapshot.gram24.sellPrice - 7086.0) <= 2.0)

        // 2. عيار 22: شراء مرجعي 6516، بيع مرجعي 6495
        assertTrue(kotlin.math.abs(snapshot.gram22.buyPrice - 6516.0) <= 2.0)
        assertTrue(kotlin.math.abs(snapshot.gram22.sellPrice - 6495.0) <= 2.0)

        // 3. عيار 21: شراء مرجعي 6220 (تطابق تام = 6220)، بيع مرجعي 6200
        assertEquals(6220.0, snapshot.gram21.buyPrice, 0.0)
        assertTrue(kotlin.math.abs(snapshot.gram21.sellPrice - 6200.0) <= 2.0)

        // 4. عيار 18: شراء مرجعي 5331، بيع مرجعي 5314
        assertTrue(kotlin.math.abs(snapshot.gram18.buyPrice - 5331.0) <= 2.0)
        assertTrue(kotlin.math.abs(snapshot.gram18.sellPrice - 5314.0) <= 2.0)

        // 5. عيار 14: شراء مرجعي 4147 (تطابق تام = 4147)، بيع مرجعي 4133
        assertEquals(4147.0, snapshot.gram14.buyPrice, 0.0)
        assertTrue(kotlin.math.abs(snapshot.gram14.sellPrice - 4133.0) <= 2.0)

        // 6. جنيه الذهب: شراء = 6220 * 8 = 49760 (تطابق تام = 49760)
        assertEquals(49760.0, snapshot.goldPound.buyPrice, 0.0)
        assertTrue(kotlin.math.abs(snapshot.goldPound.sellPrice - 49600.0) <= 10.0)

        // 7. تقرير المقارنة الشامل
        val report = GoldPriceCalculator.compareWithReference(snapshot)
        assertTrue("يجب أن تكون كل العيارات متطابقة ضمن هامش 1-2 جنيه", report.isAllWithinTolerance)
        println(report.summary)
    }

    @Test
    fun testPriceValidatorRules() {
        val validInputs = GoldPriceCalculator.RawFeedInputs(
            ounceBuyUsd = 4285.46,
            ounceSellUsd = 4284.96,
            dollarBuyEgp = 51.55,
            dollarSellEgp = 51.45,
            saghaDollarBuyEgp = 51.60,
            sourceUpdatedAt = System.currentTimeMillis()
        )
        val validRes = PriceValidator.validateInputs(validInputs, maxAllowedDataAgeSeconds = 90L)
        assertTrue(validRes.isValid)

        // بيانات قديمة تجاوزت 90 ثانية
        val oldInputs = validInputs.copy(sourceUpdatedAt = System.currentTimeMillis() - 100_000L)
        val oldRes = PriceValidator.validateInputs(oldInputs, maxAllowedDataAgeSeconds = 90L)
        assertFalse(oldRes.isValid)
        assertEquals("DATA_TOO_OLD", oldRes.errorCode)

        // أونصة شراء أقل من بيع (مقلوبة)
        val invertedOunce = validInputs.copy(ounceBuyUsd = 4000.0, ounceSellUsd = 4100.0)
        val invRes = PriceValidator.validateInputs(invertedOunce, maxAllowedDataAgeSeconds = 90L)
        assertFalse(invRes.isValid)
        assertEquals("OUNCE_INVERTED", invRes.errorCode)
    }
}
