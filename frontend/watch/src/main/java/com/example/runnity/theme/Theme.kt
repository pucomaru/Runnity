package com.example.runnity.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// 간단한 Light 컬러 스킴으로 RunnityTheme 제공
private val RunnityLightColors = lightColorScheme(
    primary = ColorPalette.Light.primary,
    secondary = ColorPalette.Light.secondary,
    background = ColorPalette.Light.background,
    surface = ColorPalette.Light.background,
    onPrimary = ColorPalette.Light.background,
    onSecondary = ColorPalette.Light.primary,
    onBackground = ColorPalette.Light.primary,
    onSurface = ColorPalette.Light.primary,
)

@Composable
fun RunnityTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RunnityLightColors,
        typography = androidx.compose.material3.Typography(
            displayLarge = Typography.LargeTitle,
            titleLarge = Typography.Title,
            titleMedium = Typography.Heading,
            titleSmall = Typography.Subtitle,
            bodyLarge = Typography.Body,
            bodyMedium = Typography.Body,
            bodySmall = Typography.Caption,
            labelSmall = Typography.CaptionSmall,
        ),
        content = content
    )
}
