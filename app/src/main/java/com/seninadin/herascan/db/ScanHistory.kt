package com.seninadin.herascan.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,          // Belgenin varsayılan adı (ör. "Tarama_20240325")
    val dateMillis: Long,       // Tarandığı tarih
    val pdfUri: String? = null, // Oluşturulan PDF'in yolu
    val thumbnailUri: String? = null // Kullanıcının görebileceği ilk sayfanın kapak fotoğrafı
)
