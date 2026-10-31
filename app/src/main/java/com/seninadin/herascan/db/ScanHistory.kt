package com.seninadin.herascan.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,          // Default document name (e.g. "Scan_20240325")
    val dateMillis: Long,       // Scan date
    val pdfUri: String? = null, // Generated PDF path
    val thumbnailUri: String? = null // Cover photo of the first page the user can see
)
