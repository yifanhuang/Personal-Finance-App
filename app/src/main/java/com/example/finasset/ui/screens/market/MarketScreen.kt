package com.example.finasset.ui.screens.market

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.finasset.ui.components.*
import com.example.finasset.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    navController: NavHostController,
    viewModel: MarketViewModel = viewModel()
) {
    val watchItems by viewModel.watchItems.collectAsState()
    val alerts by viewModel.alerts.collectAsState()

    var showAddWatch by remember { mutableStateOf(false) }
    var showAddAlert by remember { mutableStateOf(false) }
    var watchType by remember { mutableStateOf("STOCK") }
    var watchCode by remember { mutableStateOf("") }
    var watchName by remember { mutableStateOf("") }
    var alertCode by remember { mutableStateOf("") }
    var alertName by remember { mutableStateOf("") }
    var alertType by remember { mutableStateOf("STOCK") }
    var alertTarget by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("行情监控", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showAddWatch = true }) {
                        Icon(Icons.Filled.Visibility, contentDescription = "添加关注")
                    }
                    IconButton(onClick = { showAddAlert = true }) {
                        Icon(Icons.Filled.Notifications, contentDescription = "添加提醒")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("自选列表", modifier = Modifier.padding(12.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("价格提醒", modifier = Modifier.padding(12.dp))
                }
            }

            when (selectedTab) {
                0 -> {
                    if (watchItems.isEmpty()) {
                        EmptyState(icon = Icons.Filled.Visibility, title = "暂无自选", subtitle = "点击右上角添加关注标的")
                    } else {
                        LazyColumn {
                            items(watchItems) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(10.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(item.name, fontWeight = FontWeight.Medium)
                                            Text(
                                                "${item.code} · ${if (item.assetType == "STOCK") "股票" else "基金"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(String.format("%.2f", item.currentPrice), fontWeight = FontWeight.Bold)
                                            Text(
                                                String.format("%.2f%%", item.changePercent),
                                                color = if (item.changePercent >= 0) RedUp else GreenDown,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    if (alerts.isEmpty()) {
                        EmptyState(icon = Icons.Filled.Notifications, title = "暂无提醒", subtitle = "点击右上角设置价格提醒")
                    } else {
                        LazyColumn {
                            items(alerts) { alert ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(10.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(alert.assetName, fontWeight = FontWeight.Medium)
                                            Text(
                                                "${alert.assetCode} · ${alert.alertType} ${String.format("%.2f", alert.targetPrice)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(onClick = { viewModel.deleteAlert(alert.id) }) {
                                            Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddWatch) {
            AddWatchDialog(
                watchType = watchType,
                onTypeChange = { watchType = it },
                code = watchCode,
                onCodeChange = { watchCode = it },
                name = watchName,
                onNameChange = { watchName = it },
                onConfirm = {
                    if (watchCode.isNotBlank() && watchName.isNotBlank()) {
                        viewModel.addWatchItem(watchType, watchCode, watchName)
                        showAddWatch = false
                        watchCode = ""
                        watchName = ""
                    }
                },
                onDismiss = { showAddWatch = false }
            )
        }

        if (showAddAlert) {
            AddAlertDialog(
                alertType = alertType,
                onTypeChange = { alertType = it },
                code = alertCode,
                onCodeChange = { alertCode = it },
                name = alertName,
                onNameChange = { alertName = it },
                targetPrice = alertTarget,
                onTargetChange = { alertTarget = it },
                onConfirm = {
                    val target = alertTarget.toDoubleOrNull() ?: 0.0
                    if (alertCode.isNotBlank() && alertName.isNotBlank() && target > 0) {
                        viewModel.addPriceAlert(alertType, alertCode, alertName, "PRICE", target)
                        showAddAlert = false
                        alertCode = ""
                        alertName = ""
                        alertTarget = ""
                    }
                },
                onDismiss = { showAddAlert = false }
            )
        }
    }
}

@Composable
private fun AddWatchDialog(
    watchType: String,
    onTypeChange: (String) -> Unit,
    code: String,
    onCodeChange: (String) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加自选") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(label = "股票", selected = watchType == "STOCK", onClick = { onTypeChange("STOCK") })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(label = "基金", selected = watchType == "FUND", onClick = { onTypeChange("FUND") })
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = code, onValueChange = onCodeChange, label = { Text("代码") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("添加") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun AddAlertDialog(
    alertType: String,
    onTypeChange: (String) -> Unit,
    code: String,
    onCodeChange: (String) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    targetPrice: String,
    onTargetChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("价格提醒") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(label = "股票", selected = alertType == "STOCK", onClick = { onTypeChange("STOCK") })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(label = "基金", selected = alertType == "FUND", onClick = { onTypeChange("FUND") })
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = code, onValueChange = onCodeChange, label = { Text("代码") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetPrice, onValueChange = onTargetChange,
                    label = { Text("目标价格") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("添加") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
