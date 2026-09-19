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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ScreenType
import com.example.model.ZakatStatus
import com.example.ui.components.GoldScreenHeader
import com.example.ui.theme.GoldTheme
import com.example.ui.viewmodel.GoldViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * شاشة زكاة الذهب الاحترافية
 * متطابقة مع الهوية اللونية الداكنة وشروط الفقه الإسلامي المعتمدة
 */
@Composable
fun ZakatScreen(
    viewModel: GoldViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedCountry by viewModel.selectedCountry.collectAsStateWithLifecycle()
    val zakatKarat by viewModel.zakatKarat.collectAsStateWithLifecycle()
    val zakatWeight by viewModel.zakatWeight.collectAsStateWithLifecycle()
    val zakatIsHawlMet by viewModel.zakatIsHawlMet.collectAsStateWithLifecycle()
    val zakatIsPersonalJewelry by viewModel.zakatIsPersonalJewelry.collectAsStateWithLifecycle()
    val zakatPayJewelryWaraa by viewModel.zakatPayJewelryWaraa.collectAsStateWithLifecycle()
    val zakatGramPrice by viewModel.zakatGramPrice.collectAsStateWithLifecycle()
    val zakatResult by viewModel.zakatResult.collectAsStateWithLifecycle()
    val goldPriceResponse by viewModel.goldPriceResponse.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    val colors = GoldTheme.colors
    val decimalFormat = remember { DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.US)) }
    val karats = listOf(24, 21, 18, 22)

    val live21Price = goldPriceResponse.gram21.sell.takeIf { it > 0 } ?: 6300.0
    val nisab21Value = 85.0 * live21Price

    // تأكيد تعبئة السعر الافتراضي للعيار المحدد إن كان فارغاً
    LaunchedEffect(zakatKarat) {
        if (zakatGramPrice.isBlank() || zakatGramPrice == "0") {
            viewModel.updateZakatPriceForSelectedKarat()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag("zakat_screen"),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // 1. الرأس الموحد
        item {
            GoldScreenHeader(
                title = "حساب زكاة الذهب",
                onBackClick = { viewModel.navigateTo(ScreenType.HOME) },
                onInfoClick = null,
                onThemeToggle = { viewModel.toggleDarkMode() },
                isDarkMode = isDarkMode,
                testTagPrefix = "zakat"
            )
            Text(
                text = "حساب زكاة الذهب فقط لا غير",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 2. بطاقة نصاب الذهب الشرعي المرجعي (85 جم عيار 24 للبيع)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated),
                border = BorderStroke(1.dp, colors.goldPrimary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⚖️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "النصاب الشرعي للذهب (85 جم عيار 24 للبيع)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.goldLight
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "85 جم × ${viewModel.formatPrice(zakatResult.sellPrice24k)} ج.م (سعر بيع عيار 24)",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${viewModel.formatPrice(zakatResult.nisabEgpValue)} ${selectedCountry.currencySymbol}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.goldPrimary
                        )
                        Text(
                            text = "الحد الأدنى للوجوب",
                            fontSize = 10.5.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 3. خانات الإدخال الأساسية
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.borderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // أ. اختيار العيار
                    Text(
                        text = "1. عيار الذهب",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.goldLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        karats.forEach { karat ->
                            val isSelected = karat == zakatKarat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) colors.goldPrimary.copy(alpha = 0.2f)
                                        else colors.surfaceElevated
                                    )
                                    .border(
                                        width = if (isSelected) 1.8.dp else 1.dp,
                                        color = if (isSelected) colors.goldPrimary else colors.borderColor,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        viewModel.onZakatKaratSelected(karat)
                                        viewModel.updateZakatPriceForSelectedKarat()
                                    }
                                    .testTag("zakat_karat_$karat"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "عيار $karat",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.goldPrimary else colors.textSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ب. الوزن الإجمالي
                    Text(
                        text = "2. الوزن الإجمالي (جرام)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.goldLight
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = zakatWeight,
                        onValueChange = { viewModel.onZakatWeightChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("zakat_weight_input"),
                        placeholder = { Text("أدخل وزن الذهب بالجرام (مثال: 120)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.goldPrimary,
                            unfocusedBorderColor = colors.borderColor,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedContainerColor = colors.surfaceElevated,
                            unfocusedContainerColor = colors.surfaceElevated
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // ج. سعر الجرام المعتمد للحساب
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3. سعر جرام عيار $zakatKarat المعتمد (${selectedCountry.currencySymbol})",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.goldLight
                        )
                        Text(
                            text = "سعر الصاغة اللحظي",
                            fontSize = 11.sp,
                            color = colors.goldPrimary,
                            modifier = Modifier.clickable {
                                viewModel.resetZakatToLivePrice()
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = zakatGramPrice,
                        onValueChange = { viewModel.onZakatGramPriceChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("zakat_gram_price_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.goldPrimary,
                            unfocusedBorderColor = colors.borderColor,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedContainerColor = colors.surfaceElevated,
                            unfocusedContainerColor = colors.surfaceElevated
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // د. الغرض من الذهب (ادخار واستثمار / حُلي زينة شخصية)
                    Text(
                        text = "4. الغرض من الذهب",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.goldLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // سبائك / ادخار واستثمار
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (!zakatIsPersonalJewelry) colors.goldPrimary.copy(alpha = 0.18f)
                                    else colors.surfaceElevated
                                )
                                .border(
                                    width = if (!zakatIsPersonalJewelry) 1.8.dp else 1.dp,
                                    color = if (!zakatIsPersonalJewelry) colors.goldPrimary else colors.borderColor,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.onZakatPersonalJewelryChanged(false) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "💰 ادخار أو سبائك",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!zakatIsPersonalJewelry) colors.goldPrimary else colors.textSecondary
                                )
                                Text(
                                    text = "(تجب فيه الزكاة إجماعاً)",
                                    fontSize = 10.sp,
                                    color = if (!zakatIsPersonalJewelry) colors.goldLight else colors.textMuted
                                )
                            }
                        }

                        // حُلي زينة شخصية
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (zakatIsPersonalJewelry) colors.goldPrimary.copy(alpha = 0.18f)
                                    else colors.surfaceElevated
                                )
                                .border(
                                    width = if (zakatIsPersonalJewelry) 1.8.dp else 1.dp,
                                    color = if (zakatIsPersonalJewelry) colors.goldPrimary else colors.borderColor,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.onZakatPersonalJewelryChanged(true) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "💍 حُلي زينة للمرأة",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (zakatIsPersonalJewelry) colors.goldPrimary else colors.textSecondary
                                )
                                Text(
                                    text = "(معفي عند الجمهور)",
                                    fontSize = 10.sp,
                                    color = if (zakatIsPersonalJewelry) colors.goldLight else colors.textMuted
                                )
                            }
                        }
                    }

                    // خيار الورع إذا كان حُلي زينة شخصية
                    if (zakatIsPersonalJewelry) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surfaceElevated)
                                .border(1.dp, colors.borderColor, RoundedCornerShape(10.dp))
                                .clickable { viewModel.onZakatPayJewelryWaraaChanged(!zakatPayJewelryWaraa) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "إخراج الزكاة ورعاً وخروجاً من الخلاف",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "احتياطاً لرأي السادة الحنفية بإخراج 2.5%",
                                    fontSize = 10.5.sp,
                                    color = colors.textSecondary
                                )
                            }
                            Switch(
                                checked = zakatPayJewelryWaraa,
                                onCheckedChange = { viewModel.onZakatPayJewelryWaraaChanged(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.goldPrimary,
                                    checkedTrackColor = colors.goldPrimary.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // هـ. شرط مرور الحول الهجري الكامل
                    Text(
                        text = "5. هل مر عليه عام هجري كامل؟ (شرط الحَوْل)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.goldLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // خيار: نعم مر عام
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (zakatIsHawlMet) colors.success.copy(alpha = 0.18f)
                                    else colors.surfaceElevated
                                )
                                .border(
                                    width = if (zakatIsHawlMet) 1.8.dp else 1.dp,
                                    color = if (zakatIsHawlMet) colors.success else colors.borderColor,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.onZakatHawlChanged(true) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "✅ نعم، مرّ عليه عام",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (zakatIsHawlMet) colors.success else colors.textSecondary
                                )
                                Text(
                                    text = "(اكتمل الحول الشرعي)",
                                    fontSize = 10.sp,
                                    color = if (zakatIsHawlMet) colors.success.copy(alpha = 0.8f) else colors.textMuted
                                )
                            }
                        }

                        // خيار: لا لم يمر عام
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (!zakatIsHawlMet) colors.danger.copy(alpha = 0.18f)
                                    else colors.surfaceElevated
                                )
                                .border(
                                    width = if (!zakatIsHawlMet) 1.8.dp else 1.dp,
                                    color = if (!zakatIsHawlMet) colors.danger else colors.borderColor,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.onZakatHawlChanged(false) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "❌ لا، لم يمر عام",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!zakatIsHawlMet) colors.danger else colors.textSecondary
                                )
                                Text(
                                    text = "(لم يكتمل الحول)",
                                    fontSize = 10.sp,
                                    color = if (!zakatIsHawlMet) colors.danger.copy(alpha = 0.8f) else colors.textMuted
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4. زر إعادة الضبط الوحيد (الحساب فوري ولحظي مع أي تغيير)
        item {
            OutlinedButton(
                onClick = { viewModel.resetZakatForm() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 16.dp)
                    .testTag("zakat_reset_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, colors.borderColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.textSecondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "إعادة تعيين",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "إعادة ضبط الحاسبة",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 5. بطاقة النتيجة الشرعية المباشرة
        item {
            val isDue = zakatResult.status == ZakatStatus.ZAKAT_DUE
            val isExempt = zakatResult.status == ZakatStatus.EXEMPT_PERSONAL_JEWELRY
            val statusColor = when {
                isDue -> colors.success
                isExempt -> colors.goldLight
                else -> colors.warning
            }
            val statusBg = when {
                isDue -> if (isDarkMode) Color(0xFF142918) else Color(0xFFE8F5E9)
                isExempt -> if (isDarkMode) Color(0xFF1C2433) else Color(0xFFF1F5F9)
                else -> if (isDarkMode) Color(0xFF262014) else Color(0xFFFFF8E1)
            }
            val statusBorder = when {
                isDue -> colors.success
                isExempt -> colors.borderColor
                else -> colors.goldPrimary.copy(alpha = 0.5f)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("zakat_result_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = statusBg),
                border = BorderStroke(1.5.dp, statusBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // شارة القرار الشرعي
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (zakatResult.status) {
                                ZakatStatus.ZAKAT_DUE -> "🟢 تجب فيه الزكاة شرعاً (فوق النصاب)"
                                ZakatStatus.EXEMPT_PERSONAL_JEWELRY -> "⚪ معفي (حُلي زينة شخصية عند الجمهور)"
                                ZakatStatus.BELOW_NISAB -> "🔴 مفيش زكاة — أقل من النصاب"
                                ZakatStatus.HAWL_NOT_MET -> "⏳ لم يكتمل الحول الهجري"
                                else -> "ℹ️ أدخل بيانات الذهب للحساب"
                            },
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Black,
                            color = statusColor
                        )
                        Text(
                            text = "نسبة الزكاة 2.5%",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = zakatResult.reasonMessage,
                        fontSize = 12.sp,
                        color = colors.textPrimary,
                        lineHeight = 18.sp
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        thickness = 0.8.dp,
                        color = colors.borderColor
                    )

                    // 1) قيمة ما عند المستخدم
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "قيمة ما عند المستخدم:",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "${viewModel.formatPrice(zakatResult.totalGoldValue)} ج.م (${decimalFormat.format(zakatResult.totalWeight)} جم × ${viewModel.formatPrice(zakatResult.gramPriceUsed)} ج)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 2) النصاب الشرعي
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "النصاب الشرعي (85 جم عيار 24):",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "${viewModel.formatPrice(zakatResult.nisabEgpValue)} ج.م (85 × ${viewModel.formatPrice(zakatResult.sellPrice24k)} ج)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // مقدار الزكاة بالجرام
                    if (isDue) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "مقدار الزكاة بالجرام (عيار $zakatKarat):",
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "${decimalFormat.format(zakatResult.zakatAmountGrams)} جرام",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.goldLight
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // القيمة النقدية بخط كبير وواضح
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.goldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isDue) "قيمة الزكاة النقدية الواجب إخراجها (ربع العشر 2.5%)" else "حالة استحقاق الزكاة النقدية",
                                fontSize = 11.5.sp,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (isDue) "${viewModel.formatPrice(zakatResult.zakatAmountMoney)} ${selectedCountry.currencySymbol}" else "لا تجب الزكاة (دون النصاب)",
                                fontSize = if (isDue) 23.sp else 16.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDue) colors.goldPrimary else colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // توثيق السعر ومصدره
                    Text(
                        text = "📌 التوثيق: السعر المحسوب عليه ${viewModel.formatPrice(zakatResult.gramPriceUsed)} ج.م/جم (بيع عيار $zakatKarat) | المصدر: ${zakatResult.priceSourceNote}",
                        fontSize = 10.5.sp,
                        color = colors.textSecondary,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // أزرار الحفظ والمشاركة لحساب الزكاة
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // زر الحفظ
                        Button(
                            onClick = { viewModel.saveZakatCalculation() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("zakat_save_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.goldPrimary,
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
                            onClick = { viewModel.shareZakatResult(context) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("zakat_share_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, colors.goldPrimary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colors.goldLight
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "مشاركة",
                                modifier = Modifier.size(16.dp),
                                tint = colors.goldLight
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "مشاركة التقرير",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 6. التوجيه الشرعي المختصر
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "توجيه شرعي",
                        tint = colors.goldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "توجيه فقهي مختصر حول زكاة الحلي والادخار",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.goldLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• ذهب الادخار والاستثمار والسبائك: تجب فيه الزكاة بإجماع الفقهاء إذا بلغ النصاب (85 جم خالص) وحال عليه الحول بنسبة 2.5%.\n• حُليّ الزينة المعتاد للمرأة: ذهب جمهور العلماء (المالكية والشافعية والحنابلة) إلى عدم وجوب الزكاة فيه ما دام في حدود الاستعمال المعتاد، بينما أوجبها الحنفية احتياطاً. ويُنصح بمراجعة دار الإفتاء الرسمية للمسائل التفصيلية.",
                            fontSize = 11.5.sp,
                            color = colors.textSecondary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}
