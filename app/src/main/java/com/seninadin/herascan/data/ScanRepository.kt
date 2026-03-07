package com.seninadin.herascan.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.seninadin.herascan.db.ScanHistory
import com.seninadin.herascan.db.ScanHistoryDao
import kotlinx.coroutines.flow.Flow

class ScanRepository(private val scanHistoryDao: ScanHistoryDao) {

    val allScans: Flow<List<ScanHistory>> = scanHistoryDao.getAllScans()

    fun getScansPaged(): Flow<PagingData<ScanHistory>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            pagingSourceFactory = { scanHistoryDao.getScansPagingSource() }
        ).flow
    }

    suspend fun insert(scan: ScanHistory) {
        scanHistoryDao.insertScan(scan)
    }

    suspend fun delete(scan: ScanHistory) {
        scanHistoryDao.deleteScan(scan)
    }
}
