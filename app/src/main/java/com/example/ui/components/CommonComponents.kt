package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ScreenType
import com.example.ui.theme.*
import com.example.ui.theme.AlertRedBorder
import com.example.ui.theme.AlertRedText
import com.example.ui.theme.BuyBlueGradientBrush
import com.example.ui.theme.BuyGreenGradientBrush
import com.example.ui.theme.DarkBorderGold
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldShimmer
import com.example.ui.theme.GoldWarm
import com.example.ui.theme.HistoryCyanGradientBrush
import com.example.ui.theme.PurpleGradientBrush
import com.example.ui.theme.PurpleGradientStart
import com.example.ui.theme.SellRedGradientBrush
import com.example.ui.theme.SuccessGreenBg
import com.example.ui.theme.SuccessGreenBorder
import com.example.ui.theme.SuccessGreenText
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.ZakatGradientBrush

/**
 * خامسًا: الهيدر (AppHeader)
 * - شعار التطبيق (img_gold_shield_logo) واسم التطبيق "حارس الذهب".
 * - خلفية داكنة متناسقة في الوضع الليلي / فاتحة في النهاري.
 * - حواف دائرية.
 * - زر واضح للتبديل بين الوضع الليلي والنهاري (🌙 / ☀️) وزر الإعدادات والسجل.
 * - عدم زيادة ارتفاع الهيدر بلا سبب.
 * - المحافظة على اتجاه RTL.
 */
@Composable
fun AppHeader(
    onThemeToggle: () -> Unit = {},
    isDarkMode: Boolean = true,
    onSettingsClick: (() -> Unit)? = null,
    onHistoryClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    // Optional compatibility params
    currentDateText: String = "",
    currentTimeOnlyText: String = "",
    currentTimeText: String = "",
    status: String = "live",
    onRefresh: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(GoldTheme.colors.surface)
            .border(
                1.dp,
                GoldTheme.colors.borderColor,
                RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Right / Start (RTL): Logo + App Title "حارس الذهب"
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(GoldTheme.colors.surfaceElevated)
                        .border(1.dp, GoldTheme.colors.goldPrimary.copy(alpha = 0.5f), RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_gold_shield_logo),
                        contentDescription = "شعار درع حارس الذهب",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "حارس الذهب",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldTheme.colors.goldPrimary,
                    letterSpacing = 0.3.sp
                )
            }

            // Left / End (RTL): Action icons (Theme Toggle, History, Settings)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Theme Toggle Button (🌙 / ☀️)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldTheme.colors.surfaceElevated)
                        .border(1.dp, GoldTheme.colors.borderColor, RoundedCornerShape(10.dp))
                        .clickable { onThemeToggle() }
                        .testTag("theme_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isDarkMode) "☀️" else "🌙",
                        fontSize = 16.sp
                    )
                }

                // History shortcut
                if (onHistoryClick != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GoldTheme.colors.surfaceElevated)
                            .border(1.dp, GoldTheme.colors.borderColor, RoundedCornerShape(10.dp))
                            .clickable { onHistoryClick() }
                            .testTag("header_history_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📜", fontSize = 15.sp)
                    }
                }

                // Settings shortcut
                if (onSettingsClick != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GoldTheme.colors.surfaceElevated)
                            .border(1.dp, GoldTheme.colors.borderColor, RoundedCornerShape(10.dp))
                            .clickable { onSettingsClick() }
                            .testTag("header_settings_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "⚙️", fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

/**
 * سادسًا: دمج التاريخ والوقت والاتصال (DateConnectionBar)
 * مستطيل واحد فقط يجمع:
 * - اليوم، التاريخ، الوقت
 * - حالة الاتصال ومؤشر ملون
 * - زر التحديث الفوري
 */
@Composable
fun DateConnectionBar(
    currentDateText: String,
    currentTimeOnlyText: String,
    isOnline: Boolean,
    status: String,
    lastUpdatedText: String = "",
    onRefresh: (() -> Unit)? = null,
    isRefreshing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (statusLabel, statusColor) = when {
        !isOnline -> Pair("غير متاح", GoldTheme.colors.danger)
        status == "live" -> Pair("متصل", GoldTheme.colors.success)
        status == "stale" || status == "market_closed" -> Pair("السوق مغلق", GoldTheme.colors.warning)
        status == "cached" || status == "cached_failed" -> Pair(if (lastUpdatedText.isNotEmpty()) "آخر تحديث: $lastUpdatedText" else "قديم", GoldTheme.colors.warning)
        else -> Pair("متصل", GoldTheme.colors.success)
    }

    val displayDate = if (currentDateText.isNotBlank()) currentDateText else "السبت، 14 سبتمبر"
    val displayTime = if (currentTimeOnlyText.isNotBlank()) currentTimeOnlyText else "11:15 ص"
    val fullDateTime = "$displayDate - $displayTime"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GoldTheme.colors.surface)
            .border(1.dp, GoldTheme.colors.borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp)
            .testTag("date_connection_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Right (Start RTL): Date & Time in a single clean horizontal line
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(text = "📅", fontSize = 11.5.sp)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = fullDateTime,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldTheme.colors.textPrimary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Left (End RTL): Connection indicator + Refresh button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Connection badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(0.8.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = statusLabel,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        maxLines = 1
                    )
                }

                // Refresh button
                if (onRefresh != null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(GoldTheme.colors.surfaceElevated)
                            .border(1.dp, GoldTheme.colors.borderColor, RoundedCornerShape(7.dp))
                            .clickable(enabled = !isRefreshing) { onRefresh() }
                            .testTag("bar_refresh_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                color = GoldTheme.colors.goldPrimary,
                                strokeWidth = 1.5.dp,
                                modifier = Modifier.size(12.dp)
                            )
                        } else {
                            Text(text = "🔄", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * سابعًا: بطاقات الإجراءات الرئيسية
 * الصف الأول: بطاقتان متساويتان جنباً إلى جنب (شراء الذهب | بيع الذهب)
 * الصف الثاني: زر زكاة الذهب بعرض الشاشة
 */
@Composable
fun MainActionCards(
    onBuyClick: () -> Unit,
    onSellClick: () -> Unit,
    onZakatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = GoldTheme.colors.isDark

    val buyBg = if (isDark) Color(0xFF0D281A) else Color(0xFFE8F5E9)
    val buyBorder = if (isDark) Color(0xFF10B981).copy(alpha = 0.6f) else Color(0xFF81C784)
    val buyTextColor = if (isDark) Color(0xFF34D399) else Color(0xFF1B5E20)

    val sellBg = if (isDark) Color(0xFF2A1014) else Color(0xFFFFEBEE)
    val sellBorder = if (isDark) Color(0xFFEF4444).copy(alpha = 0.6f) else Color(0xFFE57373)
    val sellTextColor = if (isDark) Color(0xFFF87171) else Color(0xFFB71C1C)

    val zakatBg = if (isDark) Color(0xFF261D09) else Color(0xFFFEF9C3)
    val zakatBorder = if (isDark) Color(0xFFF59E0B).copy(alpha = 0.6f) else Color(0xFFFCD34D)
    val zakatTextColor = if (isDark) Color(0xFFFBBF24) else Color(0xFF92400E)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // الصف الأول: شراء الذهب | بيع الذهب
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // بطاقة شراء الذهب
            ActionCardItem(
                title = "شراء الذهب",
                icon = "🛍️",
                backgroundColor = buyBg,
                borderColor = buyBorder,
                textColor = buyTextColor,
                onClick = onBuyClick,
                modifier = Modifier.weight(1f),
                testTag = "main_action_buy_gold"
            )

            // بطاقة بيع الذهب
            ActionCardItem(
                title = "بيع الذهب",
                icon = "💵",
                backgroundColor = sellBg,
                borderColor = sellBorder,
                textColor = sellTextColor,
                onClick = onSellClick,
                modifier = Modifier.weight(1f),
                testTag = "main_action_sell_gold"
            )
        }

        // الصف الثاني: زر زكاة الذهب
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(zakatBg)
                .border(1.5.dp, zakatBorder, RoundedCornerShape(12.dp))
                .clickable { onZakatClick() }
                .testTag("main_action_zakat_gold"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "🕌", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "زكاة الذهب فقط لا غير",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = zakatTextColor
                )
            }
        }
    }
}

