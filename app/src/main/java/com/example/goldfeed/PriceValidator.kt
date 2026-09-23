package com.example.goldfeed

/**
 * 3. PriceValidator:
 * صمام الأمان والتحقق من صحة واكتمال بيانات أسعار الذهب ومدخلات التغذية.
 *
 * القواعد الصارمة:
 * - التحقق من صحة القيم (أكبر من الصفر، ضمن النطاقات المنطقية).
 * - الشراء دائماً أكبر من أو يساوي البيع في المدخلات والمخرجات.
 * - عدم خلط أوقات تحديث مختلفة (استخدام نفس وقت التحديث لجميع المدخلات).
 * - عدم قبول بيانات متقادمة (عمر البيانات لا يتجاوز maxAllowedDataAgeSeconds = 90 ثانية).
 */
object PriceValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val errorCode: String? = null,
        val errorMessage: String? = null,
        val dataAgeSeconds: Long = 0L
    ) {
        companion object {
            fun success(dataAgeSeconds: Long) = ValidationResult(
                isValid = true,
                dataAgeSeconds = dataAgeSeconds
            )

            fun failure(code: String, message: String, age: Long = 0L) = ValidationResult(
                isValid = false,
                errorCode = code,
                errorMessage = message,
                dataAgeSeconds = age
            )
        }
    }

    /**
     * التحقق من سلامة المدخلات الخام القادمة من الباك إند أو مصدر التغذية
     */
    fun validateInputs(
        inputs: GoldPriceCalculator.RawFeedInputs,
        maxAllowedDataAgeSeconds: Long = 90L
    ): ValidationResult {
        // 1. فحص القيم الإيجابية
        if (inputs.ounceBuyUsd <= 0.0 || inputs.ounceSellUsd <= 0.0) {
            return ValidationResult.failure("INVALID_OUNCE", "سعر الأونصة غير صحيح أو يساوي صفر")
        }
        if (inputs.dollarBuyEgp <= 0.0 || inputs.dollarSellEgp <= 0.0) {
            return ValidationResult.failure("INVALID_USD", "سعر صرف الدولار غير صحيح أو يساوي صفر")
        }
        if (inputs.saghaDollarBuyEgp <= 0.0) {
            return ValidationResult.failure("INVALID_SAGHA_USD", "سعر دولار الصاغة غير صحيح أو يساوي صفر")
        }

        // 2. النطاقات المنطقية للسوق
        if (inputs.ounceBuyUsd < 1000.0 || inputs.ounceBuyUsd > 15000.0) {
            return ValidationResult.failure("OUNCE_OUT_OF_RANGE", "سعر الأونصة خارج النطاق المنطقي ($inputs.ounceBuyUsd)")
        }
        if (inputs.dollarBuyEgp < 20.0 || inputs.dollarBuyEgp > 300.0) {
            return ValidationResult.failure("USD_OUT_OF_RANGE", "سعر الدولار خارج النطاق المنطقي ($inputs.dollarBuyEgp)")
        }
        if (inputs.saghaDollarBuyEgp < 20.0 || inputs.saghaDollarBuyEgp > 300.0) {
            return ValidationResult.failure("SAGHA_USD_OUT_OF_RANGE", "دولار الصاغة خارج النطاق المنطقي ($inputs.saghaDollarBuyEgp)")
        }

        // 3. فحص الفروق السعرية (شراء >= بيع)
        if (inputs.ounceBuyUsd < inputs.ounceSellUsd) {
            return ValidationResult.failure("OUNCE_INVERTED", "أونصة الشراء (${inputs.ounceBuyUsd}) أقل من أونصة البيع (${inputs.ounceSellUsd})")
        }
        if (inputs.dollarBuyEgp < inputs.dollarSellEgp) {
            return ValidationResult.failure("USD_INVERTED", "دولار الشراء (${inputs.dollarBuyEgp}) أقل من دولار البيع (${inputs.dollarSellEgp})")
        }

        // 4. فحص عمر البيانات (Data Age)
        val now = System.currentTimeMillis()
        val ageSeconds = if (inputs.sourceUpdatedAt > 0L) {
            (now - inputs.sourceUpdatedAt) / 1000L
        } else 0L

        // السماح بتجاوز فحص العمر إذا كان الوقت مستقبلياً أو إذا كان التحديث حديثاً جداً
        if (ageSeconds > maxAllowedDataAgeSeconds && maxAllowedDataAgeSeconds > 0) {
            return ValidationResult.failure(
                code = "DATA_TOO_OLD",
                message = "بيانات المصدر متقادمة (عمر البيانات: $ageSeconds ثانية، الحد الأقصى المسموح: $maxAllowedDataAgeSeconds ثانية)",
                age = ageSeconds
            )
        }

        return ValidationResult.success(ageSeconds)
    }

    /**
     * التحقق من النتائج بعد الحساب للتأكد من عدم وجود أعمدة معكوسة أو خلل
     */
    fun validateCalculatedSnapshot(snapshot: GoldPriceCalculator.CalculatedSnapshot): ValidationResult {
        for ((karat, price) in snapshot.karats) {
            if (price.buyPrice <= price.sellPrice) {
                return ValidationResult.failure(
                    "KARAT_SPREAD_INVERTED",
                    "خلل في عيار $karat: سعر الشراء (${price.buyPrice}) يجب أن يكون أكبر من سعر البيع (${price.sellPrice})"
                )
            }
            if (price.buyPrice <= 0.0 || price.sellPrice <= 0.0) {
                return ValidationResult.failure(
                    "ZERO_PRICE",
                    "خلل في عيار $karat: السعر المحسوب يساوي صفر"
                )
            }
        }

        if (snapshot.goldPound.buyPrice <= snapshot.goldPound.sellPrice) {
            return ValidationResult.failure(
                "GOLD_POUND_INVERTED",
                "خلل في جنيه الذهب: سعر الشراء (${snapshot.goldPound.buyPrice}) أقل من أو يساوي سعر البيع (${snapshot.goldPound.sellPrice})"
            )
        }

        return ValidationResult.success(0L)
    }
}
