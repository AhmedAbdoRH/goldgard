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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Country
import com.example.model.MarketAdminSettings
import com.example.model.ScreenType
import com.example.ui.components.CustomInputField
import com.example.ui.components.ScreenSubHeader
import com.example.ui.theme.DarkBorderGold
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.InfoCyanBg
import com.example.ui.theme.InfoCyanBorder
import com.example.ui.theme.PurpleGradientEnd
import com.example.ui.theme.PurpleGradientStart
import com.example.ui.theme.SettingsGray
import com.example.ui.theme.SettingsGrayGradientBrush
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.GoldViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: GoldViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val selectedCountry by viewModel.selectedCountry.collectAsStateWithLifecycle()
    val marketAdminSettings by viewModel.marketAdminSettings.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val isTestingGoogleSheet by viewModel.isTestingGoogleSheet.collectAsStateWithLifecycle()
    val lastGoogleSheetSyncText by viewModel.lastGoogleSheetSyncText.collectAsStateWithLifecycle()
    val hourlyCountdownText by viewModel.hourlyCountdownText.collectAsStateWithLifecycle()
    val goldPriceResponse by viewModel.goldPriceResponse.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) }

    // User settings inputs
    var buyMakingInput by remember(settings) {
        mutableStateOf(settings.defaultBuyMakingPercent.toString())
    }
    var sellDeductionInput by remember(settings) {
        mutableStateOf(settings.defaultSellDeductionPercent.toString())
    }
    var stampFeeInput by remember(settings) {
        mutableStateOf(settings.defaultStampFeePerGram.toString())
    }

    // Google Sheet URL state
    var googleSheetUrlInput by remember(marketAdminSettings) {
        mutableStateOf(marketAdminSettings.googleSheetUrl)
    }

    // Admin settings inputs
    var activeProvider by remember(marketAdminSettings) {
        mutableStateOf(marketAdminSettings.activeProvider)
    }
    var customerBuyPremiumInput by remember(marketAdminSettings) {
        mutableStateOf(marketAdminSettings.customerBuyPremiumPercent.toString())
    }
    var dealerBuyDiscountInput by remember(marketAdminSettings) {
        mutableStateOf(marketAdminSettings.dealerBuyDiscountPercent.toString())
    }
    var fixedAdjustmentInput by remember(marketAdminSettings) {
        mutableStateOf(marketAdminSettings.fixedAdjustmentEGP.toString())
    }
    var roundingMode by remember(marketAdminSettings) {
        mutableStateOf(marketAdminSettings.roundingMode)
    }
    var refreshIntervalInput by remember(marketAdminSettings) {
        mutableStateOf(marketAdminSettings.refreshIntervalSeconds.toString())
    }
    var manualGram24Input by remember(marketAdminSettings) {
        mutableStateOf(marketAdminSettings.manualGram24Price.toString())
    }
    var manualBench21BuyInput by remember(marketAdminSettings) {
        mutableStateOf(marketAdminSettings.manualBenchmark21Buy.toString())
    }
    var manualBench21SellInput by remember(marketAdminSettings) {
        mutableStateOf(marketAdminSettings.manualBenchmark21Sell.toString())
    }
    var changedByInput by remember { mutableStateOf("مسؤول التسعير") }
    var changeReasonInput by remember { mutableStateOf("تحديث هوامش السوق اليومية") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(com.example.ui.theme.GoldTheme.colors.background),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // 1. Header
        item {
            com.example.ui.components.GoldScreenHeader(
                title = "الإعدادات وإدارة السوق",
                onBackClick = { viewModel.navigateTo(ScreenType.HOME) },
                onThemeToggle = { viewModel.toggleDarkMode() },
                isDarkMode = isDarkMode,
                testTagPrefix = "settings"
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Top Switcher: Tab 0 (إعدادات المستخدم) vs Tab 1 (لوحة تحكم المشرف Admin)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF131A29),
                contentColor = GoldAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GoldAccent,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF26334A), RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "⚙️ إعدادات الحساب",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (selectedTab == 0) GoldAccent else Color(0xFF94A3B8)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "🛠️ لوحة تحكم التسعير (Admin)",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (selectedTab == 1) GoldAccent else Color(0xFF94A3B8)
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (selectedTab == 0) {
            // TAB 0: USER SETTINGS

            // Egypt Market Status Card (Luxury Obsidian Card)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
                                )
                            )
                            .border(
                                1.dp,
                                GoldPrimary.copy(alpha = 0.45f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🇪🇬", fontSize = 26.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "سوق الذهب في مصر",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "التطبيق مضبوط حصرياً لسوق الصاغة المصرية",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF0B132B).copy(alpha = 0.6f))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "العملة المعتمدة: جنيه مصري (ج.م)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldLight
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "مزود الأسعار النشط: ${marketAdminSettings.activeProvider.uppercase()}",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.65f)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(GoldPrimary)
                                            .padding(horizontal = 12.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "مفعل 🇪🇬",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Google Sheets Database Integration Card (Tab 0)
            item {
                GoogleSheetsSyncCard(
                    googleSheetUrl = googleSheetUrlInput,
                    onUrlChange = { googleSheetUrlInput = it },
                    isTesting = isTestingGoogleSheet,
                    onTestAndSync = {
                        viewModel.testAndSyncGoogleSheet(googleSheetUrlInput)
                    },
                    lastSyncText = lastGoogleSheetSyncText,
                    hourlyCountdownText = hourlyCountdownText,
                    buy21 = goldPriceResponse.gram21.buy,
                    sell21 = goldPriceResponse.gram21.sell,
                    isLive = (goldPriceResponse.status == "live"),
                    onSaveUrl = {
                        val updated = marketAdminSettings.copy(
                            activeProvider = "google_sheets",
                            providerType = "GoogleSheets",
                            googleSheetUrl = googleSheetUrlInput.trim()
                        )
                        viewModel.saveMarketAdminSettings(
                            newSettings = updated,
                            changedBy = "المستخدم",
                            reason = "ربط وتحديث شيت جوجل"
                        )
                    },
                    viewModel = viewModel
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Settings Fields
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val goldPrices by viewModel.goldPrices.collectAsStateWithLifecycle()
                    val price21 = goldPrices.find { it.karat == 21 }?.buyPrice ?: selectedCountry.defaultBenchmark21Buy
                    val makingPct = buyMakingInput.toDoubleOrNull() ?: 5.0
                    val approxAmount = price21 * (makingPct / 100.0)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        CustomInputField(
                            label = "مصنعية الشراء الافتراضية (%)",
                            value = buyMakingInput,
                            onValueChange = { buyMakingInput = it },
                            unit = "%",
                            placeholder = "5",
                            testTag = "settings_buy_making_input"
                        )
                        if (approxAmount > 0) {
                            Text(
                                text = "≈ تعادل ${viewModel.formatPrice(approxAmount)} ${selectedCountry.currencySymbol}/جرام لعيار 21 بالسعر الحالي",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PurpleGradientStart,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }

                    CustomInputField(
                        label = "خصم البيع الافتراضي (%)",
                        value = sellDeductionInput,
                        onValueChange = { sellDeductionInput = it },
                        unit = "%",
                        placeholder = "2",
                        testTag = "settings_sell_deduction_input"
                    )

                    CustomInputField(
                        label = "الدمغة والرسوم الافتراضية (${selectedCountry.currencySymbol} لكل جرام)",
                        value = stampFeeInput,
                        onValueChange = { stampFeeInput = it },
                        unit = "${selectedCountry.currencySymbol}/جرام",
                        placeholder = selectedCountry.defaultStampFee.toString(),
                        testTag = "settings_stamp_fee_input"
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // خيارات التنبيهات الصوتية والاهتزاز
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131A29)),
                    border = BorderStroke(1.dp, Color(0xFF26334A))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🔔 تنبيهات حركة الأسعار اللحظية",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "نغمة صوتية عند تغير السعر",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "نغمة 880Hz عند الصعود و 440Hz عند الهبوط",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Switch(
                                checked = marketAdminSettings.soundAlertEnabled,
                                onCheckedChange = { isChecked ->
                                    val updated = marketAdminSettings.copy(soundAlertEnabled = isChecked)
                                    viewModel.saveMarketAdminSettings(updated, "المستخدم", "تعديل التنبيه الصوتي")
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF0F172A),
                                    checkedTrackColor = GoldAccent
                                )
                            )
                        }

                        HorizontalDivider(color = Color(0xFF26334A))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "الاهتزاز عند تغير السعر",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "نبضة اهتزاز تأكيدية عند وصول تحديث سعري جديد",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Switch(
                                checked = marketAdminSettings.vibrationEnabled,
                                onCheckedChange = { isChecked ->
                                    val updated = marketAdminSettings.copy(vibrationEnabled = isChecked)
                                    viewModel.saveMarketAdminSettings(updated, "المستخدم", "تعديل التنبيه بالاهتزاز")
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF0F172A),
                                    checkedTrackColor = GoldAccent
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // بطاقة معايرة جولد بيليون (Gold Bullion Hybrid Calibration)
            item {
                var goldBullion21BuyInput by remember { mutableStateOf("") }
                var saghaDollarInput by remember(marketAdminSettings.saghaUsdRate) {
                    mutableStateOf(marketAdminSettings.saghaUsdRate.toString())
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141C2E)),
                    border = BorderStroke(1.2.dp, GoldPrimary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎯", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "معايرة دقة التسعير",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldLight
                                )
                                Text(
                                    text = "مطابقة وضبط الأسعار اللحظية لعيار 21 مع سوق الصاغة بدقة تامة",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        HorizontalDivider(color = DarkBorderGold.copy(alpha = 0.5f), thickness = 0.8.dp)

                        // 1. حقل إدخال: سعر شراء عيار 21 الفعلي في السوق
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "سعر شراء عيار 21 الفعلي في السوق (للمعايرة)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = goldBullion21BuyInput,
                                    onValueChange = { input ->
                                        goldBullion21BuyInput = input.filter { it.isDigit() || it == '.' }
                                    },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("مثال: 6385", color = Color(0xFF64748B)) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldAccent,
                                        unfocusedBorderColor = Color(0xFF26334A),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A)
                                    )
                                )

                                Button(
                                    onClick = {
                                        val entered = goldBullion21BuyInput.toDoubleOrNull() ?: 0.0
                                        if (entered > 500.0) {
                                            viewModel.calibrateWithGoldBullion21(entered)
                                            goldBullion21BuyInput = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                                ) {
                                    Text(
                                        text = "معايرة الآن ⚖️",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }

                            // يظهر أسفل الحقل: تمت المعايرة: K = [القيمة] بتاريخ [الوقت]
                            val calibKStr = String.format(Locale.US, "%.4f", marketAdminSettings.calibrationK)
                            val calibDateStr = if (marketAdminSettings.lastCalibrationDate.isNotBlank()) marketAdminSettings.lastCalibrationDate else "افتراضي"
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "تمت المعايرة: K = $calibKStr بتاريخ $calibDateStr",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GoldAccent
                                )
                            }
                        }

                        // 2. حقل إدخال: سعر دولار الصاغة (SD)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "سعر دولار الصاغة (SD) (افتراضي 51.7)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = saghaDollarInput,
                                    onValueChange = { input ->
                                        saghaDollarInput = input.filter { it.isDigit() || it == '.' }
                                    },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("51.7", color = Color(0xFF64748B)) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldAccent,
                                        unfocusedBorderColor = Color(0xFF26334A),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A)
                                    )
                                )

                                Button(
                                    onClick = {
                                        val newSd = saghaDollarInput.toDoubleOrNull() ?: 51.7
                                        viewModel.updateSaghaDollarRate(newSd)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    border = BorderStroke(1.dp, GoldAccent)
                                ) {
                                    Text(
                                        text = "حفظ SD 💾",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent
                                    )
                                }
                            }
                        }

                        // 3. زر «استعادة الافتراضي»: يعيد K إلى 0.9996 وSD إلى 51.7
                        OutlinedButton(
                            onClick = {
                                viewModel.resetCalibrationToDefault()
                                saghaDollarInput = "51.7"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                        ) {
                            Text(
                                text = "🔄 استعادة الافتراضي (K = 0.9996 و SD = 51.7)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Action Buttons
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val buyMaking = buyMakingInput.toDoubleOrNull() ?: 5.0
                            val sellDeduction = sellDeductionInput.toDoubleOrNull() ?: 2.0
                            val stamp = stampFeeInput.toDoubleOrNull() ?: selectedCountry.defaultStampFee
                            viewModel.saveSettings(buyMaking, sellDeduction, stamp)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("settings_save_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Text(
                            text = "💾 حفظ الإعدادات الشخصية",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.restoreDefaultSettings()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("settings_restore_defaults_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF475569))
                    ) {
                        Text(
                            text = "🔄 استعادة الإعدادات الافتراضية",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Guide Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFFFFBEB))
                        .border(1.2.dp, Color(0xFFFDE68A), RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💡", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "دليل وإرشادات سوق الذهب المصري 🇪🇬",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "• مصنعية الشراء: تختلف المصنعية من محل لآخر وحسب نوع المشغولات. النسبة الافتراضية 5% هي متوسط متوازن للتفاوض.",
                            fontSize = 12.sp,
                            color = Color(0xFF78350F),
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• خصم البيع (الكسر): عند بيع الذهب القديم، يخصم التاجر نسبة تقريبية (1% إلى 2%) تعويضاً عن الشوائب وهالك الصهر.",
                            fontSize = 12.sp,
                            color = Color(0xFF78350F),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            // TAB 1: ADMIN PANEL (لوحة تحكم التسعير والرقابة)

            // Diagnostic Panel (لوحة تشخيص محرك تسعير الذهب)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    border = BorderStroke(1.2.dp, GoldPrimary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔬", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "لوحة تشخيص محرك التسعير الفوري",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                        }

                        HorizontalDivider(
                            color = DarkBorderGold.copy(alpha = 0.5f),
                            thickness = 0.8.dp
                        )

                        val liveOunce = goldPriceResponse.ouncePriceUsd
                        val saghaUsd = marketAdminSettings.saghaUsdRate
                        val calibK = marketAdminSettings.calibrationK
                        val raw21 = if (liveOunce > 0.0 && saghaUsd > 0.0) {
                            (liveOunce * saghaUsd / 31.1035) * (21.0 / 24.0)
                        } else 0.0
                        val p21 = goldPriceResponse.getPairForKarat(21)

                        // 1. الأونصة العالمية الحية
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الأونصة عالميًا (XAU/USD):",
                                fontSize = 12.5.sp,
                                color = TextDark
                            )
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", liveOunce)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // 2. دولار الصاغة
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "دولار الصاغة (SD):",
                                fontSize = 12.5.sp,
                                color = TextDark
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.2f", saghaUsd)} ج.م",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }

                        // 3. معامل المعايرة K
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "معامل المعايرة اللحظي (K):",
                                fontSize = 12.5.sp,
                                color = TextDark
                            )
                            Text(
                                text = String.format(Locale.US, "%.4f", calibK),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight
                            )
                        }

                        // 4. السعر الخام لعيار 21
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "سعر عيار 21 الخام (Raw):",
                                fontSize = 12.5.sp,
                                color = TextDark
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.2f", raw21)} ج.م",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // 5. سعر شراء عيار 21 (الخام × K × 1.0019)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "شراء عيار 21: round(Raw × K × 1.0019):",
                                fontSize = 12.sp,
                                color = TextDark
                            )
                            Text(
                                text = "${com.example.util.GoldPriceFormatter.formatGram(p21.buy)} ج.م",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }

                        // 6. سعر بيع عيار 21 (الخام × K × 0.9972)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "بيع عيار 21: round(Raw × K × 0.9972):",
                                fontSize = 12.sp,
                                color = TextDark
                            )
                            Text(
                                text = "${com.example.util.GoldPriceFormatter.formatGram(p21.sell)} ج.م",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }

                        // المعادلة المستخدمة
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .border(0.5.dp, DarkBorderGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "معادلة جولد بيليون:\nRaw = XAU × SD ÷ 31.1035 × (العيار ÷ 24)\nالشراء = round(Raw × K × 1.0019)\nالبيع = round(Raw × K × 0.9972)",
                                fontSize = 10.5.sp,
                                color = GoldAccent,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Section 1: Active Provider Selector
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    border = BorderStroke(1.dp, DarkBorderGold)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "📡 مصدر ومزود أسعار الذهب النشط",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                        Text(
                            text = "اختر المزود المعتمد لحساب أسعار الشراء والبيع في السوق المصري:",
                            fontSize = 11.5.sp,
                            color = Color(0xFF94A3B8)
                        )

                        // Provider Chips Row
                        val providers = listOf(
                            Triple("google_sheets", "شيت جوجل (Google Sheets) 📊", "تحديث آلي كل ساعة من B2 و C2 لعيار 21"),
                            Triple("goldapi", "مزوّد الأسعار العالمي 🌐", "الرئيسي (سعر الأونصة العالمي)"),
                            Triple("metalprice", "Metalprice ⚡", "مزود مرخص احتياطي"),
                            Triple("manual", "يدوي للطوارئ ✍️", "تجاوز فوري للأسعار")
                        )

                        providers.forEach { (id, title, desc) ->
                            val isSelected = activeProvider.equals(id, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.8.dp,
                                        color = if (isSelected) GoldAccent else Color(0xFF26334A),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { activeProvider = id }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSelected) GoldAccent else Color.White
                                        )
                                        Text(
                                            text = desc,
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                    Text(
                                        text = if (isSelected) "● نشط" else "○",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color(0xFF34D399) else Color(0xFF64748B)
                                    )
                                }
                            }
                        }

                        // If google_sheets selected, show sync card inside admin too
                        if (activeProvider.equals("google_sheets", ignoreCase = true)) {
                            HorizontalDivider(color = Color(0xFF26334A), modifier = Modifier.padding(vertical = 4.dp))
                            GoogleSheetsSyncCard(
                                googleSheetUrl = googleSheetUrlInput,
                                onUrlChange = { googleSheetUrlInput = it },
                                isTesting = isTestingGoogleSheet,
                                onTestAndSync = {
                                    viewModel.testAndSyncGoogleSheet(googleSheetUrlInput)
                                },
                                lastSyncText = lastGoogleSheetSyncText,
                                hourlyCountdownText = hourlyCountdownText,
                                buy21 = goldPriceResponse.gram21.buy,
                                sell21 = goldPriceResponse.gram21.sell,
                                isLive = (goldPriceResponse.status == "live"),
                                onSaveUrl = {
                                    val updated = marketAdminSettings.copy(
                                        activeProvider = "google_sheets",
                                        providerType = "GoogleSheets",
                                        googleSheetUrl = googleSheetUrlInput.trim()
                                    )
                                    viewModel.saveMarketAdminSettings(
                                        newSettings = updated,
                                        changedBy = changedByInput.ifBlank { "مدير النظام" },
                                        reason = "ربط وتحديث شيت جوجل"
                                    )
                                },
                                viewModel = viewModel
                            )
                        }

                        // If manual provider selected, show direct inputs
                        if (activeProvider.equals("manual", ignoreCase = true)) {
                            HorizontalDivider(color = Color(0xFF26334A), modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                text = "⚠️ إدخال الأسعار يدوياً للطوارئ:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFFBBF24)
                            )
                            CustomInputField(
                                label = "سعر جرام 24 اليدوي (ج.م)",
                                value = manualGram24Input,
                                onValueChange = { manualGram24Input = it },
                                unit = "ج.م",
                                placeholder = "7220",
                                testTag = "admin_manual_p24_input"
                            )
                            CustomInputField(
                                label = "سعر عيار 21 شراء يدوي (ج.م)",
                                value = manualBench21BuyInput,
                                onValueChange = { manualBench21BuyInput = it },
                                unit = "ج.م",
                                placeholder = "6330",
                                testTag = "admin_manual_p21_buy_input"
                            )
                            CustomInputField(
                                label = "سعر عيار 21 بيع يدوي (ج.م)",
                                value = manualBench21SellInput,
                                onValueChange = { manualBench21SellInput = it },
                                unit = "ج.م",
                                placeholder = "6280",
                                testTag = "admin_manual_p21_sell_input"
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Section 2: Pricing Margins
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    border = BorderStroke(1.dp, DarkBorderGold)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "📐 هوامش التسعير والعمولات",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )

                        CustomInputField(
                            label = "نسبة هامش سعر البيع للعميل (%)",
                            value = customerBuyPremiumInput,
                            onValueChange = { customerBuyPremiumInput = it },
                            unit = "%",
                            placeholder = "0.35",
                            testTag = "admin_buy_premium_input"
                        )
                        Text(
                            text = "السعر الذي يدفعه العميل عند الشراء من الصائغ = السعر الصافي × (1 + النسبة)",
                            fontSize = 10.5.sp,
                            color = Color(0xFF6EE7B7)
                        )

                        CustomInputField(
                            label = "نسبة خصم سعر الشراء من العميل (%)",
                            value = dealerBuyDiscountInput,
                            onValueChange = { dealerBuyDiscountInput = it },
                            unit = "%",
                            placeholder = "0.70",
                            testTag = "admin_sell_discount_input"
                        )
                        Text(
                            text = "السعر الذي يدفعه الصائغ للعميل عند البيع = سعر البيع للعميل × (1 - النسبة)",
                            fontSize = 10.5.sp,
                            color = Color(0xFFFDA4AF)
                        )

                        CustomInputField(
                            label = "تعديل ثابت بالجنيه (+ / -)",
                            value = fixedAdjustmentInput,
                            onValueChange = { fixedAdjustmentInput = it },
                            unit = "ج.م",
                            placeholder = "0.0",
                            testTag = "admin_fixed_adj_input"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Section 3: Rounding Mode & Refresh Interval
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    border = BorderStroke(1.dp, DarkBorderGold)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "🔢 خيارات التقريب وفترة التحديث",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )

                        Text(
                            text = "طريقة تقريب أسعار الجرام المعروضة:",
                            fontSize = 11.5.sp,
                            color = Color(0xFF94A3B8)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val modes = listOf(
                                "NONE" to "بدون تقريب",
                                "NEAREST_1" to "أقرب 1 ج.م",
                                "NEAREST_5" to "أقرب 5 ج.م"
                            )
                            modes.forEach { (modeKey, modeTitle) ->
                                val isSelected = roundingMode == modeKey
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) GoldPrimary else Color(0xFF162035))
                                        .clickable { roundingMode = modeKey }
                                        .padding(vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = modeTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color(0xFF0F172A) else Color.White
                                    )
                                }
                            }
                        }

                        CustomInputField(
                            label = "فترة التحديث التلقائي (بالثواني)",
                            value = refreshIntervalInput,
                            onValueChange = { refreshIntervalInput = it },
                            unit = "ثانية",
                            placeholder = "60",
                            testTag = "admin_refresh_interval_input"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Section 4: Audit Signature & Save Action
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131A29)),
                    border = BorderStroke(1.2.dp, Color(0xFFF59E0B).copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "✍️ توثيق التعديل الإداري (Audit Signature)",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFDE68A)
                        )

                        CustomInputField(
                            label = "اسم المسؤول أو المشرف",
                            value = changedByInput,
                            onValueChange = { changedByInput = it },
                            unit = "👤",
                            placeholder = "مدير التسعير",
                            testTag = "admin_changed_by_input"
                        )

                        CustomInputField(
                            label = "سبب التعديل أو المذكرة",
                            value = changeReasonInput,
                            onValueChange = { changeReasonInput = it },
                            unit = "📝",
                            placeholder = "تحديث أسعار الافتتاح اليومي",
                            testTag = "admin_reason_input"
                        )

                        Button(
                            onClick = {
                                val updated = marketAdminSettings.copy(
                                    activeProvider = activeProvider,
                                    providerType = if (activeProvider.equals("google_sheets", ignoreCase = true)) "GoogleSheets" else activeProvider,
                                    googleSheetUrl = googleSheetUrlInput.trim(),
                                    customerBuyPremiumPercent = customerBuyPremiumInput.toDoubleOrNull() ?: 0.35,
                                    dealerBuyDiscountPercent = dealerBuyDiscountInput.toDoubleOrNull() ?: 0.70,
                                    fixedAdjustmentEGP = fixedAdjustmentInput.toDoubleOrNull() ?: 0.0,
                                    roundingMode = roundingMode,
                                    refreshIntervalSeconds = (refreshIntervalInput.toLongOrNull() ?: 60L).coerceAtLeast(15L),
                                    manualGram24Price = manualGram24Input.toDoubleOrNull() ?: 7220.0,
                                    manualBenchmark21Buy = manualBench21BuyInput.toDoubleOrNull() ?: 6330.0,
                                    manualBenchmark21Sell = manualBench21SellInput.toDoubleOrNull() ?: 6280.0
                                )
                                viewModel.saveMarketAdminSettings(
                                    newSettings = updated,
                                    changedBy = changedByInput.ifBlank { "مدير النظام" },
                                    reason = changeReasonInput.ifBlank { "تحديث هوامش الصاغة" }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("admin_save_market_settings_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Text(
                                text = "💾 حفظ الهوامش وتسجيل في سجل التدقيق",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Section 5: Audit Log History List
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
                            text = "📜 سجل تدقيق التعديلات (Audit Log)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                        Text(
                            text = "${auditLogs.size} عملية مسجلة",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (auditLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF131A29))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد تعديلات إدارية سابقة مسجلة.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    } else {
                        val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar", "EG"))
                        auditLogs.take(10).forEach { log ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF131A29)),
                                border = BorderStroke(0.8.dp, Color(0xFF26334A))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "👤 ${log.changedBy}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = GoldAccent
                                        )
                                        Text(
                                            text = dateFormat.format(Date(log.timestamp)),
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Text(
                                        text = "السبب: ${log.changeReason}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "القيم السابقة: ${log.previousValues}",
                                        fontSize = 9.5.sp,
                                        color = Color(0xFFFDA4AF)
                                    )
                                    Text(
                                        text = "القيم الجديدة: ${log.newValues}",
                                        fontSize = 9.5.sp,
                                        color = Color(0xFF6EE7B7)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun GoogleSheetsSyncCard(
    googleSheetUrl: String,
    onUrlChange: (String) -> Unit,
    isTesting: Boolean,
    onTestAndSync: () -> Unit,
    lastSyncText: String,
    hourlyCountdownText: String,
    buy21: Double,
    sell21: Double,
    isLive: Boolean = false,
    onSaveUrl: () -> Unit,
    viewModel: GoldViewModel
) {
    var showCsvDialog by remember { mutableStateOf(false) }
    var csvInputText by remember { mutableStateOf("") }

    if (showCsvDialog) {
        AlertDialog(
            onDismissRequest = { showCsvDialog = false },
            title = {
                Text(
                    text = "📋 استيراد بيانات جدول أسعار الذهب (CSV)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2A1B12)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "الصق أسطر الجدول أو نص CSV مباشرة، وسيقوم التطبيق بقراءة الأسعار وتحديث كافة الشاشات فوراً:",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                    OutlinedTextField(
                        value = csvInputText,
                        onValueChange = { csvInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        placeholder = {
                            Text(
                                "2026-09-13 10:27,6265,6235,gold-price-live.com\nأو الصق الجدول كاملاً هنا...",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (csvInputText.isNotBlank()) {
                            viewModel.applyCsvData(csvInputText)
                        }
                        showCsvDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97724))
                ) {
                    Text("✅ تطبيق الأسعار", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCsvDialog = false }) {
                    Text("إلغاء", color = Color(0xFF64748B))
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.2.dp, Color(0xFFE2D7C5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🌐", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "المصدر الأساسي: gold-price-live.com",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2A1B12)
                        )
                        Text(
                            text = "الرابط الوحيد والمعتمد لتحديد سعر جرام عيار 21",
                            fontSize = 11.sp,
                            color = Color(0xFF78350F)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFECFDF5))
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "المصدر الأساسي 🌟",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857)
                    )
                }
            }

            // Live Connection Status Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFECFDF5))
                    .border(1.2.dp, Color(0xFF10B981), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🟢", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "معتمد ومتصل: https://gold-price-live.com/",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                        Text(
                            text = "يتم استخراج وتحديث أسعار الذهب والشراء والبيع مباشرة من هذا المصدر المعتمد.",
                            fontSize = 10.5.sp,
                            color = Color(0xFF047857)
                        )
                    }
                }
            }

            // Instructions Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "📋 خطوات ضبط خلايا ومشاركة الشيت:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "1️⃣ تأكد من إتاحة الشيت: اضغط زر 'مشاركة' (Share) في أعلى الشيت ⬅️ غيّر الوصول العام إلى 'أي شخص لديه الرابط' (Anyone with the link can view).\n2️⃣ أو من القائمة: ملف (File) ⬅️ مشاركة (Share) ⬅️ نشر على الويب (Publish to web) ⬅️ اضغط نشر (Publish).\n3️⃣ الخلية B2: اكتب فيها سعر الشراء المباشر لعيار 21 (مثال: 6330)\n4️⃣ الخلية C2: اكتب فيها سعر البيع المباشر لعيار 21 (مثال: 6280)\n5️⃣ اضغط Enter بعد كتابة القيم حتى تُحفظ في سيرفر جوجل.",
                        fontSize = 10.8.sp,
                        color = Color(0xFF475569),
                        lineHeight = 16.sp
                    )
                }
            }

            // Input Field
            CustomInputField(
                label = "رابط ملف جوجل شيت (Google Sheet URL)",
                value = googleSheetUrl,
                onValueChange = onUrlChange,
                unit = "🔗",
                placeholder = "https://docs.google.com/spreadsheets/d/...",
                testTag = "google_sheet_url_input"
            )

            // Current Values Readout (B2 & C2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Buy Box (B2)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF0FDF4))
                        .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "شراء عيار 21 (B2)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${viewModel.formatPrice(buy21)} ج.م",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF14532D)
                        )
                    }
                }

                // Sell Box (C2)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "بيع عيار 21 (C2)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${viewModel.formatPrice(sell21)} ج.م",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF7F1D1D)
                        )
                    }
                }
            }

            // Sync Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آخر قراءة: ${if (lastSyncText.isNotBlank()) lastSyncText else "الآن"}",
                    fontSize = 10.5.sp,
                    color = Color(0xFF6B7280)
                )

                Text(
                    text = hourlyCountdownText,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFD97724)
                )
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.fetchLiveFromGoldPriceLiveSite() },
                    enabled = !isTesting,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97724))
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "جارِ السحب من الموقع...", fontSize = 11.5.sp, color = Color.White)
                    } else {
                        Text(
                            text = "🔄 سحب مباشر من gold-price-live",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                OutlinedButton(
                    onClick = onTestAndSync,
                    enabled = !isTesting,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFD97724)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2A1B12))
                ) {
                    Text(
                        text = "🔄 تحديث الشيت",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2A1B12)
                    )
                }
            }

            // Quick Toggle Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.applyLatestGrokPrices() },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF2563EB)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1D4ED8))
                ) {
                    Text(
                        text = "⚡ أحدث تحديث (2,222 / 6,235)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D4ED8)
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.applyDefaultLivePrices() },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF065F46))
                ) {
                    Text(
                        text = "⚡ تسعير الموقع (6,265 / 6,235)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857)
                    )
                }
            }

            // Secondary Buttons: Save URL & Paste CSV
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSaveUrl,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFD97724)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2A1B12))
                ) {
                    Text(
                        text = "💾 حفظ رابط الشيت",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2A1B12)
                    )
                }

                OutlinedButton(
                    onClick = { showCsvDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF64748B)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF334155))
                ) {
                    Text(
                        text = "📋 لصق جدول CSV",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                }
            }
        }
    }
}
