package com.ingridientsinc.recipe.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Terracotta80,
    onPrimary = Terracotta20,
    primaryContainer = Terracotta40,
    onPrimaryContainer = Terracotta80,

    secondary = Sage80,
    onSecondary = Sage20,
    secondaryContainer = Sage40,
    onSecondaryContainer = Sage80,

    tertiary = Amber80,
    onTertiary = Amber20,
    tertiaryContainer = Amber40,
    onTertiaryContainer = Amber80,

    background = CharcoalBrown,
    onBackground = Cream,
    surface = DarkSurface,
    onSurface = Cream,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = LightBrown,
    outline = MediumBrown,
    outlineVariant = DarkContainer,
)

private val LightColorScheme = lightColorScheme(
    primary = Terracotta40,
    onPrimary = Cream,
    primaryContainer = Terracotta80,
    onPrimaryContainer = Terracotta20,

    secondary = Sage40,
    onSecondary = Cream,
    secondaryContainer = Sage80,
    onSecondaryContainer = Sage20,

    tertiary = Amber40,
    onTertiary = Cream,
    tertiaryContainer = Amber80,
    onTertiaryContainer = Amber20,

    background = Cream,
    onBackground = DarkBrown,
    surface = WarmWhite,
    onSurface = DarkBrown,
    surfaceVariant = LightBrown,
    onSurfaceVariant = MediumBrown,
    outline = MediumBrown,
    outlineVariant = Color(0xFFE8D5C0),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

@Composable
fun RecipesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // disabled to enforce our palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}