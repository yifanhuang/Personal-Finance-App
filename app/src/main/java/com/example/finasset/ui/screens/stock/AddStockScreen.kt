package com.example.finasset.ui.screens.stock

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
fun AddStockScreen(
    navController: NavHostController,
    viewModel: StockViewModel = viewModel()
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var currentPrice by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var fee by remember { mutableStateOf("0") }
    var isLoading by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新增股票", fontWeight = FontWeight.Bold) },
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
            // 代码输入 + 自动查询
            OutlinedTextField(
                value = code, onValueChange = { code = it },
                label = { Text("股票代码 (如 600036 或 sh600036)") },
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
                                        val quote = QuoteApi.getStockQuote(code.trim())
                                        isFetching = false
                                        if (quote != null) {
                                            name = quote.name
                                            currentPrice = String.format("%.2f", quote.currentPrice)
                                        } else {
                                            snackbarHostState.showSnackbar("无法获取行情，请检查代码")
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
                label = { Text("股票名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isFetching,
                leadingIcon = { Icon(Icons.Filled.TextFields, null) }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = currentPrice, onValueChange = { currentPrice = it },
                label = { Text("当前市价 (自动获取)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Filled.AttachMoney, null) }
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

            // 自动计算展示
            val amount = totalAmount.toDoubleOrNull() ?: 0.0
            val price = currentPrice.toDoubleOrNull() ?: 0.0
            val shares = if (price > 0) amount / price else 0.0

            if (amount > 0 && price > 0) {
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
                            Text("买入价", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format("%.2f", price), fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("持仓数量", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format("%.0f 股", shares), fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("市值", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format("%.2f", amount), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val p = price
                    val f = fee.toDoubleOrNull() ?: 0.0
                    if (code.isNotBlank() && name.isNotBlank() && amount > 0 && p > 0) {
                        viewModel.addStock(code.trim(), name.trim(), p, shares, f)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = code.isNotBlank() && name.isNotBlank() && amount > 0 && price > 0 && !isFetching
            ) {
                Icon(Icons.Filled.Check, null)
                Spacer(Modifier.width(6.dp))
                Text("确认添加")
            }
        }
    }
}
