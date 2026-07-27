package com.teguholica.chat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = WaGreen,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = WaTealLight,
    secondary = WaGreenLight,
    background = androidx.compose.ui.graphics.Color.White,
    surface = androidx.compose.ui.graphics.Color.White,
    onBackground = androidx.compose.ui.graphics.Color(0xFF303030),
    onSurface = androidx.compose.ui.graphics.Color(0xFF303030),
    error = androidx.compose.ui.graphics.Color(0xFFE53935),
)

private val DarkColors = darkColorScheme(
    primary = WaDarkPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = WaDarkBubbleOut,
    secondary = WaGreenLight,
    background = WaDarkBg,
    surface = WaDarkSurface,
    onBackground = androidx.compose.ui.graphics.Color(0xFFE9EDEF),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE9EDEF),
    surfaceVariant = WaDarkNav,
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF8696A0),
    error = androidx.compose.ui.graphics.Color(0xFFEF5350),
)

@Composable
fun ChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
