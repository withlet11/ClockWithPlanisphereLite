/*
 * Theme.kt
 *
 * Copyright 2020-2026 Yasuhiro Yamakawa <withlet11@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.withlet11.clockwithplanispherelite

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    // seed color #082080
    primary = Color(0xFF545C8C),
    onPrimary = Color(0xFFFAF8FF),
    secondary = Color(0xFF5B5E72),
    onSecondary = Color(0xFFFAF8FF),
    tertiary = Color(0xFF71557C),
    onTertiary = Color(0xFFFFF7FC),
    error = Color(0xFFA8364B),
    onError = Color(0xFFFFF7F7),
    primaryContainer = Color(0xFFBFC6FD),
    onPrimaryContainer = Color(0xFF373F6D),
    secondaryContainer = Color(0xFFE0E1F9),
    onSecondaryContainer = Color(0xFF4E5065),
    tertiaryContainer = Color(0xFFF1CEFC),
    onTertiaryContainer = Color(0xFF5D4368),
    errorContainer = Color(0xFFF97386),
    onErrorContainer = Color(0xFF6E0523),
    surfaceDim = Color(0xFFDAD9E4),
    surface = Color(0xFFFBF8FE),
    surfaceBright = Color(0xFFFBF8FE),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F2FB),
    surfaceContainer = Color(0xFFEFEDF6),
    surfaceContainerHigh = Color(0xFFE9E7F1),
    surfaceContainerHighest = Color(0xFFE3E1ED),
    onSurface = Color(0xFF31323B),
    onSurfaceVariant = Color(0xFF5E5E68),
    outline = Color(0xFF7A7A84),
    outlineVariant = Color(0xFFB2B1BC),
    inverseSurface = Color(0xFF0E0E12),
    inverseOnSurface = Color(0xFF9E9CA2),
    inversePrimary = Color(0xFF868DC1),
    scrim = Color(0xFF000000),
    primaryFixed = Color(0xFFBFC6FD),
    primaryFixedDim = Color(0xFFB1B8EE),
    onPrimaryFixed = Color(0xFF232B57),
    onPrimaryFixedVariant = Color(0xFF404876),
    secondaryFixed = Color(0xFFE0E1F9),
    secondaryFixedDim = Color(0xFFD1D3EB),
    onSecondaryFixed = Color(0xFF3C3E51),
    onSecondaryFixedVariant = Color(0xFF585A6F),
    tertiaryFixed = Color(0xFFF1CEFC),
    tertiaryFixedDim = Color(0xFFE3C0EE),
    onTertiaryFixed = Color(0xFF493055),
    onTertiaryFixedVariant = Color(0xFF674C72)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF969BC4),
    onPrimary = Color(0xFF181D3F),
    secondary = Color(0xFF9B9CB3),
    onSecondary = Color(0xFF1C1F31),
    tertiary = Color(0xFFB293BE),
    onTertiary = Color(0xFF2E1639),
    error = Color(0xFFF97386),
    onError = Color(0xFF490013),
    primaryContainer = Color(0xFF4B5074),
    onPrimaryContainer = Color(0xFFDFE1FF),
    secondaryContainer = Color(0xFF383A4D),
    onSecondaryContainer = Color(0xFFBCBED5),
    tertiaryContainer = Color(0xFFF1CEFC),
    onTertiaryContainer = Color(0xFF5D4368),
    errorContainer = Color(0xFF871C34),
    onErrorContainer = Color(0xFFFF97A3),
    surfaceDim = Color(0xFF0E0E12),
    surface = Color(0xFF0E0E12),
    surfaceBright = Color(0xFF2B2B34),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF121318),
    surfaceContainer = Color(0xFF181920),
    surfaceContainerHigh = Color(0xFF1E1F26),
    surfaceContainerHighest = Color(0xFF24252E),
    onSurface = Color(0xFFE6E4F0),
    onSurfaceVariant = Color(0xFFABAAB5),
    outline = Color(0xFF75747F),
    outlineVariant = Color(0xFF474750),
    inverseSurface = Color(0xFFFBF8FE),
    inverseOnSurface = Color(0xFF55545A),
    inversePrimary = Color(0xFF575D82),
    scrim = Color(0xFF000000),
    primaryFixed = Color(0xFFBFC6FD),
    primaryFixedDim = Color(0xFFB1B8EE),
    onPrimaryFixed = Color(0xFF232B57),
    onPrimaryFixedVariant = Color(0xFF404876),
    secondaryFixed = Color(0xFFE0E1F9),
    secondaryFixedDim = Color(0xFFD1D3EB),
    onSecondaryFixed = Color(0xFF3C3E51),
    onSecondaryFixedVariant = Color(0xFF585A6F),
    tertiaryFixed = Color(0xFFF1CEFC),
    tertiaryFixedDim = Color(0xFFE3C0EE),
    onTertiaryFixed = Color(0xFF493055),
    onTertiaryFixedVariant = Color(0xFF674C72)
)

@Composable
fun CwpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CwpTopAppBar(
    title: @Composable () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
    windowInsets: androidx.compose.foundation.layout.WindowInsets = androidx.compose.material3.TopAppBarDefaults.windowInsets,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior? = null
) {
    androidx.compose.material3.TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = windowInsets,
        scrollBehavior = scrollBehavior
    )
}
