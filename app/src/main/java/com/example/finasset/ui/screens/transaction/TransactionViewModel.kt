package com.example.finasset.ui.screens.transaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.finasset.FinAssetApp
import com.example.finasset.data.db.entity.TransactionEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as FinAssetApp

    val transactions = app.transactionRepo.allTransactions
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _filterType = MutableStateFlow("ALL")
    val filterType: StateFlow<String> = _filterType.asStateFlow()

    val filteredTransactions = combine(transactions, _filterType) { txs, filter ->
        if (filter == "ALL") txs
        else txs.filter { it.assetType == filter }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setFilter(filter: String) { _filterType.value = filter }

    fun addTransaction(assetType: String, assetId: Long, code: String, name: String, txType: String, price: Double, shares: Double, fee: Double, amount: Double, note: String) {
        viewModelScope.launch {
            app.database.transactionDao().insert(
                TransactionEntity(
                    assetType = assetType, assetId = assetId,
                    assetCode = code, assetName = name,
                    txType = txType, price = price, shares = shares,
                    fee = fee, amount = amount, notes = note
                )
            )
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch { app.transactionRepo.deleteById(id) }
    }
}
