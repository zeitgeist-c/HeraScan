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
//  To use DM Sans:
//  1. add this to build.gradle (app):
//       implementation("androidx.compose.ui:ui-text-google-fonts:1.6.8")
//  2. uncomment the block below and remove the FontFamily.Default
//     line.
//
//  The system font (Roboto) is used for now.
// ════════════════════════════════════════════════════════════════

// ─── Uncomment this block to use DM Sans ────
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

private val AppFont = FontFamily.Default // Replace with DmSans

val HeraScanTypography = Typography(

    // App title
    titleLarge = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.3).sp
    ),

    // Section titles (e.g. "Active Scan")
    titleMedium = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.1).sp
    ),

    // Card names, small titles
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

    // Descriptions
    bodyMedium = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),

    // Secondary description
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

    // Chip, badge, small labels
    labelMedium = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),

    // Uppercase section headers (SECTION LABELS)
    labelSmall = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.08.sp
    )
)

// Alias for backward compatibility
val Typography = HeraScanTypography
