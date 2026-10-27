package com.seninadin.herascan.utils

import android.util.Log
import com.seninadin.herascan.BuildConfig

/**
 * Merkezi log kapısı: yalnız hata ayıklama derlemelerinde Logcat'e yazar.
 * Sürüm derlemesinde hiçbir şey yazmaz — log satırları kullanıcı belge adı,
 * dosya yolu gibi kişisel veri taşıyabildiği için release'te tamamen sessizdir.
 * Kullanıcıya gösterilen mesajlar her zaman string kaynağından gelir.
 */
object Logger {
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) Log.e(tag, message, throwable)
    }
}
