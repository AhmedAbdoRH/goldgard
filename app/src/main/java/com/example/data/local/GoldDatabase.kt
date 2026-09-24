package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TransactionEntity::class, SettingsEntity::class, MarketSettingsAuditLogEntity::class, PriceSnapshotEntity::class],
    version = 6,
    exportSchema = false
)
abstract class GoldDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun settingsDao(): SettingsDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun priceSnapshotDao(): PriceSnapshotDao

    companion object {
        @Volatile
        private var INSTANCE: GoldDatabase? = null

        fun getDatabase(context: Context): GoldDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GoldDatabase::class.java,
                    "gold_guard_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
