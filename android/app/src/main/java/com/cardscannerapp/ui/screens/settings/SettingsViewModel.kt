package com.cardscannerapp.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardscannerapp.data.repository.ContactRepository
import com.cardscannerapp.data.repository.SettingsRepository
import com.cardscannerapp.domain.model.AppSettings
import com.cardscannerapp.domain.model.DEFAULT_APP_SETTINGS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(val settings: AppSettings = DEFAULT_APP_SETTINGS, val isLoading: Boolean = true)

class SettingsViewModel(private val settingsRepository: SettingsRepository, private val contactRepository: ContactRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    init { viewModelScope.launch { settingsRepository.appSettings.collect { settings -> _uiState.value = _uiState.value.copy(settings = settings, isLoading = false) } } }
    fun updateSettings(settings: AppSettings) { viewModelScope.launch { settingsRepository.updateSettings(settings) } }
    fun resetAllData() { viewModelScope.launch { settingsRepository.resetSettings(); contactRepository.deleteAllContacts() } }
}
