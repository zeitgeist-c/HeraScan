package com.seninadin.herascan.data

import com.seninadin.herascan.db.ScanHistory
import com.seninadin.herascan.db.ScanHistoryDao
import kotlinx.coroutines.flow.Flow

class ScanRepository(private val scanHistoryDao: ScanHistoryDao) {

    val allScans: Flow<List<ScanHistory>> = scanHistoryDao.getAllScans()

    suspend fun insert(scan: ScanHistory) {
        scanHistoryDao.insertScan(scan)
    }

    suspend fun delete(scan: ScanHistory) {
        scanHistoryDao.deleteScan(scan)
    }
}
