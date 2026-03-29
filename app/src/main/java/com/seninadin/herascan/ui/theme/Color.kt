@file:Suppress("SpellCheckingInspection")
package com.seninadin.herascan.ui.theme

import androidx.compose.ui.graphics.Color

// ════════════════════════════════════════════════════════════════
//  HeraScan Design System — Color Tokens
//  Palette: Professional Blue · Trust Green · OCR Amber
// ════════════════════════════════════════════════════════════════

// ─── Primary Blue ────────────────────────────────────────────────
val Blue50  = Color(0xFFEBF2FF)
val Blue100 = Color(0xFFBDD5FA)
val Blue200 = Color(0xFF8EBBF7)
val Blue400 = Color(0xFF3D7EE8)
val Blue600 = Color(0xFF1558C8)
val Blue800 = Color(0xFF0D3D8E)
val Blue900 = Color(0xFF072660)

// ─── Trust Green ─────────────────────────────────────────────────
val Green50  = Color(0xFFE6F5EF)
val Green100 = Color(0xFFB3E2D0)
val Green400 = Color(0xFF18A874)
val Green600 = Color(0xFF0A7C55)
val Green800 = Color(0xFF065039)
val Green900 = Color(0xFF033425)

// ─── OCR Amber ───────────────────────────────────────────────────
val Amber50  = Color(0xFFFEF3E0)
val Amber100 = Color(0xFFFDE0A8)
val Amber400 = Color(0xFFF0A830)
val Amber600 = Color(0xFFD97706)
val Amber800 = Color(0xFF92400E)
val Amber900 = Color(0xFF5C2800)

// ─── Danger Red ──────────────────────────────────────────────────
val Red50  = Color(0xFFFEE2E2)
val Red600 = Color(0xFFDC2626)
val Red800 = Color(0xFF7F1D1D)

// ─── Neutral Gray ────────────────────────────────────────────────
val Gray50  = Color(0xFFEEF2F7)
val Gray100 = Color(0xFFDDE5EF)
val Gray200 = Color(0xFFC8D5E8)
val Gray500 = Color(0xFF6B7A8D)
val Gray700 = Color(0xFF374151)
val Gray900 = Color(0xFF111827)

// ─── Dark Navy ───────────────────────────────────────────────────
val Navy950 = Color(0xFF0B1322)
val Navy900 = Color(0xFF131F30)
val Navy800 = Color(0xFF1A2A40)
val Navy700 = Color(0xFF223350)
val Navy600 = Color(0xFF2A4068)
val Navy400 = Color(0xFF3A5878)
val Navy200 = Color(0xFF7A9CC0)
val Navy100 = Color(0xFFB8D0E8)
val Navy50  = Color(0xFFE8F0FC)

// ════════════════════════════════════════════════════════════════
//  Semantic Tokens — Light Theme
// ════════════════════════════════════════════════════════════════

val md_theme_light_primary              = Blue600
val md_theme_light_onPrimary            = Color.White
val md_theme_light_primaryContainer     = Blue50
val md_theme_light_onPrimaryContainer   = Blue800

val md_theme_light_secondary            = Green600
val md_theme_light_onSecondary          = Color.White
val md_theme_light_secondaryContainer   = Green50
val md_theme_light_onSecondaryContainer = Green800

val md_theme_light_tertiary             = Amber600
val md_theme_light_onTertiary           = Color.White
val md_theme_light_tertiaryContainer    = Amber50
val md_theme_light_onTertiaryContainer  = Amber800

val md_theme_light_error                = Red600
val md_theme_light_onError              = Color.White
val md_theme_light_errorContainer       = Red50
val md_theme_light_onErrorContainer     = Red800

val md_theme_light_background           = Gray50
val md_theme_light_onBackground         = Gray900
val md_theme_light_surface              = Color.White
val md_theme_light_onSurface            = Gray900
val md_theme_light_surfaceVariant       = Color(0xFFF4F7FB)
val md_theme_light_onSurfaceVariant     = Gray700
val md_theme_light_outline              = Gray100
val md_theme_light_outlineVariant       = Gray200

// ════════════════════════════════════════════════════════════════
//  Semantic Tokens — Dark Theme
// ════════════════════════════════════════════════════════════════

val md_theme_dark_primary               = Color(0xFF5B9EFF)
val md_theme_dark_onPrimary             = Navy950
val md_theme_dark_primaryContainer      = Color(0xFF0D2040)
val md_theme_dark_onPrimaryContainer    = Navy100

val md_theme_dark_secondary             = Color(0xFF22D49A)
val md_theme_dark_onSecondary           = Navy950
val md_theme_dark_secondaryContainer    = Color(0xFF082A1E)
val md_theme_dark_onSecondaryContainer  = Color(0xFF5EEDC4)

val md_theme_dark_tertiary              = Color(0xFFFBBB40)
val md_theme_dark_onTertiary            = Navy950
val md_theme_dark_tertiaryContainer     = Color(0xFF2A1A00)
val md_theme_dark_onTertiaryContainer   = Color(0xFFFCD87A)

val md_theme_dark_error                 = Color(0xFFF87171)
val md_theme_dark_onError               = Color(0xFF2A0808)
val md_theme_dark_errorContainer        = Color(0xFF2A0808)
val md_theme_dark_onErrorContainer      = Color(0xFFFCA5A5)

val md_theme_dark_background            = Navy950
val md_theme_dark_onBackground          = Navy50
val md_theme_dark_surface               = Navy900
val md_theme_dark_onSurface             = Navy50
val md_theme_dark_surfaceVariant        = Navy800
val md_theme_dark_onSurfaceVariant      = Navy200
val md_theme_dark_outline               = Navy700
val md_theme_dark_outlineVariant        = Navy600
