package com.seninadin.herascan.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ScanHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scanHistory: ScanHistory)

    @Delete
    suspend fun deleteScan(scanHistory: ScanHistory)

    @Update
    suspend fun updateScan(scanHistory: ScanHistory)

    @Query("SELECT * FROM scan_history ORDER BY dateMillis DESC")
    fun getScansPagingSource(): PagingSource<Int, ScanHistory>
}
