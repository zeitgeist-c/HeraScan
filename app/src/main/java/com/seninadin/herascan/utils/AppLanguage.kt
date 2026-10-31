package com.seninadin.herascan.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * In-app language preference — single contract point.
 *
 * Rules (13-language task):
 * - A saved explicit preference persists; updates/restarts never reset it.
 * - Without a preference a fresh profile starts in English; the system language never wins.
 * - "Restarting the app" is not needed: the AppCompatDelegate swap
 *   recreates the activity itself; scanning/OCR work is unaffected.
 */
object AppLanguage {
    const val PREFS = "herascan_prefs"
    const val KEY = "app_language"
    const val DEFAULT = "en"

    /** Code, BCP-47 tag, display name in its own language. No flags. */
    val LANGUAGES: List<Triple<String, String, String>> = listOf(
        Triple("en", "en", "English"),
        Triple("tr", "tr", "Türkçe"),
        Triple("de", "de", "Deutsch"),
        Triple("es", "es", "Español"),
        Triple("fr", "fr", "Français"),
        Triple("it", "it", "Italiano"),
        Triple("pt-BR", "pt-BR", "Português (Brasil)"),
        Triple("ru", "ru", "Русский"),
        Triple("pl", "pl", "Polski"),
        Triple("nl", "nl", "Nederlands"),
        Triple("zh-CN", "zh-CN", "简体中文"),
        Triple("ja", "ja", "日本語"),
        Triple("ko", "ko", "한국어"),
    )

    fun savedTag(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null)

    fun currentTag(context: Context): String = savedTag(context) ?: DEFAULT

    /** Save and apply. Unknown/broken codes fall back to English. */
    fun apply(context: Context, code: String) {
        val entry = LANGUAGES.find { it.first == code } ?: LANGUAGES.first()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, entry.first)
            .apply()
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(entry.second)
        )
    }

    /** Once at startup: apply the saved preference; without a record English
     *  applies but is NOT written — the onboarding language picker must see the
     *  "no record" state. apply() saves once the user chooses. */
    fun applyStoredOrDefault(context: Context) {
        val tag = savedTag(context) ?: DEFAULT
        val entry = LANGUAGES.find { it.first == tag } ?: LANGUAGES.first()
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(entry.second)
        )
    }
}
