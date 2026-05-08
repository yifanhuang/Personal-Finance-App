package com.example.finasset.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.finasset.data.db.dao.*
import com.example.finasset.data.db.entity.*

@Database(
    entities = [
        StockEntity::class,
        FundEntity::class,
        TransactionEntity::class,
        AssetSnapshotEntity::class,
        PriceAlertEntity::class,
        WatchItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
    abstract fun fundDao(): FundDao
    abstract fun transactionDao(): TransactionDao
    abstract fun assetSnapshotDao(): AssetSnapshotDao
    abstract fun priceAlertDao(): PriceAlertDao
    abstract fun watchItemDao(): WatchItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finasset.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
