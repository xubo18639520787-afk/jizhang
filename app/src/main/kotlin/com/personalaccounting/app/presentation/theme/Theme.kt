package com.personalaccounting.app.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.personalaccounting.app.data.preferences.ThemePreferences

// 主色调定义
object AppColors {
    // 薄荷绿主题
    val MintGreen = Color(0xFF4CAF50)
    val MintGreenVariant = Color(0xFF388E3C)
    val MintGreenLight = Color(0xFF81C784)
    
    // 橙色强调色
    val Orange = Color(0xFFFF9800)
    val OrangeVariant = Color(0xFFF57C00)
    val OrangeLight = Color(0xFFFFB74D)
    
    // 蓝色主题
    val Blue = Color(0xFF2196F3)
    val BlueVariant = Color(0xFF1976D2)
    val BlueLight = Color(0xFF64B5F6)
    
    // 紫色主题
    val Purple = Color(0xFF9C27B0)
    val PurpleVariant = Color(0xFF7B1FA2)
    val PurpleLight = Color(0xFFBA68C8)
    
    // 红色主题
    val Red = Color(0xFFF44336)
    val RedVariant = Color(0xFFD32F2F)
    val RedLight = Color(0xFFE57373)
    
    // 青色主题
    val Teal = Color(0xFF009688)
    val TealVariant = Color(0xFF00796B)
    val TealLight = Color(0xFF4DB6AC)
}

/**
 * 获取强调色方案
 */
fun getAccentColors(accentColor: ThemePreferences.AccentColor) = when (accentColor) {
    ThemePreferences.AccentColor.MINT_GREEN -> Triple(AppColors.MintGreen, AppColors.MintGreenVariant, AppColors.MintGreenLight)
    ThemePreferences.AccentColor.ORANGE -> Triple(AppColors.Orange, AppColors.OrangeVariant, AppColors.OrangeLight)
    ThemePreferences.AccentColor.BLUE -> Triple(AppColors.Blue, AppColors.BlueVariant, AppColors.BlueLight)
    ThemePreferences.AccentColor.PURPLE -> Triple(AppColors.Purple, AppColors.PurpleVariant, AppColors.PurpleLight)
    ThemePreferences.AccentColor.RED -> Triple(AppColors.Red, AppColors.RedVariant, AppColors.RedLight)
    ThemePreferences.AccentColor.TEAL -> Triple(AppColors.Teal, AppColors.TealVariant, AppColors.TealLight)
}

/**
 * 创建深色主题配色方案
 */
fun createDarkColorScheme(accentColor: ThemePreferences.AccentColor) = darkColorScheme(
    primary = getAccentColors(accentColor).first,
    onPrimary = Color.White,
    primaryContainer = getAccentColors(accentColor).second,
    onPrimaryContainer = Color.White,
    
    secondary = AppColors.Orange,
    onSecondary = Color.White,
    secondaryContainer = AppColors.OrangeVariant,
    onSecondaryContainer = Color.White,
    
    tertiary = getAccentColors(accentColor).third,
    onTertiary = Color.White,
    tertiaryContainer = getAccentColors(accentColor).second,
    onTertiaryContainer = Color.White,
    
    background = Color(0xFF121212),
    onBackground = Color(0xFFE3E3E3),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE3E3E3),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFCACACA),
    
    outline = Color(0xFF737373),
    outlineVariant = Color(0xFF404040),
    
    error = Color(0xFFCF6679),
    onError = Color.Black,
    errorContainer = Color(0xFFB00020),
    onErrorContainer = Color.White,
    
    inverseSurface = Color(0xFFE3E3E3),
    inverseOnSurface = Color(0xFF121212),
    inversePrimary = getAccentColors(accentColor).second,
    
    surfaceTint = getAccentColors(accentColor).first,
    scrim = Color.Black
)

/**
 * 创建浅色主题配色方案
 */
fun createLightColorScheme(accentColor: ThemePreferences.AccentColor) = lightColorScheme(
    primary = getAccentColors(accentColor).first,
    onPrimary = Color.White,
    primaryContainer = getAccentColors(accentColor).third,
    onPrimaryContainer = Color.Black,
    
    secondary = AppColors.Orange,
    onSecondary = Color.White,
    secondaryContainer = AppColors.OrangeLight,
    onSecondaryContainer = Color.Black,
    
    tertiary = getAccentColors(accentColor).third,
    onTertiary = Color.White,
    tertiaryContainer = getAccentColors(accentColor).third,
    onTertiaryContainer = Color.Black,
    
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF3F3F3),
    onSurfaceVariant = Color(0xFF49454F),
    
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = getAccentColors(accentColor).third,
    
    surfaceTint = getAccentColors(accentColor).first,
    scrim = Color.Black
)

@Composable
fun PersonalAccountingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    accentColor: ThemePreferences.AccentColor = ThemePreferences.AccentColor.MINT_GREEN,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> createDarkColorScheme(accentColor)
        else -> createLightColorScheme(accentColor)
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}