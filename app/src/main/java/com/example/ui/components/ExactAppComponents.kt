package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ScreenType
import com.example.ui.theme.GoldTheme
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * تنسيق الأرقام بدون أي كسور عشرية وبفواصل آلاف واضحة
 */
fun formatIntegerPrice(value: Long): String {
    return DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)).format(value)
}

/**
 * حدود متقطعة (Dashed Border) لقسم الدولار المستقل
 */
fun Modifier.dashedBorder(width: Dp, color: Color, cornerRadius: Dp) = drawBehind {
    val stroke = Stroke(
        width = width.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
    )
}

/**
 * الشريط العلوي الثابت (Sticky Top Bar)
 * - عنوان "أسعار الذهب في مصر" بخط عريض 20px بلون ذهبي (#f4c542).
 * - نص فرعي صغير "تحديث لحظي".
 * - نقطة دائرية نابضة خضراء (تتحول لأصفر عند التأخر، لأحمر عند الانقطاع).
 * - نص حالة ("السوق مفتوح • مباشر" أو "انقطع الاتصال" أو "السوق مغلق").
 * - نص "آخر تحديث: [الوقت بصيغة hh:mm:ss]".
 * - شريط أحمر يعرض "انقطع الاتصال — آخر تحديث: [الوقت]" عند انقطاع الاتصال.
 */
@Composable
fun StickyTopBar(
    statusText: String,
    lastUpdatedTime: String,
    isDisconnected: Boolean,
    isRetrying: Boolean,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse Animation for status dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val dotColor = when {
        isDisconnected -> GoldTheme.colors.danger
        isRetrying -> GoldTheme.colors.warning
        else -> GoldTheme.colors.success
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(GoldTheme.colors.surface)
            .statusBarsPadding()
    ) {
        // Red Disconnection Banner if disconnected
        if (isDisconnected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GoldTheme.colors.danger)
                    .padding(vertical = 6.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "انقطع الاتصال — آخر تحديث: $lastUpdatedTime",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Right: Title and Subtitle
            Column {
                Text(
                    text = "أسعار الذهب في مصر",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldTheme.colors.goldPrimary,
                    modifier = Modifier.testTag("app_title")
                )
                Text(
                    text = "تحديث لحظي",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = GoldTheme.colors.textSecondary
                )
            }

            // Center / Left: Status + Pulsing Dot + Last Updated + Settings
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = dotColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .scale(dotScale)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                    if (lastUpdatedTime.isNotBlank()) {
                        Text(
                            text = "آخر تحديث: $lastUpdatedTime",
                            fontSize = 10.sp,
                            color = GoldTheme.colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Settings Gear Icon (Optional settings modal)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldTheme.colors.background)
                        .clickable { onOpenSettings() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚙️", fontSize = 16.sp)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(GoldTheme.colors.borderColor)
        )
    }
}

/**
 * شريط تنقل سفلي ثابت بأربعة تبويبات فقط:
 * 1. "الأسعار" مع أيقونة 📊 (الافتراضي عند الفتح).
 * 2. "شراء" مع أيقونة 🛒.
 * 3. "بيع" مع أيقونة 💰.
 * 4. "زكاة" مع أيقونة ✨.
 */
@Composable
fun ExactBottomNav(
    currentScreen: ScreenType,
    onNavigate: (ScreenType) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            .background(GoldTheme.colors.surface)
            .border(1.dp, GoldTheme.colors.borderColor, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            .padding(vertical = 6.dp, horizontal = 8.dp)
            .testTag("exact_bottom_nav")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. الأسعار
            NavTabItem(
                icon = "📊",
                label = "الأسعار",
                isSelected = currentScreen == ScreenType.PRICES,
                testTag = "nav_tab_prices",
                onClick = { onNavigate(ScreenType.PRICES) }
            )

            // 2. شراء
            NavTabItem(
                icon = "🛒",
                label = "شراء",
                isSelected = currentScreen == ScreenType.BUY,
                testTag = "nav_tab_buy",
                onClick = { onNavigate(ScreenType.BUY) }
            )

            // 3. بيع
            NavTabItem(
                icon = "💰",
                label = "بيع",
                isSelected = currentScreen == ScreenType.SELL,
                testTag = "nav_tab_sell",
                onClick = { onNavigate(ScreenType.SELL) }
            )

            // 4. زكاة
            NavTabItem(
                icon = "✨",
                label = "زكاة",
                isSelected = currentScreen == ScreenType.ZAKAT,
                testTag = "nav_tab_zakat",
                onClick = { onNavigate(ScreenType.ZAKAT) }
            )
        }
    }
}

@Composable
private fun NavTabItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) GoldTheme.colors.goldPrimary.copy(alpha = 0.15f) else Color.Transparent,
        label = "tab_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) GoldTheme.colors.goldPrimary.copy(alpha = 0.5f) else Color.Transparent,
        label = "tab_border"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
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

/**
 * نافذة الإعدادات (مغلقة افتراضياً تفتح فقط عند الضغط على أيقونة الترس)
 * تحتوي على:
 * - تشغيل/إيقاف الصوت
 * - تشغيل/إيقاف الإشعارات
 * - عتبة الوميض (افتراضي 3)
 * - نسبة التالف الافتراضية (افتراضي 0)
 */
@Composable
fun SettingsDialog(
    soundEnabled: Boolean,
    onSoundChanged: (Boolean) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsChanged: (Boolean) -> Unit,
    flashThreshold: Double,
    onThresholdChanged: (Double) -> Unit,
    defaultDamagePercent: Double,
    onDamageChanged: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var thresholdText by remember { mutableStateOf(flashThreshold.toInt().toString()) }
    var damageText by remember { mutableStateOf(defaultDamagePercent.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GoldTheme.colors.surface,
        title = {
            Text(
                text = "الإعدادات",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = GoldTheme.colors.goldPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Sound Alert Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تشغيل الصوت عند تغير السعر",
                        fontSize = 13.5.sp,
                        color = GoldTheme.colors.textPrimary
                    )
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = onSoundChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldTheme.colors.goldPrimary,
                            checkedTrackColor = GoldTheme.colors.goldPrimary.copy(alpha = 0.4f)
                        )
                    )
                }

                // Notifications Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تشغيل الإشعارات",
                        fontSize = 13.5.sp,
                        color = GoldTheme.colors.textPrimary
                    )
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldTheme.colors.goldPrimary,
                            checkedTrackColor = GoldTheme.colors.goldPrimary.copy(alpha = 0.4f)
                        )
                    )
                }

                // Flash Threshold
                OutlinedTextField(
                    value = thresholdText,
                    onValueChange = {
                        thresholdText = it
                        it.toDoubleOrNull()?.let { v -> onThresholdChanged(v) }
                    },
                    label = { Text("عتبة الوميض (جنيه) — افتراضي 3") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldTheme.colors.goldPrimary,
                        unfocusedBorderColor = GoldTheme.colors.borderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Default Damage Percent
                OutlinedTextField(
                    value = damageText,
                    onValueChange = {
                        damageText = it
                        it.toDoubleOrNull()?.let { v -> onDamageChanged(v) }
                    },
                    label = { Text("نسبة التالف الافتراضية (%) — افتراضي 0") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldTheme.colors.goldPrimary,
                        unfocusedBorderColor = GoldTheme.colors.borderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldTheme.colors.goldPrimary)
            ) {
                Text("إغلاق", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}
