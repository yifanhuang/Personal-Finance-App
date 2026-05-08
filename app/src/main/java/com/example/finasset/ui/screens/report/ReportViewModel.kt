package com.example.finasset.ui.screens.report

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.finasset.FinAssetApp
import com.example.finasset.data.model.PnlRecord
import com.example.finasset.data.model.PeriodReport
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReportViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as FinAssetApp

    val redUpGreenDown = app.preferencesManager.redUpGreenDown
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _pnlRecords = MutableStateFlow<List<PnlRecord>>(emptyList())
    val pnlRecords: StateFlow<List<PnlRecord>> = _pnlRecords.asStateFlow()

    private val _periodReport = MutableStateFlow(PeriodReport())
    val periodReport: StateFlow<PeriodReport> = _periodReport.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _pnlRecords.value = app.assetSnapshotRepo.getPnlRecords(90)

                val stockPnl = app.stockRepo.getTotalPnl()
                val fundPnl = app.fundRepo.getTotalPnl()
                val dividend = app.transactionRepo.getTotalDividend()
                val totalInvest = app.transactionRepo.getTotalInvest()
                val totalSell = app.transactionRepo.getTotalSell()

                _periodReport.value = PeriodReport(
                    period = "累计",
                    totalPnl = stockPnl + fundPnl,
                    stockPnl = stockPnl,
                    fundPnl = fundPnl,
                    dividendIncome = dividend,
                    totalInvest = totalInvest,
                    totalSell = totalSell
                )
            } catch (e: Exception) { e.printStackTrace() }
            finally { _isLoading.value = false }
        }
    }
}
