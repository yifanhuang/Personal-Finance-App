package com.example.finasset.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "funds")
data class FundEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String,
    val name: String,
    val fundType: String = "",
    val buyNav: Double,
    var currentNav: Double = 0.0,
    var shares: Double,
    var investAmount: Double,
    var fee: Double = 0.0,
    var isDingtou: Boolean = false,
    var dingtouAmount: Double = 0.0,
    var dingtouPeriod: String = "",
    var tag: String = "",
    var tagColor: String = "#FF6200EE",
    var notes: String = "",
    val createTime: Long = System.currentTimeMillis(),
    var updateTime: Long = System.currentTimeMillis(),
    var isArchived: Boolean = false
)
