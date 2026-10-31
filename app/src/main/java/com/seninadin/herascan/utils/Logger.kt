package com.seninadin.herascan.utils

import android.util.Log
import com.seninadin.herascan.BuildConfig

/**
 * Central log gate: writes to Logcat in debug builds only.
 * Writes nothing in release builds — log lines may carry personal data like
 * document names and file paths, so release builds stay fully silent.
 * User-visible messages always come from string resources.
 */
object Logger {
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) Log.e(tag, message, throwable)
    }
}
