package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "BUY" or "SELL"
    val karat: Int,
    val weight: Double,
    val pricePerGram: Double,
    val makingCharges: Double = 0.0,
    val stampFee: Double = 0.0,
    val extraFees: Double = 0.0,
    val deductionPercent: Double = 0.0,
    val totalFairPrice: Double,
    val shopQuotedPrice: Double = 0.0,
    val priceDifference: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDate: String = "",
    val note: String = "", // وصف العملية (مثل: شبكة، سبيكة)
    val receiptId: String = "",
    val shopName: String = "", // اسم المحل
    val referenceGramPrice: Double = 0.0,
    val referenceRawPrice: Double = 0.0,
    val marketFactor: Double = 1.0,
    val priceSource: String = "",
    val deviationPercent: Double = 0.0,
    val isDeviationWarning: Boolean = false
)

@Entity(tableName = "app_settings")
data class SettingsEntity(
    @PrimaryKey
    val key: String,
    val value: String
)

@Entity(tableName = "market_settings_audit_logs")
data class MarketSettingsAuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val settingName: String = "",
    val oldValue: String = "",
    val newValue: String = "",
    val changedBy: String = "",
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDate: String = ""
) {
    val changeReason: String get() = reason
    val previousValues: String get() = oldValue
    val newValues: String get() = newValue
}

