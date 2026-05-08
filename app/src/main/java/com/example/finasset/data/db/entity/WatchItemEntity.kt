package com.example.finasset.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_items")
data class WatchItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assetType: String,
    val code: String,
    val name: String = "",
    var currentPrice: Double = 0.0,
    var changePercent: Double = 0.0,
    var groupName: String = "",
    var sortOrder: Int = 0,
    val createTime: Long = System.currentTimeMillis()
)
