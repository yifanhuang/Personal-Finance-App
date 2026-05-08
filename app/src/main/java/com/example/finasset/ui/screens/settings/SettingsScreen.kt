package com.example.finasset.ui.screens.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.finasset.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val redUpGreenDown by viewModel.redUpGreenDown.collectAsState()
    val refreshInterval by viewModel.refreshInterval.collectAsState()
    val autoRefresh by viewModel.autoRefresh.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showIntervalDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Appearance
            Text(
                "外观设置",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Filled.DarkMode,
                        title = "主题模式",
                        subtitle = when (themeMode) { "dark" -> "深色模式"; "light" -> "浅色模式"; else -> "跟随系统" },
                        onClick = { showThemeDialog = true }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(
                        icon = Icons.Filled.SwapVert,
                        title = "涨跌颜色",
                        subtitle = if (redUpGreenDown) "红涨绿跌" else "绿涨红跌",
                        trailing = {
                            Switch(
                                checked = redUpGreenDown,
                                onCheckedChange = { viewModel.setRedUpGreenDown(it) }
                            )
                        }
                    )
                }
            }

            // Data
            Text(
                "数据管理",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Filled.Sync,
                        title = "自动刷新",
                        subtitle = if (autoRefresh) "已开启" else "已关闭",
                        trailing = {
                            Switch(
                                checked = autoRefresh,
                                onCheckedChange = { viewModel.setAutoRefresh(it) }
                            )
                        }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(
                        icon = Icons.Filled.Timer,
                        title = "刷新频率",
                        subtitle = when (refreshInterval) {
                            30 -> "每30分钟"; 60 -> "每小时"; 180 -> "每3小时"; else -> "每小时"
                        },
                        onClick = { showIntervalDialog = true }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(
                        icon = Icons.Filled.FileDownload,
                        title = "导出数据",
                        subtitle = "导出为 Excel/CSV 报表",
                        onClick = { }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(
                        icon = Icons.Filled.Backup,
                        title = "云端备份",
                        subtitle = "百度云 / 阿里云 / 手机云服务",
                        onClick = { }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(
                        icon = Icons.Filled.Restore,
                        title = "恢复数据",
                        subtitle = "从备份恢复持仓数据",
                        onClick = { }
                    )
                }
            }

            // About
            Text(
                "关于",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Filled.Info,
                        title = "版本号",
                        subtitle = "v1.0.0",
                        onClick = { }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(
                        icon = Icons.Filled.Code,
                        title = "资产管家",
                        subtitle = "纯本地 · 无广告 · 隐私优先",
                        onClick = { }
                    )
                }
            }

            Spacer(Modifier.height(80.dp))
        }

        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text("选择主题") },
                text = {
                    Column {
                        listOf(
                            "system" to "跟随系统",
                            "light" to "浅色模式",
                            "dark" to "深色模式"
                        ).forEach { (key, label) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = themeMode == key,
                                    onClick = { viewModel.setTheme(key); showThemeDialog = false }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(label, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showThemeDialog = false }) { Text("关闭") } }
            )
        }

        if (showIntervalDialog) {
            val intervals = listOf(30 to "每30分钟", 60 to "每小时", 180 to "每3小时", 360 to "每6小时")
            AlertDialog(
                onDismissRequest = { showIntervalDialog = false },
                title = { Text("刷新频率") },
                text = {
                    Column {
                        intervals.forEach { (mins, label) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = refreshInterval == mins,
                                    onClick = { viewModel.setRefreshInterval(mins); showIntervalDialog = false }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(label, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showIntervalDialog = false }) { Text("关闭") } }
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String = "",
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val content = @Composable {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(icon, null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.bodyLarge)
                    if (subtitle.isNotEmpty()) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (trailing != null) {
                trailing()
            }
        }
    }

    if (onClick != null) {
        Surface(onClick = onClick) { content() }
    } else {
        content()
    }
}