@Composable
private fun ActionCardItem(
    title: String,
    icon: String,
    backgroundColor: Color,
    borderColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

/**
 * عاشرًا: قسم سوق الذهب في مصر
 * الحادي عشر: جدول الأسعار (GoldPriceTable)
 */
@Composable
fun MarketPricesSection(
    goldPriceResponse: com.example.model.GoldPriceResponse,
    formatPrice: (Double) -> String,
    onRefresh: (() -> Unit)? = null,
    isRefreshing: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // عنوان القسم
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

        // بصمة الحالة: آخر تحديث
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
                text = "آخر تحديث: $lastUpdate",
                fontSize = 11.5.sp,
                color = GoldTheme.colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // بطاقات / جدول العيارات (24، 22، 21 ⭐، 18، 14)
        GoldPriceTable(
            goldPriceResponse = goldPriceResponse,
            formatPrice = formatPrice
        )

        Spacer(modifier = Modifier.height(8.dp))

        // بطاقة أسعار الدولار تحت جدول العيارات: سعر الدولار + دولار الصاغة
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = GoldTheme.colors.surface),
            border = BorderStroke(1.dp, GoldTheme.colors.borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // سعر الدولار
                Column(modifier = Modifier.weight(1.2f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💵", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "سعر الدولار",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "شراء ${String.format(java.util.Locale.US, "%.2f", goldPriceResponse.usdBuyRate)} — بيع ${String.format(java.util.Locale.US, "%.2f", goldPriceResponse.usdSellRate)} ج.م",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldTheme.colors.goldPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                        .background(GoldTheme.colors.borderColor)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // دولار الصاغة
                Column(
                    modifier = Modifier.weight(0.9f),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚖️", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "دولار الصاغة",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldTheme.colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${goldPriceResponse.saghaUsdRate} ج.م",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldTheme.colors.goldPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // قسم قابل للطي: "الأونصة والجنيه والسبائك ⏷" (يبدأ مطوياً، بحاوية سكرول عمودية)
        FoldableUnitsAndBullionSection(
            goldPriceResponse = goldPriceResponse
        )
    }
}

/**
 * الحادي عشر: جدول أسعار العيارات (GoldPriceTable)
 * رأس الجدول: العيار | شراء جديد | بيع مستعمل
 * الصفوف: 24، 22، 21 ⭐، 18، 14
 */
@Composable
fun GoldPriceTable(
    goldPriceResponse: com.example.model.GoldPriceResponse,
    formatPrice: (Double) -> String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GoldTheme.colors.surface),
        border = BorderStroke(1.dp, GoldTheme.colors.borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Header Row: العيار | شراء جديد | بيع مستعمل
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(GoldTheme.colors.surfaceElevated)
                    .padding(vertical = 7.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "العيار",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = GoldTheme.colors.textPrimary,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "شراء جديد",
                    modifier = Modifier.weight(1.3f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = GoldTheme.colors.success,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "بيع مستعمل",
                    modifier = Modifier.weight(1.3f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = GoldTheme.colors.danger,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Rows بالترتيب القياسي: 24، 22، 21 ⭐، 18، 14
            val standardKarats = listOf(24, 22, 21, 18, 14)
            standardKarats.forEachIndexed { index, karat ->
                val pair = goldPriceResponse.getPairForKarat(karat)
                val isBenchmark = karat == 21

                // صرامة العرض: شراء دايمًا الرقم الأعلى (أخضر)، بيع دايمًا الرقم الأدنى (أحمر)
                val rawBuy = pair.buy
                val rawSell = pair.sell
                val safeBuy = maxOf(rawBuy, rawSell)
                val safeSell = minOf(rawBuy, rawSell)
                if (safeBuy < safeSell) {
                    throw AssertionError("خطأ في التسعير: سعر الشراء ($safeBuy) أقل من سعر البيع ($safeSell) لعيار $karat")
                }

                val gap = safeBuy - safeSell
                val mid = (safeBuy + safeSell) / 2.0
                val gapPercent = if (mid > 0.0) (gap / mid) * 100.0 else 0.0

                val gapText = "الفارق: ${Math.round(gap)} ج.م (${String.format(java.util.Locale.US, "%.2f", gapPercent)}%)"

                GoldPriceRow(
                    label = "عيار $karat",
                    isBenchmark = isBenchmark,
                    buyText = "${Math.round(safeBuy)} ج.م",
                    sellText = "${Math.round(safeSell)} ج.م",
                    gapText = gapText
                )

                if (index < standardKarats.size - 1) {
                    HorizontalDivider(
                        color = GoldTheme.colors.borderColor.copy(alpha = 0.5f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * قسم قابل للطي بعنوان "الأونصة والجنيه والسبائك ⏷"
 * يبدأ مطويًا (collapsed).
 * المحتوى جوه حاوية max-height مع scroll عمودي، لترتيب:
 * 1. الأونصة = جرام 24 × 31.1035
 * 2. جنيه الذهب = جرام 21 × 8
 * 3. السبائك عيار 24 للأوزان القياسية (1، 2.5، 5، 10، 20، 50، 100، 250 جرام)
 * التقريب موحد: الأونصة والجنيه والسبائك بلا كسور (أقرب جنيه صحيح).
 */
@Composable
fun FoldableUnitsAndBullionSection(
    goldPriceResponse: com.example.model.GoldPriceResponse,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false
) {
    var isExpanded by rememberSaveable { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GoldTheme.colors.surface),
        border = BorderStroke(1.dp, GoldTheme.colors.borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // شريط العنوان القابل للضغط لفتح وطي القسم
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(GoldTheme.colors.surfaceElevated)
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("toggle_bullion_section"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🪙", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isExpanded) "الأونصة والجنيه والسبائك ⏶" else "الأونصة والجنيه والسبائك ⏷",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldTheme.colors.goldPrimary
                    )
                }

                Text(
                    text = if (isExpanded) "إخفاء" else "عرض التفاصيل",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = GoldTheme.colors.textSecondary
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    // Header Row: الصنف | شراء جديد | بيع مستعمل
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldTheme.colors.surfaceElevated.copy(alpha = 0.7f))
                            .padding(vertical = 6.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الصنف / الوزن",
                            modifier = Modifier.weight(1.2f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = GoldTheme.colors.textPrimary
                        )
                        Text(
                            text = "شراء جديد",
                            modifier = Modifier.weight(1.2f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = GoldTheme.colors.success,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "بيع مستعمل",
                            modifier = Modifier.weight(1.2f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = GoldTheme.colors.danger,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // حاوية سكرول عمودية بارتفاع أقصى
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        val g24 = goldPriceResponse.gram24
                        val g21 = goldPriceResponse.gram21
                        val margin = goldPriceResponse.bullionMarginPerGram

                        // 1. الأونصة = جرام 24 × 31.1035
                        val ounceBuy = g24.buy * 31.1035
                        val ounceSell = g24.sell * 31.1035
                        GoldPriceRow(
                            label = "الأونصة (31.10 جم)",
                            isBenchmark = false,
                            buyText = "${com.example.util.GoldPriceFormatter.formatWhole(ounceBuy)} ج.م",
                            sellText = "${com.example.util.GoldPriceFormatter.formatWhole(ounceSell)} ج.م"
                        )

                        HorizontalDivider(
                            color = GoldTheme.colors.borderColor.copy(alpha = 0.5f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // 2. جنيه الذهب = جرام 21 × 8
                        val poundBuy = g21.buy * 8.0
                        val poundSell = g21.sell * 8.0
                        GoldPriceRow(
                            label = "جنيه ذهب (8 جم)",
                            isBenchmark = false,
                            buyText = "${com.example.util.GoldPriceFormatter.formatWhole(poundBuy)} ج.م",
                            sellText = "${com.example.util.GoldPriceFormatter.formatWhole(poundSell)} ج.م"
                        )

                        HorizontalDivider(
                            color = GoldTheme.colors.borderColor.copy(alpha = 0.5f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // 3. السبائك عيار 24 بالأوزان القياسية
                        val standardWeights = listOf(
                            1.0 to "سبيكة 1 جرام",
                            2.5 to "سبيكة 2.5 جرام",
                            5.0 to "سبيكة 5 جرام",
                            10.0 to "سبيكة 10 جرام",
                            20.0 to "سبيكة 20 جرام",
                            50.0 to "سبيكة 50 جرام",
                            100.0 to "سبيكة 100 جرام",
                            250.0 to "سبيكة 250 جرام"
                        )

                        standardWeights.forEachIndexed { idx, (weight, name) ->
                            val bullionBuy = (g24.buy * weight) + (margin * weight)
                            val bullionSell = (g24.sell * weight)

                            GoldPriceRow(
                                label = name,
                                isBenchmark = false,
                                buyText = "${com.example.util.GoldPriceFormatter.formatWhole(bullionBuy)} ج.م",
                                sellText = "${com.example.util.GoldPriceFormatter.formatWhole(bullionSell)} ج.م"
                            )

                            if (idx < standardWeights.size - 1) {
                                HorizontalDivider(
                                    color = GoldTheme.colors.borderColor.copy(alpha = 0.35f),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoldPriceRow(
    label: String,
    isBenchmark: Boolean,
    buyText: String,
    sellText: String,
    gapText: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isBenchmark) GoldTheme.colors.goldPrimary.copy(alpha = 0.08f)
                else Color.Transparent
            )
            .border(
                width = if (isBenchmark) 0.8.dp else 0.dp,
                color = if (isBenchmark) GoldTheme.colors.goldPrimary.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(vertical = 6.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Karat label + ⭐ with 6-8px space, plus gap below
        Column(
            modifier = Modifier.weight(1.1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isBenchmark) FontWeight.Black else FontWeight.Bold,
                    color = if (isBenchmark) GoldTheme.colors.goldPrimary else GoldTheme.colors.textPrimary
                )
                if (isBenchmark) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "⭐", fontSize = 11.sp)
                }
            }
            if (gapText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = gapText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = GoldTheme.colors.textSecondary
                )
            }
        }

        // Buy price (Green, horizontal format)
        Text(
            text = buyText,
            modifier = Modifier.weight(1.3f),
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = GoldTheme.colors.success,
            textAlign = TextAlign.Center
        )

        // Sell price (Red, horizontal format)
        Text(
            text = sellText,
            modifier = Modifier.weight(1.3f),
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = GoldTheme.colors.danger,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Backward compatibility CompactStatusBar
 */
@Composable
fun CompactStatusBar(
    currentTimeText: String,
    status: String,
    lastChecked: String = "",
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    DateConnectionBar(
        currentDateText = currentTimeText.substringBefore(" - "),
        currentTimeOnlyText = currentTimeText.substringAfter(" - ", ""),
        isOnline = status != "failed" && status != "anomaly",
        status = status,
        lastUpdatedText = lastChecked,
        onRefresh = onRefresh,
        modifier = modifier
    )
}

/**
 * Live Market Trend Card with smooth glowing golden line chart
 * matches the Live Market Trend section in the reference screenshot
 */
@Composable
fun LiveMarketTrendCard(
    currentBenchmarkPrice: Double = 6315.0,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderGold),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Live Market Trend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📈",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Live Market Trend",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F3A2E))
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "●",
                            color = Color(0xFF10B981),
                            fontSize = 8.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = "مباشر الصاغة",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6EE7B7)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Chart Area with price points and curve
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0B101D))
                    .border(1.dp, Color(0xFF1C273F), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                // Price indicator lines on right
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 2.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "6,350 ج.م",
                        fontSize = 9.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "6,315 ج.م",
                        fontSize = 9.sp,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "6,280 ج.م",
                        fontSize = 9.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Drawing the wave trend line with golden gradient fill
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .height(85.dp)
                        .align(Alignment.CenterStart)
                ) {
                    val w = size.width
                    val h = size.height

                    // Trend points (simulating gold market fluctuations)
                    val points = listOf(
                        Offset(0f, h * 0.72f),
                        Offset(w * 0.12f, h * 0.65f),
                        Offset(w * 0.22f, h * 0.78f),
                        Offset(w * 0.35f, h * 0.50f),
                        Offset(w * 0.48f, h * 0.58f),
                        Offset(w * 0.60f, h * 0.38f),
                        Offset(w * 0.72f, h * 0.44f),
                        Offset(w * 0.85f, h * 0.25f),
                        Offset(w * 0.95f, h * 0.15f),
                        Offset(w, h * 0.20f)
                    )

                    // Path for the golden stroke line
                    val strokePath = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            val cpx = (prev.x + curr.x) / 2f
                            cubicTo(cpx, prev.y, cpx, curr.y, curr.x, curr.y)
                        }
                    }

                    // Path for the gradient fill area underneath
                    val fillPath = Path().apply {
                        addPath(strokePath)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }

                    // Fill gradient under curve
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = 0.35f),
                                Color(0xFFF59E0B).copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )

                    // Draw golden glowing line
                    drawPath(
                        path = strokePath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFF59E0B), Color(0xFFFFD700), Color(0xFFFBBF24))
                        ),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Glow circle at current peak
                    val lastPoint = points.last()
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = 4.dp.toPx(),
                        center = lastPoint
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = lastPoint
                    )
                }

                // Horizontal timeline milestones (24 س, 3 ي, 20 ي, 21 ي)
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "24 س", fontSize = 9.5.sp, color = Color(0xFF94A3B8))
                    Text(text = "3 ي", fontSize = 9.5.sp, color = Color(0xFF94A3B8))
                    Text(text = "20 ي", fontSize = 9.5.sp, color = Color(0xFF94A3B8))
                    Text(text = "21 ي", fontSize = 9.5.sp, color = Color(0xFF94A3B8))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Anti-fraud market advice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF162035))
                    .border(1.dp, Color(0xFF263757), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "💡", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "نصيحة حارس الذهب: استقرار سعري - احذر دفع مصنعية تفوق 8% في المشغولات العادية",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = GoldLight
                )
            }
        }
    }
}

/**
 * Primary 3 Action Buttons:
 * 🟡 شراء ذهب | 🟢 بيع ذهب | 🕌 زكاة الذهب
 * Responsive layout (2+1 on mobile, 3 columns on tablet/desktop) with prominent touch targets and no broken Arabic words.
 */
@Composable
fun QuickActionTripleCards(
    onBuyClick: () -> Unit,
    onSellClick: () -> Unit,
    onZakatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        val isWide = maxWidth >= 540.dp
        if (isWide) {
            // Wide screens: 3 equal cards in a single row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PrimaryActionCard(
                    title = "شراء ذهب",
                    subtitle = "احسب التكلفة والمصنعية",
                    icon = "🟡",
                    brush = Brush.linearGradient(listOf(Color(0xFF92400E), Color(0xFFD97706), Color(0xFFF59E0B))),
                    testTag = "buy_gold_triple_card",
                    onClick = onBuyClick,
                    modifier = Modifier.weight(1f),
                    cardHeight = 72.dp
                )
                PrimaryActionCard(
                    title = "بيع ذهب",
                    subtitle = "احسب القيمة والخصومات",
                    icon = "🟢",
                    brush = Brush.linearGradient(listOf(Color(0xFF064E3B), Color(0xFF059669), Color(0xFF10B981))),
                    testTag = "sell_gold_triple_card",
                    onClick = onSellClick,
                    modifier = Modifier.weight(1f),
                    cardHeight = 72.dp
                )
                PrimaryActionCard(
                    title = "زكاة الذهب",
                    subtitle = "احسب النصاب ومقدار الزكاة",
                    icon = "🕌",
                    brush = Brush.linearGradient(listOf(Color(0xFF115E59), Color(0xFF0D9488), Color(0xFF14B8A6))),
                    testTag = "zakat_gold_triple_card",
                    onClick = onZakatClick,
                    modifier = Modifier.weight(1f),
                    cardHeight = 72.dp
                )
            }
        } else {
            // Mobile screens: Row of 2 (Buy & Sell) + Row of 1 (Zakat full width)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PrimaryActionCard(
                        title = "شراء ذهب",
                        subtitle = "احسب التكلفة والمصنعية",
                        icon = "🟡",
                        brush = Brush.linearGradient(listOf(Color(0xFF92400E), Color(0xFFD97706), Color(0xFFF59E0B))),
                        testTag = "buy_gold_triple_card",
                        onClick = onBuyClick,
                        modifier = Modifier.weight(1f),
                        cardHeight = 78.dp
                    )
                    PrimaryActionCard(
                        title = "بيع ذهب",
                        subtitle = "احسب القيمة والخصم",
                        icon = "🟢",
                        brush = Brush.linearGradient(listOf(Color(0xFF064E3B), Color(0xFF059669), Color(0xFF10B981))),
                        testTag = "sell_gold_triple_card",
                        onClick = onSellClick,
                        modifier = Modifier.weight(1f),
                        cardHeight = 78.dp
                    )
                }

                ZakatActionCard(
                    onClick = onZakatClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ZakatActionCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1.0f, label = "zakat_scale")

    Card(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("zakat_gold_triple_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BeigeCardSurface),
        border = BorderStroke(1.2.dp, Color(0xFF059669).copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BeigeCardSurface)
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                // السطر الأول: الأيقونة والعنوان "حساب زكاة الذهب" وشارة حساب النصاب
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🕌", fontSize = 17.sp)
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "حساب زكاة الذهب",
                            color = TextBrownDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE6F4EA))
                            .border(1.dp, Color(0xFF059669).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = "احسب النصاب الآن 👈",
                            color = Color(0xFF047857),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // السطر الثاني: نصاب الذهب والقدر الواجب إخراجه شرعاً واضح وكامل بدون اقتصاص
                Text(
                    text = "احسب النصاب الشرعي (85 جرام عيار 24) • الزكاة 2.5% بعد مرور عام هجري كامل",
                    color = TextBrownDark,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun PrimaryActionCard(
    title: String,
    subtitle: String,
    icon: String,
    brush: Brush,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardHeight: androidx.compose.ui.unit.Dp = 64.dp,
    isFullWidth: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1.0f, label = "card_scale")

    Card(
        modifier = modifier
            .scale(scale)
            .height(cardHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush)
                .border(1.2.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isFullWidth) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = icon, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = icon, fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Bottom Navigation Bar matching the 5-tab bar in the phone mockup:
 * 🏠 الرئيسية | 🧮 احسب | 📈 الأسعار | 💡 نصائح (منع الغش) | 👤 حسابي / السجل
 */
/**
 * الخامس عشر: شريط التنقل السفلي (GoldGuardBottomNav)
 * يحتوي على أربعة عناصر فقط:
 * الترتيب من اليمين إلى اليسار (RTL):
 * 1. الرئيسية
 * 2. الأسعار
 * 3. شراء
 * 4. بيع
 */
@Composable
fun GoldGuardBottomNav(
    currentScreen: ScreenType,
    onNavigate: (ScreenType) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(GoldTheme.colors.surface)
            .border(1.dp, GoldTheme.colors.borderColor, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .testTag("gold_bottom_nav")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. الرئيسية
            BottomNavItem(
                icon = "🏠",
                label = "الرئيسية",
                isSelected = currentScreen == ScreenType.HOME,
                testTag = "nav_tab_home",
                onClick = { onNavigate(ScreenType.HOME) }
            )

            // 2. الأسعار
            BottomNavItem(
                icon = "📈",
                label = "الأسعار",
                isSelected = currentScreen == ScreenType.PRICES,
                testTag = "nav_tab_prices",
                onClick = { onNavigate(ScreenType.PRICES) }
            )

            // 3. شراء
            BottomNavItem(
                icon = "🛍️",
                label = "شراء",
                isSelected = currentScreen == ScreenType.BUY,
                testTag = "nav_tab_buy",
                onClick = { onNavigate(ScreenType.BUY) }
            )

            // 4. بيع
            BottomNavItem(
                icon = "💵",
                label = "بيع",
                isSelected = currentScreen == ScreenType.SELL,
                testTag = "nav_tab_sell",
                onClick = { onNavigate(ScreenType.SELL) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) GoldTheme.colors.goldPrimary.copy(alpha = 0.12f)
                else Color.Transparent
            )
            .border(
                1.dp,
                if (isSelected) GoldTheme.colors.goldPrimary.copy(alpha = 0.4f)
                else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) GoldTheme.colors.goldPrimary else GoldTheme.colors.textSecondary
            )
        }
    }
}

@Composable
fun ScreenSubHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    gradientBrush: Brush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFFDFBF7))
    )
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
            .background(gradientBrush)
            .border(
                1.dp,
                BeigeBorderSubtle,
                RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp)
            )
            .shadow(3.dp, RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp), spotColor = Color(0x182C1810))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3EDE2))
                    .border(1.dp, Color(0xFFDFD4C2), CircleShape)
                    .testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = TextBrownDark
                )
            }

            Text(
                text = title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                color = TextBrownDark,
                textAlign = TextAlign.Center
            )

            // Balance placeholder
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
fun ConnectivityBanner(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isOnline) SuccessGreenBg else AlertRedBg
    val borderColor = if (isOnline) SuccessGreenBorder else AlertRedBorder
    val textColor = if (isOnline) SuccessGreenText else AlertRedText
    val text = if (isOnline) "🟢 متصل بالإنترنت - الأسعار حية ومحدثة" else "⚠️ بدون إنترنت - تعمل بالأسعار المحفوظة محلياً"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun GradientButton(
    text: String,
    brush: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Int = 54,
    icon: String? = null,
    subtitle: String? = null,
    titleFontSize: TextUnit = 17.sp,
    subtitleFontSize: TextUnit = 11.5.sp,
    paddingHorizontal: Dp = 14.dp,
    testTag: String = "gradient_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.975f else 1.0f, label = "scale")

    Box(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .height(height.dp)
            .shadow(elevation = if (isPressed) 2.dp else 4.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(brush)
            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = paddingHorizontal, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Text(text = icon, fontSize = (titleFontSize.value + 3).sp)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
            if (!subtitle.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = subtitleFontSize,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CustomInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    unit: String = "",
    placeholder: String = "",
    testTag: String = "input_field",
    isDark: Boolean = true
) {
    // Eye-friendly comfortable background and crisp typography
    val containerBg = if (isDark) Color(0xFF141C2B) else Color(0xFFF8FAFC)
    val borderCol = if (isDark) Color(0xFF2A3952) else Color(0xFFE2E8F0)
    val labelCol = if (isDark) Color(0xFFFBBF24) else Color(0xFF1E293B)
    val fieldBg = if (isDark) Color(0xFF0D131F) else Color.White
    val textCol = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = labelCol
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                        fontSize = 15.sp
                    )
                },
                trailingIcon = {
                    if (unit.isNotEmpty()) {
                        Text(
                            text = unit,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24),
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textCol,
                    unfocusedTextColor = textCol,
                    focusedBorderColor = Color(0xFFF59E0B),
                    unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                    focusedContainerColor = fieldBg,
                    unfocusedContainerColor = fieldBg
                )
            )
        }
    }
}

