package com.example.finasset.ui.screens.stock

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.finasset.data.db.entity.StockEntity
import com.example.finasset.ui.components.*
import com.example.finasset.ui.navigation.Screen
import com.example.finasset.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockListScreen(
    navController: NavHostController,
    viewModel: StockViewModel = viewModel()
) {
    val stocks by viewModel.stocks.collectAsState()
    val redUpGreenDown by viewModel.redUpGreenDown.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("股票资产", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Market.route) }) {
                        Icon(Icons.Filled.Search, contentDescription = "搜索")
                    }
                    IconButton(onClick = { navController.navigate(Screen.AddStock.route) }) {
                        Icon(Icons.Filled.Add, contentDescription = "新增")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (stocks.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.TrendingUp,
                title = "暂无股票持仓",
                subtitle = "点击右上角 + 添加你的第一只股票"
            )
        } else {
            val totalValue = stocks.sumOf { it.shares * it.currentPrice }
            val totalCost = stocks.sumOf { it.shares * it.buyPrice }
            val totalPnl = totalValue - totalCost

            Column(modifier = Modifier.padding(innerPadding)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("持仓市值", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format("%.2f", totalValue), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("持仓成本", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format("%.2f", totalCost), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("浮动盈亏", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            PnlText(value = totalPnl, redUpGreenDown = redUpGreenDown, fontSize = 16)
                        }
                    }
                }

                LazyColumn {
                    items(stocks, key = { it.id }) { stock ->
                        val mv = stock.shares * stock.currentPrice
                        val cost = stock.shares * stock.buyPrice
                        val pnl = mv - cost
                        val pnlPct = if (cost > 0) (pnl / cost * 100) else 0.0
                        AssetItemCard(
                            title = stock.name,
                            subtitle = "${stock.code} · ${String.format("%.0f", stock.shares)}股 · ${java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault()).format(java.util.Date(stock.createTime))}",
                            value = String.format("%.2f", mv),
                            pnlValue = pnl,
                            pnlPercent = pnlPct,
                            redUpGreenDown = redUpGreenDown,
                            tag = stock.tag,
                            tagColor = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(stock.tagColor)),
                            onClick = { navController.navigate(Screen.StockDetail.createRoute(stock.id)) }
                        )
                    }
                }
            }
        }
    }
}
