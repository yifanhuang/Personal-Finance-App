package com.example.finasset.data.db.dao

import androidx.room.*
import com.example.finasset.data.db.entity.FundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FundDao {
    @Query("SELECT * FROM funds WHERE isArchived = 0 ORDER BY updateTime DESC")
    fun getAllActive(): Flow<List<FundEntity>>

    @Query("SELECT * FROM funds WHERE isArchived = 1 ORDER BY updateTime DESC")
    fun getAllArchived(): Flow<List<FundEntity>>

    @Query("SELECT * FROM funds WHERE id = :id")
    suspend fun getById(id: Long): FundEntity?

    @Query("SELECT * FROM funds WHERE code = :code AND isArchived = 0")
    suspend fun getByCode(code: String): FundEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fund: FundEntity): Long

    @Update
    suspend fun update(fund: FundEntity)

    @Delete
    suspend fun delete(fund: FundEntity)

    @Query("DELETE FROM funds WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE funds SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE funds SET currentNav = :nav, updateTime = :time WHERE id = :id")
    suspend fun updateNav(id: Long, nav: Double, time: Long = System.currentTimeMillis())

    @Query("SELECT SUM(shares * currentNav) FROM funds WHERE isArchived = 0")
    suspend fun getTotalValue(): Double?

    @Query("SELECT SUM((currentNav - buyNav) * shares) FROM funds WHERE isArchived = 0")
    suspend fun getTotalPnl(): Double?

    @Query("SELECT * FROM funds WHERE isDingtou = 1 AND isArchived = 0")
    suspend fun getDingtouFunds(): List<FundEntity>
}