/**
 * Modern luxury top header for Buy, Sell, and Zakat screens:
 * Features:
 * - Back/Menu button
 * - Centered Illuminated Gold Shield mini-logo with gold aura
 * - Bold Golden Screen Title (e.g. شراء الذهب / بيع الذهب)
 * - Info (i) icon for quick guidance & tips
 */
@Composable
fun GoldScreenHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onInfoClick: (() -> Unit)? = null,
    onThemeToggle: (() -> Unit)? = null,
    isDarkMode: Boolean = true,
    testTagPrefix: String = "gold_header"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(GoldTheme.colors.surface)
            .border(
                1.dp,
                GoldTheme.colors.borderColor,
                RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldTheme.colors.surfaceElevated)
                    .border(1.dp, GoldTheme.colors.borderColor, RoundedCornerShape(10.dp))
                    .clickable { onBackClick() }
                    .testTag("${testTagPrefix}_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "←",
                    fontSize = 18.sp,
                    color = GoldTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Center: Shield Mini-Logo + Screen Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldTheme.colors.surfaceElevated)
                        .border(
                            1.dp,
                            GoldTheme.colors.goldPrimary.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_gold_shield_logo),
                        contentDescription = "شعار درع حارس الذهب",
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldTheme.colors.goldPrimary,
                    letterSpacing = 0.3.sp
                )
            }

            // Actions: Theme toggle and/or Info button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (onThemeToggle != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GoldTheme.colors.surfaceElevated)
                            .border(1.dp, GoldTheme.colors.borderColor, RoundedCornerShape(10.dp))
                            .clickable { onThemeToggle() }
                            .testTag("${testTagPrefix}_theme_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isDarkMode) "☀️" else "🌙",
                            fontSize = 16.sp
                        )
                    }
                }

                if (onInfoClick != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GoldTheme.colors.surfaceElevated)
                            .border(1.dp, GoldTheme.colors.borderColor, RoundedCornerShape(10.dp))
                            .clickable { onInfoClick() }
                            .testTag("${testTagPrefix}_info_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ℹ️",
                            fontSize = 16.sp
                        )
                    }
                } else if (onThemeToggle == null) {
                    Spacer(modifier = Modifier.size(38.dp))
                }
            }
        }
    }
}

