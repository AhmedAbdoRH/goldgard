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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import com.example.model.ComparisonStatus
import com.example.model.ScreenType
import com.example.ui.components.GoldScreenHeader
import com.example.ui.theme.GoldTheme
import com.example.ui.viewmodel.GoldViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * تاسعًا: شاشة بيع الذهب
 * 1. خانات الإدخال: العيار، الوزن الإجمالي، وزن الفصوص مع خيار الخصم، سعر شراء المحل المعروض.
 * 2. بطاقة النتيجة: الوزن الصافي، السعر العادل للجرام، إجمالي المبلغ المستحق، توضيح التلاعب أو الخصم.
 * 3. الملاحظات والتنبيهات: تنبيه إذا كان السعر أقل من السوق، نصيحة قبل البيع.
 * 4. الأزرار: حساب، إعادة تعيين، حفظ في السجل.
 */
@Composable
fun SellGoldScreen(
    viewModel: GoldViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sellKarat by viewModel.sellKarat.collectAsStateWithLifecycle()
    val sellGramPrice by viewModel.sellGramPrice.collectAsStateWithLifecycle()
    val sellWeight by viewModel.sellWeight.collectAsStateWithLifecycle()
    val sellStonesWeight by viewModel.sellStonesWeight.collectAsStateWithLifecycle()
    val sellDeductStones by viewModel.sellDeductStones.collectAsStateWithLifecycle()
    val sellDamagedPercent by viewModel.sellDamagedPercent.collectAsStateWithLifecycle()
    val sellShopOffer by viewModel.sellShopOffer.collectAsStateWithLifecycle()
    val sellShopName by viewModel.sellShopName.collectAsStateWithLifecycle()
    val sellResult by viewModel.sellResult.collectAsStateWithLifecycle()
    val selectedCountry by viewModel.selectedCountry.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    val decimalFormat = remember { DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.US)) }
    val karats = listOf(18, 21, 22, 24)

    var showGramPriceEdit by remember { mutableStateOf(false) }

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
                title = "بيع الذهب",
                onBackClick = { viewModel.navigateTo(ScreenType.HOME) },
                onInfoClick = { viewModel.navigateTo(ScreenType.TIPS) },
                onThemeToggle = { viewModel.toggleDarkMode() },
                isDarkMode = isDarkMode,
                testTagPrefix = "sell_screen"
            )
        }

        // 2. اختيار العيار
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
                        text = "اختر العيار:",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldTheme.colors.textPrimary
                    )
                    Text(
                        text = "سعر شراء الصائغ عيار $sellKarat: ${sellGramPrice.ifEmpty { "0" }} ${selectedCountry.currencySymbol}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldTheme.colors.goldPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    karats.forEach { karat ->
                        val isSelected = karat == sellKarat
                        val label = if (karat == 21) "21 ⭐" else "$karat"
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
                                .clickable { viewModel.onSellKaratSelected(karat) }
                                .testTag("sell_karat_$karat"),
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
            }
        }

        // 3. خانات الإدخال الأساسية: الوزن الإجمالي، وزن الفصوص، سعر الجرام، سعر المحل المعروض
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
                    // 1. سعر شراء الجرام بالسوق (مع خيار تعديل)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "السعر العادل لجرام الكسر بالسوق",
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
                                    value = sellGramPrice,
                                    onValueChange = { viewModel.onSellGramPriceChanged(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sell_gram_price_input"),
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
                            text = "اسم المحل / المشتري",
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
                                    value = sellShopName,
                                    onValueChange = { viewModel.onSellShopNameChanged(it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("sell_shop_name_input"),
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GoldTheme.colors.textPrimary,
                                        textAlign = TextAlign.Start
                                    ),
                                    singleLine = true,
                                    cursorBrush = SolidColor(GoldTheme.colors.goldPrimary),
                                    decorationBox = { inner ->
                                        if (sellShopName.isEmpty()) {
                                            Text(
                                                "اسم محل الذهب أو المشتري (اختياري)",
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

                    // 3. الوزن الإجمالي بالميزان
                    Column {
                        Text(
                            text = "الوزن الإجمالي بالميزان",
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
                                    value = sellWeight,
                                    onValueChange = { viewModel.onSellWeightChanged(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sell_weight_input"),
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
                                        if (sellWeight.isEmpty()) {
                                            Text("مثال: 15.4", color = GoldTheme.colors.textMuted, fontSize = 14.sp)
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

                    // 4. وزن الفصوص وخيار خصم الفصوص
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "وزن الفصوص (إن وُجد)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.textPrimary
                            )

                            // خيار خصم الفصوص
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { viewModel.onSellDeductStonesChanged(!sellDeductStones) }
                            ) {
                                Checkbox(
                                    checked = sellDeductStones,
                                    onCheckedChange = { viewModel.onSellDeductStonesChanged(it) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = GoldTheme.colors.goldPrimary,
                                        checkmarkColor = Color.Black
                                    ),
                                    modifier = Modifier.padding(end = 2.dp)
                                )
                                Text(
                                    text = if (sellDeductStones) "يُخصم وزن الفصوص" else "لا تخصم الفصوص (تُباع بوزنها)",
                                    fontSize = 11.sp,
                                    color = if (sellDeductStones) GoldTheme.colors.textSecondary else GoldTheme.colors.goldPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
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
                                    value = sellStonesWeight,
                                    onValueChange = { viewModel.onSellStonesWeightChanged(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sell_stones_input"),
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
                                        if (sellStonesWeight.isEmpty()) {
                                            Text("اتركه فارغاً إن لم تكن هناك فصوص", color = GoldTheme.colors.textMuted, fontSize = 12.sp)
                                        }
                                        inner()
                                    }
                                )
                                Text(
                                    text = "جرام",
                                    fontSize = 12.sp,
                                    color = GoldTheme.colors.textSecondary
                                )
                            }
                        }
                    }

                    // 5. نسبة التالف / الهالك
                    Column {
                        Text(
                            text = "نسبة التالف / الهالك (%) (اختياري)",
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
                                    value = sellDamagedPercent,
                                    onValueChange = { viewModel.onSellDamagedPercentChanged(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sell_damaged_input"),
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
                                        if (sellDamagedPercent.isEmpty() || sellDamagedPercent == "0") {
                                            Text("0%", color = GoldTheme.colors.textMuted, fontSize = 13.sp)
                                        }
                                        inner()
                                    }
                                )
                                Text(
                                    text = "%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldTheme.colors.textSecondary
                                )
                            }
                        }
                    }

                    // سعر المحل المعروض (لكشف التلاعب)
                    Column {
                        Text(
                            text = "السعر الإجمالي المعروض عليك من المحل (اختياري)",
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
                                    value = sellShopOffer,
                                    onValueChange = { viewModel.onSellShopOfferChanged(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sell_shop_offer_input"),
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
                                        if (sellShopOffer.isEmpty()) {
                                            Text("أدخل المبلغ الذي عرضه المحل لمقارنته", color = GoldTheme.colors.textMuted, fontSize = 13.sp)
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

        // 4. بطاقة النتيجة: تفاصيل الحساب والمبلغ المستحق بخلفية مميزة وأزرار المشاركة والحفظ
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("sell_result_card"),
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
                        text = "إجمالي المبلغ المستحق لك عند البيع",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldTheme.colors.textSecondary
                    )

                    // سطر قيمة التالف فوق الصافي
                    if (sellResult.damagedValue > 0.0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "قيمة التالف: -${viewModel.formatPrice(sellResult.damagedValue)} ${selectedCountry.currencySymbol}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.danger
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // إجمالي المبلغ بخط كبير وذهبي
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = viewModel.formatPrice(sellResult.expectedTotalPayout),
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

                    if (sellShopName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldTheme.colors.goldPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "المحل: $sellShopName",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.goldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = GoldTheme.colors.goldPrimary.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // تفاصيل الفاتورة والوزن الصافي
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "الوزن الصافي للذهب:",
                                fontSize = 13.sp,
                                color = GoldTheme.colors.textSecondary
                            )
                            val displayNetWeight = if (sellResult.netWeight > 0) sellResult.netWeight else (sellWeight.toDoubleOrNull() ?: 0.0)
                            Text(
                                text = "${decimalFormat.format(displayNetWeight)} جرام",
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
                                text = "السعر العادل للجرام (كسر):",
                                fontSize = 13.sp,
                                color = GoldTheme.colors.textSecondary
                            )
                            Text(
                                text = "${viewModel.formatPrice(sellResult.netGramPrice.takeIf { it > 0 } ?: (sellGramPrice.toDoubleOrNull() ?: 0.0))} ${selectedCountry.currencySymbol}",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.goldPrimary
                            )
                        }

                        if (sellResult.stonesWeight > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "وزن الفصوص ${if (sellResult.deductedStones) "(مخصوم)" else "(غير مخصوم)"}:",
                                    fontSize = 13.sp,
                                    color = GoldTheme.colors.textSecondary
                                )
                                Text(
                                    text = "${decimalFormat.format(sellResult.stonesWeight)} جرام",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (sellResult.deductedStones) GoldTheme.colors.danger else GoldTheme.colors.success
                                )
                            }
                        }

                        if (sellResult.damagedValue > 0.0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "قيمة التالف (${sellResult.damagedPercent}%):",
                                    fontSize = 13.sp,
                                    color = GoldTheme.colors.danger
                                )
                                Text(
                                    text = "-${viewModel.formatPrice(sellResult.damagedValue)} ${selectedCountry.currencySymbol}",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldTheme.colors.danger
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. الملاحظات والتنبيهات: كشف التلاعب ونصيحة البيع
        item {
            val offer = sellResult.shopOffer
            val fair = sellResult.expectedTotalPayout
            val isShopPayingLess = offer > 0 && offer < (fair - 1.0)
            val diff = fair - offer

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (offer > 0 && isShopPayingLess) {
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
                                text = "⚠️ تنبيه: عرض المحل أقل من السعر العادل في السوق!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.danger
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "المحل يعرض مبلغاً أقل بـ ${viewModel.formatPrice(diff)} ${selectedCountry.currencySymbol} عن حقك العادل. قد يكون ذلك بسبب مبالغة في خصم الفصوص أو التبخيس في سعر الجرام.",
                                fontSize = 11.5.sp,
                                color = GoldTheme.colors.textPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // نصيحة قبل البيع
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
                            text = "نصيحة قبل البيع: تأكد من وزن الذهب أمامك على ميزان حساس معتمد، وإذا كان الذهب مشترياً بفصوص من ماركة معتمدة (مثل لازوردي)، يحق لك استرداد جزء من وزن الفصوص حسب الفاتورة الأصلية.",
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
                        onClick = { viewModel.saveSellTransaction() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("sell_save_button"),
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
                        onClick = { viewModel.shareSellResult(context) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("sell_share_button"),
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
                    onClick = { viewModel.resetSellForm() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("sell_reset_button"),
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
