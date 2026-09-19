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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.model.PriceDirection
import com.example.model.ScreenType
import com.example.ui.components.AppHeader
import com.example.ui.components.DateConnectionBar
import com.example.ui.components.MainActionCards
import com.example.ui.components.MarketPricesSection
import com.example.ui.theme.GoldTheme
import com.example.ui.viewmodel.GoldViewModel
import java.util.Locale

/**
 * رابعًا: ترتيب الصفحة الرئيسية
 * 1. الهيدر والشعار.
 * 2. مستطيل واحد يجمع التاريخ والوقت وحالة الاتصال.
 * 3. زرا "شراء الذهب" و"بيع الذهب".
 * 4. زر "زكاة الذهب".
 * 5. قسم "سوق الذهب في مصر".
 * 6. جدول الأسعار.
 * 7. شريط التنقل السفلي (في Scaffold).
 */
@Composable
fun HomeScreen(
    viewModel: GoldViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentDateText by viewModel.currentDateText.collectAsStateWithLifecycle()
    val currentTimeOnlyText by viewModel.currentTimeOnlyText.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val lastUpdatedText by viewModel.lastUpdatedText.collectAsStateWithLifecycle()
    val goldPriceResponse by viewModel.goldPriceResponse.collectAsStateWithLifecycle()
    val priceDirection by viewModel.priceDirection.collectAsStateWithLifecycle()
    val priceDiffEGP by viewModel.priceDiffEGP.collectAsStateWithLifecycle()
    val priceDiffPercent by viewModel.priceDiffPercent.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val manualP21Input by viewModel.manualP21Input.collectAsStateWithLifecycle()
    val manualUsdMidInput by viewModel.manualUsdMidInput.collectAsStateWithLifecycle()

    var showPriceExplainerDialog by remember { mutableStateOf(false) }

    // نافذة تفاصيل وفهم التسعير
    if (showPriceExplainerDialog) {
        AlertDialog(
            onDismissRequest = { showPriceExplainerDialog = false },
            containerColor = GoldTheme.colors.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "ℹ️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "دليل فهم وحساب أسعار الذهب",
                        color = GoldTheme.colors.goldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• سعر الشراء الجديد: السعر الذي تدفعه للصائغ عند شراء الذهب الخام دون مصنعية.",
                        color = GoldTheme.colors.textPrimary,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    )
                    HorizontalDivider(color = GoldTheme.colors.borderColor)
                    Text(
                        text = "• سعر البيع المستعمل: السعر الذي يدفعه الصائغ لك عند بيع الذهب القديم له.",
                        color = GoldTheme.colors.textPrimary,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    )
                    HorizontalDivider(color = GoldTheme.colors.borderColor)
                    Text(
                        text = "• المصنعية والدمغة: تضاف للمشغولات الجديدة وتختلف حسب الورشة والعيار.",
                        color = GoldTheme.colors.textPrimary,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPriceExplainerDialog = false }) {
                    Text("فهمت", color = GoldTheme.colors.goldPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GoldTheme.colors.background),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. الهيدر والشعار
        item {
            AppHeader(
                onThemeToggle = { viewModel.toggleDarkMode() },
                isDarkMode = isDarkMode,
                onSettingsClick = { viewModel.navigateTo(ScreenType.SETTINGS) },
                onHistoryClick = { viewModel.navigateTo(ScreenType.HISTORY) }
            )
        }

        // 2. مستطيل واحد يجمع التاريخ والوقت وحالة الاتصال
        item {
            DateConnectionBar(
                currentDateText = currentDateText,
                currentTimeOnlyText = currentTimeOnlyText,
                isOnline = isOnline,
                status = goldPriceResponse.status,
                lastUpdatedText = lastUpdatedText,
                onRefresh = { viewModel.refreshPrices(silent = false, forceRefresh = true) },
                isRefreshing = isRefreshing
            )
        }

        // 3 & 4. زرا "شراء الذهب" و"بيع الذهب" ثم زر "زكاة الذهب"
        item {
            MainActionCards(
                onBuyClick = { viewModel.navigateTo(ScreenType.BUY) },
                onSellClick = { viewModel.navigateTo(ScreenType.SELL) },
                onZakatClick = { viewModel.navigateTo(ScreenType.ZAKAT) }
            )
        }

        // شريط التغير واتجاه السعر (إذا وجد تغير في السعر)
        if (priceDirection != PriceDirection.STABLE && kotlin.math.abs(priceDiffEGP) > 0.05) {
            item {
                val isUp = priceDirection == PriceDirection.UP
                val pillColor = if (isUp) GoldTheme.colors.success else GoldTheme.colors.danger
                val iconSymbol = if (isUp) "▲" else "▼"
                val actionWord = if (isUp) "ارتفاع" else "انخفاض"
                val diffSign = if (isUp) "+" else ""

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(pillColor.copy(alpha = 0.1f))
                        .border(0.8.dp, pillColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$iconSymbol $actionWord عيار 21 بمقدار $diffSign${viewModel.formatPrice(priceDiffEGP)} ج.م ($diffSign${String.format(Locale.US, "%.2f", priceDiffPercent)}%)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = pillColor
                    )
                }
            }
        }

        // محرك التسعير الوحيد المعتمد (نظام جولد بيليون)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("pricing_engine_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = GoldTheme.colors.surface),
                border = BorderStroke(1.dp, GoldTheme.colors.goldPrimary.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⚖️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "محرك التسعير المعتمد",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldTheme.colors.goldPrimary
                            )
                        }
                        Text(
                            text = "نظام جولد بيليون",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.goldPrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldTheme.colors.goldPrimary.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = "الأسعار مشتقة بنسبة النقاء من عيار 21 (نظام جولد بيليون)",
                        fontSize = 11.5.sp,
                        color = GoldTheme.colors.textSecondary
                    )

                    // الحقل الوحيد الذي يقود الأسعار: سعر عيار 21 الحالي (متوسط السوق)
                    OutlinedTextField(
                        value = manualP21Input,
                        onValueChange = { viewModel.onManualP21InputChanged(it) },
                        label = { Text("سعر عيار 21 الحالي (متوسط السوق)") },
                        trailingIcon = {
                            Text(
                                text = "ج.م",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.goldPrimary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_p21_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldTheme.colors.goldPrimary,
                            unfocusedBorderColor = GoldTheme.colors.borderColor,
                            focusedLabelColor = GoldTheme.colors.goldPrimary,
                            unfocusedLabelColor = GoldTheme.colors.textSecondary,
                            cursorColor = GoldTheme.colors.goldPrimary
                        )
                    )

                    // الحقل الاختياري: سعر صرف الدولار (متوسط السوق)
                    OutlinedTextField(
                        value = manualUsdMidInput,
                        onValueChange = { viewModel.onManualUsdMidInputChanged(it) },
                        label = { Text("سعر صرف الدولار (متوسط السوق) — اختياري") },
                        trailingIcon = {
                            Text(
                                text = "ج.م",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.textSecondary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        },
                        supportingText = {
                            Text(
                                text = "افتراضي 52.25 — لا يشترك في تسعير الذهب، يُشتق منه سعر الدولار فقط",
                                fontSize = 10.5.sp,
                                color = GoldTheme.colors.textSecondary
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_usd_mid_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldTheme.colors.goldPrimary,
                            unfocusedBorderColor = GoldTheme.colors.borderColor,
                            focusedLabelColor = GoldTheme.colors.goldPrimary,
                            unfocusedLabelColor = GoldTheme.colors.textSecondary,
                            cursorColor = GoldTheme.colors.goldPrimary
                        )
                    )

                    // زر تطبيق وتأكيد الأسعار
                    Button(
                        onClick = {
                            viewModel.applyManualPricing(
                                p21 = viewModel.parseNumber(manualP21Input),
                                usdMid = viewModel.parseNumber(manualUsdMidInput)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("apply_manual_pricing_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldTheme.colors.goldPrimary,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "تطبيق واعتماد الأسعار",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 5 & 6. قسم "سوق الذهب في مصر" وجدول الأسعار
        item {
            MarketPricesSection(
                goldPriceResponse = goldPriceResponse,
                formatPrice = { viewModel.formatPrice(it) },
                onRefresh = { viewModel.refreshPrices(silent = false, forceRefresh = true) },
                isRefreshing = isRefreshing
            )
        }

        // أزرار سريعة مساعدة (تحديث فوري + شرح الأسعار)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.refreshPrices(silent = false, forceRefresh = true) },
                    enabled = !isRefreshing,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("refresh_prices_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldTheme.colors.surface,
                        contentColor = GoldTheme.colors.textPrimary
                    ),
                    border = BorderStroke(1.dp, GoldTheme.colors.borderColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            color = GoldTheme.colors.goldPrimary,
                            strokeWidth = 1.8.dp,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "جاري التحديث...",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary
                        )
                    } else {
                        Text(
                            text = "🔄 تحديث فوري",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary
                        )
                    }
                }

                Button(
                    onClick = { showPriceExplainerDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("price_explainer_button"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GoldTheme.colors.borderColor),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldTheme.colors.surface,
                        contentColor = GoldTheme.colors.textSecondary
                    )
                ) {
                    Text(
                        text = "ℹ️ تفاصيل التسعير",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldTheme.colors.textSecondary
                    )
                }
            }
        }

        // بطاقة دليل كشف الغش والتلاعب 🛡️
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { viewModel.navigateTo(ScreenType.TIPS) }
                    .testTag("anti_fraud_tips_hero_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GoldTheme.colors.surface),
                border = BorderStroke(1.dp, GoldTheme.colors.goldPrimary.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GoldTheme.colors.goldPrimary.copy(alpha = 0.12f))
                                .border(1.dp, GoldTheme.colors.goldPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🛡️", fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "دليل كشف الغش والتلاعب",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.goldPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "فاحص المصنعية، كشف فخ الفصوص، وميزان الصاغة",
                                fontSize = 11.sp,
                                color = GoldTheme.colors.textSecondary
                            )
                        }
                    }

                    Text(
                        text = "👈",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
