package com.example.finasset.ui.screens.fund

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.finasset.data.network.QuoteApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFundScreen(
    navController: NavHostController,
    viewModel: FundViewModel = viewModel()
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var fundType by remember { mutableStateOf("MIXED") }
    var currentNav by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var fee by remember { mutableStateOf("0") }
    var isDingtou by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新增基金", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = code, onValueChange = { code = it },
                label = { Text("基金代码 (如 000001)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Tag, null) },
                trailingIcon = {
                    if (isFetching) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(
                            onClick = {
                                if (code.isNotBlank()) {
                                    isFetching = true
                                    scope.launch {
                                        val quote = QuoteApi.getFundQuote(code.trim())
                                        isFetching = false
                                        if (quote != null) {
                                            name = quote.name
                                            currentNav = String.format("%.4f", quote.currentNav)
                                            fundType = quote.fundType
                                        } else {
                                            snackbarHostState.showSnackbar("无法获取净值，请检查代码")
                                        }
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Search, "查询")
                        }
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("基金名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isFetching,
                leadingIcon = { Icon(Icons.Filled.TextFields, null) }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = currentNav, onValueChange = {},
                label = { Text("当前净值 (自动获取)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = false,
                leadingIcon = { Icon(Icons.Filled.TrendingUp, null) }
            )

            Spacer(Modifier.height(12.dp))

            // Fund type display
            val typeLabel = when (fundType) {
                "STOCK" -> "股票型"; "MIXED" -> "混合型"; "BOND" -> "债券型"
                "INDEX" -> "指数型"; "MMF" -> "货币型"; "ETF" -> "ETF"; else -> fundType
            }
            OutlinedTextField(
                value = typeLabel, onValueChange = {},
                label = { Text("基金类型 (自动识别)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = false,
                leadingIcon = { Icon(Icons.Filled.Category, null) }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = totalAmount, onValueChange = { totalAmount = it },
                label = { Text("持仓总金额 (元)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Filled.AccountBalanceWallet, null) }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = fee, onValueChange = { fee = it },
                label = { Text("手续费") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Filled.Receipt, null) }
            )

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isDingtou, onCheckedChange = { isDingtou = it })
                Text("设置定投", style = MaterialTheme.typography.bodyMedium)
            }

            // 自动计算展示
            val amount = totalAmount.toDoubleOrNull() ?: 0.0
            val nav = currentNav.toDoubleOrNull() ?: 0.0
            val shares = if (nav > 0) amount / nav else 0.0

            if (amount > 0 && nav > 0) {
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("买入净值", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format("%.4f", nav), fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("持有份额", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format("%.2f", shares), fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("投入金额", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format("%.2f", amount), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val a = amount
                    val n = nav
                    val f = fee.toDoubleOrNull() ?: 0.0
                    if (code.isNotBlank() && name.isNotBlank() && a > 0 && n > 0) {
                        viewModel.addFund(code.trim(), name.trim(), n, a, f, fundType, isDingtou)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = code.isNotBlank() && name.isNotBlank() && totalAmount.isNotBlank() && currentNav.isNotBlank() && !isFetching
            ) {
                Icon(Icons.Filled.Check, null)
                Spacer(Modifier.width(6.dp))
                Text("确认添加")
            }
        }
    }
}