/**
 * Eye-friendly, spacious, high-precision card for evaluating dealer prices in gold shops.
 * Complies strictly with user guidelines on spacing, contrast, Arabic terms, and disclaimers.
 */
@Composable
fun TraderPriceComparisonCard(
    traderPrice: String,
    onPriceChange: (String) -> Unit,
    selectedKarat: Int,
    onKaratSelected: (Int) -> Unit,
    isBuyOperation: Boolean,
    onOperationChange: (Boolean) -> Unit,
    comparison: com.example.model.TraderPriceComparison,
    currencySymbol: String = "ج.م",
    modifier: Modifier = Modifier
) {
    val karats = listOf(21, 18, 24, 22, 14)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A3952)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title & Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF241E10))
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚖️", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "مقارنة سعر التاجر بالسوق",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFF1F5F9)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "قارن سعر الجرام الذي أخبرك به الصائغ مع سعر البورصة الخام فوراً",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Operation Type Toggle: (أشتري من الصائغ / أبيع للصائغ)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0D131F))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Buy from Jeweler
                val isBuySelected = isBuyOperation
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isBuySelected) Color(0xFF0F3A2E) else Color.Transparent
                        )
                        .border(
                            1.dp,
                            if (isBuySelected) Color(0xFF10B981) else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onOperationChange(true) }
                        .testTag("trader_op_buy"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🛍️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "أشتري من الصائغ",
                            fontSize = 12.5.sp,
                            fontWeight = if (isBuySelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isBuySelected) Color(0xFF6EE7B7) else Color(0xFF94A3B8)
                        )
                    }
                }

                // Sell to Jeweler
                val isSellSelected = !isBuyOperation
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSellSelected) Color(0xFF381A1E) else Color.Transparent
                        )
                        .border(
                            1.dp,
                            if (isSellSelected) Color(0xFFF43F5E) else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onOperationChange(false) }
                        .testTag("trader_op_sell"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💵", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "أبيع للصائغ",
                            fontSize = 12.5.sp,
                            fontWeight = if (isSellSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSellSelected) Color(0xFFFDA4AF) else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Karat Selection Chips
            Text(
                text = "اختر العيار:",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCBD5E1)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                karats.forEach { karat ->
                    val isSelected = selectedKarat == karat
                    val isPopular = karat == 21
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                if (isSelected) Color(0xFF2E2412) else Color(0xFF0D131F)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFFF59E0B) else Color(0xFF1E293B),
                                RoundedCornerShape(9.dp)
                            )
                            .clickable { onKaratSelected(karat) }
                            .testTag("trader_karat_$karat"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPopular) "21 ⭐" else "$karat",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                            color = if (isSelected) Color(0xFFFBBF24) else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dealer Price Input Field
            Text(
                text = if (isBuyOperation) "سعر الجرام الذي طلبه منك التاجر:" else "سعر الجرام الذي عرضه عليك التاجر:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFBBF24)
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = traderPrice,
                onValueChange = onPriceChange,
                placeholder = {
                    Text(
                        text = "مثال: 6350",
                        color = Color(0xFF64748B),
                        fontSize = 15.sp
                    )
                },
                trailingIcon = {
                    Text(
                        text = currencySymbol,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFBBF24),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF8FAFC),
                    textAlign = TextAlign.Start
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trader_price_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFF8FAFC),
                    unfocusedTextColor = Color(0xFFF8FAFC),
                    focusedBorderColor = Color(0xFFF59E0B),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF0D131F),
                    unfocusedContainerColor = Color(0xFF0D131F)
                )
            )

            // Results Evaluation Section
            if (comparison.isEvaluated && comparison.traderPrice > 0.0) {
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0E1624))
                        .border(1.dp, Color(0xFF202C3F), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Market reference row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "السعر المرجعي في السوق (الخام):",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "${String.format(java.util.Locale.US, "%,.2f", comparison.marketPrice)} $currencySymbol",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2E8F0)
                            )
                        }

                        // Trader quoted row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "سعر التاجر المدخل:",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "${String.format(java.util.Locale.US, "%,.2f", comparison.traderPrice)} $currencySymbol",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24)
                            )
                        }

                        HorizontalDivider(color = Color(0xFF1E293B))

                        // Difference row
                        val diffSign = if (comparison.difference > 0) "+" else ""
                        val diffColor = if (isBuyOperation) {
                            if (comparison.difference <= 5.0) Color(0xFF34D399) else Color(0xFFFBBF24)
                        } else {
                            if (comparison.difference >= -5.0) Color(0xFF34D399) else Color(0xFFF87171)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الفارق عن السعر المرجعي:",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFCBD5E1)
                            )
                            Text(
                                text = "$diffSign${String.format(java.util.Locale.US, "%,.2f", comparison.difference)} $currencySymbol (${diffSign}${String.format(java.util.Locale.US, "%.1f", comparison.differencePercent)}%)",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black,
                                color = diffColor
                            )
                        }

                        // Assessment Badge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(diffColor.copy(alpha = 0.12f))
                                .border(1.dp, diffColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "💡 ${comparison.assessment}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = diffColor,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Permanent Advisory Note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1F2C))
                    .padding(8.dp)
            ) {
                Text(
                    text = "⚠️ هذه مقارنة إرشادية مع سعر الذهب الخام، وقد تختلف القيمة الفعلية حسب التاجر ونسبة المصنعية والدمغة وحالة المشغولات.",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 14.sp
                )
            }
        }
    }
}

