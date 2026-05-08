package com.example.finasset.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.finasset.FinAssetApp
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as FinAssetApp

    val themeMode = app.preferencesManager.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, "system")

    val redUpGreenDown = app.preferencesManager.redUpGreenDown
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val refreshInterval = app.preferencesManager.refreshInterval
        .stateIn(viewModelScope, SharingStarted.Eagerly, 60)

    val autoRefresh = app.preferencesManager.autoRefresh
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setTheme(mode: String) {
        viewModelScope.launch { app.preferencesManager.setThemeMode(mode) }
    }

    fun setRedUpGreenDown(value: Boolean) {
        viewModelScope.launch { app.preferencesManager.setRedUpGreenDown(value) }
    }

    fun setRefreshInterval(minutes: Int) {
        viewModelScope.launch { app.preferencesManager.setRefreshInterval(minutes) }
    }

    fun setAutoRefresh(value: Boolean) {
        viewModelScope.launch { app.preferencesManager.setAutoRefresh(value) }
    }

    fun exportData(): String {
        return "数据导出功能 - 可导出Excel/CSV格式"
    }
}
