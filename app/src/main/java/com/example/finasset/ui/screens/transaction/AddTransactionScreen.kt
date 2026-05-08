package com.example.finasset.ui.screens.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavHostController,
    assetType: String = "STOCK",
    assetId: Long = 0L,
    viewModel: TransactionViewModel = viewModel()
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var txType by remember { mutableStateOf("BUY") }
    var price by remember { mutableStateOf("") }
    var shares by remember { mutableStateOf("") }
    var fee by remember { mutableStateOf("0") }
    var note by remember { mutableStateOf("") }
    var txTypeExpanded by remember { mutableStateOf(false) }

    val stockTxTypes = listOf("BUY" to "买入", "SELL" to "卖出", "ADD" to "加仓", "REDUCE" to "减仓", "DIVIDEND" to "分红")
    val fundTxTypes = listOf("BUY" to "申购", "REDEEM" to "赎回", "DINGTOU" to "定投", "DIVIDEND" to "分红")
    val txTypes = if (assetType == "STOCK") stockTxTypes else fundTxTypes

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新增记录", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = code, onValueChange = { code = it },
                label = { Text(if (assetType == "STOCK") "股票代码" else "基金代码") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Tag, null) }
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("名称") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Filled.TextFields, null) }
            )
            Spacer(Modifier.height(12.dp))

            // Transaction type dropdown
            ExposedDropdownMenuBox(expanded = txTypeExpanded, onExpandedChange = { txTypeExpanded = it }) {
                OutlinedTextField(
                    value = txTypes.find { it.first == txType }?.second ?: txType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("交易类型") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = txTypeExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    leadingIcon = { Icon(Icons.Filled.SwapVert, null) }
                )
                ExposedDropdownMenu(expanded = txTypeExpanded, onDismissRequest = { txTypeExpanded = false }) {
                    txTypes.forEach { (key, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = { txType = key; txTypeExpanded = false })
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = price, onValueChange = { price = it },
                label = { Text("价格/净值") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Filled.AttachMoney, null) }
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = shares, onValueChange = { shares = it },
                label = { Text(if (assetType == "STOCK") "数量 (股)" else "份额") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Filled.Numbers, null) }
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = fee, onValueChange = { fee = it },
                label = { Text("手续费") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Filled.Receipt, null) }
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note, onValueChange = { note = it },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                leadingIcon = { Icon(Icons.Filled.Note, null) }
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val p = price.toDoubleOrNull() ?: 0.0
                    val s = shares.toDoubleOrNull() ?: 0.0
                    val f = fee.toDoubleOrNull() ?: 0.0
                    val amount = p * s
                    if (code.isNotBlank() && name.isNotBlank() && p > 0 && s > 0) {
                        viewModel.addTransaction(assetType, assetId, code, name, txType, p, s, f, amount, note)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = code.isNotBlank() && name.isNotBlank() && price.isNotBlank() && shares.isNotBlank()
            ) {
                Icon(Icons.Filled.Check, null)
                Spacer(Modifier.width(6.dp))
                Text("确认添加")
            }
        }
    }
}
