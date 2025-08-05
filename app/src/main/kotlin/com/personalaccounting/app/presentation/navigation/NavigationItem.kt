package com.personalaccounting.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 底部导航项目定义
 */
sealed class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : NavigationItem(
        route = "home",
        title = "首页",
        icon = Icons.Default.Home
    )
    
    object AddTransaction : NavigationItem(
        route = "add_transaction",
        title = "记账",
        icon = Icons.Default.Add
    )
    
    object Statistics : NavigationItem(
        route = "statistics",
        title = "统计",
        icon = Icons.Default.BarChart
    )
    
    object Settings : NavigationItem(
        route = "settings",
        title = "设置",
        icon = Icons.Default.Settings
    )
}

/**
 * 获取所有底部导航项目
 */
val bottomNavigationItems = listOf(
    NavigationItem.Home,
    NavigationItem.AddTransaction,
    NavigationItem.Statistics,
    NavigationItem.Settings
)