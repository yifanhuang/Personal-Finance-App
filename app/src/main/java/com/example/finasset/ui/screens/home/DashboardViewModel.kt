package com.example.finasset.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.finasset.FinAssetApp
import com.example.finasset.data.model.AssetOverview
import com.example.finasset.data.network.QuoteApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class CurvePoint(val date: String, val totalValue: Double)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as FinAssetApp
    private val _overview = MutableStateFlow(AssetOverview())
    val overview: StateFlow<AssetOverview> = _overview.asStateFlow()
    private val _curvePoints = MutableStateFlow<List<CurvePoint>>(emptyList())
    val curvePoints: StateFlow<List<CurvePoint>> = _curvePoints.asStateFlow()
    private val _curvePeriod = MutableStateFlow("day")
    val curvePeriod: StateFlow<String> = _curvePeriod.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _isLoadingCurve = MutableStateFlow(false)
    val isLoadingCurve: StateFlow<Boolean> = _isLoadingCurve.asStateFlow()
    val redUpGreenDown = app.preferencesManager.redUpGreenDown.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    init { refreshWithPrices() }

    fun refreshWithPrices() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val stocks = app.database.stockDao().getAllActive().firstOrNull() ?: emptyList()
                val funds = app.database.fundDao().getAllActive().firstOrNull() ?: emptyList()
                val (sp, fn) = QuoteApi.refreshAllPrices(stocks.map { it.code }, funds.map { it.code })
                stocks.forEach { s -> sp[s.code]?.let { if (it > 0) app.stockRepo.updatePrice(s.id, it) } }
                funds.forEach { f -> fn[f.code]?.let { if (it > 0) app.fundRepo.updateNav(f.id, it) } }
                computeOverview(); generateCurve("day")
            } catch (_: Exception) { computeOverview() }
            finally { _isLoading.value = false }
        }
    }

    fun switchPeriod(period: String) { _curvePeriod.value = period; viewModelScope.launch { generateCurve(period) } }

    private suspend fun computeOverview() {
        val sv = app.stockRepo.getTotalValue(); val fv = app.fundRepo.getTotalValue()
        val sp = app.stockRepo.getTotalPnl(); val fp = app.fundRepo.getTotalPnl()
        val tp = sp + fp; val ta = sv + fv; val invested = ta - tp
        val stocks = app.database.stockDao().getAllActive().firstOrNull() ?: emptyList()
        val funds = app.database.fundDao().getAllActive().firstOrNull() ?: emptyList()
        _overview.value = AssetOverview(ta, sv, fv, 0.0, 0.0, tp, if (invested > 0) tp / invested * 100 else 0.0, stocks.size, funds.size)
    }

    private suspend fun generateCurve(period: String) {
        _isLoadingCurve.value = true
        val stocks = app.database.stockDao().getAllActive().firstOrNull() ?: emptyList()
        val funds = app.database.fundDao().getAllActive().firstOrNull() ?: emptyList()
        val dateMap = linkedMapOf<String, Double>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (stock in stocks) {
            try {
                val count = when (period) { "week" -> 52; "month" -> 24; else -> 250 }
                val kline = QuoteApi.getStockKLine(stock.code, period, count)
                if (kline.dates.isNotEmpty()) {
                    for (i in kline.dates.indices) {
                        dateMap[kline.dates[i].take(10)] = (dateMap[kline.dates[i].take(10)] ?: 0.0) + stock.shares * kline.closes[i]
                    }
                } else {
                    // 合成：从买入价到现价的线性趋势
                    fillSyntheticStock(dateMap, stock.buyPrice, stock.currentPrice, stock.shares, period, sdf)
                }
            } catch (_: Exception) { fillSyntheticStock(dateMap, stock.buyPrice, stock.currentPrice, stock.shares, period, sdf) }
        }

        for (fund in funds) {
            try {
                val navHistory = QuoteApi.getFundNavHistory(fund.code, 365)
                val usable = navHistory.filter { it.nav > 0 }
                if (usable.isNotEmpty()) {
                    for (np in usable) { dateMap[np.date] = (dateMap[np.date] ?: 0.0) + fund.shares * np.nav }
                } else {
                    fillSyntheticFund(dateMap, fund.buyNav, fund.currentNav, fund.shares, period, sdf)
                }
            } catch (_: Exception) { fillSyntheticFund(dateMap, fund.buyNav, fund.currentNav, fund.shares, period, sdf) }
        }

        val count = when (period) { "week" -> 52; "month" -> 24; else -> 90 }
        val sorted = dateMap.entries.sortedBy { it.key }.takeLast(count)
        _curvePoints.value = sorted.map { CurvePoint(it.key, it.value) }
        _isLoadingCurve.value = false
    }

    private fun fillSyntheticStock(map: LinkedHashMap<String, Double>, buy: Double, cur: Double, shares: Double, period: String, sdf: SimpleDateFormat) {
        val cal = Calendar.getInstance()
        val days = when (period) { "week" -> 52; "month" -> 24; else -> 90 }
        for (i in days - 1 downTo 0) {
            val t = i.toDouble() / (days - 1).coerceAtLeast(1)
            val price = buy + (cur - buy) * t + (Math.random() - 0.5) * (cur - buy).coerceAtLeast(0.1) * 0.3
            map[sdf.format(cal.time)] = (map[sdf.format(cal.time)] ?: 0.0) + shares * price
            when (period) { "week" -> cal.add(Calendar.DAY_OF_YEAR, -7); "month" -> cal.add(Calendar.MONTH, -1); else -> cal.add(Calendar.DAY_OF_YEAR, -1) }
        }
    }

    private fun fillSyntheticFund(map: LinkedHashMap<String, Double>, buyNav: Double, curNav: Double, shares: Double, period: String, sdf: SimpleDateFormat) {
        val cal = Calendar.getInstance()
        val days = when (period) { "week" -> 52; "month" -> 24; else -> 90 }
        for (i in days - 1 downTo 0) {
            val t = i.toDouble() / (days - 1).coerceAtLeast(1)
            val nav = buyNav + (curNav - buyNav) * t + (Math.random() - 0.5) * (curNav - buyNav).coerceAtLeast(0.01) * 0.2
            map[sdf.format(cal.time)] = (map[sdf.format(cal.time)] ?: 0.0) + shares * nav
            when (period) { "week" -> cal.add(Calendar.DAY_OF_YEAR, -7); "month" -> cal.add(Calendar.MONTH, -1); else -> cal.add(Calendar.DAY_OF_YEAR, -1) }
        }
    }
}
