/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bookkeeping.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF70D8A0),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF00391F),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE7ECE9),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFBDC8C1),
    outline = androidx.compose.ui.graphics.Color(0xFF86968D),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFF46534B),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFDCEFE4),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF254B36),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFDCEFE4),
    surface = androidx.compose.ui.graphics.Color(0xFF1B2220),
    surfaceContainerHigh = androidx.compose.ui.graphics.Color(0xFF2A3430),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF254B36),
    secondary = secondaryDark,
    tertiary = tertiaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF007A43),
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color(0xFF202724),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF4C5952),
    outline = androidx.compose.ui.graphics.Color(0xFF7E8983),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFFBFCAC3),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF004C29),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFE0F0E8),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF004C29),
    surface = androidx.compose.ui.graphics.Color(0xFFF8FAF9),
    surfaceContainerHigh = androidx.compose.ui.graphics.Color(0xFFE9EEEC),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFDCEFE4),
    secondary = secondaryLight,
    tertiary = tertiaryLight
)

@Composable
fun BookkeepingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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

