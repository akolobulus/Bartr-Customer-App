package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = BartrBlue,
    onPrimary = Color.White,
    primaryContainer = BartrBlueS2,
    secondary = BartrBlueS2,
    background = Color(0xFF10141D),
    surface = Color(0xFF171D28),
    onBackground = Color.White,
    onSurface = Color.White,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BartrBlue,
    onPrimary = Color.White,
    primaryContainer = BartrBlueT4,
    onPrimaryContainer = BartrBlueS2,
    secondary = BartrBlueS2,
    onSecondary = Color.White,
    background = Color.White,
    surface = Color.White,
    onBackground = BartrInk,
    onSurface = BartrInk,
    surfaceVariant = BartrBackdrop,
    onSurfaceVariant = BartrInkSoft,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

