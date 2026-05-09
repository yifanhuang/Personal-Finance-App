package com.example.finasset.ui.screens.stock

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.finasset.FinAssetApp
import com.example.finasset.data.db.entity.StockEntity
import com.example.finasset.data.db.entity.TransactionEntity
import com.example.finasset.data.model.StockSummary
import com.example.finasset.data.network.KLineData
import com.example.finasset.data.network.QuoteApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StockViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as FinAssetApp

    val stocks = app.stockRepo.allStocks.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val redUpGreenDown = app.preferencesManager.redUpGreenDown.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _stockDetail = MutableStateFlow<StockEntity?>(null)
    val stockDetail: StateFlow<StockEntity?> = _stockDetail.asStateFlow()
    private val _stockTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val stockTransactions: StateFlow<List<TransactionEntity>> = _stockTransactions.asStateFlow()
    private val _stockSummary = MutableStateFlow<StockSummary?>(null)
    val stockSummary: StateFlow<StockSummary?> = _stockSummary.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _klineData = MutableStateFlow(KLineData(emptyList(), emptyList(), emptyList(), emptyList(), emptyList()))
    val klineData: StateFlow<KLineData> = _klineData.asStateFlow()
    private val _klinePeriod = MutableStateFlow("day")
    val klinePeriod: StateFlow<String> = _klinePeriod.asStateFlow()
    private val _isLoadingKline = MutableStateFlow(false)
    val isLoadingKline: StateFlow<Boolean> = _isLoadingKline.asStateFlow()

    fun loadStockDetail(stockId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val stock = app.stockRepo.getById(stockId)
            _stockDetail.value = stock
            if (stock != null) {
                val summaries = app.stockRepo.getStockSummaries()
                _stockSummary.value = summaries.find { it.id == stockId }
                loadKLine(stock.code, "day")
            }
            app.database.transactionDao().getByAsset(stockId, "STOCK").firstOrNull()?.let { _stockTransactions.value = it }
            _isLoading.value = false
        }
    }

    fun loadKLine(code: String, period: String) {
        _klinePeriod.value = period
        viewModelScope.launch {
            _isLoadingKline.value = true
            val count = when (period) { "day" -> 365; "week" -> 120; "month" -> 60; else -> 365 }
            _klineData.value = QuoteApi.getStockKLine(code, period, count)
            _isLoadingKline.value = false
        }
    }

    fun addStock(code: String, name: String, buyPrice: Double, shares: Double, fee: Double) {
        viewModelScope.launch {
            app.stockRepo.insert(StockEntity(code = code, name = name, buyPrice = buyPrice, currentPrice = buyPrice, shares = shares, availableShares = shares, fee = fee))
        }
    }

    fun updatePrice(stockId: Long, price: Double) {
        viewModelScope.launch {
            app.stockRepo.updatePrice(stockId, price)
            val summaries = app.stockRepo.getStockSummaries()
            _stockSummary.value = summaries.find { it.id == stockId }
        }
    }

    fun deleteStock(stockId: Long) { viewModelScope.launch { app.stockRepo.deleteById(stockId) } }
    fun archiveStock(stockId: Long) { viewModelScope.launch { app.stockRepo.archive(stockId) } }

    fun addTransaction(stockId: Long, txType: String, price: Double, shares: Double, fee: Double) {
        viewModelScope.launch {
            val stock = app.stockRepo.getById(stockId) ?: return@launch
            val amount = price * shares
            app.database.transactionDao().insert(TransactionEntity(assetType = "STOCK", assetId = stockId, assetCode = stock.code, assetName = stock.name, txType = txType, price = price, shares = shares, fee = fee, amount = amount))
            when (txType) {
                "BUY", "ADD" -> {
                    val newShares = stock.shares + shares
                    val totalCost = stock.shares * stock.buyPrice + amount + fee
                    app.stockRepo.update(stock.copy(shares = newShares, availableShares = stock.availableShares + shares, buyPrice = if (newShares > 0) totalCost / newShares else stock.buyPrice))
                }
                "SELL", "REDUCE" -> app.stockRepo.update(stock.copy(shares = (stock.shares - shares).coerceAtLeast(0.0), availableShares = (stock.availableShares - shares).coerceAtLeast(0.0)))
            }
            loadStockDetail(stockId)
        }
    }
}
