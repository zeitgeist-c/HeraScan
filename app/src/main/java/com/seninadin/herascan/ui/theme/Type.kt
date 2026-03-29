@file:Suppress("SpellCheckingInspection")
package com.seninadin.herascan.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ════════════════════════════════════════════════════════════════
//  HeraScan Typography — DM Sans
//
//  DM Sans kullanmak için:
//  1. build.gradle (app) dosyasına şunu ekleyin:
//       implementation("androidx.compose.ui:ui-text-google-fonts:1.6.8")
//  2. Aşağıdaki yorum satırlarını kaldırın ve FontFamily.Default
//     satırını kaldırın.
//
//  Şu an için sistem fontu (Roboto) kullanılmaktadır.
// ════════════════════════════════════════════════════════════════

// ─── DM Sans ile kullanmak için bu bloğun yorumunu kaldırın ────
// import androidx.compose.ui.text.googlefonts.Font
// import androidx.compose.ui.text.googlefonts.GoogleFont
//
// private val provider = GoogleFont.Provider(
//     providerAuthority = "com.google.android.gms.fonts",
//     providerPackage   = "com.google.android.gms",
//     certificates      = R.array.com_google_android_gms_fonts_certs
// )
// private val DmSansFontName = GoogleFont("DM Sans")
// private val DmSans = FontFamily(
//     Font(googleFont = DmSansFontName, fontProvider = provider, weight = FontWeight.Normal),
//     Font(googleFont = DmSansFontName, fontProvider = provider, weight = FontWeight.Medium),
//     Font(googleFont = DmSansFontName, fontProvider = provider, weight = FontWeight.SemiBold),
// )
// ────────────────────────────────────────────────────────────────

private val AppFont = FontFamily.Default // DmSans ile değiştirin

val HeraScanTypography = Typography(

    // Uygulama başlığı
    titleLarge = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.3).sp
    ),

    // Bölüm başlıkları (ör. "Aktif Tarama")
    titleMedium = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.1).sp
    ),

    // Kart isimleri, küçük başlıklar
    titleSmall = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),

    // Genel metin
    bodyLarge = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),

    // Açıklamalar
    bodyMedium = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),

    // İkincil açıklama
    bodySmall = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),

    // Buton / aksiyon etiketleri
    labelLarge = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),

    // Chip, badge, küçük etiketler
    labelMedium = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),

    // Bölüm üstü kapitaller (SECTION LABELS)
    labelSmall = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.08.sp
    )
)

// Geriye dönük uyumluluk için takma ad
val Typography = HeraScanTypography
