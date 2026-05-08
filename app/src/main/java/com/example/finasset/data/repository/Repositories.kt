package com.example.finasset.data.repository

import com.example.finasset.data.db.dao.*
import com.example.finasset.data.db.entity.*
import com.example.finasset.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class StockRepository(private val dao: StockDao) {
    val allStocks: Flow<List<StockEntity>> = dao.getAllActive()
    val archivedStocks: Flow<List<StockEntity>> = dao.getAllArchived()

    suspend fun getById(id: Long) = dao.getById(id)
    suspend fun insert(stock: StockEntity) = dao.insert(stock)
    suspend fun update(stock: StockEntity) = dao.update(stock)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun archive(id: Long) = dao.archive(id)
    suspend fun updatePrice(id: Long, price: Double) = dao.updatePrice(id, price)
    suspend fun getTotalValue() = dao.getTotalValue() ?: 0.0
    suspend fun getTotalPnl() = dao.getTotalPnl() ?: 0.0

    suspend fun getStockSummaries(): List<StockSummary> {
        val stocks = dao.getAllActive().firstOrNull() ?: emptyList()
        val totalValue = stocks.sumOf { it.shares * it.currentPrice }
        if (totalValue == 0.0) return emptyList()
        return stocks.map { stock ->
            val mv = stock.shares * stock.currentPrice
            val cost = stock.shares * stock.buyPrice
            StockSummary(
                id = stock.id, code = stock.code, name = stock.name,
                buyPrice = stock.buyPrice, currentPrice = stock.currentPrice,
                shares = stock.shares, marketValue = mv, costValue = cost,
                pnl = mv - cost,
                pnlPercent = if (cost > 0) ((mv - cost) / cost * 100) else 0.0,
                tag = stock.tag, tagColor = stock.tagColor,
                weight = (mv / totalValue * 100)
            )
        }
    }
}

class FundRepository(private val dao: FundDao) {
    val allFunds: Flow<List<FundEntity>> = dao.getAllActive()
    val archivedFunds: Flow<List<FundEntity>> = dao.getAllArchived()

    suspend fun getById(id: Long) = dao.getById(id)
    suspend fun insert(fund: FundEntity) = dao.insert(fund)
    suspend fun update(fund: FundEntity) = dao.update(fund)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun archive(id: Long) = dao.archive(id)
    suspend fun updateNav(id: Long, nav: Double) = dao.updateNav(id, nav)
    suspend fun getTotalValue() = dao.getTotalValue() ?: 0.0
    suspend fun getTotalPnl() = dao.getTotalPnl() ?: 0.0

    suspend fun getFundSummaries(): List<FundSummary> {
        val funds = dao.getAllActive().firstOrNull() ?: emptyList()
        val totalValue = funds.sumOf { it.shares * it.currentNav }
        if (totalValue == 0.0) return emptyList()
        return funds.map { fund ->
            val mv = fund.shares * fund.currentNav
            FundSummary(
                id = fund.id, code = fund.code, name = fund.name,
                buyNav = fund.buyNav, currentNav = fund.currentNav,
                shares = fund.shares, investAmount = fund.investAmount,
                marketValue = mv, pnl = mv - fund.investAmount,
                pnlPercent = if (fund.investAmount > 0) ((mv - fund.investAmount) / fund.investAmount * 100) else 0.0,
                fundType = fund.fundType, tag = fund.tag, tagColor = fund.tagColor,
                isDingtou = fund.isDingtou, weight = (mv / totalValue * 100)
            )
        }
    }
}

class TransactionRepository(private val dao: TransactionDao) {
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAll()

    fun getByAsset(assetId: Long, assetType: String) = dao.getByAsset(assetId, assetType)
    fun getByTxType(txType: String) = dao.getByTxType("STOCK")
    fun getByTimeRange(start: Long, end: Long) = dao.getByTimeRange(start, end)

    suspend fun insert(tx: TransactionEntity) = dao.insert(tx)
    suspend fun update(tx: TransactionEntity) = dao.update(tx)
    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun getTotalInvest() = dao.getTotalInvest() ?: 0.0
    suspend fun getTotalSell() = dao.getTotalSell() ?: 0.0
    suspend fun getTotalDividend() = dao.getTotalDividend() ?: 0.0
}

class AssetSnapshotRepository(private val dao: AssetSnapshotDao) {
    val allSnapshots: Flow<List<AssetSnapshotEntity>> = dao.getAll()

    suspend fun getLatest() = dao.getLatest()
    suspend fun getByDate(date: String) = dao.getByDate(date)
    suspend fun insert(snapshot: AssetSnapshotEntity) = dao.insert(snapshot)

    suspend fun saveDailySnapshot(
        stockValue: Double, fundValue: Double, cashValue: Double,
        dailyPnl: Double, cumulativeReturn: Double
    ) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        dao.insert(
            AssetSnapshotEntity(
                date = today,
                totalStockValue = stockValue,
                totalFundValue = fundValue,
                totalCashValue = cashValue,
                totalAssets = stockValue + fundValue + cashValue,
                dailyPnl = dailyPnl,
                cumulativeReturn = cumulativeReturn
            )
        )
    }

    suspend fun getPnlRecords(days: Int = 30): List<PnlRecord> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        val startDate = sdf.format(cal.time)
        val endDate = sdf.format(Date())
        val snapshots = dao.getByDateRange(startDate, endDate).firstOrNull() ?: emptyList()
        return snapshots.map { PnlRecord(it.date, it.dailyPnl, it.totalAssets) }
    }
}

class PriceAlertRepository(private val dao: PriceAlertDao) {
    val allAlerts: Flow<List<PriceAlertEntity>> = dao.getAll()

    suspend fun insert(alert: PriceAlertEntity) = dao.insert(alert)
    suspend fun update(alert: PriceAlertEntity) = dao.update(alert)
    suspend fun delete(alert: PriceAlertEntity) = dao.delete(alert)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun getActiveAlerts() = dao.getActiveAlerts()
}

class WatchItemRepository(private val dao: WatchItemDao) {
    val allItems: Flow<List<WatchItemEntity>> = dao.getAll()

    suspend fun insert(item: WatchItemEntity) = dao.insert(item)
    suspend fun update(item: WatchItemEntity) = dao.update(item)
    suspend fun delete(item: WatchItemEntity) = dao.delete(item)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
