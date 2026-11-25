package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = DarkCoralPrimary,
    onPrimary = DarkOnCoralPrimary,
    primaryContainer = DarkCoralContainer,
    onPrimaryContainer = DarkOnCoralContainer,
    secondary = DarkSageSecondary,
    onSecondary = DarkOnSageSecondary,
    secondaryContainer = DarkSageContainer,
    onSecondaryContainer = DarkOnSageContainer,
    tertiary = DarkHoneyTertiary,
    onTertiary = DarkOnHoneyTertiary,
    tertiaryContainer = DarkHoneyContainer,
    onTertiaryContainer = DarkOnHoneyContainer,
    background = DarkWarmBackground,
    onBackground = DarkWarmInk,
    surface = DarkWarmSurface,
    onSurface = DarkWarmInk,
    surfaceVariant = DarkWarmSurfaceContainer,
    onSurfaceVariant = DarkWarmMutedInk,
    surfaceContainer = DarkWarmSurfaceContainer,
    surfaceContainerHigh = DarkWarmSurfaceContainerHigh,
    outline = DarkWarmOutline,
    outlineVariant = DarkWarmOutline
)

private val LightColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = OnCoralPrimary,
    primaryContainer = CoralContainer,
    onPrimaryContainer = OnCoralContainer,
    secondary = SageSecondary,
    onSecondary = OnSageSecondary,
    secondaryContainer = SageContainer,
    onSecondaryContainer = OnSageContainer,
    tertiary = HoneyTertiary,
    onTertiary = OnHoneyTertiary,
    tertiaryContainer = HoneyContainer,
    onTertiaryContainer = OnHoneyContainer,
    background = WarmBackground,
    onBackground = EspressoInk,
    surface = WarmSurface,
    onSurface = EspressoInk,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = WarmMutedInk,
    surfaceContainer = WarmSurfaceContainer,
    surfaceContainerHigh = WarmSurfaceContainerHigh,
    outline = WarmOutline,
    outlineVariant = WarmOutlineVariant,
    error = SoftError,
    errorContainer = SoftErrorContainer,
    onErrorContainer = OnSoftErrorContainer
)

private val ShonamoniShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun ShonamoniTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ShonamoniShapes,
        content = content
    )
}
