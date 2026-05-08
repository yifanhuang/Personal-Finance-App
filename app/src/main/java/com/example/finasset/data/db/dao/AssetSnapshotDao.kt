package com.example.finasset.data.db.dao

import androidx.room.*
import com.example.finasset.data.db.entity.AssetSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetSnapshotDao {
    @Query("SELECT * FROM asset_snapshots ORDER BY date DESC")
    fun getAll(): Flow<List<AssetSnapshotEntity>>

    @Query("SELECT * FROM asset_snapshots WHERE date = :date")
    suspend fun getByDate(date: String): AssetSnapshotEntity?

    @Query("SELECT * FROM asset_snapshots WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getByDateRange(startDate: String, endDate: String): Flow<List<AssetSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: AssetSnapshotEntity)

    @Query("DELETE FROM asset_snapshots WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("SELECT * FROM asset_snapshots ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): AssetSnapshotEntity?
}
