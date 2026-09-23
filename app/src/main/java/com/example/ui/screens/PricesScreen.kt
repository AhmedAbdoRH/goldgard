package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ScreenType
import com.example.ui.components.DateConnectionBar
import com.example.ui.components.FoldableUnitsAndBullionSection
import com.example.ui.components.GoldPriceTable
import com.example.ui.components.GoldScreenHeader
import com.example.ui.theme.GoldTheme
import com.example.ui.viewmodel.GoldViewModel
import com.example.util.GoldPriceFormatter

/**
 * شاشة "الأسعار" التفصيلية:
 * تعرض جدول أسعار العيارات بدقة جولدن بيلون (24، 22، 21، 18، 14)،
 * قسم الدولار ودولار الصاغة،
 * وقسم الأونصة والجنيه والسبائك بكافة الأوزان القياسية.
 */
@Composable
fun PricesScreen(
    viewModel: GoldViewModel,
    modifier: Modifier = Modifier
) {
    val goldPrices by viewModel.goldPriceResponse.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val currentDateText by viewModel.currentDateText.collectAsStateWithLifecycle()
    val currentTimeOnlyText by viewModel.currentTimeOnlyText.collectAsStateWithLifecycle()
    val lastUpdatedText by viewModel.lastUpdatedText.collectAsStateWithLifecycle()

    val p21 = goldPrices.getPairForKarat(21)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GoldTheme.colors.background)
            .testTag("prices_screen_root"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GoldScreenHeader(
                title = "أسعار الذهب والسبائك",
                onBackClick = { viewModel.navigateTo(ScreenType.HOME) }
            )
        }

        // شريط التاريخ والاتصال
        item {
            DateConnectionBar(
                currentDateText = currentDateText,
                currentTimeOnlyText = currentTimeOnlyText,
                isOnline = isOnline,
                status = goldPrices.status,
                lastUpdatedText = lastUpdatedText,
                onRefresh = { viewModel.refreshPrices(silent = false, forceRefresh = true) },
                isRefreshing = isRefreshing
            )
        }

        // بطاقة مميزة لعيار 21 (المعيار الأساسي للسوق)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GoldTheme.colors.goldPrimary.copy(alpha = 0.08f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, GoldTheme.colors.goldPrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⭐", fontSize = 16.sp)
                            Spacer(modifier = Modifier.padding(horizontal = 3.dp))
                            Text(
                                text = "عيار 21 (العيار القياسي الأكثر تداولاً)",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldTheme.colors.goldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldTheme.colors.surface)
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "شراء جديد", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = GoldTheme.colors.success)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${GoldPriceFormatter.formatWithGrouping(p21.buy)} ج.م",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldTheme.colors.success
                            )
                        }

                        Spacer(modifier = Modifier.padding(horizontal = 6.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldTheme.colors.surface)
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "بيع مستعمل", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = GoldTheme.colors.danger)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${GoldPriceFormatter.formatWithGrouping(p21.sell)} ج.م",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldTheme.colors.danger
                            )
                        }
                    }
                }
            }
        }

        // جدول العيارات بدقة جولدن بيلون
        item {
            Text(
                text = "جدول أسعار الجرام لجميع العيارات",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GoldTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            GoldPriceTable(
                goldPriceResponse = goldPrices,
                formatPrice = { viewModel.formatPrice(it) }
            )
        }

        // بطاقة أسعار صرف الدولار
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GoldTheme.colors.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldTheme.colors.borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💵", fontSize = 14.sp)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = "سعر صرف الدولار الرسمي:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary
                        )
                    }

                    Text(
                        text = "شراء ${String.format(java.util.Locale.US, "%.2f", goldPrices.usdBuyRate)} — بيع ${String.format(java.util.Locale.US, "%.2f", goldPrices.usdSellRate)} ج.م",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldTheme.colors.goldPrimary
                    )
                }
            }
        }

        // قسم الأونصة والجنيه والسبائك بكافة الأوزان
        item {
            FoldableUnitsAndBullionSection(
                goldPriceResponse = goldPrices,
                initiallyExpanded = true
            )
        }

        // زر تحديث الأسعار
        item {
            Button(
                onClick = { viewModel.refreshPrices(silent = false, forceRefresh = true) },
                enabled = !isRefreshing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("prices_refresh_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldTheme.colors.surfaceElevated,
                    contentColor = GoldTheme.colors.textPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldTheme.colors.borderColor)
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        color = GoldTheme.colors.goldPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.height(16.dp)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                    Text(text = "جاري التحديث...", fontSize = 13.sp)
                } else {
                    Text(text = "🔄 تحديث الأسعار الآن", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
