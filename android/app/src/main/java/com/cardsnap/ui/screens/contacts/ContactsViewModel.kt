package com.cardsnap.ui.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardsnap.data.repository.ContactRepository
import com.cardsnap.domain.ContactDeduplicator
import com.cardsnap.domain.model.ContactCard
import com.cardsnap.domain.model.ContactsState
import com.cardsnap.util.VCardParser
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ContactsViewModel(private val contactRepository: ContactRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<ContactsState>(ContactsState.Loading)
    val uiState: StateFlow<ContactsState> = _uiState.asStateFlow()

    private val _importEvent = Channel<ImportResult>(Channel.BUFFERED)
    val importEvent = _importEvent.receiveAsFlow()

    private val _duplicatePairs = MutableStateFlow<List<Pair<ContactCard, ContactCard>>>(emptyList())
    val duplicatePairs: StateFlow<List<Pair<ContactCard, ContactCard>>> = _duplicatePairs.asStateFlow()

    private val _mergeEvent = MutableSharedFlow<String>()
    val mergeEvent: SharedFlow<String> = _mergeEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            try {
                contactRepository.getAllContacts().collect { contacts ->
                    _uiState.value = if (contacts.isNotEmpty()) ContactsState.Success(contacts) else ContactsState.Empty
                    if (contacts.isNotEmpty()) {
                        _duplicatePairs.value = ContactDeduplicator.findExactDuplicates(contacts) +
                            ContactDeduplicator.findFuzzyDuplicates(contacts)
                    } else {
                        _duplicatePairs.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ContactsState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteContact(contact: ContactCard) { viewModelScope.launch { contactRepository.deleteContact(contact) } }

    fun mergeContacts(keep: ContactCard, remove: ContactCard) {
        viewModelScope.launch {
            val merged = keep.copy(
                name = keep.name.ifBlank { remove.name },
                firstName = keep.firstName.ifBlank { remove.firstName },
                lastName = keep.lastName.ifBlank { remove.lastName },
                company = keep.company.ifBlank { remove.company },
                title = keep.title.ifBlank { remove.title },
                email = keep.email.ifBlank { remove.email },
                phone = keep.phone.ifBlank { remove.phone },
                address = keep.address.ifBlank { remove.address },
                website = keep.website.ifBlank { remove.website },
                imageUri = keep.imageUri ?: remove.imageUri
            )
            contactRepository.updateContact(merged)
            contactRepository.deleteContact(remove)
            _mergeEvent.emit("Contacts merged")
        }
    }

    fun dismissDuplicatePair(keep: ContactCard, remove: ContactCard) {
        _duplicatePairs.value = _duplicatePairs.value.filterNot { pair ->
            (pair.first.id == keep.id && pair.second.id == remove.id) ||
                (pair.first.id == remove.id && pair.second.id == keep.id)
        }
    }

    fun dismissDuplicates() {
        _duplicatePairs.value = emptyList()
    }

    fun importVCard(vcfContent: String) {
        viewModelScope.launch {
            try {
                val contacts = VCardParser.parseMultiple(vcfContent)
                if (contacts.isEmpty()) {
                    _importEvent.send(ImportResult.Error("No valid contacts found in file"))
                    return@launch
                }
                contacts.forEach { contactRepository.insertContact(it) }
                _importEvent.send(ImportResult.Success(contacts.size))
            } catch (e: Exception) {
                _importEvent.send(ImportResult.Error(e.message ?: "Import failed"))
            }
        }
    }

    sealed interface ImportResult {
        data class Success(val count: Int) : ImportResult
        data class Error(val message: String) : ImportResult
    }
}
