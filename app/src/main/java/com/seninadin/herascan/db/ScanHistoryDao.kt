package com.seninadin.herascan.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scanHistory: ScanHistory)

    @Delete
    suspend fun deleteScan(scanHistory: ScanHistory)

    // Tüm geçmişi tarihe göre (en yenisi en üstte) Flow ile dinlemek için
    @Query("SELECT * FROM scan_history ORDER BY dateMillis DESC")
    fun getAllScans(): Flow<List<ScanHistory>>
}
