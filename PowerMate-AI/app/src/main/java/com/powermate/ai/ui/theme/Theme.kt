package com.powermate.ai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Core Surface Colors ────────────────────────────────────────────────────
val AmoledBlack    = Color(0xFF020912)
val SurfaceDark    = Color(0xFF0A1628)
val CardDark       = Color(0xFF0F1C2E)
val CardElevated   = Color(0xFF162236)
val CardHighlight  = Color(0xFF1E2D42)

// ── Brand Colors ───────────────────────────────────────────────────────────
val PrimaryBlue    = Color(0xFF2563EB)
val SoftPrimary    = Color(0xFFB4C5FF)
val Cyan           = Color(0xFF22D3EE)
val CyanDeep       = Color(0xFF0891B2)
val ElectricBlue   = Color(0xFF3B82F6)

// ── Status Colors ──────────────────────────────────────────────────────────
val SuccessGreen   = Color(0xFF22C55E)
val SuccessDeep    = Color(0xFF16A34A)
val WarningAmber   = Color(0xFFF59E0B)
val WarningDeep    = Color(0xFFD97706)
val DangerRed      = Color(0xFFEF4444)
val DangerDeep     = Color(0xFFDC2626)

// ── Text Colors ────────────────────────────────────────────────────────────
val TextMain       = Color(0xFFF8FAFC)
val TextSecondary  = Color(0xFF94A3B8)
val TextMuted      = Color(0xFF475569)
val TextAccent     = Color(0xFFE2E8F0)

// ── Gradient Palettes (use with Brush.linearGradient) ─────────────────────
val GradientPrimary   = listOf(Color(0xFF1D4ED8), Color(0xFF22D3EE))
val GradientSuccess   = listOf(Color(0xFF16A34A), Color(0xFF22D3EE))
val GradientWarning   = listOf(Color(0xFFD97706), Color(0xFFF59E0B))
val GradientDanger    = listOf(Color(0xFFDC2626), Color(0xFFF97316))
val GradientPurple    = listOf(Color(0xFF7C3AED), Color(0xFF22D3EE))
val GradientGold      = listOf(Color(0xFFCA8A04), Color(0xFFFBBF24))

private val DarkColors = darkColorScheme(
    primary              = SoftPrimary,
    onPrimary            = Color(0xFF001A72),
    primaryContainer     = PrimaryBlue,
    onPrimaryContainer   = TextMain,
    secondary            = Cyan,
    tertiary             = SuccessGreen,
    background           = AmoledBlack,
    onBackground         = TextMain,
    surface              = SurfaceDark,
    onSurface            = TextMain,
    surfaceVariant       = CardElevated,
    onSurfaceVariant     = TextSecondary,
    error                = DangerRed,
    outline              = TextMuted
)

@Composable
fun PowerMateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}
