package com.example.finasset.ui.navigation

sealed class Screen(val route: String, val title: String = "", val icon: String = "") {
    object Home : Screen("home", "总览", "dashboard")
    object StockList : Screen("stock_list", "股票", "trending_up")
    object StockDetail : Screen("stock_detail/{stockId}", "个股详情") {
        fun createRoute(stockId: Long) = "stock_detail/$stockId"
    }
    object FundList : Screen("fund_list", "基金", "account_balance")
    object FundDetail : Screen("fund_detail/{fundId}", "基金详情") {
        fun createRoute(fundId: Long) = "fund_detail/$fundId"
    }
    object Transaction : Screen("transaction", "记账", "receipt_long")
    object Report : Screen("report", "报表", "bar_chart")
    object Market : Screen("market", "行情", "show_chart")
    object Settings : Screen("settings", "设置", "settings")

    object AddStock : Screen("add_stock", "新增股票")
    object AddFund : Screen("add_fund", "新增基金")
    object AddTransaction : Screen("add_transaction/{assetType}", "新增记录") {
        fun createRoute(assetType: String = "STOCK") = "add_transaction/$assetType"
    }
    object AddTransactionForAsset : Screen("add_transaction_for/{assetType}/{assetId}", "新增记录") {
        fun createRoute(assetType: String, assetId: Long) = "add_transaction_for/$assetType/$assetId"
    }
}
