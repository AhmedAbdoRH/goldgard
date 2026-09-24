package com.example

import com.example.data.remote.provider.ExactLivePricingEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * اختبار القبول (Test of Acceptance - القسم 10)
 * مدخل raw24 = 7210.158 (المستخرج عند XAU = 4371.30 مع دولار الصاغة 51.3):
 * - عيار 24: شراء = 7210 | بيع = 7169
 * - عيار 22: شراء = 6610 | بيع = 6572
 * - عيار 21: شراء = 6310 | بيع = 6273
 * - عيار 18: شراء = 5410 | بيع = 5377
 * - عيار 14: شراء = 4210 | بيع = 4182
 * - جنيه الذهب: شراء = 50480 | بيع = 50184
 * - الأونصة بالجنيه: شراء = 224256 | بيع = 222981
 * - الأونصة بالدولار: 4371
 */
class ExactLivePricingEngineTest {

    @Test
    fun testAcceptanceCriteriaSection10WithRaw24() {
        // اختبار القبول المباشر بالقيمة raw24 = 7210.158 المذكورة نصاً في القسم 10
        val raw24 = 7210.158
        val xau = 4371.30
        val result = ExactLivePricingEngine.computePricingFromRaw24(
            raw24 = raw24,
            xau = xau,
            updatedAtIso = "2026-09-22T00:00:00Z",
            officialUsdRate = 51.9
        )

        // عيار 24: شراء = 7210 | بيع = 7169
        assertEquals(7210L, result.karatPrices[24]?.first)
        assertEquals(7169L, result.karatPrices[24]?.second)

        // عيار 22: شراء = 6610 | بيع = 6572
        assertEquals(6610L, result.karatPrices[22]?.first)
        assertEquals(6572L, result.karatPrices[22]?.second)

        // عيار 21: شراء = 6310 | بيع = 6273
        assertEquals(6310L, result.karatPrices[21]?.first)
        assertEquals(6273L, result.karatPrices[21]?.second)

        // عيار 18: شراء = 5410 | بيع = 5377
        assertEquals(5410L, result.karatPrices[18]?.first)
        assertEquals(5377L, result.karatPrices[18]?.second)

        // عيار 14: شراء = 4210 | بيع = 4182
        assertEquals(4210L, result.karatPrices[14]?.first)
        assertEquals(4182L, result.karatPrices[14]?.second)

        // جنيه الذهب: شراء = 50480 | بيع = 50184
        assertEquals(50480L, result.goldPoundBuy)
        assertEquals(50184L, result.goldPoundSell)

        // الأونصة بالجنيه: شراء = 224256 | بيع = 222981
        assertEquals(224256L, result.ounceEgpBuy)
        assertEquals(222981L, result.ounceEgpSell)

        // الأونصة بالدولار: 4371
        assertEquals(4371L, result.ounceUsdRound)

        // اشتراط جوهري: شراء > بيع في كل صف
        for (k in listOf(24, 22, 21, 18, 14)) {
            val buy = result.karatPrices[k]?.first ?: 0L
            val sell = result.karatPrices[k]?.second ?: 0L
            assertTrue("شراء يجب أن يكون أكبر من بيع للعيار $k", buy > sell)
        }
    }

    @Test
    fun testLiveXauCalculation() {
        val xau = 4371.30
        val result = ExactLivePricingEngine.computePricing(
            xau = xau,
            updatedAtIso = "2026-09-22T00:00:00Z",
            officialUsdRate = 51.9
        )

        // شراء دائماً أكبر من بيع
        for (k in listOf(24, 22, 21, 18, 14)) {
            val buy = result.karatPrices[k]?.first ?: 0L
            val sell = result.karatPrices[k]?.second ?: 0L
            assertTrue("شراء يجب أن يكون أكبر من بيع للعيار $k", buy > sell)
        }

        // الأونصة بالدولار مقربة
        assertEquals(4371L, result.ounceUsdRound)
    }
}
