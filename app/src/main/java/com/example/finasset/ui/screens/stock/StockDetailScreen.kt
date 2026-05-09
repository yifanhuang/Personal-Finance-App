package com.example.finasset.ui.screens.stock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.finasset.ui.components.*
import com.example.finasset.ui.navigation.Screen
import com.example.finasset.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(navController: NavHostController, stockId: Long, viewModel: StockViewModel = viewModel()) {
    val stock by viewModel.stockDetail.collectAsState()
    val summary by viewModel.stockSummary.collectAsState()
    val transactions by viewModel.stockTransactions.collectAsState()
    val redUpGreenDown by viewModel.redUpGreenDown.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val klineData by viewModel.klineData.collectAsState()
    val klinePeriod by viewModel.klinePeriod.collectAsState()
    val isLoadingKline by viewModel.isLoadingKline.collectAsState()

    LaunchedEffect(stockId) { viewModel.loadStockDetail(stockId) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPriceDialog by remember { mutableStateOf(false) }
    var priceInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stock?.name ?: "", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { showPriceDialog = true }) { Icon(Icons.Filled.Edit, null) }
                    IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Filled.Delete, null) }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading && stock == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (stock == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("\u80A1\u7968\u4E0D\u5B58\u5728", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val s = stock!!
            val mv = s.shares * s.currentPrice; val cost = s.shares * s.buyPrice
            val pnl = mv - cost; val pnlPct = if (cost > 0) (pnl / cost * 100) else 0.0

            Column(Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())) {
                Card(
                    Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Ink.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(4.dp), elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Column {
                                Text(s.code, fontFamily = FontFamily.Monospace, fontSize = 12.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(8.dp))
                                Text(String.format("%.2f", mv), fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Ink)
                                Text("\u6301\u4ED3\u5E02\u503C", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                PnlText(value = pnl, redUpGreenDown = redUpGreenDown, fontSize = 22)
                                PnlText(value = pnlPct, showPercent = true, redUpGreenDown = redUpGreenDown, fontSize = 14)
                                Text("\u6D6E\u52A8\u4E8F\u76C8", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(16.dp)); Divider(color = Ink.copy(alpha = 0.08f)); Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            DetailMinimal("\u4E70\u5165\u4EF7", String.format("%.2f", s.buyPrice))
                            DetailMinimal("\u5F53\u524D\u4EF7", String.format("%.2f", s.currentPrice))
                            DetailMinimal("\u6301\u4ED3\u6570", String.format("%.0f", s.shares))
                            DetailMinimal("\u6210\u672C", String.format("%.2f", cost))
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            DetailMinimal("\u4E70\u5165\u65F6\u95F4", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(s.createTime)))
                            DetailMinimal("\u624B\u7EED\u8D39", String.format("%.2f", s.fee))
                            DetailMinimal("\u6301\u4ED3\u5360\u6BD4", if (summary != null) String.format("%.1f%%", summary!!.weight) else "-")
                            DetailMinimal("", "")
                        }
                    }
                }

                SectionHeaderSerif(title = "K\u7EBF\u56FE")
                LazyRow(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("day" to "\u65E5K", "week" to "\u5468K", "month" to "\u6708K")) { (key, label) ->
                        FilterChip(label = label, selected = klinePeriod == key, onClick = { viewModel.loadKLine(s.code, key) })
                    }
                }
                if (isLoadingKline) {
                    Box(Modifier.fillMaxWidth().height(320.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp) }
                } else {
                    EChartsKLine(
                        klineData = klineData,
                        isCandlestick = true,
                        redUpGreenDown = redUpGreenDown,
                        period = klinePeriod,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(320.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))
                SectionHeaderSerif(title = "\u4EA4\u6613\u8BB0\u5F55")
                if (transactions.isEmpty()) {
                    EmptyState(icon = Icons.Filled.ReceiptLong, title = "\u6682\u65E0\u4EA4\u6613\u8BB0\u5F55")
                } else {
                    LazyColumn {
                        items(transactions) { tx ->
                            Card(
                                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
                                colors = CardDefaults.cardColors(containerColor = Ink.copy(alpha = 0.02f)),
                                shape = RoundedCornerShape(4.dp), elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        FilterChip(label = tx.txType, selected = true, onClick = {})
                                        Spacer(Modifier.height(2.dp))
                                        Text(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(tx.createTime)),
                                            fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(String.format("%.2f x %.0f", tx.price, tx.shares), fontFamily = FontFamily.Serif, fontSize = 13.sp)
                                        Text(String.format("%.2f", tx.amount), fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }

        if (showDeleteDialog) ConfirmDialog(
            title = "\u5220\u9664\u6301\u4ED3", message = "\u786E\u5B9A\u5220\u9664\u8BE5\u80A1\u7968\u6301\u4ED3\u53CA\u76F8\u5173\u8BB0\u5F55\uFF1F", confirmText = "\u5220\u9664",
            onConfirm = { viewModel.deleteStock(stockId); showDeleteDialog = false; navController.popBackStack() }, onDismiss = { showDeleteDialog = false }
        )
        if (showPriceDialog) AlertDialog(
            onDismissRequest = { showPriceDialog = false }, title = { Text("\u66F4\u65B0\u5F53\u524D\u4EF7", fontFamily = FontFamily.Serif) },
            text = { OutlinedTextField(priceInput, { priceInput = it }, label = { Text("\u5F53\u524D\u4EF7\u683C") }, singleLine = true) },
            confirmButton = { TextButton(onClick = { priceInput.toDoubleOrNull()?.let { viewModel.updatePrice(stockId, it) }; showPriceDialog = false; priceInput = "" }) { Text("\u786E\u8BA4") } },
            dismissButton = { TextButton(onClick = { showPriceDialog = false }) { Text("\u53D6\u6D88") } }
        )
    }
}

@Composable
private fun DetailMinimal(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
        Text(value, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}

@Composable
private fun SectionHeaderSerif(title: String) {
    Text(title, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), letterSpacing = 0.5.sp)
}
