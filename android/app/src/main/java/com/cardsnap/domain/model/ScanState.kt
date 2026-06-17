package com.cardsnap.domain.model

sealed interface ScanState {
    data object Idle : ScanState

    data object Processing : ScanState

    data class Results(
        val contact: ContactCard,
        val extractedText: String,
        val imageUri: String
    ) : ScanState

    data class Saved(val contact: ContactCard) : ScanState

    data class Error(val error: ScanError) : ScanState

    data object Offline : ScanState
}
