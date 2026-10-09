package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KrunalQuantDao {

    // --- Trade Positions ---
    @Query("SELECT * FROM trade_positions ORDER BY CASE WHEN status = 'OPEN' THEN 0 ELSE 1 END, openedAt DESC")
    fun observeAllTrades(): Flow<List<TradePositionEntity>>

    @Query("SELECT COUNT(*) FROM trade_positions")
    suspend fun getTradeCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradePositionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrades(trades: List<TradePositionEntity>)

    @Update
    suspend fun updateTrade(trade: TradePositionEntity)

    @Query("DELETE FROM trade_positions WHERE id = :id")
    suspend fun deleteTradeById(id: Long)

    // --- Company Vault Records ---
    @Query("SELECT * FROM company_records ORDER BY isPinned DESC, updatedAt DESC")
    fun observeAllCompanyRecords(): Flow<List<CompanyRecordEntity>>

    @Query("SELECT COUNT(*) FROM company_records")
    suspend fun getCompanyRecordCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanyRecord(record: CompanyRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanyRecords(records: List<CompanyRecordEntity>)

    @Update
    suspend fun updateCompanyRecord(record: CompanyRecordEntity)

    @Query("DELETE FROM company_records WHERE id = :id")
    suspend fun deleteCompanyRecordById(id: Long)

    // --- Strategy Configuration ---
    @Query("SELECT * FROM strategy_config WHERE id = 1 LIMIT 1")
    fun observeStrategyConfig(): Flow<StrategyConfigEntity?>

    @Query("SELECT * FROM strategy_config WHERE id = 1 LIMIT 1")
    suspend fun getStrategyConfigOnce(): StrategyConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStrategyConfig(config: StrategyConfigEntity)
}
