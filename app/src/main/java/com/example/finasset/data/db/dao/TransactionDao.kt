package com.example.finasset.data.db.dao

import androidx.room.*
import com.example.finasset.data.db.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY createTime DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE assetId = :assetId AND assetType = :assetType ORDER BY createTime DESC")
    fun getByAsset(assetId: Long, assetType: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE assetType = :assetType ORDER BY createTime DESC")
    fun getByAssetType(assetType: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE txType = :txType ORDER BY createTime DESC")
    fun getByTxType(txType: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE createTime BETWEEN :startTime AND :endTime ORDER BY createTime DESC")
    fun getByTimeRange(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: TransactionEntity): Long

    @Update
    suspend fun update(tx: TransactionEntity)

    @Delete
    suspend fun delete(tx: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT SUM(amount) FROM transactions WHERE txType = 'BUY' OR txType = 'DINGTOU'")
    suspend fun getTotalInvest(): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE txType = 'SELL' OR txType = 'REDEEM'")
    suspend fun getTotalSell(): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE txType = 'DIVIDEND'")
    suspend fun getTotalDividend(): Double?
}
