package com.example.finasset.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "price_alerts")
data class PriceAlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assetType: String,
    val assetCode: String,
    val assetName: String = "",
    val alertType: String,
    val targetPrice: Double,
    var isTriggered: Boolean = false,
    var isEnabled: Boolean = true,
    val createTime: Long = System.currentTimeMillis()
)
