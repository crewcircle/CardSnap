package com.cardscannerapp.ui.screens.editcontact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardscannerapp.data.repository.ContactRepository
import com.cardscannerapp.domain.model.ContactCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditContactUiState(val contact: ContactCard? = null, val isLoading: Boolean = true, val errorMessage: String? = null)

class EditContactViewModel(private val contactRepository: ContactRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(EditContactUiState())
    val uiState: StateFlow<EditContactUiState> = _uiState.asStateFlow()
    fun loadContact(contactId: String) {
        viewModelScope.launch {
            try {
                val contact = contactRepository.getContactById(contactId)
                _uiState.value = _uiState.value.copy(contact = contact, isLoading = false, errorMessage = if (contact == null) "Contact not found" else null)
            } catch (e: Exception) { _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Failed to load contact: ${e.message}") }
        }
    }
    fun saveContact(contact: ContactCard) { viewModelScope.launch { contactRepository.updateContact(contact) } }
    fun deleteContact(contact: ContactCard) { viewModelScope.launch { contactRepository.deleteContact(contact) } }
}
