package com.seninadin.herascan.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Uygulama içi dil tercihi — tek sözleşme noktası.
 *
 * Kurallar (13 dil görevi):
 * - Kayıtlı açık tercih kalıcıdır; güncelleme/yeniden başlatma sıfırlamaz.
 * - Tercih yoksa yeni profil İngilizce başlar; sistem dili asla öne geçmez.
 * - "Uygulamayı yeniden başlatma" gerekmez: AppCompatDelegate değişimi
 *   activity'yi kendisi yeniden oluşturur, tarama/OCR işi etkilenmez.
 */
object AppLanguage {
    const val PREFS = "herascan_prefs"
    const val KEY = "app_language"
    const val DEFAULT = "en"

    /** Kod, BCP-47 etiketi, kendi adıyla görünen ad. Bayrak kullanılmaz. */
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

    /** Kaydet ve uygula. Bilinmeyen/bozuk kod gelirse İngilizceye düşer. */
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

    /** Açılışta bir kez: kayıtlı tercih yoksa İngilizceyi kilitle. */
    fun applyStoredOrDefault(context: Context) {
        apply(context, savedTag(context) ?: DEFAULT)
    }
}
