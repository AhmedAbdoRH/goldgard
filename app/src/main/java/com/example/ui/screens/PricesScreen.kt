package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import java.util.Locale

@Composable
fun PricesScreen(
    viewModel: GoldViewModel,
    modifier: Modifier = Modifier
) {
    val currentDateText by viewModel.currentDateText.collectAsStateWithLifecycle()
    val currentTimeOnlyText by viewModel.currentTimeOnlyText.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val lastUpdatedText by viewModel.lastUpdatedText.collectAsStateWithLifecycle()
    val goldPriceResponse by viewModel.goldPriceResponse.collectAsStateWithLifecycle()
    val countdownSeconds by viewModel.countdownSeconds.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    val formattedOunceUsd = if (goldPriceResponse.ouncePriceUsd > 0) {
        String.format(Locale.US, "%.2f", goldPriceResponse.ouncePriceUsd)
    } else {
        String.format(Locale.US, "%.2f", goldPriceResponse.ouncePrice)
    }
    val formattedUsdRate = com.example.util.GoldPriceFormatter.formatUsdBuySell(
        goldPriceResponse.usdBuyRate,
        goldPriceResponse.usdSellRate
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GoldTheme.colors.background),
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Screen Header
        item {
            GoldScreenHeader(
                title = "أسعار الذهب لحظيًا",
                onBackClick = { viewModel.navigateTo(ScreenType.HOME) },
                onThemeToggle = { viewModel.toggleDarkMode() },
                isDarkMode = isDarkMode,
                testTagPrefix = "prices_screen"
            )
        }

        // 2. Date and Connection Status Bar
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

        // 3. Global Market Summary (الأونصة عالميًا وسعر الصرف)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = GoldTheme.colors.surface),
                border = BorderStroke(1.dp, GoldTheme.colors.borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ounce in USD
                    Column {
                        Text(
                            text = "الأونصة عالميًا",
                            fontSize = 11.sp,
                            color = GoldTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$$formattedOunceUsd",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.goldPrimary
                        )
                    }

                    // USD to EGP rate
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "سعر الدولار",
                            fontSize = 11.sp,
                            color = GoldTheme.colors.textSecondary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$formattedUsdRate ج.م",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Auto refresh countdown
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldTheme.colors.surfaceElevated)
                            .border(1.dp, GoldTheme.colors.borderColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "⏱️", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${countdownSeconds}ث",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary
                        )
                    }
                }
            }
        }

        // 4. Gold Prices Table & Bullion Section
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
                        text = "سوق الذهب في مصر",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldTheme.colors.goldPrimary
                    )

                    if (goldPriceResponse.isManualCalibration) {
                        Text(
                            text = "⚙️ معايرة يدوية",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.goldPrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldTheme.colors.goldPrimary.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                val lastUpdate = goldPriceResponse.lastUpdatedLocal.ifBlank { "الآن" }
                if (goldPriceResponse.isStale || goldPriceResponse.status == "cached_failed") {
                    Text(
                        text = "🔴 غير حية — آخر تحديث: $lastUpdate",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldTheme.colors.danger
                    )
                } else {
                    Text(
                        text = "آخر تحديث: $lastUpdate — الدولار: $formattedUsdRate ج.م",
                        fontSize = 11.sp,
                        color = GoldTheme.colors.textSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                GoldPriceTable(
                    goldPriceResponse = goldPriceResponse,
                    formatPrice = { viewModel.formatPrice(it) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                FoldableUnitsAndBullionSection(
                    goldPriceResponse = goldPriceResponse,
                    initiallyExpanded = true
                )
            }
        }

        // 5. Instant Refresh Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Button(
                    onClick = { viewModel.refreshPrices(silent = false, forceRefresh = true) },
                    enabled = !isRefreshing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("prices_refresh_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldTheme.colors.surface,
                        contentColor = GoldTheme.colors.textPrimary
                    ),
                    border = BorderStroke(1.dp, GoldTheme.colors.borderColor),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            color = GoldTheme.colors.goldPrimary,
                            strokeWidth = 1.8.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "جاري جلب الأسعار...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "🔄 تحديث الأسعار فوريًا",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 6. Educational Note on Making Charges & Spread
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GoldTheme.colors.surface),
                border = BorderStroke(0.8.dp, GoldTheme.colors.borderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💡", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تنبيه هام حول أسعار الصاغة",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.goldPrimary
                        )
                    }
                    Text(
                        text = "• الأسعار المعروضة أعلاه هي أسعار الذهب الخام في البورصة دون احتساب المصنعية أو الضريبة أو الدمغة.\n• عند شراء المشغولات الجديدة يضاف للمصنعية بين 80 إلى 250 ج.م للجرام حسب التصميم والورشة.\n• عند بيع الذهب القديم المستعمل لا يدفع المشتري أي مصنعية ويتم الخصم بناءً على سعر البيع المستعمل المعلن.",
                        fontSize = 11.5.sp,
                        color = GoldTheme.colors.textSecondary,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
