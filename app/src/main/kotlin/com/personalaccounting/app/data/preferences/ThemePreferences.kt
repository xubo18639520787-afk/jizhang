package com.personalaccounting.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

/**
 * 主题偏好设置管理器
 */
@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val ACCENT_COLOR_KEY = stringPreferencesKey("accent_color")
    }
    
    /**
     * 主题模式枚举
     */
    enum class ThemeMode {
        SYSTEM,  // 跟随系统
        LIGHT,   // 浅色模式
        DARK     // 深色模式
    }
    
    /**
     * 强调色枚举
     */
    enum class AccentColor {
        MINT_GREEN,    // 薄荷绿（默认）
        ORANGE,        // 橙色
        BLUE,          // 蓝色
        PURPLE,        // 紫色
        RED,           // 红色
        TEAL           // 青色
    }
    
    /**
     * 获取深色模式设置
     */
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }
    
    /**
     * 获取动态颜色设置
     */
    val isDynamicColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DYNAMIC_COLOR_KEY] ?: true
    }
    
    /**
     * 获取主题模式设置
     */
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val mode = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(mode)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }
    
    /**
     * 获取强调色设置
     */
    val accentColor: Flow<AccentColor> = context.dataStore.data.map { preferences ->
        val color = preferences[ACCENT_COLOR_KEY] ?: AccentColor.MINT_GREEN.name
        try {
            AccentColor.valueOf(color)
        } catch (e: IllegalArgumentException) {
            AccentColor.MINT_GREEN
        }
    }
    
    /**
     * 设置深色模式
     */
    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDark
        }
    }
    
    /**
     * 设置动态颜色
     */
    suspend fun setDynamicColor(isDynamic: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = isDynamic
        }
    }
    
    /**
     * 设置主题模式
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }
    
    /**
     * 设置强调色
     */
    suspend fun setAccentColor(color: AccentColor) {
        context.dataStore.edit { preferences ->
            preferences[ACCENT_COLOR_KEY] = color.name
        }
    }
}