package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("UPDATE transactions SET note = :note WHERE id = :id")
    suspend fun updateTransactionNote(id: Long, note: String)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}

@Dao
interface SettingsDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    fun getSetting(key: String): Flow<String?>

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingDirect(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: SettingsEntity)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM market_settings_audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<MarketSettingsAuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MarketSettingsAuditLogEntity): Long

    @Query("DELETE FROM market_settings_audit_logs")
    suspend fun clearAll()
}

@Dao
interface PriceSnapshotDao {
    @Query("SELECT * FROM price_snapshots ORDER BY timestamp DESC LIMIT 1")
    fun getLatestSnapshot(): Flow<PriceSnapshotEntity?>

    @Query("SELECT * FROM price_snapshots ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSnapshotDirect(): PriceSnapshotEntity?

    @Query("SELECT * FROM price_snapshots ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSnapshots(limit: Int = 50): Flow<List<PriceSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: PriceSnapshotEntity): Long

    @Query("DELETE FROM price_snapshots")
    suspend fun clearAll()
}
