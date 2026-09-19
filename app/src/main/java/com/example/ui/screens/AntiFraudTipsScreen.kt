package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.ScreenType
import com.example.ui.components.GoldScreenHeader
import com.example.ui.components.ScreenSubHeader
import com.example.ui.theme.DarkBorderGold
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkCardSurfaceElevated
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldShimmer
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.GoldViewModel

@Composable
fun AntiFraudTipsScreen(
    viewModel: GoldViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    // Interactive making fee checker state
    var selectedKarat by remember { mutableIntStateOf(21) }
    var makingFeeInput by remember { mutableFloatStateOf(350f) }

    // Interactive checklist state
    val checklistState = remember {
        mutableStateMapOf(
            0 to false,
            1 to false,
            2 to false,
            3 to false,
            4 to false
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 85.dp)
    ) {
        // 1. Header
        item {
            GoldScreenHeader(
                title = "دليل كشف التلاعب",
                onBackClick = { viewModel.navigateTo(ScreenType.HOME) },
                onThemeToggle = { viewModel.toggleDarkMode() },
                isDarkMode = isDarkMode,
                testTagPrefix = "tips_screen"
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 2. Banner: Brand Mascot Showcase & Protecting users from scams
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                border = BorderStroke(1.dp, DarkBorderGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1E2840), Color(0xFF131A29))
                            )
                        )
                ) {
                    // Visual Mascot Showcase Art
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_gold_guardian_showcase),
                            contentDescription = "حارس الذهب - شريكك الذكي في الذهب",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0xCC0D1322))
                                    )
                                )
                        )
                        Text(
                            text = "حارس الذهب - شريكك الذكي في الذهب 🛡️",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🛡️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "حمايتك حق مكتسب.. لا تدع أحداً يخدعك!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "سوق الذهب يعتمد على الوزن والمصنعية والعيار. إليك أهم الأدوات والإرشادات الميدانية التي تكشف أي محاولة تلاعب قبل إتمام البيع أو الشراء.",
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 3. Interactive Making Fee Fair Checker (كاشف المصنعية التفاعلي)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                border = BorderStroke(1.dp, DarkBorderGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🔍", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "كاشف المصنعية العادلة",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "اختر العيار وحرك المؤشر لقيمة المصنعية التي طلبها التاجر لتعرف هل هي عادلة أم مبالغ فيها:",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Karat Selector Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(24 to "24 (سبائك)", 21 to "21 (مشغولات)", 18 to "18 (مشغولات)").forEach { (karat, label) ->
                            val isSelected = selectedKarat == karat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0xFFD97706) else Color(0xFF1E2840))
                                    .border(
                                        1.dp,
                                        if (isSelected) GoldAccent else Color(0xFF334155),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        selectedKarat = karat
                                        makingFeeInput = when (karat) {
                                            24 -> 80f
                                            18 -> 300f
                                            else -> 350f
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val approxPrice = when (selectedKarat) {
                        24 -> 7220.0
                        18 -> 5420.0
                        else -> 6330.0
                    }
                    val fee = makingFeeInput.toInt()
                    val feePercent = if (approxPrice > 0) (fee / approxPrice) * 100.0 else 5.5

                    // Slider for making fee
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "المصنعية المطلوبة للجرام:", fontSize = 13.sp, color = Color.White)
                        Text(
                            text = "$fee ج.م / جم (${String.format(java.util.Locale.US, "%.1f", feePercent)}%)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldAccent
                        )
                    }

                    Slider(
                        value = makingFeeInput,
                        onValueChange = { makingFeeInput = it },
                        valueRange = 40f..700f,
                        steps = 65,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldAccent,
                            activeTrackColor = GoldPrimary,
                            inactiveTrackColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Verdict Card
                    val (verdictTitle, verdictMsg, verdictColor, verdictBg) = when {
                        selectedKarat == 24 -> {
                            if (fee <= 75) {
                                Quadruple("🟢 مصنعية عادلة ومثالية للسبائك", "ممتازة جداً! هذه هي المصنعية النموذجية للسبائك المغلفة ذات الكاش باك (BTC، SAM وغيرها) بين 65 إلى 75 ج.م/جم.", Color(0xFF10B981), Color(0xFF0D2A22))
                            } else if (fee <= 100) {
                                Quadruple("🟡 مصنعية مقبولة للأوزان الصغيرة", "مقبولة للسبائك متناهية الصغر (1 جم أو 2.5 جم)، لكن مبالغ فيها للسبائك الأكبر من 5 جرام.", Color(0xFFF59E0B), Color(0xFF281F0D))
                            } else {
                                Quadruple("🔴 مصنعية مبالغ فيها جداً لسبائك 24!", "احذر! السبيكة الخام لا تزيد مصنعيتها المعتادة عن 70-90 ج.م كحد أقصى مع استرداد كاش باك.", Color(0xFFEF4444), Color(0xFF2D151B))
                            }
                        }
                        selectedKarat == 21 -> {
                            if (fee < 310) {
                                Quadruple("🟢 مصنعية ممتازة ومخفضة (أقل من 5%)", "سعر ممتاز ومخفض جداً لمشغولات عيار 21 والجنيهات الذهبية والغوايش الشعبية.", Color(0xFF10B981), Color(0xFF0D2A22))
                            } else if (fee <= 380) {
                                Quadruple("🟢 مصنعية عادلة ونموذجية (5% إلى 6%)", "سعر عادل ومتوافق تماماً مع النطاق العادل المعتمد بسوق الصاغة المصري (5% إلى 6% من سعر الجرام).", Color(0xFF10B981), Color(0xFF0D2A22))
                            } else if (fee <= 450) {
                                Quadruple("🟡 مصنعية متوسطة (فوق 6% - حاول الفصال)", "مقبولة للمشغولات الدقيقة ذات التفاصيل، ننصحك بالفصال لخفض 30-50 ج في الجرام للوصول لنسبة 5-6%.", Color(0xFFF59E0B), Color(0xFF281F0D))
                            } else {
                                Quadruple("🔴 مصنعية مرتفعة ومبالغ فيها جداً!", "احذر! هذه المصنعية تتجاوز 7% وتصل لأكثر من ذلك، الصائغ يبالغ في هامش ربحه، اطلب تخفيضها فوراً أو قارن بمحل آخر.", Color(0xFFEF4444), Color(0xFF2D151B))
                            }
                        }
                        else -> { // 18
                            if (fee < 270) {
                                Quadruple("🟢 مصنعية ممتازة ومخفضة لعيار 18", "سعر ممتاز جداً وأقل من 5% لمشغولات عيار 18.", Color(0xFF10B981), Color(0xFF0D2A22))
                            } else if (fee <= 330) {
                                Quadruple("🟢 مصنعية عادلة ونموذجية (5% إلى 6%)", "هذا هو النطاق العادل والمثالي لمشغولات عيار 18 الأنيقة وفق سعر السوق الحالي.", Color(0xFF10B981), Color(0xFF0D2A22))
                            } else if (fee <= 450) {
                                Quadruple("🟡 مصنعية مقبولة للبراندات والتصميمات الإيطالية", "معتادة للماركات الشهيرة والكوليهات الإيطالية المعقدة، لكن يمكنك التفاوض لتخفيضها.", Color(0xFFF59E0B), Color(0xFF281F0D))
                            } else {
                                Quadruple("🔴 مصنعية استغلالية مبالغ فيها!", "تتجاوز 8% - 9% وهو رقم مبالغ فيه جداً للمشغولات العادية إلا إذا كانت مرصعة بأحجار ألماس معتمدة بشهادة.", Color(0xFFEF4444), Color(0xFF2D151B))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(verdictBg)
                            .border(1.dp, verdictColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = verdictTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = verdictColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = verdictMsg,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Prominent Explanatory Note (توضيح المصنعية العادلة 5% إلى 6%)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1B2333))
                            .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(text = "💡", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "توضيح مهم: المصنعية العادلة للمشغولات الذهبية تتراوح تقريباً بين 5% إلى 6% من سعر جرام الذهب الحالي (حوالي 315 إلى 380 ج.م للجرام لعيار 21، وحوالي 270 إلى 330 ج.م لعيار 18). أما السبائك (عيار 24) فمصنعيتها أقل بكثير (بين 65 إلى 95 ج.م فقط مع استرداد كاش باك عند البيع).",
                                fontSize = 11.5.sp,
                                lineHeight = 17.sp,
                                color = GoldLight
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4. Top 5 Real Scams in Gold Markets (أخطر 5 ألاعيب وكيف تتجنبها)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "🚨 أشهر 5 ألاعيب في الصاغة وكيف تحمي نفسك:",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )

                FraudTrapCard(
                    number = "1",
                    title = "فخ خصم الفصوص عند البيع (أشهر حيلة)",
                    trapDescription = "عند الشراء: يزن الصائغ القطعة بالفصوص ويحاسبك عليها بسعر الذهب كاملاً! وعندما تبيعها: يقول لك 'سأخصم جرامين فصوص'!",
                    howToDefend = "الحل: إذا كانت فاتورة الشراء الأصلية مدون بها 'ترد بفصوصها'، يحق لك قانوناً بيعها بنفس وزن الفصوص دون خصم مليمتر واحد!"
                )

                FraudTrapCard(
                    number = "2",
                    title = "فخ دمج المصنعية مع الضريبة والدمغة",
                    trapDescription = "يقول لك التاجر: 'المصنعية شاملة كل شيء 220 جنيه' حتى يخفي نسبة ربحه المبالغ فيها ويوهمك أن الزيادة للدولة.",
                    howToDefend = "الحل: اطلب تفصيل الفاتورة: كم سعر الذهب الخام؟ كم المصنعية الصافية؟ كم ضريبة القيمة المضافة (14% على المصنعية فقط وليس على الذهب)؟"
                )

                FraudTrapCard(
                    number = "3",
                    title = "فخ التلاعب بقراءة الميزان الحساس",
                    trapDescription = "عدم تصفير الميزان (Tare) قبل وضع القطعة، أو وجود تيار هواء مباشر يرفع القراءة بمقدار 50 إلى 200 مليجرام.",
                    howToDefend = "الحل: اطلب تصفير الميزان أمام عينيك حتى يظهر (0.000 جم)، وتأكد من أن الميزان داخل صندوق زجاجي مانع للهواء."
                )

                FraudTrapCard(
                    number = "4",
                    title = "فخ نسبة الكسر والهالك الزائدة عند بيع الذهب المستعمل",
                    trapDescription = "خصم نسبة 4% إلى 7% من قيمة الذهب بحجة 'الشوائب والتنظيف والصهر' عند بيعه كسر.",
                    howToDefend = "الحل: الذهب الكسر لا يُخصم منه إلا فارق سعر البيع والشراء الرسمي المعلن في التطبيق فقط، ولا تدفع أي رسوم تنظيف وهمية!"
                )

                FraudTrapCard(
                    number = "5",
                    title = "فخ السبائك غير المعتمدة وبدون كاش باك",
                    trapDescription = "بيع سبائك مقلدة أو بدون غلاف أمان سيريال (Serial Number) وبدون كاش باك عند إعادة البيع.",
                    howToDefend = "الحل: اشترِ دائماً السبائك المغلفة بغلاف أمان غير مخدوش من شركات معتمدة ومعروفة (BTC، SAM، Master Gold) مع التأكد من فاتورة رسمية."
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 5. Interactive Inspection Checklist (قائمة الفحص قبل مغادرة المحل)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📋", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "قائمة فحص قبل مغادرة محل الصاغة",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "راجع هذه البنود الـ 5 نقطة بنقطة أثناء وجودك داخل المحل لضمان حقك كاملاً:",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val items = listOf(
                        "تأكدت من تصفير الميزان (0.000 جم) وقراءة الوزن أمام عيني.",
                        "الفاتورة موضح بها رقم العيار وختم الدمغة الرسمية بوضوح.",
                        "الفاتورة تفصل سعر الجرام الخام عن المصنعية والدمغة والضريبة.",
                        "تم تدوين شرط 'ترد بفصوصها' كتابياً في الفاتورة إذا كان بها فصوص.",
                        "الفاتورة مختومة بختم المحل الأصلي وموضح بها رقم السجل التجاري."
                    )

                    items.forEachIndexed { index, text ->
                        val isChecked = checklistState[index] == true
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChecked) Color(0xFF0D2A22) else Color(0xFF161E2E))
                                .border(
                                    1.dp,
                                    if (isChecked) Color(0xFF10B981).copy(alpha = 0.6f) else Color(0xFF26334A),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { checklistState[index] = !isChecked }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checklistState[index] = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF10B981),
                                    uncheckedColor = Color(0xFF94A3B8)
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = text,
                                fontSize = 12.sp,
                                color = if (isChecked) Color.White else Color(0xFFCBD5E1),
                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        if (index < items.size - 1) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 6. Navigate to calculator CTA
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(ScreenType.BUY) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text(
                        text = "🧮 احسب عمليتك الآن وتأكد من السعر العادل",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1300)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun FraudTrapCard(
    number: String,
    title: String,
    trapDescription: String,
    howToDefend: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Trap description
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2D151B))
                    .border(1.dp, Color(0xFF7F1D1D), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "⚠️ الحيلة: $trapDescription",
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFFFECDD3)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Defense solution
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0D2A22))
                    .border(1.dp, Color(0xFF065F46), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "🛡️ كيف تحمي نفسك: $howToDefend",
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFFA7F3D0),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
