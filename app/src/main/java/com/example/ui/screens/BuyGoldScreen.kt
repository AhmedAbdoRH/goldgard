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
    val buyResult by viewModel.buyResult.collectAsStateWithLifecycle()

    var showSaveDialog by remember { mutableStateOf(false) }
    var saveNote by remember { mutableStateOf("") }
    var makingInputMode by remember { mutableStateOf("PERCENT") } // "PERCENT" or "AMOUNT"

    val availableKarats = listOf(24, 22, 21, 18, 14)

    // تقييم المصنعية
    val makingPctVal = buyMakingPercent.toDoubleOrNull() ?: 0.0
    val makingBadgeInfo = when {
        makingPctVal <= 0.0 -> null
        makingPctVal < 5.0 -> Triple("مصنعية ممتازة (سبائك/استثمار)", GoldTheme.colors.success, "✨")
        makingPctVal <= 9.0 -> Triple("مصنعية معتادة ومعقولة", GoldTheme.colors.goldPrimary, "✅")
        makingPctVal <= 13.0 -> Triple("مصنعية مرتفعة (مصوغات فاخرة)", GoldTheme.colors.warning, "⚠️")
        else -> Triple("مصنعية عالية ومبالغ فيها!", GoldTheme.colors.danger, "🚨")
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = GoldTheme.colors.surface,
            title = {
                Text(
                    text = "حفظ عملية الشراء في السجل",
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
                        placeholder = { Text("مثال: خاتم، سبيكة، سوار...", fontSize = 12.sp) },
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
                        viewModel.saveBuyTransaction(saveNote)
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
            .testTag("buy_screen_root"),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // العنوان وزر الرجوع
        item {
            GoldScreenHeader(
                title = "حاسبة شراء الذهب والمشغولات",
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
                        val isSelected = karat == buyKarat
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
                                .clickable { viewModel.onBuyKaratSelected(karat) }
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

        // إدخال الوزن وسعر الجرام (بشكل متماسك ونظيف)
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
                    // وزن الذهب بالجرام
                    Column {
                        Text(
                            text = "الوزن بالجرام:",
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = buyWeight,
                            onValueChange = { viewModel.onBuyWeightChanged(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("buy_weight_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            placeholder = { Text("أدخل الوزن بالجرام...", color = GoldTheme.colors.textSecondary, fontSize = 12.5.sp) },
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

                    // سعر الجرام للعيار
                    Column {
                        Text(
                            text = "سعر جرام عيار $buyKarat (بدون مصنعية):",
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = buyGramPrice,
                            onValueChange = { viewModel.onBuyGramPriceChanged(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("buy_gram_price_input"),
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

        // المصنعية والدمغة (تمام)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المصنعية والدمغة:",
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.goldPrimary,
                            fontSize = 13.sp
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldTheme.colors.background)
                                .border(0.8.dp, GoldTheme.colors.borderColor, RoundedCornerShape(6.dp))
                        ) {
                            Text(
                                text = "نسبة %",
                                modifier = Modifier
                                    .clickable { makingInputMode = "PERCENT" }
                                    .background(if (makingInputMode == "PERCENT") GoldTheme.colors.goldPrimary else Color.Transparent)
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (makingInputMode == "PERCENT") Color.Black else GoldTheme.colors.textSecondary
                            )
                            Text(
                                text = "مبلغ ج.م",
                                modifier = Modifier
                                    .clickable { makingInputMode = "AMOUNT" }
                                    .background(if (makingInputMode == "AMOUNT") GoldTheme.colors.goldPrimary else Color.Transparent)
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (makingInputMode == "AMOUNT") Color.Black else GoldTheme.colors.textSecondary
                            )
                        }
                    }

                    if (makingInputMode == "PERCENT") {
                        OutlinedTextField(
                            value = buyMakingPercent,
                            onValueChange = { viewModel.onBuyMakingPercentChanged(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("buy_making_percent_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            trailingIcon = {
                                Text(
                                    text = "%",
                                    color = GoldTheme.colors.goldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(end = 10.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldTheme.colors.goldPrimary,
                                unfocusedBorderColor = GoldTheme.colors.borderColor,
                                focusedTextColor = GoldTheme.colors.textPrimary,
                                unfocusedTextColor = GoldTheme.colors.textPrimary
                            )
                        )
                        // القيمة المادية بالفلوس واضحة قدام المستخدم
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldTheme.colors.background)
                                .border(0.8.dp, GoldTheme.colors.borderColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "القيمة المادية للمصنعية:",
                                fontSize = 11.5.sp,
                                color = GoldTheme.colors.textSecondary
                            )
                            Text(
                                text = "${buyMakingAmountPerGram.ifBlank { "0" }} ج.م / للجرام",
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.goldPrimary,
                                fontSize = 12.5.sp
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = buyMakingAmountPerGram,
                            onValueChange = { viewModel.onBuyMakingAmountChanged(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("buy_making_amount_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            trailingIcon = {
                                Text(
                                    text = "ج.م/جم",
                                    color = GoldTheme.colors.goldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(end = 10.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldTheme.colors.goldPrimary,
                                unfocusedBorderColor = GoldTheme.colors.borderColor,
                                focusedTextColor = GoldTheme.colors.textPrimary,
                                unfocusedTextColor = GoldTheme.colors.textPrimary
                            )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldTheme.colors.background)
                                .border(0.8.dp, GoldTheme.colors.borderColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "النسبة المعادلة من سعر الذهب:",
                                fontSize = 11.5.sp,
                                color = GoldTheme.colors.textSecondary
                            )
                            Text(
                                text = "${buyMakingPercent.ifBlank { "0" }}%",
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.goldPrimary,
                                fontSize = 12.5.sp
                            )
                        }
                    }

                    // شارة تقييم المصنعية
                    makingBadgeInfo?.let { (text, color, icon) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(color.copy(alpha = 0.1f))
                                .border(0.8.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = icon, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                        }
                    }

                    // حقل الدمغة والضريبة للجرام
                    Column {
                        Text(
                            text = "الدمغة والضريبة للجرام (ج.م):",
                            fontSize = 12.sp,
                            color = GoldTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = buyStampFee,
                            onValueChange = { viewModel.onBuyStampFeeChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            trailingIcon = {
                                Text("ج.م/جم", color = GoldTheme.colors.textSecondary, fontSize = 11.sp, modifier = Modifier.padding(end = 10.dp))
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

        // مقارنة السعر المعروض مع المحل (بدون اسم المحل وبشكل مباشر)
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
                        text = "السعر الذي طلبه الصائغ للمقارنة (اختياري):",
                        fontWeight = FontWeight.Bold,
                        color = GoldTheme.colors.textPrimary,
                        fontSize = 12.5.sp
                    )
                    OutlinedTextField(
                        value = buyShopQuotedPrice,
                        onValueChange = { viewModel.onBuyShopPriceChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("buy_shop_price_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        placeholder = { Text("أدخل إجمالي المبلغ الذي طلبه الصائغ...", fontSize = 11.5.sp) },
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

        // تفاصيل الفاتورة: السعر الإجمالي العادل أول حاجة فوق بشكل بارز وواضح جداً
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("buy_calculation_result_card"),
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
                    // السعر الإجمالي العادل: الكلمة فوق وتحت منها الرقم وواضح وزي الفل
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
                            text = "السعر الإجمالي العادل (المفروض تدفعه)",
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${GoldPriceFormatter.formatWithGrouping(buyResult.totalFairPrice)} ج.م",
                            fontWeight = FontWeight.Black,
                            color = GoldTheme.colors.goldPrimary,
                            fontSize = 25.sp
                        )
                    }

                    HorizontalDivider(color = GoldTheme.colors.borderColor)

                    // تفاصيل الفاتورة تحت السعر الإجمالي
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("سعر الجرام الأساسي:", color = GoldTheme.colors.textSecondary, fontSize = 12.sp)
                        Text(
                            text = "${buyGramPrice} ج.م",
                            fontWeight = FontWeight.SemiBold,
                            color = GoldTheme.colors.textPrimary,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("قيمة الذهب الخام:", color = GoldTheme.colors.textSecondary, fontSize = 12.sp)
                        Text(
                            text = "${GoldPriceFormatter.formatWithGrouping(buyResult.rawGoldPrice)} ج.م",
                            fontWeight = FontWeight.SemiBold,
                            color = GoldTheme.colors.textPrimary,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("إجمالي المصنعية (${buyMakingPercent}%):", color = GoldTheme.colors.textSecondary, fontSize = 12.sp)
                        Text(
                            text = "${GoldPriceFormatter.formatWithGrouping(buyResult.makingTotal)} ج.م",
                            fontWeight = FontWeight.SemiBold,
                            color = GoldTheme.colors.textPrimary,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("إجمالي الدمغة والضريبة:", color = GoldTheme.colors.textSecondary, fontSize = 12.sp)
                        Text(
                            text = "${GoldPriceFormatter.formatWithGrouping(buyResult.stampTotal)} ج.م",
                            fontWeight = FontWeight.SemiBold,
                            color = GoldTheme.colors.textPrimary,
                            fontSize = 12.sp
                        )
                    }

                    // مقارنة سعر المحل إن وُجد
                    if (buyResult.shopPrice > 0) {
                        HorizontalDivider(color = GoldTheme.colors.borderColor)
                        val diff = buyResult.difference
                        val diffPct = buyResult.differencePercent
                        val status = buyResult.comparisonStatus

                        val (statusText, statusColor) = when (status) {
                            ComparisonStatus.MORE_EXPENSIVE -> {
                                Pair(
                                    "⚠️ المحل يطلب زيادة قدرها ${GoldPriceFormatter.formatWithGrouping(diff)} ج.م (+${String.format(Locale.US, "%.1f", diffPct)}%)",
                                    GoldTheme.colors.danger
                                )
                            }
                            ComparisonStatus.CHEAPER -> {
                                Pair(
                                    "🌟 الصائغ أرخص من السعر العادل بـ ${GoldPriceFormatter.formatWithGrouping(kotlin.math.abs(diff))} ج.م (-${String.format(Locale.US, "%.1f", kotlin.math.abs(diffPct))}%)",
                                    GoldTheme.colors.success
                                )
                            }
                            else -> {
                                Pair("✅ سعر المحل مطابق تماماً للسعر العادل", GoldTheme.colors.success)
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

        // أزرار الإجراءات: مشاركة الفاتورة (شير فقط بدون واتساب منفصل)، وحفظ في السجل، ومسح
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // زر مشاركة الفاتورة بالشير
                Button(
                    onClick = { viewModel.shareBuyResult(context) },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(42.dp)
                        .testTag("buy_share_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldTheme.colors.surface),
                    border = BorderStroke(1.dp, GoldTheme.colors.goldPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("📤 مشاركة الفاتورة", color = GoldTheme.colors.goldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // حفظ في السجل
                Button(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(42.dp)
                        .testTag("buy_save_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldTheme.colors.goldPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("💾 حفظ السجل", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // إعادة ضبط ومسح
                Button(
                    onClick = { viewModel.resetBuyForm() },
                    modifier = Modifier
                        .weight(0.7f)
                        .height(42.dp)
                        .testTag("buy_reset_button"),
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
