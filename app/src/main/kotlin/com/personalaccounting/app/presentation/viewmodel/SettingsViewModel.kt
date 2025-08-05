package com.personalaccounting.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalaccounting.app.data.export.DataExportManager
import com.personalaccounting.app.data.preferences.ThemePreferences
import com.personalaccounting.app.data.preferences.ThemePreferences.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
    private val dataExportManager: DataExportManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                themePreferences.themeMode.collect { themeMode ->
                    _uiState.value = _uiState.value.copy(
                        currentThemeMode = themeMode,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )
            }
        }
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(themeMode)
        }
    }

    fun exportData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isExporting = true)
                
                // 导出为JSON格式
                val result = dataExportManager.exportToJson()
                
                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportMessage = "数据导出成功！文件已保存到: ${result.filePath}",
                        showExportDialog = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportMessage = "数据导出失败: ${result.message}",
                        showExportDialog = true
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportMessage = "数据导出异常: ${e.message}",
                    showExportDialog = true
                )
            }
        }
    }

    fun importData(filePath: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isImporting = true)
                
                val result = dataExportManager.importFromJson(filePath)
                
                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        importMessage = "数据导入成功！共导入 ${result.importedCount} 条记录",
                        showImportDialog = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        importMessage = "数据导入失败: ${result.message}",
                        showImportDialog = true
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    importMessage = "数据导入异常: ${e.message}",
                    showImportDialog = true
                )
            }
        }
    }

    fun clearExportMessage() {
        _uiState.value = _uiState.value.copy(
            showExportDialog = false,
            exportMessage = null
        )
    }

    fun clearImportMessage() {
        _uiState.value = _uiState.value.copy(
            showImportDialog = false,
            importMessage = null
        )
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isClearing = true)
                
                val result = dataExportManager.clearAllData()
                
                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        isClearing = false,
                        clearMessage = "所有数据已清除",
                        showClearDialog = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isClearing = false,
                        clearMessage = "清除数据失败: ${result.message}",
                        showClearDialog = true
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isClearing = false,
                    clearMessage = "清除数据异常: ${e.message}",
                    showClearDialog = true
                )
            }
        }
    }

    fun clearClearMessage() {
        _uiState.value = _uiState.value.copy(
            showClearDialog = false,
            clearMessage = null
        )
    }
}

data class SettingsUiState(
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val isClearing: Boolean = false,
    val currentThemeMode: ThemeMode = ThemeMode.SYSTEM,
    val exportMessage: String? = null,
    val importMessage: String? = null,
    val clearMessage: String? = null,
    val showExportDialog: Boolean = false,
    val showImportDialog: Boolean = false,
    val showClearDialog: Boolean = false
)