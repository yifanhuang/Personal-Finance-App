package com.example.finasset.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assetType: String,
    val assetId: Long,
    val assetCode: String = "",
    val assetName: String = "",
    val txType: String,
    val price: Double,
    var shares: Double,
    var fee: Double = 0.0,
    var amount: Double,
    var tag: String = "",
    var notes: String = "",
    var photoPath: String = "",
    val createTime: Long = System.currentTimeMillis()
)
