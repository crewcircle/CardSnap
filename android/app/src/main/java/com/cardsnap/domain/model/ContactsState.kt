package com.cardsnap.domain.model

sealed interface ContactsState {
    data object Loading : ContactsState

    data class Success(val contacts: List<ContactCard>) : ContactsState

    data class Error(val message: String) : ContactsState

    data object Empty : ContactsState
}
