package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Blue,
    onPrimary = Base,
    primaryContainer = Surface2,
    onPrimaryContainer = Blue,
    secondary = Mauve,
    onSecondary = Base,
    secondaryContainer = Surface1,
    onSecondaryContainer = Mauve,
    tertiary = Teal,
    onTertiary = Base,
    tertiaryContainer = Surface0,
    onTertiaryContainer = Teal,
    error = Red,
    onError = Base,
    errorContainer = Surface0,
    onErrorContainer = Red,
    background = Base,
    onBackground = Text,
    surface = Mantle,
    onSurface = Text,
    surfaceVariant = Crust,
    onSurfaceVariant = Subtext1,
    outline = Overlay0
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Dark Theme for "Catppuccin Mocha"
  dynamicColor: Boolean = false, // Disable dynamic colors to ensure the theme applies
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
