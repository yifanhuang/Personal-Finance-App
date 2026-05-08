package com.example.finasset.data.db.dao

import androidx.room.*
import com.example.finasset.data.db.entity.StockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM stocks WHERE isArchived = 0 ORDER BY updateTime DESC")
    fun getAllActive(): Flow<List<StockEntity>>

    @Query("SELECT * FROM stocks WHERE isArchived = 1 ORDER BY updateTime DESC")
    fun getAllArchived(): Flow<List<StockEntity>>

    @Query("SELECT * FROM stocks WHERE id = :id")
    suspend fun getById(id: Long): StockEntity?

    @Query("SELECT * FROM stocks WHERE code = :code AND isArchived = 0")
    suspend fun getByCode(code: String): StockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stock: StockEntity): Long

    @Update
    suspend fun update(stock: StockEntity)

    @Delete
    suspend fun delete(stock: StockEntity)

    @Query("DELETE FROM stocks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE stocks SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE stocks SET currentPrice = :price, updateTime = :time WHERE id = :id")
    suspend fun updatePrice(id: Long, price: Double, time: Long = System.currentTimeMillis())

    @Query("SELECT SUM(shares * currentPrice) FROM stocks WHERE isArchived = 0")
    suspend fun getTotalValue(): Double?

    @Query("SELECT SUM((currentPrice - buyPrice) * shares) FROM stocks WHERE isArchived = 0")
    suspend fun getTotalPnl(): Double?
}
