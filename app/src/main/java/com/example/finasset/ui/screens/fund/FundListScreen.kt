package com.example.finasset.ui.screens.fund

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.finasset.data.db.entity.FundEntity
import com.example.finasset.ui.components.*
import com.example.finasset.ui.navigation.Screen
import com.example.finasset.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundListScreen(
    navController: NavHostController,
    viewModel: FundViewModel = viewModel()
) {
    val funds by viewModel.funds.collectAsState()
    val redUpGreenDown by viewModel.redUpGreenDown.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("基金资产", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Market.route) }) {
                        Icon(Icons.Filled.Search, contentDescription = "搜索")
                    }
                    IconButton(onClick = { navController.navigate(Screen.AddFund.route) }) {
                        Icon(Icons.Filled.Add, contentDescription = "新增")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (funds.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.AccountBalance,
                title = "暂无基金持仓",
                subtitle = "点击右上角 + 添加你的第一只基金"
            )
        } else {
            val totalValue = funds.sumOf { it.shares * it.currentNav }
            val totalCost = funds.sumOf { it.investAmount }
            val totalPnl = totalValue - totalCost

            Column(modifier = Modifier.padding(innerPadding)) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatColumn("持仓市值", String.format("%.2f", totalValue))
                        StatColumn("投入本金", String.format("%.2f", totalCost))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("浮动盈亏", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            PnlText(value = totalPnl, redUpGreenDown = redUpGreenDown, fontSize = 16)
                        }
                    }
                }

                LazyColumn {
                    items(funds, key = { it.id }) { fund ->
                        val mv = fund.shares * fund.currentNav
                        val pnl = mv - fund.investAmount
                        val pnlPct = if (fund.investAmount > 0) (pnl / fund.investAmount * 100) else 0.0
                        val typeLabel = when (fund.fundType) {
                            "STOCK" -> "股票型"
                            "MIXED" -> "混合型"
                            "BOND" -> "债券型"
                            "INDEX" -> "指数型"
                            "MMF" -> "货币型"
                            else -> fund.fundType
                        }
                        AssetItemCard(
                            title = fund.name,
                            subtitle = "${fund.code} · ${typeLabel}${if (fund.isDingtou) " · 定投" else ""} · ${java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault()).format(java.util.Date(fund.createTime))}",
                            value = String.format("%.2f", mv),
                            pnlValue = pnl,
                            pnlPercent = pnlPct,
                            redUpGreenDown = redUpGreenDown,
                            tag = fund.tag,
                            tagColor = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(fund.tagColor)),
                            onClick = { navController.navigate(Screen.FundDetail.createRoute(fund.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
