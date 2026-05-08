package com.example.finasset.ui.screens.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.finasset.ui.components.*
import com.example.finasset.ui.navigation.Screen
import com.example.finasset.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    navController: NavHostController,
    viewModel: ReportViewModel = viewModel()
) {
    val pnlRecords by viewModel.pnlRecords.collectAsState()
    val report by viewModel.periodReport.collectAsState()
    val redUpGreenDown by viewModel.redUpGreenDown.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("收益报表", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "刷新")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                SectionHeader(title = "收益概览")
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("累计总盈亏", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Spacer(Modifier.height(4.dp))
                        PnlText(value = report.totalPnl, redUpGreenDown = redUpGreenDown, fontSize = 28)
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(title = "股票盈亏", value = String.format("%.2f", report.stockPnl),
                        valueColor = if (redUpGreenDown) {
                            if (report.stockPnl >= 0) RedUp else GreenDown
                        } else { if (report.stockPnl >= 0) GreenUp else RedDown },
                        modifier = Modifier.weight(1f))
                    StatCard(title = "基金盈亏", value = String.format("%.2f", report.fundPnl),
                        valueColor = if (redUpGreenDown) {
                            if (report.fundPnl >= 0) RedUp else GreenDown
                        } else { if (report.fundPnl >= 0) GreenUp else RedDown },
                        modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(title = "分红收入", value = String.format("%.2f", report.dividendIncome), modifier = Modifier.weight(1f))
                    StatCard(title = "累计投入", value = String.format("%.2f", report.totalInvest), modifier = Modifier.weight(1f))
                    StatCard(title = "累计卖出", value = String.format("%.2f", report.totalSell), modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(16.dp))

                SectionHeader(title = "资产走势 (近90日)")
                if (pnlRecords.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val maxValue = pnlRecords.maxOfOrNull { it.cumulativeAssets } ?: 1.0
                            val minValue = pnlRecords.minOfOrNull { it.cumulativeAssets } ?: 0.0
                            val range = (maxValue - minValue).coerceAtLeast(1.0)

                            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    val displayRecords = if (pnlRecords.size > 90) pnlRecords.takeLast(90) else pnlRecords
                                    displayRecords.forEach { record ->
                                        val heightFraction = ((record.cumulativeAssets - minValue) / range).toFloat().coerceIn(0.02f, 1f)
                                        val barColor = if (record.pnl >= 0) RedUp else GreenDown
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight(heightFraction)
                                                .padding(horizontal = 0.5.dp)
                                                .clip(RoundedCornerShape(topStart = 1.dp, topEnd = 1.dp))
                                                .background(barColor)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            if (pnlRecords.size >= 2) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(pnlRecords.first().date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(pnlRecords.last().date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.Transaction.route) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.ReceiptLong, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("查看交易明细")
                    }
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}
