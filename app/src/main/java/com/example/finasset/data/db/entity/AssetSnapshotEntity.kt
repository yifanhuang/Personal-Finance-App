package com.example.finasset.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asset_snapshots")
data class AssetSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val totalStockValue: Double,
    val totalFundValue: Double,
    val totalCashValue: Double,
    val totalAssets: Double,
    val dailyPnl: Double,
    val cumulativeReturn: Double,
    val createTime: Long = System.currentTimeMillis()
)
