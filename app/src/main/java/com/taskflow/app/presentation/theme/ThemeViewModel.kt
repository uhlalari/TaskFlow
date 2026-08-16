package com.taskflow.app.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.data.local.preferences.ThemeManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(private val themeManager: ThemeManager) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = themeManager.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun onToggleTheme(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setDarkTheme(enabled)
        }
    }
}
