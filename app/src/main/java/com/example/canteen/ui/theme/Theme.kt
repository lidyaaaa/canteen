package com.example.canteen.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.canteen.ui.theme.*

private val LightColorScheme = lightColorScheme(
    primary = YellowPrimary,
    onPrimary = Black,
    secondary = YellowLight,
    onSecondary = Black,
    background = GrayBg,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    error = RedError,
    onError = White
)

@Composable
fun CanteenTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}