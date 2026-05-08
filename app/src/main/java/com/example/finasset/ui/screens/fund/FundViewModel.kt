package com.example.finasset.ui.screens.fund

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.finasset.FinAssetApp
import com.example.finasset.data.db.entity.FundEntity
import com.example.finasset.data.db.entity.TransactionEntity
import com.example.finasset.data.model.FundSummary
import com.example.finasset.data.network.NavPoint
import com.example.finasset.data.network.QuoteApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FundViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as FinAssetApp

    val funds = app.fundRepo.allFunds.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val redUpGreenDown = app.preferencesManager.redUpGreenDown.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _fundDetail = MutableStateFlow<FundEntity?>(null)
    val fundDetail: StateFlow<FundEntity?> = _fundDetail.asStateFlow()
    private val _fundTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val fundTransactions: StateFlow<List<TransactionEntity>> = _fundTransactions.asStateFlow()
    private val _fundSummary = MutableStateFlow<FundSummary?>(null)
    val fundSummary: StateFlow<FundSummary?> = _fundSummary.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _navHistory = MutableStateFlow<List<NavPoint>>(emptyList())
    val navHistory: StateFlow<List<NavPoint>> = _navHistory.asStateFlow()
    private val _isLoadingNav = MutableStateFlow(false)
    val isLoadingNav: StateFlow<Boolean> = _isLoadingNav.asStateFlow()

    fun loadFundDetail(fundId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val fund = app.fundRepo.getById(fundId)
            _fundDetail.value = fund
            if (fund != null) {
                val summaries = app.fundRepo.getFundSummaries()
                _fundSummary.value = summaries.find { it.id == fundId }
                loadNavHistory(fund.code)
            }
            app.database.transactionDao().getByAsset(fundId, "FUND").firstOrNull()?.let { _fundTransactions.value = it }
            _isLoading.value = false
        }
    }

    private fun loadNavHistory(code: String) {
        viewModelScope.launch {
            _isLoadingNav.value = true
            val history = QuoteApi.getFundNavHistory(code, 365)
            if (history.isNotEmpty()) {
                _navHistory.value = history
            } else {
                // 生成模拟净值走势
                val fund = _fundDetail.value
                if (fund != null) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val cal = Calendar.getInstance()
                    val synthetic = mutableListOf<NavPoint>()
                    for (i in 90 downTo 1) {
                        val t = i.toDouble() / 90.0
                        val nav = fund.buyNav + (fund.currentNav - fund.buyNav) * t + (Math.random() - 0.5) * (fund.currentNav - fund.buyNav).coerceAtLeast(0.01) * 0.2
                        synthetic.add(NavPoint(sdf.format(cal.time), nav))
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                    }
                    _navHistory.value = synthetic.reversed()
                }
            }
            _isLoadingNav.value = false
        }
    }

    fun addFund(code: String, name: String, nav: Double, amount: Double, fee: Double, fundType: String, isDingtou: Boolean) {
        viewModelScope.launch {
            app.fundRepo.insert(FundEntity(code = code, name = name, buyNav = nav, currentNav = nav, shares = amount / nav, investAmount = amount, fee = fee, fundType = fundType, isDingtou = isDingtou))
        }
    }

    fun updateNav(fundId: Long, nav: Double) {
        viewModelScope.launch {
            app.fundRepo.updateNav(fundId, nav)
            val summaries = app.fundRepo.getFundSummaries()
            _fundSummary.value = summaries.find { it.id == fundId }
        }
    }

    fun deleteFund(fundId: Long) { viewModelScope.launch { app.fundRepo.deleteById(fundId) } }

    fun addTransaction(fundId: Long, txType: String, nav: Double, amount: Double, fee: Double) {
        viewModelScope.launch {
            val fund = app.fundRepo.getById(fundId) ?: return@launch
            val shares = amount / nav
            app.database.transactionDao().insert(TransactionEntity(assetType = "FUND", assetId = fundId, assetCode = fund.code, assetName = fund.name, txType = txType, price = nav, shares = shares, fee = fee, amount = amount))
            when (txType) {
                "BUY", "DINGTOU" -> {
                    val newShares = fund.shares + shares
                    val newAmount = fund.investAmount + amount
                    app.fundRepo.update(fund.copy(shares = newShares, investAmount = newAmount, buyNav = newAmount / newShares))
                }
                "REDEEM" -> app.fundRepo.update(fund.copy(shares = (fund.shares - shares).coerceAtLeast(0.0), investAmount = (fund.investAmount - amount).coerceAtLeast(0.0)))
            }
            loadFundDetail(fundId)
        }
    }
}
