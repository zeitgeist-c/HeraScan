package com.seninadin.herascan.utils

import android.util.Log
import com.seninadin.herascan.BuildConfig

/**
 * Merkezi log kapısı: hata ayıklama derlemelerinde Logcat'e yazar,
 * sürüm derlemelerinde bilgi (d) loglarını atar.
 * Kullanıcıya gösterilen mesajlar her zaman string kaynağından gelir;
 * iç hata detayı (yol, URI, exception mesajı) buraya yazılır.
 */
object Logger {
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) Log.e(tag, message, throwable) else Log.e(tag, message)
    }
}
