package com.example.finasset.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
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
fun DashboardScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = viewModel()
) {
    val overview by viewModel.overview.collectAsState()
    val curvePoints by viewModel.curvePoints.collectAsState()
    val curvePeriod by viewModel.curvePeriod.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingCurve by viewModel.isLoadingCurve.collectAsState()
    val isRefreshing by viewModel.isLoading.collectAsState()
    val redUpGreenDown by viewModel.redUpGreenDown.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("\u8D44\u4EA7\u7BA1\u5BB6", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                },
                actions = {
                    if (isRefreshing) {
                        CircularProgressIndicator(Modifier.size(20.dp).padding(end = 8.dp), strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "\u8BBE\u7F6E")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.refreshWithPrices() },
                containerColor = Ink
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "\u5237\u65B0", tint = Paper)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (isLoading && curvePoints.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 72.dp)
            ) {
                // guizang: 总资产 - 大字报
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Ink.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "\u603B\u8D44\u4EA7",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            String.format("%.2f", overview.totalAssets),
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = Ink
                        )
                    }
                }

                // 盈亏三指标
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val pnlColor = when {
                        overview.dailyPnl > 0 && redUpGreenDown -> RedUp
                        overview.dailyPnl > 0 && !redUpGreenDown -> GreenUp
                        overview.dailyPnl < 0 && redUpGreenDown -> GreenDown
                        overview.dailyPnl < 0 && !redUpGreenDown -> RedDown
                        else -> Ink
                    }
                    StatCardMinimal("\u5F53\u65E5\u4E8F\u76C8", String.format("%.2f", overview.dailyPnl), pnlColor, Modifier.weight(1f))
                    StatCardMinimal("\u7D2F\u8BA1\u6536\u76CA", String.format("%.2f", overview.totalPnl),
                        if (overview.totalPnl >= 0 && redUpGreenDown) RedUp else if (overview.totalPnl >= 0) GreenUp else if (redUpGreenDown) GreenDown else RedDown,
                        Modifier.weight(1f))
                }

                // 资产配比
                SectionHeaderSerif(title = "\u8D44\u4EA7\u914D\u7F6E")
                val total = overview.totalAssets
                val stockPct = if (total > 0) (overview.stockValue / total * 100) else 0.0
                val fundPct = if (total > 0) (overview.fundValue / total * 100) else 0.0
                val cashPct = if (total > 0) (overview.cashValue / total * 100) else 0.0
                if (total > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                    ) {
                        if (stockPct > 0) Box(Modifier.weight(stockPct.toFloat().coerceAtLeast(0f)).fillMaxHeight().background(StockColor))
                        if (fundPct > 0) { Spacer(Modifier.width(2.dp)); Box(Modifier.weight(fundPct.toFloat().coerceAtLeast(0f)).fillMaxHeight().background(Ink)) }
                        if (cashPct > 0) { Spacer(Modifier.width(2.dp)); Box(Modifier.weight(cashPct.toFloat().coerceAtLeast(0f)).fillMaxHeight().background(AccentSecondary)) }
                    }
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Legend("\u80A1\u7968", StockColor, String.format("%.1f%%", stockPct), String.format("%.0f", overview.stockValue))
                        Legend("\u57FA\u91D1", Ink, String.format("%.1f%%", fundPct), String.format("%.0f", overview.fundValue))
                        Legend("\u73B0\u91D1", AccentSecondary, String.format("%.1f%%", cashPct), String.format("%.0f", overview.cashValue))
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 总收益走势
                SectionHeaderSerif(title = "\u603B\u6536\u76CA\u8D70\u52BF")
                LazyRow(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("day" to "\u65E5", "week" to "\u5468", "month" to "\u6708")) { (key, label) ->
                        FilterChip(label = label, selected = curvePeriod == key, onClick = { viewModel.switchPeriod(key) })
                    }
                }
                if (isLoadingCurve) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp) }
                } else if (curvePoints.isNotEmpty()) {
                    val klineData = com.example.finasset.data.network.QuoteApi.buildCandlesFromCloseSeries(
                        dates = curvePoints.map { it.date },
                        closes = curvePoints.map { it.totalValue }
                    )
                    EChartsKLine(
                        klineData = klineData,
                        isCandlestick = true,
                        redUpGreenDown = redUpGreenDown,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(280.dp)
                    )
                } else {
                    Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (overview.stockCount + overview.fundCount == 0) "\u8BF7\u6DFB\u52A0\u6301\u4ED3\u540E\u67E5\u770B\u8D70\u52BF" else "\u52A0\u8F7D\u8D70\u52BF\u6570\u636E\u4E2D...\u8BF7\u70B9\u51FB\u5237\u65B0",
                                fontFamily = FontFamily.SansSerif, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 快捷操作
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.AddStock.route) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp)
                    ) { Text("+\u65B0\u589E\u80A1\u7968", fontFamily = FontFamily.SansSerif, fontSize = 13.sp) }
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.AddFund.route) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp)
                    ) { Text("+\u65B0\u589E\u57FA\u91D1", fontFamily = FontFamily.SansSerif, fontSize = 13.sp) }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionHeaderSerif(title: String) {
    Text(
        title,
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Ink,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun StatCardMinimal(title: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Ink.copy(alpha = 0.03f)),
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        }
    }
}

@Composable
private fun Legend(name: String, color: Color, pct: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).clip(RoundedCornerShape(1.dp)).background(color))
            Spacer(Modifier.width(4.dp))
            Text(name, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(pct, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(value, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
