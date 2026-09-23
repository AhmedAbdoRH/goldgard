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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ScreenType
import com.example.model.ZakatStatus
import com.example.ui.components.GoldScreenHeader
import com.example.ui.theme.GoldTheme
import com.example.ui.viewmodel.GoldViewModel
import com.example.util.GoldPriceFormatter

@Composable
fun ZakatScreen(
    viewModel: GoldViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val zakatWeight by viewModel.zakatWeight.collectAsStateWithLifecycle()
    val zakatKarat by viewModel.zakatKarat.collectAsStateWithLifecycle()
    val zakatGramPrice by viewModel.zakatGramPrice.collectAsStateWithLifecycle()
    val zakatIsHawlMet by viewModel.zakatIsHawlMet.collectAsStateWithLifecycle()
    val zakatIsPersonalJewelry by viewModel.zakatIsPersonalJewelry.collectAsStateWithLifecycle()
    val zakatPayJewelryWaraa by viewModel.zakatPayJewelryWaraa.collectAsStateWithLifecycle()
    val zakatResult by viewModel.zakatResult.collectAsStateWithLifecycle()

    var showSaveDialog by remember { mutableStateOf(false) }
    var saveNote by remember { mutableStateOf("") }

    val availableKarats = listOf(24, 22, 21, 18, 14)

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = GoldTheme.colors.surface,
            title = {
                Text(
                    text = "حفظ حساب الزكاة في السجل",
                    color = GoldTheme.colors.goldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "ملاحظة توضيحية للعملية (اختياري):",
                        fontSize = 12.sp,
                        color = GoldTheme.colors.textSecondary
                    )
                    OutlinedTextField(
                        value = saveNote,
                        onValueChange = { saveNote = it },
                        placeholder = { Text("مثال: زكاة مال عام 1446هـ...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldTheme.colors.goldPrimary,
                            unfocusedBorderColor = GoldTheme.colors.borderColor
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveZakatCalculation(saveNote)
                        showSaveDialog = false
                        saveNote = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldTheme.colors.goldPrimary)
                ) {
                    Text("حفظ", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("إلغاء", color = GoldTheme.colors.textSecondary)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GoldTheme.colors.background)
            .testTag("zakat_screen_root"),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // العنوان وزر الرجوع
        item {
            GoldScreenHeader(
                title = "حاسبة زكاة الذهب الشرعية",
                onBackClick = { viewModel.navigateTo(ScreenType.HOME) }
            )
        }

        // بطاقة النصاب الشرعي بالأبيض والذهبي بشكل أفقي واسع
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GoldTheme.colors.surface),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, GoldTheme.colors.goldPrimary.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "النصاب الشرعي: 85 جرام عيار 24",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.5.sp
                    )
                    Text(
                        text = "${GoldPriceFormatter.formatWithGrouping(zakatResult.nisabEgpValue)} ج.م",
                        fontWeight = FontWeight.Black,
                        color = GoldTheme.colors.goldPrimary,
                        fontSize = 14.5.sp
                    )
                }
            }
        }

        // أزرار اختيار العيار (نظيفة بدون سعر صغير تحتها)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GoldTheme.colors.surface),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, GoldTheme.colors.borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableKarats.forEach { karat ->
                        val isSelected = karat == zakatKarat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) GoldTheme.colors.goldPrimary else GoldTheme.colors.background
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) GoldTheme.colors.goldPrimary else GoldTheme.colors.borderColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.onZakatKaratSelected(karat) }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "عيار $karat",
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else GoldTheme.colors.textPrimary,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }
        }

        // إدخال الوزن وسعر الجرام (مباشر وبدون حشو أو أزرار إضافية)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GoldTheme.colors.surface),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, GoldTheme.colors.borderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // وزن الذهب الإجمالي
                    Column {
                        Text(
                            text = "إجمالي وزن الذهب بالجرام:",
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = zakatWeight,
                            onValueChange = { viewModel.onZakatWeightChanged(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("zakat_weight_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            placeholder = { Text("أدخل إجمالي الوزن بالجرام...", color = GoldTheme.colors.textSecondary, fontSize = 12.5.sp) },
                            trailingIcon = {
                                Text("جم", color = GoldTheme.colors.textSecondary, fontSize = 12.sp, modifier = Modifier.padding(end = 10.dp))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldTheme.colors.goldPrimary,
                                unfocusedBorderColor = GoldTheme.colors.borderColor,
                                focusedTextColor = GoldTheme.colors.textPrimary,
                                unfocusedTextColor = GoldTheme.colors.textPrimary
                            )
                        )
                    }

                    // سعر جرام البيع المعتمد لحساب الزكاة
                    Column {
                        Text(
                            text = "سعر جرام عيار $zakatKarat المعتمد للزكاة:",
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = zakatGramPrice,
                            onValueChange = { viewModel.onZakatGramPriceChanged(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("zakat_gram_price_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            trailingIcon = {
                                Text("ج.م", color = GoldTheme.colors.textSecondary, fontSize = 12.sp, modifier = Modifier.padding(end = 10.dp))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldTheme.colors.goldPrimary,
                                unfocusedBorderColor = GoldTheme.colors.borderColor,
                                focusedTextColor = GoldTheme.colors.textPrimary,
                                unfocusedTextColor = GoldTheme.colors.textPrimary
                            )
                        )
                    }
                }
            }
        }

        // الغرض من اقتناء الذهب والضابط الشرعي
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GoldTheme.colors.surface),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, GoldTheme.colors.borderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "الغرض والنية من اقتناء الذهب:",
                        fontWeight = FontWeight.Bold,
                        color = GoldTheme.colors.goldPrimary,
                        fontSize = 12.5.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // سبائك وادخار واقتناء
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!zakatIsPersonalJewelry) GoldTheme.colors.goldPrimary else GoldTheme.colors.background)
                                .border(0.8.dp, if (!zakatIsPersonalJewelry) GoldTheme.colors.goldPrimary else GoldTheme.colors.borderColor, RoundedCornerShape(8.dp))
                                .clickable { viewModel.onZakatPersonalJewelryChanged(false) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "سبائك / ادخار واقتناء",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!zakatIsPersonalJewelry) Color.Black else GoldTheme.colors.textPrimary
                            )
                        }
                        // حُلي نساء للزينة الشخصية
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (zakatIsPersonalJewelry) GoldTheme.colors.goldPrimary else GoldTheme.colors.background)
                                .border(0.8.dp, if (zakatIsPersonalJewelry) GoldTheme.colors.goldPrimary else GoldTheme.colors.borderColor, RoundedCornerShape(8.dp))
                                .clickable { viewModel.onZakatPersonalJewelryChanged(true) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "حُلي نساء للزينة الشخصية",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (zakatIsPersonalJewelry) Color.Black else GoldTheme.colors.textPrimary
                            )
                        }
                    }

                    if (zakatIsPersonalJewelry) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldTheme.colors.background)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "إخراج الزكاة تورعاً واحتياطاً:",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GoldTheme.colors.textPrimary
                                )
                                Text(
                                    text = "خروجاً من خلاف السادة الحنفية (مستحب)",
                                    fontSize = 10.sp,
                                    color = GoldTheme.colors.textSecondary
                                )
                            }
                            Switch(
                                checked = zakatPayJewelryWaraa,
                                onCheckedChange = { viewModel.onZakatPayJewelryWaraaChanged(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = GoldTheme.colors.goldPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // شرط الحول الشرعي (مر عليه عام كامل أو لم يمر)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GoldTheme.colors.surface),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, GoldTheme.colors.borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مرور عام هجري كامل (الحول الشرعي):",
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary,
                            fontSize = 12.5.sp
                        )
                        Text(
                            text = if (zakatIsHawlMet) "نعم، مر عليه عام كامل" else "لا، لم يمر عليه عام بعد",
                            color = if (zakatIsHawlMet) GoldTheme.colors.success else GoldTheme.colors.warning,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = zakatIsHawlMet,
                        onCheckedChange = { viewModel.onZakatHawlChanged(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = GoldTheme.colors.goldPrimary
                        )
                    )
                }
            }
        }

        // بطاقة النتيجة وحساب الزكاة والحكم الشرعي تحت خالص
        item {
            val status = zakatResult.status
            val isDue = status == ZakatStatus.ZAKAT_DUE

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("zakat_calculation_result_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GoldTheme.colors.surface),
                border = BorderStroke(1.2.dp, if (isDue) GoldTheme.colors.goldPrimary else GoldTheme.colors.borderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isDue) {
                        // إجمالي الزكاة الواجبة نقدياً: الكلمة فوق وتحتها الرقم وواضح
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldTheme.colors.goldPrimary.copy(alpha = 0.12f))
                                .border(1.dp, GoldTheme.colors.goldPrimary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (zakatIsPersonalJewelry && zakatPayJewelryWaraa)
                                    "مقدار الزكاة المُخرجة تورعاً واحتياطاً (2.5%)"
                                else
                                    "مقدار الزكاة الواجب إخراجها نقدياً (2.5% ربع العُشر)",
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.textPrimary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${GoldPriceFormatter.formatWithGrouping(zakatResult.zakatAmountMoney)} ج.م",
                                fontWeight = FontWeight.Black,
                                color = GoldTheme.colors.goldPrimary,
                                fontSize = 25.sp
                            )
                            Text(
                                text = "أو إخراجها عيناً: ${String.format(java.util.Locale.US, "%.2f", zakatResult.zakatAmountGrams)} جم عيار $zakatKarat",
                                fontWeight = FontWeight.SemiBold,
                                color = GoldTheme.colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }

                        HorizontalDivider(color = GoldTheme.colors.borderColor)
                    } else {
                        // حالة عدم الوجوب مع بيان شيك للحكم والسبب
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldTheme.colors.surfaceElevated)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = when (status) {
                                    ZakatStatus.EXEMPT_PERSONAL_JEWELRY -> "⚪ معفي من الزكاة شرعاً (حُلي زينة شخصية)"
                                    ZakatStatus.HAWL_NOT_MET -> "🟡 لا تجب الزكاة (الحول لم يكتمل بعد)"
                                    ZakatStatus.BELOW_NISAB -> "🔴 لا تجب الزكاة (أقل من النصاب الشرعي)"
                                    else -> "حاسبة الزكاة الشرعية"
                                },
                                color = when (status) {
                                    ZakatStatus.EXEMPT_PERSONAL_JEWELRY -> GoldTheme.colors.goldPrimary
                                    ZakatStatus.HAWL_NOT_MET -> GoldTheme.colors.warning
                                    ZakatStatus.BELOW_NISAB -> GoldTheme.colors.danger
                                    else -> GoldTheme.colors.textPrimary
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = when (status) {
                                    ZakatStatus.EXEMPT_PERSONAL_JEWELRY -> "السبب الشرعي: مذهب جمهور فقهاء المسلمين (المالكية والشافعية والحنابلة) أن حُلي المرأة المتخذ للزينة المباحة والاستعمال الشخصي المعتاد لا زكاة فيه لعدم قصد النماء. ويمكنك تفعيل خيار التورع بالأعلى لإخراجها احتياطاً."
                                    ZakatStatus.HAWL_NOT_MET -> "السبب الشرعي: شرط حولان الحول (مرور عام هجري كامل) لم يكتمل بعد على امتلاك الذهب، ومرور الحول شرط وجوب أساسي في زكاة الذهب."
                                    ZakatStatus.BELOW_NISAB -> "السبب الشرعي: إجمالي الذهب الصافي الخالص (${String.format(java.util.Locale.US, "%.1f", zakatResult.pureGoldEquivalent)} جم عيار 24) لم يبلغ النصاب الشرعي وهو 85 جرام ذهب خالص (متبقي لبلوغ النصاب: ${String.format(java.util.Locale.US, "%.1f", zakatResult.differenceToNisab)} جم عيار $zakatKarat)."
                                    else -> "يرجى إدخال وزن الذهب لتحديد الحكم الشرعي ومقدار الزكاة بدقة."
                                },
                                color = GoldTheme.colors.textSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }

                        HorizontalDivider(color = GoldTheme.colors.borderColor)
                    }

                    // تفاصيل الحساب
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("الوزن الإجمالي المدخل:", color = GoldTheme.colors.textSecondary, fontSize = 12.sp)
                        Text(
                            text = "${zakatWeight.ifBlank { "0" }} جم (عيار $zakatKarat)",
                            fontWeight = FontWeight.SemiBold,
                            color = GoldTheme.colors.textPrimary,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("المعادل من الذهب الصافي (عيار 24):", color = GoldTheme.colors.textSecondary, fontSize = 12.sp)
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.2f", zakatResult.pureGoldEquivalent)} جم (النصاب: 85 جم)",
                            fontWeight = FontWeight.SemiBold,
                            color = GoldTheme.colors.textPrimary,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("إجمالي القيمة التقديرية للذهب:", color = GoldTheme.colors.textSecondary, fontSize = 12.sp)
                        Text(
                            text = "${GoldPriceFormatter.formatWithGrouping(zakatResult.totalGoldValue)} ج.م",
                            fontWeight = FontWeight.SemiBold,
                            color = GoldTheme.colors.textPrimary,
                            fontSize = 12.sp
                        )
                    }

                    // الحكم الشرعي تحت خالص
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldTheme.colors.background)
                            .border(0.8.dp, GoldTheme.colors.borderColor, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "⚖️ خلاصة الحكم الشرعي:",
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.goldPrimary,
                                fontSize = 11.5.sp
                            )
                            Text(
                                text = zakatResult.reasonMessage.ifBlank {
                                    if (isDue) "تجب فيه الزكاة شرعاً (بلغ النصاب وحال عليه الحول - ربع العُشر 2.5%)."
                                    else "لا تجب فيه الزكاة لعدم توفر شروط الوجوب كاملة."
                                },
                                color = GoldTheme.colors.textPrimary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // أزرار الإجراءات: مشاركة التقرير (شير فقط بدون واتساب منفصل)، وحفظ في السجل، ومسح
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // زر مشاركة التقرير بالشير
                Button(
                    onClick = { viewModel.shareZakatResult(context) },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(42.dp)
                        .testTag("zakat_share_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldTheme.colors.surface),
                    border = BorderStroke(1.dp, GoldTheme.colors.goldPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("📤 مشاركة التقرير", color = GoldTheme.colors.goldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // حفظ في السجل
                Button(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(42.dp)
                        .testTag("zakat_save_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldTheme.colors.goldPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("💾 حفظ السجل", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // مسح وإعادة ضبط
                Button(
                    onClick = { viewModel.resetZakatForm() },
                    modifier = Modifier
                        .weight(0.7f)
                        .height(42.dp)
                        .testTag("zakat_reset_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldTheme.colors.surface),
                    border = BorderStroke(1.dp, GoldTheme.colors.borderColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("🔄 مسح", color = GoldTheme.colors.textSecondary, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }
            }
        }
    }
}
