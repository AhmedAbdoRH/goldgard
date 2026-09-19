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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TransactionEntity
import com.example.model.ScreenType
import com.example.ui.components.ScreenSubHeader
import com.example.ui.theme.BuyGreenStart
import com.example.ui.theme.CardSurface
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.HistoryCyan
import com.example.ui.theme.HistoryCyanGradientBrush
import com.example.ui.theme.PurpleGradientEnd
import com.example.ui.theme.PurpleGradientStart
import com.example.ui.theme.SellRedStart
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.GoldViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

@Composable
fun HistoryScreen(
    viewModel: GoldViewModel,
    modifier: Modifier = Modifier
) {
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val filter by viewModel.historyFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.historySearchQuery.collectAsStateWithLifecycle()
    val selectedCountry by viewModel.selectedCountry.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }
    var transactionToEditNote by remember { mutableStateOf<TransactionEntity?>(null) }
    var editNoteText by remember { mutableStateOf("") }

    val filteredTransactions = remember(allTransactions, filter, searchQuery) {
        allTransactions.filter { item ->
            val matchesType = when (filter) {
                "BUY" -> item.type == "BUY"
                "SELL" -> item.type == "SELL"
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.note.contains(searchQuery.trim(), ignoreCase = true) ||
                item.formattedDate.contains(searchQuery.trim(), ignoreCase = true) ||
                "عيار ${item.karat}".contains(searchQuery.trim())
            }
            matchesType && matchesSearch
        }
    }

    val decimalFormat = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.US))

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(com.example.ui.theme.GoldTheme.colors.background),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // 1. Header
        item {
            com.example.ui.components.GoldScreenHeader(
                title = "سجل العمليات",
                onBackClick = { viewModel.navigateTo(ScreenType.HOME) },
                onThemeToggle = { viewModel.toggleDarkMode() },
                isDarkMode = isDarkMode,
                testTagPrefix = "history"
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 2. Search Field
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setHistorySearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "🔍 ابحث في السجل (بالوصف، العيار، التاريخ...)",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setHistorySearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "مسح البحث",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF131A26),
                        unfocusedContainerColor = Color(0xFF131A26),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = Color(0xFF26334A)
                    )
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 3. Filter Buttons (3 columns grid: All, Buy, Sell)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All
                val isAllSelected = filter == "ALL"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isAllSelected) Color(0xFFB45309) else Color(0xFF131A26))
                        .border(
                            width = if (isAllSelected) 1.5.dp else 1.dp,
                            color = if (isAllSelected) GoldAccent else Color(0xFF26334A),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.setHistoryFilter("ALL") }
                        .testTag("filter_all"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "الكل (${allTransactions.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAllSelected) Color.White else Color(0xFFCBD5E1)
                    )
                }

                // Buy
                val isBuySelected = filter == "BUY"
                val buyCount = allTransactions.count { it.type == "BUY" }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isBuySelected) Color(0xFF065F46) else Color(0xFF131A26))
                        .border(
                            width = if (isBuySelected) 1.5.dp else 1.dp,
                            color = if (isBuySelected) Color(0xFF34D399) else Color(0xFF26334A),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.setHistoryFilter("BUY") }
                        .testTag("filter_buy"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "شراء ($buyCount)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBuySelected) Color.White else Color(0xFFCBD5E1)
                    )
                }

                // Sell
                val isSellSelected = filter == "SELL"
                val sellCount = allTransactions.count { it.type == "SELL" }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSellSelected) Color(0xFF9F1239) else Color(0xFF131A26))
                        .border(
                            width = if (isSellSelected) 1.5.dp else 1.dp,
                            color = if (isSellSelected) Color(0xFFFB7185) else Color(0xFF26334A),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.setHistoryFilter("SELL") }
                        .testTag("filter_sell"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "بيع ($sellCount)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSellSelected) Color.White else Color(0xFFCBD5E1)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 4. Transactions List or Empty State
        if (filteredTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
                    border = BorderStroke(1.dp, Color(0xFF26334A))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = if (searchQuery.isNotEmpty()) "🔍" else "📭", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "لا توجد نتائج تطابق بحثك" else "لا توجد عمليات مسجلة حالياً",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "جرب البحث بكلمة أخرى أو امسح البحث" else "احسب أسعار الشراء أو البيع واضغط على 'حفظ في السجل' لمتابعتها هنا.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { item ->
                TransactionCard(
                    transaction = item,
                    decimalFormat = decimalFormat,
                    currencySymbol = selectedCountry.currencySymbol,
                    onDelete = { viewModel.deleteTransaction(item.id) },
                    onEditNote = {
                        transactionToEditNote = item
                        editNoteText = item.note
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 5. Clear All Button
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Button(
                        onClick = { showClearDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("history_clear_all_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text(
                            text = "🗑️ مسح الكل",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Dialog for Editing/Adding Note
    transactionToEditNote?.let { target ->
        AlertDialog(
            onDismissRequest = { transactionToEditNote = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "✏️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (target.note.isBlank()) "إضافة وصف للعملية" else "تعديل وصف العملية",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "اكتب وصفاً أو اسماً لتمييز هذه العملية (عيار ${target.karat} - ${target.weight} جرام):",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    OutlinedTextField(
                        value = editNoteText,
                        onValueChange = { editNoteText = it },
                        placeholder = { Text("مثال: شبكة، غوايش، خاتم...", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleGradientStart,
                            focusedContainerColor = CardSurface,
                            unfocusedContainerColor = CardSurface
                        )
                    )

                    // Quick suggestion chips
                    Text(
                        text = "اقتراحات سريعة:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )

                    val suggestions = listOf("💍 شبكة", "🪙 سبيكة", "✨ غوايش", "💍 خاتم زواج", "🎁 هدية", "💰 بيع كسر")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        suggestions.take(3).forEach { tag ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { editNoteText = tag.substringAfter(" ") },
                                color = Color(0xFFF3E8FF),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PurpleGradientStart,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        suggestions.drop(3).forEach { tag ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { editNoteText = tag.substringAfter(" ") },
                                color = Color(0xFFF3E8FF),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PurpleGradientStart,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateTransactionNote(target.id, editNoteText)
                        transactionToEditNote = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleGradientStart),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("حفظ الوصف", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToEditNote = null }) {
                    Text("إلغاء", color = TextMuted)
                }
            }
        )
    }

    // Confirmation Dialog for Clear All
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = "تأكيد مسح السجل",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = "هل أنت متأكد من رغبتك في حذف جميع العمليات المحفوظة في السجل نهائياً؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllTransactions()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("مسح الكل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun TransactionCard(
    transaction: TransactionEntity,
    decimalFormat: DecimalFormat,
    currencySymbol: String,
    onDelete: () -> Unit,
    onEditNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBuy = transaction.type == "BUY"
    val badgeBg = if (isBuy) Color(0xFF0C2B1D) else Color(0xFF331218)
    val badgeColor = if (isBuy) Color(0xFF6EE7B7) else Color(0xFFFDA4AF)
    val badgeText = if (isBuy) "🛒 شراء" else "💰 بيع"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
        border = BorderStroke(1.dp, Color(0xFF26334A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Note Banner / Tag (Champagne Gold Luxury Pill)
            if (transaction.note.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E283D))
                        .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "🏷️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = transaction.note,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight
                            )
                        }

                        IconButton(
                            onClick = onEditNote,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل الوصف",
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onEditNote() }
                        .padding(vertical = 2.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "إضافة وصف",
                        tint = GoldLight,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+ إضافة وصف للعملية",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = GoldLight
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Top row: Type Badge, Date, Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBg)
                            .border(0.8.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = transaction.formattedDate,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF1E283D))
            Spacer(modifier = Modifier.height(10.dp))

            // Details row: Karat, Weight, Price per gram
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "العيار", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text(
                        text = "عيار ${transaction.karat}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column {
                    Text(text = "الوزن", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text(
                        text = "${transaction.weight} جرام",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column {
                    Text(text = "سعر الجرام", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text(
                        text = "${decimalFormat.format(transaction.pricePerGram)} $currencySymbol",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Making / Deduction info
            if (isBuy && transaction.makingCharges > 0) {
                val perGram = if (transaction.weight > 0) transaction.makingCharges / transaction.weight else 0.0
                val pct = if (transaction.pricePerGram > 0 && perGram > 0) (perGram / transaction.pricePerGram) * 100.0 else 0.0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "المصنعية (${decimalFormat.format(pct)}%):", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text(
                        text = "${decimalFormat.format(transaction.makingCharges)} $currencySymbol (${decimalFormat.format(perGram)} $currencySymbol/ج)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            } else if (!isBuy && transaction.deductionPercent > 0) {
                val deductionPerGram = transaction.pricePerGram * (transaction.deductionPercent / 100.0)
                val deductionTotal = deductionPerGram * transaction.weight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "خصم الهالك (${decimalFormat.format(transaction.deductionPercent)}%):", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text(
                        text = "-${decimalFormat.format(deductionTotal)} $currencySymbol (${decimalFormat.format(deductionPerGram)} $currencySymbol/ج)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFDA4AF)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Shop Price Comparison Info (if entered)
            if (transaction.shopQuotedPrice > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F1522))
                        .border(1.dp, Color(0xFF1E283D), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سعر المحل: ${decimalFormat.format(transaction.shopQuotedPrice)} $currencySymbol",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        val diff = transaction.priceDifference
                        if (diff != 0.0) {
                            val isOver = if (isBuy) diff > 0 else diff < 0
                            val diffColor = if (isOver) Color(0xFFFF5252) else Color(0xFF00FF66)
                            val diffLabel = if (diff > 0) "+${decimalFormat.format(abs(diff))} $currencySymbol" else "-${decimalFormat.format(abs(diff))} $currencySymbol"
                            Text(
                                text = "الفرق: $diffLabel",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = diffColor
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Total Fair Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBuy) "السعر العادل الإجمالي:" else "القيمة الإجمالية للبيع:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE2E8F0)
                )
                Text(
                    text = "${decimalFormat.format(transaction.totalFairPrice)} $currencySymbol",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isBuy) GoldAccent else Color(0xFF34D399)
                )
            }
        }
    }
}
