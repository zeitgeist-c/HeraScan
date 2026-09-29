package com.seninadin.herascan.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Merkezi tarih biçimlendiriciler (tek sözleşme noktası).
 * Cihaz diline saygı için [Locale.getDefault] kullanılır.
 */
object DateFormats {
    private const val HISTORY_PATTERN = "d MMM yyyy"
    private const val SCAN_PATTERN = "dd MMMM yyyy HH:mm"
    private val FILE_PATTERN_LOCALE = Locale.US
    private const val FILE_PATTERN = "yyyyMMdd_HHmm"

    fun formatHistoryDate(millis: Long): String =
        SimpleDateFormat(HISTORY_PATTERN, Locale.getDefault()).format(Date(millis))

    fun formatScanTimestamp(date: Date): String =
        SimpleDateFormat(SCAN_PATTERN, Locale.getDefault()).format(date)

    fun formatFileTimestamp(date: Date): String =
        SimpleDateFormat(FILE_PATTERN, FILE_PATTERN_LOCALE).format(date)
}
