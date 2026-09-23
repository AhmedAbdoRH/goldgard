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
import com.example.model.ComparisonStatus
import com.example.model.ScreenType
import com.example.ui.components.GoldScreenHeader
import com.example.ui.theme.GoldTheme
import com.example.ui.viewmodel.GoldViewModel
import com.example.util.GoldPriceFormatter
import java.util.Locale

@Composable
fun SellGoldScreen(
    viewModel: GoldViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sellKarat by viewModel.sellKarat.collectAsStateWithLifecycle()
    val sellGramPrice by viewModel.sellGramPrice.collectAsStateWithLifecycle()
    val sellWeight by viewModel.sellWeight.collectAsStateWithLifecycle()
    val sellDeductionPercent by viewModel.sellDeductionPercent.collectAsStateWithLifecycle()
    val sellShopOffer by viewModel.sellShopOffer.collectAsStateWithLifecycle()
    val sellResult by viewModel.sellResult.collectAsStateWithLifecycle()

    var showSaveDialog by remember { mutableStateOf(false) }
    var saveNote by remember { mutableStateOf("") }

    val availableKarats = listOf(24, 22, 21, 18, 14)

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = GoldTheme.colors.surface,
            title = {
                Text(
                    text = "حفظ عملية البيع في السجل",
                    color = GoldTheme.colors.goldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "ملاحظة توضيحية لعملية البيع (اختياري):",
                        fontSize = 12.sp,
                        color = GoldTheme.colors.textSecondary
                    )
                    OutlinedTextField(
                        value = saveNote,
                        onValueChange = { saveNote = it },
                        placeholder = { Text("مثال: بيع كسر ذهب، إسواره...", fontSize = 12.sp) },
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
                        viewModel.saveSellTransaction(saveNote)
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
            .testTag("sell_screen_root"),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // العنوان وزر الرجوع
        item {
            GoldScreenHeader(
                title = "حاسبة بيع الذهب والكسر",
                onBackClick = { viewModel.navigateTo(ScreenType.HOME) }
            )
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
                        val isSelected = karat == sellKarat
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
                                .clickable { viewModel.onSellKaratSelected(karat) }
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

        // إدخال الوزن وسعر الجرام (بشكل متماسك ومضغوط بدون فراغات زائدة)
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // الوزن بالجرام
                    Column {
                        Text(
                            text = "الوزن بالجرام:",
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = sellWeight,
                            onValueChange = { viewModel.onSellWeightChanged(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sell_weight_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            placeholder = { Text("أدخل وزن الذهب بالجرام...", color = GoldTheme.colors.textSecondary, fontSize = 12.5.sp) },
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

                    // سعر جرام البيع
                    Column {
                        Text(
                            text = "سعر شراء الصائغ للجرام عيار $sellKarat:",
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = sellGramPrice,
                            onValueChange = { viewModel.onSellGramPriceChanged(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sell_gram_price_input"),
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

        // نسبة الخصم فقط (كم في المائة مثلاً 2%)
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
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "نسبة الخصم (%):",
                        fontWeight = FontWeight.Bold,
                        color = GoldTheme.colors.textPrimary,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = sellDeductionPercent,
                        onValueChange = { viewModel.onSellDeductionPercentChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        placeholder = { Text("مثال: 2", fontSize = 12.5.sp) },
                        trailingIcon = {
                            Text("% (خصم شوائب/كسر)", color = GoldTheme.colors.textSecondary, fontSize = 11.5.sp, modifier = Modifier.padding(end = 10.dp))
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

        // المبلغ الذي عرضه عليك الصائغ للمقارنة
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
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "المبلغ الذي عرضه عليك الصائغ (ج.م):",
                        fontWeight = FontWeight.Bold,
                        color = GoldTheme.colors.textPrimary,
                        fontSize = 12.5.sp
                    )
                    OutlinedTextField(
                        value = sellShopOffer,
                        onValueChange = { viewModel.onSellShopOfferChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sell_shop_offer_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        placeholder = { Text("أدخل المبلغ الذي عرضه عليك الصائغ للمقارنة...", fontSize = 11.5.sp) },
                        trailingIcon = {
                            Text("ج.م", color = GoldTheme.colors.textSecondary, fontSize = 11.5.sp, modifier = Modifier.padding(end = 10.dp))
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

        // بطاقة النتيجة: المبلغ العادل المستحق لك أول حاجة فوق، وبعد كده التفاصيل تحت منه
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sell_calculation_result_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GoldTheme.colors.surface),
                border = BorderStroke(1.2.dp, GoldTheme.colors.goldPrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // المبلغ العادل المستحق: الكلمة فوق وتحت منها الرقم وواضح وزي الفل
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
                            text = "المبلغ العادل المستحق لك (كاش فوراً)",
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${GoldPriceFormatter.formatWithGrouping(sellResult.expectedTotalPayout)} ج.م",
                            fontWeight = FontWeight.Black,
                            color = GoldTheme.colors.goldPrimary,
                            fontSize = 25.sp
                        )
                    }

                    HorizontalDivider(color = GoldTheme.colors.borderColor)

                    // التفاصيل تحت المبلغ العادل
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("وزن الذهب:", color = GoldTheme.colors.textSecondary, fontSize = 12.sp)
                        Text(
                            text = "${sellWeight.ifBlank { "0" }} جم (عيار $sellKarat)",
                            fontWeight = FontWeight.SemiBold,
                            color = GoldTheme.colors.textPrimary,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("سعر الجرام قبل الخصم:", color = GoldTheme.colors.textSecondary, fontSize = 12.sp)
                        Text(
                            text = "${sellGramPrice} ج.م",
                            fontWeight = FontWeight.SemiBold,
                            color = GoldTheme.colors.textPrimary,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("سعر الجرام الصافي بعد الخصم:", color = GoldTheme.colors.textSecondary, fontSize = 12.sp)
                        Text(
                            text = "${GoldPriceFormatter.formatWithGrouping(sellResult.netGramPrice)} ج.م",
                            fontWeight = FontWeight.SemiBold,
                            color = GoldTheme.colors.textPrimary,
                            fontSize = 12.sp
                        )
                    }

                    if (sellResult.deductionPerGram > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("قيمة الخصم للجرام (${sellDeductionPercent}%):", color = GoldTheme.colors.textSecondary, fontSize = 12.sp)
                            Text(
                                text = "-${GoldPriceFormatter.formatWithGrouping(sellResult.deductionPerGram)} ج.م/جم",
                                fontWeight = FontWeight.SemiBold,
                                color = GoldTheme.colors.danger,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // مقارنة عرض الصائغ إن وُجد
                    if (sellResult.shopOffer > 0) {
                        HorizontalDivider(color = GoldTheme.colors.borderColor)
                        val diff = sellResult.difference
                        val diffPct = sellResult.differencePercent
                        val status = sellResult.comparisonStatus

                        val (statusText, statusColor) = when (status) {
                            ComparisonStatus.MORE_EXPENSIVE -> {
                                Pair(
                                    "⚠️ تنبيه: الصائغ يبخس السعر بـ ${GoldPriceFormatter.formatWithGrouping(kotlin.math.abs(diff))} ج.م أقل من القيمة العادلة (-${String.format(Locale.US, "%.1f", kotlin.math.abs(diffPct))}%)",
                                    GoldTheme.colors.danger
                                )
                            }
                            ComparisonStatus.CHEAPER -> {
                                Pair(
                                    "🌟 الصائغ عارض أعلى من القيمة بـ ${GoldPriceFormatter.formatWithGrouping(diff)} ج.م (+${String.format(Locale.US, "%.1f", diffPct)}%)",
                                    GoldTheme.colors.success
                                )
                            }
                            else -> {
                                Pair("✅ عرض الصائغ مطابق تماماً للقيمة العادلة", GoldTheme.colors.success)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColor.copy(alpha = 0.12f))
                                .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = statusText,
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }
        }

        // أزرار الإجراءات: مشاركة التقييم (شير فقط بدون واتساب منفصل)، وحفظ في السجل، ومسح
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // زر مشاركة التقييم بالشير
                Button(
                    onClick = { viewModel.shareSellResult(context) },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(42.dp)
                        .testTag("sell_share_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldTheme.colors.surface),
                    border = BorderStroke(1.dp, GoldTheme.colors.goldPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("📤 مشاركة التقييم", color = GoldTheme.colors.goldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // حفظ في السجل
                Button(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(42.dp)
                        .testTag("sell_save_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldTheme.colors.goldPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("💾 حفظ السجل", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // مسح وإعادة ضبط
                Button(
                    onClick = { viewModel.resetSellForm() },
                    modifier = Modifier
                        .weight(0.7f)
                        .height(42.dp)
                        .testTag("sell_reset_button"),
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
