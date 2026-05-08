package com.example.finasset.data.db.dao

import androidx.room.*
import com.example.finasset.data.db.entity.WatchItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchItemDao {
    @Query("SELECT * FROM watch_items ORDER BY sortOrder ASC, createTime DESC")
    fun getAll(): Flow<List<WatchItemEntity>>

    @Query("SELECT * FROM watch_items WHERE assetType = :assetType ORDER BY sortOrder ASC")
    fun getByType(assetType: String): Flow<List<WatchItemEntity>>

    @Query("SELECT * FROM watch_items WHERE code = :code")
    suspend fun getByCode(code: String): WatchItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WatchItemEntity): Long

    @Update
    suspend fun update(item: WatchItemEntity)

    @Delete
    suspend fun delete(item: WatchItemEntity)

    @Query("DELETE FROM watch_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}
