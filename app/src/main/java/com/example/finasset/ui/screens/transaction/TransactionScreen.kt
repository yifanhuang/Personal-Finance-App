package com.example.finasset.ui.screens.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.finasset.ui.components.*
import com.example.finasset.ui.navigation.Screen
import com.example.finasset.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    navController: NavHostController,
    viewModel: TransactionViewModel = viewModel()
) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val currentFilter by viewModel.filterType.collectAsState()

    val filters = listOf("ALL" to "全部", "STOCK" to "股票", "FUND" to "基金")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("交易记录", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.AddTransaction.createRoute("STOCK")) }) {
                        Icon(Icons.Filled.Add, contentDescription = "新增")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { (key, label) ->
                    FilterChip(label = label, selected = currentFilter == key, onClick = { viewModel.setFilter(key) })
                }
            }

            if (transactions.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.ReceiptLong,
                    title = "暂无交易记录",
                    subtitle = "点击右上角 + 开始记账"
                )
            } else {
                LazyColumn {
                    items(transactions, key = { it.id }) { tx ->
                        val txLabel = mapOf(
                            "BUY" to "买入", "SELL" to "卖出", "ADD" to "加仓",
                            "REDUCE" to "减仓", "DINGTOU" to "定投", "REDEEM" to "赎回",
                            "DIVIDEND" to "分红"
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (tx.assetType == "STOCK") Icons.Filled.TrendingUp else Icons.Filled.AccountBalance,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (tx.assetType == "STOCK") StockColor else FundColor
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(tx.assetName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                                        Spacer(Modifier.width(6.dp))
                                        TagChip(tagName = txLabel[tx.txType] ?: tx.txType, color = Accent)
                                    }
                                    Text(
                                        String.format("%.2f", tx.amount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "${tx.assetCode} · ${String.format("%.2f", tx.price)} × ${String.format("%.2f", tx.shares)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(tx.createTime)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (tx.notes.isNotEmpty()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(tx.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}
