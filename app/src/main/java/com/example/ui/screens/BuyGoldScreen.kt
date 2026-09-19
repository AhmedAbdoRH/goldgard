package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ScreenType
import com.example.ui.components.GoldScreenHeader
import com.example.ui.theme.GoldTheme
import com.example.ui.viewmodel.GoldViewModel
import java.util.Locale

/**
 * ثامنًا: شاشة شراء الذهب
 * 1. خانات الإدخال الأساسية: العيار/النوع (24, 22, 21, 18, جنيه ذهب, سبيكة)، الوزن، المصنعية، الدمغة والضريبة.
 * 2. بطاقة النتيجة: السعر قبل المصنعية، إجمالي المصنعية، الضريبة/الدمغة، السعر النهائي بخط كبير وذهبي.
 * 3. الملاحظات التحذيرية: تنبيه إذا كانت المصنعية مبالغًا فيها، نصيحة الشراء.
 * 4. الأزرار: حساب، إعادة تعيين، حفظ في السجل.
 */
@Composable
fun BuyGoldScreen(
    viewModel: GoldViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val buyKarat by viewModel.buyKarat.collectAsStateWithLifecycle()
    val buyGramPrice by viewModel.buyGramPrice.collectAsStateWithLifecycle()
    val buyWeight by viewModel.buyWeight.collectAsStateWithLifecycle()
    val buyMakingPercent by viewModel.buyMakingPercent.collectAsStateWithLifecycle()
    val buyMakingAmountPerGram by viewModel.buyMakingAmountPerGram.collectAsStateWithLifecycle()
    val buyStampFee by viewModel.buyStampFee.collectAsStateWithLifecycle()
    val buyShopQuotedPrice by viewModel.buyShopQuotedPrice.collectAsStateWithLifecycle()
    val buyShopName by viewModel.buyShopName.collectAsStateWithLifecycle()
    val buyResult by viewModel.buyResult.collectAsStateWithLifecycle()
    val selectedCountry by viewModel.selectedCountry.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    var showGramPriceEdit by remember { mutableStateOf(false) }

    // خيارات العيارات والأنواع
    val goldTypes = listOf(
        Pair("18", 18),
        Pair("21 ⭐", 21),
        Pair("22", 22),
        Pair("24", 24),
        Pair("جنيه ذهب", -1), // 8g 21k
        Pair("سبيكة 24", -2)  // 24k bullion
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GoldTheme.colors.background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. ترويسة الشاشة
        item {
            GoldScreenHeader(
                title = "شراء الذهب",
                onBackClick = { viewModel.navigateTo(ScreenType.HOME) },
                onInfoClick = { viewModel.navigateTo(ScreenType.TIPS) },
                onThemeToggle = { viewModel.toggleDarkMode() },
                isDarkMode = isDarkMode,
                testTagPrefix = "buy_screen"
            )
        }

        // 2. اختيار نوع الذهب / العيار
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "نوع الذهب / العيار:",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldTheme.colors.textPrimary
                    )
                    Text(
                        text = "سعر الجرام: ${buyGramPrice.ifEmpty { "0" }} ${selectedCountry.currencySymbol}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldTheme.colors.goldPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // صف الأزرار للعيارات
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    goldTypes.take(4).forEach { (label, karat) ->
                        val isSelected = karat == buyKarat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) GoldTheme.colors.goldPrimary.copy(alpha = 0.18f)
                                    else GoldTheme.colors.surface
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) GoldTheme.colors.goldPrimary else GoldTheme.colors.borderColor,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.onBuyKaratSelected(karat) }
                                .testTag("buy_karat_$karat"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) GoldTheme.colors.goldPrimary else GoldTheme.colors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // صف العملات والسبائك
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    goldTypes.drop(4).forEach { (label, code) ->
                        val isPound = code == -1
                        val isSelected = if (isPound) buyKarat == 21 && buyWeight == "8" else buyKarat == 24
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) GoldTheme.colors.goldPrimary.copy(alpha = 0.18f)
                                    else GoldTheme.colors.surface
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) GoldTheme.colors.goldPrimary else GoldTheme.colors.borderColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (isPound) {
                                        viewModel.onBuyKaratSelected(21)
                                        viewModel.onBuyWeightChanged("8")
                                        viewModel.onBuyMakingPercentChanged("3")
                                    } else {
                                        viewModel.onBuyKaratSelected(24)
                                        viewModel.onBuyMakingPercentChanged("2.5")
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                color = if (isSelected) GoldTheme.colors.goldPrimary else GoldTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // 3. خانات الإدخال الأساسية: الوزن، سعر الجرام، المصنعية، الدمغة والضريبة
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = GoldTheme.colors.surface),
                border = BorderStroke(1.dp, GoldTheme.colors.borderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. سعر الجرام (شراء) في السوق
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "سعر الجرام الصافي (سعر السوق)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.textPrimary
                            )
                            Text(
                                text = if (showGramPriceEdit) "تم التعديل" else "تعديل السعر",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GoldTheme.colors.goldPrimary,
                                modifier = Modifier.clickable { showGramPriceEdit = !showGramPriceEdit }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldTheme.colors.surfaceElevated)
                                .border(
                                    1.dp,
                                    if (showGramPriceEdit) GoldTheme.colors.goldPrimary else GoldTheme.colors.borderColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                BasicTextField(
                                    value = buyGramPrice,
                                    onValueChange = { viewModel.onBuyGramPriceChanged(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("buy_gram_price_input"),
                                    textStyle = TextStyle(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldTheme.colors.textPrimary,
                                        textAlign = TextAlign.Start
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    cursorBrush = SolidColor(GoldTheme.colors.goldPrimary)
                                )
                                Text(
                                    text = "${selectedCountry.currencySymbol}/جم",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldTheme.colors.textSecondary
                                )
                            }
                        }
                    }

                    // 2. اسم المحل / الصائغ (مباشرة تحت السعر كما طلب المستخدم)
                    Column {
                        Text(
                            text = "اسم المحل / الصائغ",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldTheme.colors.surfaceElevated)
                                .border(1.dp, GoldTheme.colors.borderColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicTextField(
                                    value = buyShopName,
                                    onValueChange = { viewModel.onBuyShopNameChanged(it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("buy_shop_name_input"),
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GoldTheme.colors.textPrimary,
                                        textAlign = TextAlign.Start
                                    ),
                                    singleLine = true,
                                    cursorBrush = SolidColor(GoldTheme.colors.goldPrimary),
                                    decorationBox = { inner ->
                                        if (buyShopName.isEmpty()) {
                                            Text(
                                                "اسم محل الذهب أو الصائغ (اختياري)",
                                                color = GoldTheme.colors.textMuted,
                                                fontSize = 13.sp
                                            )
                                        }
                                        inner()
                                    }
                                )
                            }
                        }
                    }

                    // 3. الوزن بالجرام
                    Column {
                        Text(
                            text = "الوزن بالجرام",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldTheme.colors.surfaceElevated)
                                .border(1.dp, GoldTheme.colors.borderColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                BasicTextField(
                                    value = buyWeight,
                                    onValueChange = { viewModel.onBuyWeightChanged(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("buy_weight_input"),
                                    textStyle = TextStyle(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldTheme.colors.textPrimary,
                                        textAlign = TextAlign.Start
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    cursorBrush = SolidColor(GoldTheme.colors.goldPrimary),
                                    decorationBox = { inner ->
                                        if (buyWeight.isEmpty()) {
                                            Text("مثال: 10.5", color = GoldTheme.colors.textMuted, fontSize = 14.sp)
                                        }
                                        inner()
                                    }
                                )
                                Text(
                                    text = "جرام",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldTheme.colors.textSecondary
                                )
                            }
                        }
                    }

                    // 4. المصنعية: قيمة المصنعية جنب النسبة المئوية (% كام ف المية)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المصنعية (القيمة والنسبة المئوية)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.textPrimary
                            )
                            val makingVal = buyMakingAmountPerGram.toDoubleOrNull() ?: 0.0
                            if (makingVal > 0) {
                                Text(
                                    text = "= ${viewModel.formatPrice(makingVal)} ج/جم (${buyMakingPercent}%)",
                                    fontSize = 11.5.sp,
                                    color = GoldTheme.colors.goldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        // سطر يجمع قيمة المصنعية والنسبة المئوية جنباً إلى جنب
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // قيمة المصنعية للجرام
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "القيمة (ج.م/جم)",
                                    fontSize = 11.sp,
                                    color = GoldTheme.colors.textSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GoldTheme.colors.surfaceElevated)
                                        .border(1.dp, GoldTheme.colors.borderColor, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        BasicTextField(
                                            value = buyMakingAmountPerGram,
                                            onValueChange = { viewModel.onBuyMakingAmountPerGramChanged(it) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("buy_making_amount_input"),
                                            textStyle = TextStyle(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldTheme.colors.textPrimary,
                                                textAlign = TextAlign.Start
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            cursorBrush = SolidColor(GoldTheme.colors.goldPrimary),
                                            decorationBox = { inner ->
                                                if (buyMakingAmountPerGram.isEmpty()) {
                                                    Text("مثال: 120", color = GoldTheme.colors.textMuted, fontSize = 13.sp)
                                                }
                                                inner()
                                            }
                                        )
                                        Text(
                                            text = "ج.م",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldTheme.colors.textSecondary
                                        )
                                    }
                                }
                            }

                            // نسبة المصنعية (% كام ف المية)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "النسبة (% كام ف المية)",
                                    fontSize = 11.sp,
                                    color = GoldTheme.colors.textSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GoldTheme.colors.surfaceElevated)
                                        .border(1.dp, GoldTheme.colors.borderColor, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        BasicTextField(
                                            value = buyMakingPercent,
                                            onValueChange = { viewModel.onBuyMakingPercentChanged(it) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("buy_making_percent_input"),
                                            textStyle = TextStyle(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldTheme.colors.textPrimary,
                                                textAlign = TextAlign.Start
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            cursorBrush = SolidColor(GoldTheme.colors.goldPrimary),
                                            decorationBox = { inner ->
                                                if (buyMakingPercent.isEmpty()) {
                                                    Text("مثال: 5", color = GoldTheme.colors.textMuted, fontSize = 13.sp)
                                                }
                                                inner()
                                            }
                                        )
                                        Text(
                                            text = "%",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldTheme.colors.goldPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // أزرار سريعة لاختيار نسبة المصنعية
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("2.5", "4", "5", "7", "10").forEach { pct ->
                                val isSelected = buyMakingPercent == pct
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(30.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelected) GoldTheme.colors.goldPrimary.copy(alpha = 0.2f)
                                            else GoldTheme.colors.surfaceElevated
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) GoldTheme.colors.goldPrimary else GoldTheme.colors.borderColor,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { viewModel.onBuyMakingPercentChanged(pct) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$pct%",
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) GoldTheme.colors.goldPrimary else GoldTheme.colors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    // ضريبة القيمة المضافة والدمغة لكل جرام
                    Column {
                        Text(
                            text = "الدمغة وضريبة القيمة المضافة",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldTheme.colors.surfaceElevated)
                                .border(1.dp, GoldTheme.colors.borderColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                BasicTextField(
                                    value = buyStampFee,
                                    onValueChange = { viewModel.onBuyStampFeeChanged(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("buy_stamp_input"),
                                    textStyle = TextStyle(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldTheme.colors.textPrimary,
                                        textAlign = TextAlign.Start
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    cursorBrush = SolidColor(GoldTheme.colors.goldPrimary)
                                )
                                Text(
                                    text = "${selectedCountry.currencySymbol}/جم",
                                    fontSize = 12.sp,
                                    color = GoldTheme.colors.textSecondary
                                )
                            }
                        }
                    }

                    // السعر المعروض من المحل (اختياري لكشف التلاعب)
                    Column {
                        Text(
                            text = "سعر المحل المطلوب منك (اختياري لكشف التلاعب)",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldTheme.colors.surfaceElevated)
                                .border(1.dp, GoldTheme.colors.borderColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                BasicTextField(
                                    value = buyShopQuotedPrice,
                                    onValueChange = { viewModel.onBuyShopPriceChanged(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("buy_shop_price_input"),
                                    textStyle = TextStyle(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldTheme.colors.textPrimary,
                                        textAlign = TextAlign.Start
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    cursorBrush = SolidColor(GoldTheme.colors.goldPrimary),
                                    decorationBox = { inner ->
                                        if (buyShopQuotedPrice.isEmpty()) {
                                            Text("أدخل ما طلبه الصائغ لمقارنته", color = GoldTheme.colors.textMuted, fontSize = 13.sp)
                                        }
                                        inner()
                                    }
                                )
                                Text(
                                    text = selectedCountry.currencySymbol,
                                    fontSize = 12.sp,
                                    color = GoldTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. بطاقة النتيجة: تفاصيل الحساب والسعر النهائي بخلفية مميزة وأزرار المشاركة والحفظ
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("buy_result_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF242012) else Color(0xFFFFF9E6)
                ),
                border = BorderStroke(1.5.dp, GoldTheme.colors.goldPrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "السعر النهائي العادل المطلوب دفعه",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldTheme.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // السعر النهائي بخط كبير وواضح باللون الذهبي
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = viewModel.formatPrice(buyResult.totalFairPrice),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldTheme.colors.goldPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedCountry.currencySymbol,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.goldPrimary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    if (buyShopName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldTheme.colors.goldPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "المحل: $buyShopName",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.goldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = GoldTheme.colors.goldPrimary.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // تفاصيل الفاتورة
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "إجمالي السعر قبل المصنعية:",
                                fontSize = 13.sp,
                                color = GoldTheme.colors.textSecondary
                            )
                            Text(
                                text = "${viewModel.formatPrice(buyResult.rawGoldPrice)} ${selectedCountry.currencySymbol}",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.textPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "إجمالي قيمة المصنعية:",
                                fontSize = 13.sp,
                                color = GoldTheme.colors.textSecondary
                            )
                            Text(
                                text = "${viewModel.formatPrice(buyResult.makingTotal)} ${selectedCountry.currencySymbol}",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.goldPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "الضريبة والدمغة القانونية:",
                                fontSize = 13.sp,
                                color = GoldTheme.colors.textSecondary
                            )
                            Text(
                                text = "${viewModel.formatPrice(buyResult.stampTotal + buyResult.extraFees)} ${selectedCountry.currencySymbol}",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GoldTheme.colors.textPrimary
                            )
                        }
                    }


                }
            }
        }

        // 5. الملاحظات التحذيرية ونصيحة الشراء
        item {
            val shopVal = buyResult.shopPrice
            val diffVal = buyResult.difference
            val isOverpriced = diffVal > 1.0

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (shopVal > 0 && isOverpriced) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(GoldTheme.colors.danger.copy(alpha = 0.12f))
                            .border(1.dp, GoldTheme.colors.danger.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "⚠️ تنبيه: السعر المعروض أعلى من السعر العادل!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.danger
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "المحل يطلب زيادة قدرها ${viewModel.formatPrice(diffVal)} ${selectedCountry.currencySymbol}. تفاوض على تخفيض المصنعية للوصول للسعر العادل.",
                                fontSize = 11.5.sp,
                                color = GoldTheme.colors.textPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // نصيحة الشراء
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldTheme.colors.surface)
                        .border(0.8.dp, GoldTheme.colors.borderColor, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(text = "💡", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "نصيحة الشراء: اطلب فاتورة ضريبية رسمية مدوناً بها رقم السجل التجاري، والعيار بدقة، والوزن بالجرام، وقيمة المصنعية مفصولة عن سعر الذهب الخام.",
                            fontSize = 11.5.sp,
                            color = GoldTheme.colors.textSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // 6. الأزرار الأساسية: حفظ في السجل | مشاركة | إعادة تعيين
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // صف زري الحفظ والمشاركة
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // زر الحفظ
                    Button(
                        onClick = { viewModel.saveBuyTransaction() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("buy_save_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldTheme.colors.goldPrimary,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "💾 حفظ في السجل",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // زر المشاركة
                    OutlinedButton(
                        onClick = { viewModel.shareBuyResult(context) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("buy_share_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GoldTheme.colors.goldPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = GoldTheme.colors.goldLight
                        )
                    ) {
                        Text(
                            text = "📤 مشاركة الفاتورة",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // زر إعادة تعيين
                Button(
                    onClick = { viewModel.resetBuyForm() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("buy_reset_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldTheme.colors.surface,
                        contentColor = GoldTheme.colors.textPrimary
                    ),
                    border = BorderStroke(1.dp, GoldTheme.colors.borderColor)
                ) {
                    Text(
                        text = "🔄 إعادة تعيين",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
