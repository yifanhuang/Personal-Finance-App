package com.example.finasset.data.model

data class StockSummary(
    val id: Long = 0,
    val code: String = "",
    val name: String = "",
    val buyPrice: Double = 0.0,
    val currentPrice: Double = 0.0,
    val shares: Double = 0.0,
    val marketValue: Double = 0.0,
    val costValue: Double = 0.0,
    val pnl: Double = 0.0,
    val pnlPercent: Double = 0.0,
    val tag: String = "",
    val tagColor: String = "#FF6200EE",
    val weight: Double = 0.0
)

data class FundSummary(
    val id: Long = 0,
    val code: String = "",
    val name: String = "",
    val buyNav: Double = 0.0,
    val currentNav: Double = 0.0,
    val shares: Double = 0.0,
    val investAmount: Double = 0.0,
    val marketValue: Double = 0.0,
    val pnl: Double = 0.0,
    val pnlPercent: Double = 0.0,
    val fundType: String = "",
    val tag: String = "",
    val tagColor: String = "#FF6200EE",
    val isDingtou: Boolean = false,
    val weight: Double = 0.0
)

data class AssetOverview(
    val totalAssets: Double = 0.0,
    val stockValue: Double = 0.0,
    val fundValue: Double = 0.0,
    val cashValue: Double = 0.0,
    val dailyPnl: Double = 0.0,
    val totalPnl: Double = 0.0,
    val totalReturnPercent: Double = 0.0,
    val stockCount: Int = 0,
    val fundCount: Int = 0
)

data class PnlRecord(
    val date: String = "",
    val pnl: Double = 0.0,
    val cumulativeAssets: Double = 0.0
)

data class PeriodReport(
    val period: String = "",
    val totalPnl: Double = 0.0,
    val stockPnl: Double = 0.0,
    val fundPnl: Double = 0.0,
    val dividendIncome: Double = 0.0,
    val totalInvest: Double = 0.0,
    val totalSell: Double = 0.0
)
