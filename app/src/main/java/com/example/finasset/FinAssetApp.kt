package com.example.finasset

import android.app.Application
import com.example.finasset.data.PreferencesManager
import com.example.finasset.data.db.AppDatabase
import com.example.finasset.data.repository.*

class FinAssetApp : Application() {
    lateinit var database: AppDatabase
        private set

    lateinit var preferencesManager: PreferencesManager
        private set

    lateinit var stockRepo: StockRepository
        private set
    lateinit var fundRepo: FundRepository
        private set
    lateinit var transactionRepo: TransactionRepository
        private set
    lateinit var assetSnapshotRepo: AssetSnapshotRepository
        private set
    lateinit var priceAlertRepo: PriceAlertRepository
        private set
    lateinit var watchItemRepo: WatchItemRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        preferencesManager = PreferencesManager(this)

        stockRepo = StockRepository(database.stockDao())
        fundRepo = FundRepository(database.fundDao())
        transactionRepo = TransactionRepository(database.transactionDao())
        assetSnapshotRepo = AssetSnapshotRepository(database.assetSnapshotDao())
        priceAlertRepo = PriceAlertRepository(database.priceAlertDao())
        watchItemRepo = WatchItemRepository(database.watchItemDao())
    }
}
