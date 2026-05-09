package com.example.finasset.ui.screens.fund

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
fun FundDetailScreen(
    navController: NavHostController,
    fundId: Long,
    viewModel: FundViewModel = viewModel()
) {
    val fund by viewModel.fundDetail.collectAsState()
    val summary by viewModel.fundSummary.collectAsState()
    val transactions by viewModel.fundTransactions.collectAsState()
    val redUpGreenDown by viewModel.redUpGreenDown.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val navHistory by viewModel.navHistory.collectAsState()
    val isLoadingNav by viewModel.isLoadingNav.collectAsState()

    LaunchedEffect(fundId) { viewModel.loadFundDetail(fundId) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showNavDialog by remember { mutableStateOf(false) }
    var navInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(fund?.name ?: "", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { showNavDialog = true }) { Icon(Icons.Filled.Edit, null) }
                    IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Filled.Delete, null) }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading && fund == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (fund == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) { Text("\u57FA\u91D1\u4E0D\u5B58\u5728") }
        } else {
            val f = fund!!
            val mv = f.shares * f.currentNav
            val pnl = mv - f.investAmount
            val pnlPct = if (f.investAmount > 0) (pnl / f.investAmount * 100) else 0.0

            Column(Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())) {
                Card(
                    Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Ink.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(4.dp), elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Column {
                                Text(f.code, fontFamily = FontFamily.Monospace, fontSize = 12.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Spacer(Modifier.height(16.dp))
                        Divider(color = Ink.copy(alpha = 0.08f))
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            DetailMinimal("\u4E70\u5165\u51C0\u503C", String.format("%.4f", f.buyNav))
                            DetailMinimal("\u5F53\u524D\u51C0\u503C", String.format("%.4f", f.currentNav))
                            DetailMinimal("\u6301\u6709\u4EFD\u989D", String.format("%.2f", f.shares))
                            DetailMinimal("\u6295\u5165\u91D1\u989D", String.format("%.2f", f.investAmount))
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            DetailMinimal("\u7533\u8D2D\u65F6\u95F4", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(f.createTime)))
                            DetailMinimal("\u624B\u7EED\u8D39", String.format("%.2f", f.fee))
                            DetailMinimal("\u6301\u4ED3\u5360\u6BD4", if (summary != null) String.format("%.1f%%", summary!!.weight) else "-")
                            DetailMinimal("", "")
                        }
                        if (f.isDingtou) {
                            Spacer(Modifier.height(8.dp))
                            AssistChip(onClick = {}, label = { Text("\u5B9A\u6295: ${String.format("%.0f", f.dingtouAmount)}\u5143") },
                                leadingIcon = { Icon(Icons.Filled.Schedule, null, Modifier.size(16.dp)) })
                        }
                    }
                }

                // NAV trend chart
                SectionHeaderSerif(title = "\u51C0\u503C\u8D70\u52BF")
                if (isLoadingNav) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp) }
                } else if (navHistory.isNotEmpty()) {
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    val baseDates = navHistory.map { it.date }.toMutableList()
                    val baseCloses = navHistory.map { it.nav }.toMutableList()
                    if (baseDates.isNotEmpty()) {
                        if (baseDates.last() == today) baseCloses[baseCloses.lastIndex] = f.currentNav
                        else { baseDates.add(today); baseCloses.add(f.currentNav) }
                    }
                    val klineData = com.example.finasset.data.network.QuoteApi.buildCandlesFromCloseSeries(
                        dates = baseDates,
                        closes = baseCloses
                    )
                    EChartsKLine(
                        klineData = klineData,
                        isCandlestick = true,
                        redUpGreenDown = redUpGreenDown,
                        period = "day",
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(280.dp)
                    )
                } else {
                    Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Text("\u52A0\u8F7D\u4E2D...\u8BF7\u7A0D\u540E", fontFamily = FontFamily.SansSerif, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
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
                                        Text(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(tx.createTime)),
                                            fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(String.format("%.4f x %.2f", tx.price, tx.shares), fontFamily = FontFamily.Serif, fontSize = 13.sp)
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
            title = "\u5220\u9664\u6301\u4ED3",
            message = "\u786E\u5B9A\u5220\u9664\u8BE5\u57FA\u91D1\u6301\u4ED3\u53CA\u76F8\u5173\u8BB0\u5F55\uFF1F",
            confirmText = "\u5220\u9664",
            onConfirm = { viewModel.deleteFund(fundId); showDeleteDialog = false; navController.popBackStack() },
            onDismiss = { showDeleteDialog = false })
        if (showNavDialog) AlertDialog(
            onDismissRequest = { showNavDialog = false },
            title = { Text("\u66F4\u65B0\u51C0\u503C", fontFamily = FontFamily.Serif) },
            text = { OutlinedTextField(navInput, { navInput = it }, label = { Text("\u5F53\u524D\u51C0\u503C") }, singleLine = true) },
            confirmButton = { TextButton(onClick = { navInput.toDoubleOrNull()?.let { viewModel.updateNav(fundId, it) }; showNavDialog = false; navInput = "" }) { Text("\u786E\u8BA4") } },
            dismissButton = { TextButton(onClick = { showNavDialog = false }) { Text("\u53D6\u6D88") } })
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
