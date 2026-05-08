package com.example.finasset.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stocks")
data class StockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String,
    val name: String,
    val buyPrice: Double,
    var currentPrice: Double = 0.0,
    var shares: Double,
    var availableShares: Double = shares,
    var fee: Double = 0.0,
    var tag: String = "",
    var tagColor: String = "#FF6200EE",
    var notes: String = "",
    val createTime: Long = System.currentTimeMillis(),
    var updateTime: Long = System.currentTimeMillis(),
    var isArchived: Boolean = false
)
