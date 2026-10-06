package io.github.marchansergi.cleancard.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Gold = Color(0xFFD9B45A)

private val ColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Color(0xFF1A1405),
    primaryContainer = Color(0xFF3A2F14),
    onPrimaryContainer = Color(0xFFF3DFA6),
    secondary = Color(0xFFBFA36A),
    onSecondary = Color(0xFF1A1405),
    background = Color(0xFF0E0E10),
    onBackground = Color(0xFFECECEC),
    surface = Color(0xFF0E0E10),
    onSurface = Color(0xFFECECEC),
    surfaceVariant = Color(0xFF232327),
    onSurfaceVariant = Color(0xFFB9B9C0),
    surfaceContainerLow = Color(0xFF141416),
    surfaceContainer = Color(0xFF18181B),
    surfaceContainerHigh = Color(0xFF1E1E22),
    surfaceContainerHighest = Color(0xFF26262B),
    outline = Color(0xFF55555C),
    error = Color(0xFFFF8A80),
)

@Composable
fun CleanCardTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColorScheme, content = content)
}
