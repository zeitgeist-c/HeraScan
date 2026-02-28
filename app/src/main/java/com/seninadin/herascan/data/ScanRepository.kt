package com.seninadin.herascan.data

import com.seninadin.herascan.db.ScanHistory
import com.seninadin.herascan.db.ScanHistoryDao
import kotlinx.coroutines.flow.Flow

/**
 * ScanHistory verilerine erişimi yöneten repository sınıfı.
 * 
 * Veritabanı işlemleri için [ScanHistoryDao] kullanır ve verileri UI katmanına sunar.
 */
class ScanRepository(private val scanHistoryDao: ScanHistoryDao) {

    /**
     * Tüm tarama geçmişini tarihe göre azalan (en yeni en üstte) sırada Flow olarak döner.
     */
    val allScans: Flow<List<ScanHistory>> = scanHistoryDao.getAllScans()

    /**
     * Yeni bir tarama kaydını veritabanına ekler.
     * OnConflictStrategy.REPLACE tanımlı olduğundan ID çakışması durumunda üzerine yazar.
     */
    suspend fun insert(scan: ScanHistory) {
        scanHistoryDao.insertScan(scan)
    }

    /**
     * Verilen tarama kaydını veritabanından siler.
     */
    suspend fun delete(scan: ScanHistory) {
        scanHistoryDao.deleteScan(scan)
    }
}
