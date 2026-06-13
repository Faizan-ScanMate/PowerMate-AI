package com.powermate.ai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AmoledBlack = Color(0xFF050B14)
val SurfaceDark = Color(0xFF0D141D)
val CardDark = Color(0xFF111827)
val CardElevated = Color(0xFF19202A)
val PrimaryBlue = Color(0xFF2563EB)
val SoftPrimary = Color(0xFFB4C5FF)
val Cyan = Color(0xFF22D3EE)
val SuccessGreen = Color(0xFF22C55E)
val WarningAmber = Color(0xFFF59E0B)
val DangerRed = Color(0xFFEF4444)
val TextMain = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFFCBD5E1)

private val DarkColors = darkColorScheme(
    primary = SoftPrimary,
    onPrimary = Color(0xFF002A78),
    primaryContainer = PrimaryBlue,
    onPrimaryContainer = TextMain,
    secondary = Cyan,
    tertiary = SuccessGreen,
    background = AmoledBlack,
    onBackground = TextMain,
    surface = SurfaceDark,
    onSurface = TextMain,
    surfaceVariant = CardElevated,
    onSurfaceVariant = TextSecondary,
    error = DangerRed
)

@Composable
fun PowerMateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}
