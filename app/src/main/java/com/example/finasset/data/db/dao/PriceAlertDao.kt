package com.example.finasset.data.db.dao

import androidx.room.*
import com.example.finasset.data.db.entity.PriceAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceAlertDao {
    @Query("SELECT * FROM price_alerts ORDER BY createTime DESC")
    fun getAll(): Flow<List<PriceAlertEntity>>

    @Query("SELECT * FROM price_alerts WHERE isEnabled = 1 AND isTriggered = 0")
    suspend fun getActiveAlerts(): List<PriceAlertEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: PriceAlertEntity): Long

    @Update
    suspend fun update(alert: PriceAlertEntity)

    @Delete
    suspend fun delete(alert: PriceAlertEntity)

    @Query("DELETE FROM price_alerts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
