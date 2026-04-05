package com.cardscannerapp.ui.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardscannerapp.data.repository.ContactRepository
import com.cardscannerapp.domain.model.ContactCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContactsUiState(val contacts: List<ContactCard> = emptyList(), val isLoading: Boolean = true)

class ContactsViewModel(private val contactRepository: ContactRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()
    init { viewModelScope.launch { contactRepository.getAllContacts().collect { contacts -> _uiState.value = _uiState.value.copy(contacts = contacts, isLoading = false) } } }
    fun deleteContact(contact: ContactCard) { viewModelScope.launch { contactRepository.deleteContact(contact) } }
}
