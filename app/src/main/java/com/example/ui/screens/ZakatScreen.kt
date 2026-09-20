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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * شاشة «زكاة الذهب»
 * - الاسم: زكاة الذهب
 * - حد النصاب الشرعي: 85 جرام عيار 24 بسعر البيع (مكتوب بأرقام بفواصل مقروءة).
 * - شرط مرور الحول: التحقق هل مر عام هجري كامل على بلوغ النصاب أم لا.
 * - القيمة = الوزن × سعر بيع جرام العيار المختار.
 * - إذا القيمة >= النصاب ومر الحول: تجب الزكاة (2.5% من إجمالي القيمة).
 * - توضيح الحكم الشرعي في أسفل الشاشة.
 */
@Composable
fun ZakatScreen(
    viewModel: GoldViewModel,
    modifier: Modifier = Modifier
) {
    val goldPriceResponse by viewModel.goldPriceResponse.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    var selectedKarat by rememberSaveable { mutableIntStateOf(21) }
    var weightInput by rememberSaveable { mutableStateOf("") }
    var hasPassedOneHijriYear by rememberSaveable { mutableStateOf(true) }

    val karats = listOf(24, 22, 21, 18, 14)
    val numberFormatter = remember { DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)) }

    // سعر بيع عيار 24 اللحظي
    val sell24 = goldPriceResponse.gram24.sell

    // سعر بيع جرام العيار المختار
    val chosenKaratSellPrice = when (selectedKarat) {
        24 -> goldPriceResponse.gram24.sell
        22 -> goldPriceResponse.gram22.sell
        21 -> goldPriceResponse.gram21.sell
        18 -> goldPriceResponse.gram18.sell
        14 -> goldPriceResponse.gram14.sell
        else -> goldPriceResponse.gram21.sell
    }

    val weight = weightInput.toDoubleOrNull() ?: 0.0

    // الحسابات المعتمدة
    val totalValue = weight * chosenKaratSellPrice
    val nisab = 85.0 * sell24
    val reachedNisab = totalValue >= nisab && nisab > 0.0 && weight > 0.0
    val isZakatDue = reachedNisab && hasPassedOneHijriYear
    val zakatAmount = if (isZakatDue) Math.round(totalValue * 0.025) else 0L

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GoldTheme.colors.background)
            .testTag("zakat_screen"),
        contentPadding = PaddingValues(bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // العنوان المعتمد: «زكاة الذهب» فقط
        item {
            GoldScreenHeader(
                title = "زكاة الذهب",
                onBackClick = { viewModel.navigateTo(ScreenType.HOME) },
                onThemeToggle = { viewModel.toggleDarkMode() },
                isDarkMode = isDarkMode,
                testTagPrefix = "zakat"
            )
        }

        // بطاقة حد النصاب اللحظي (85 جم عيار 24 للبيع مع فاصلة للأرقام)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = GoldTheme.colors.surfaceElevated),
                border = BorderStroke(1.dp, GoldTheme.colors.goldPrimary.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "⚖️", fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "حد النصاب الشرعي",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldTheme.colors.goldPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "ما يعادل 85 جرام من الذهب الخالص (عيار 24)",
                                fontSize = 11.5.sp,
                                color = GoldTheme.colors.textSecondary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${numberFormatter.format(Math.round(nisab))} ج.م",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldTheme.colors.textPrimary
                            )
                            Text(
                                text = "بسعر البيع اللحظي",
                                fontSize = 10.5.sp,
                                color = GoldTheme.colors.textSecondary
                            )
                        }
                    }

                    HorizontalDivider(
                        color = GoldTheme.colors.borderColor.copy(alpha = 0.5f),
                        thickness = 0.8.dp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سعر جرام عيار 24 المعتمد:",
                            fontSize = 12.sp,
                            color = GoldTheme.colors.textSecondary
                        )
                        Text(
                            text = "${numberFormatter.format(Math.round(sell24))} ج.م",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary
                        )
                    }
                }
            }
        }

        // بطاقة المدخلات: الوزن بالجرام + العيار + شرط مرور سنة (حول كامل)
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // اختيار العيار
                    Text(
                        text = "عيار الذهب",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldTheme.colors.textPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        karats.forEach { karat ->
                            val isSelected = karat == selectedKarat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) GoldTheme.colors.goldPrimary.copy(alpha = 0.18f)
                                        else GoldTheme.colors.surfaceElevated
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) GoldTheme.colors.goldPrimary else GoldTheme.colors.borderColor,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedKarat = karat }
                                    .testTag("zakat_karat_$karat"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$karat",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) GoldTheme.colors.goldPrimary else GoldTheme.colors.textSecondary
                                )
                            }
                        }
                    }

                    // الوزن بالجرام
                    Text(
                        text = "الوزن الإجمالي بالجرام",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldTheme.colors.textPrimary
                    )

                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it.filter { c -> c.isDigit() || c == '.' } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("zakat_weight_input"),
                        placeholder = { Text("أدخل وزن الذهب بالجرام (مثال: 100)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldTheme.colors.goldPrimary,
                            unfocusedBorderColor = GoldTheme.colors.borderColor,
                            focusedTextColor = GoldTheme.colors.textPrimary,
                            unfocusedTextColor = GoldTheme.colors.textPrimary
                        )
                    )

                    // معلومات السعر المعتمد
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سعر بيع جرام عيار $selectedKarat:",
                            fontSize = 12.sp,
                            color = GoldTheme.colors.textSecondary
                        )
                        Text(
                            text = "${numberFormatter.format(Math.round(chosenKaratSellPrice))} ج.م",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary
                        )
                    }

                    if (weight > 0.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "إجمالي قيمة الذهب:",
                                fontSize = 12.sp,
                                color = GoldTheme.colors.textSecondary
                            )
                            Text(
                                text = "${numberFormatter.format(Math.round(totalValue))} ج.م",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.textPrimary
                            )
                        }
                    }

                    HorizontalDivider(color = GoldTheme.colors.borderColor.copy(alpha = 0.5f))

                    // شرط مرور سنة هجرية (الحول)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldTheme.colors.surfaceElevated.copy(alpha = 0.5f))
                            .clickable { hasPassedOneHijriYear = !hasPassedOneHijriYear }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = hasPassedOneHijriYear,
                            onCheckedChange = { hasPassedOneHijriYear = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = GoldTheme.colors.goldPrimary,
                                checkmarkColor = Color.Black
                            ),
                            modifier = Modifier.testTag("zakat_hawl_checkbox")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "مر عليه عام هجري كامل (الحول)؟",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.textPrimary
                            )
                            Text(
                                text = if (hasPassedOneHijriYear) "نعم، مر عليه حول كامل في حيازتك" else "لا، لم يمر عام هجري حتى الآن",
                                fontSize = 11.5.sp,
                                color = if (hasPassedOneHijriYear) GoldTheme.colors.goldPrimary else GoldTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // نتيجة الزكاة المعتمدة نصياً
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isZakatDue) GoldTheme.colors.success.copy(alpha = 0.12f)
                    else GoldTheme.colors.surfaceElevated
                ),
                border = BorderStroke(
                    1.dp,
                    if (isZakatDue) GoldTheme.colors.success.copy(alpha = 0.5f)
                    else GoldTheme.colors.borderColor
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (weight <= 0.0) {
                        Text(
                            text = "أدخل وزن الذهب لحساب الزكاة",
                            fontSize = 13.5.sp,
                            color = GoldTheme.colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    } else if (!reachedNisab) {
                        Text(
                            text = "لا زكاة — أقل من النصاب",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.danger,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "قيمة الذهب (${numberFormatter.format(Math.round(totalValue))} ج.م) أقل من حد النصاب الشرعي (${numberFormatter.format(Math.round(nisab))} ج.م)",
                            fontSize = 11.5.sp,
                            color = GoldTheme.colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    } else if (!hasPassedOneHijriYear) {
                        Text(
                            text = "لا تجب الزكاة حالياً — لم يكتمل الحول",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.goldPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "الذهب بلغ النصاب الشرعي، ولكن من شروط الزكاة أن يمضي عليه عام هجري كامل في ملكيتك.",
                            fontSize = 11.5.sp,
                            color = GoldTheme.colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "الزكاة الواجبة: ${numberFormatter.format(zakatAmount)} ج.م",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldTheme.colors.success,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "(بلغ النصاب الشرعي ومر الحول: يُخرج 2.5% من إجمالي القيمة)",
                            fontSize = 12.sp,
                            color = GoldTheme.colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // نقطة توضيح الحكم الشرعي في النهاية
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GoldTheme.colors.surface),
                border = BorderStroke(1.dp, GoldTheme.colors.borderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📖", fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "توضيح الحكم الشرعي لزكاة الذهب",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.goldPrimary
                        )
                    }

                    Text(
                        text = "1. حد النصاب: هو 85 جرامًا من الذهب الخالص عيار 24 (أو ما يعادل قيمته من العيارات الأخرى كـ 97.14 جرام لعيار 21 أو 113.3 جرام لعيار 18).\n" +
                                "2. مرور الحول: يشترط أن يمر عام هجري كامل على بلوغ النصاب وهو في ملكك الفعلي.\n" +
                                "3. مقدار الزكاة: ربع العشر (2.5%) يُحسب من القيمة السوقية الحالية للذهب وقت إخراج الزكاة.\n" +
                                "4. ذهب الزينة المباح للمرأة: عند جمهور الفقهاء ليس فيه زكاة إذا كان للاستعمال الشخصي المعتاد، وتجب الزكاة عند الحنفية ومن احتاط، كما تجب اتفاقاً فيما كان للادخار أو التجارة.",
                        fontSize = 11.5.sp,
                        color = GoldTheme.colors.textSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
