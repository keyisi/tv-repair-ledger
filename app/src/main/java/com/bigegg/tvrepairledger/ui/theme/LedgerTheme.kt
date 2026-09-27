package com.bigegg.tvrepairledger.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink900 = Color(0xFF101828)
val Ink700 = Color(0xFF344054)
val Ink500 = Color(0xFF667085)
val SurfaceCanvas = Color(0xFFF3F6F8)
val SurfaceCard = Color(0xFFFFFFFF)
val LineSoft = Color(0xFFE4E7EC)
val RepairBlue = Color(0xFF145C74)
val RepairBlueDark = Color(0xFF0D3F50)
val ProfitGreen = Color(0xFF0F7A5A)
val CostAmber = Color(0xFFB36B00)
val DangerRed = Color(0xFFB42318)

private val ledgerColorScheme = lightColorScheme(
    primary = RepairBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9EEF4),
    onPrimaryContainer = RepairBlueDark,
    secondary = ProfitGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDF4EA),
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = CostAmber,
    onTertiary = Color.White,
    background = SurfaceCanvas,
    onBackground = Ink900,
    surface = SurfaceCard,
    onSurface = Ink900,
    surfaceVariant = Color(0xFFEAF0F3),
    onSurfaceVariant = Ink700,
    outline = LineSoft,
    error = DangerRed
)

@Composable
fun LedgerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ledgerColorScheme,
        content = content
    )
}

val ColorScheme.positive: Color
    get() = ProfitGreen

val ColorScheme.cost: Color
    get() = CostAmber
