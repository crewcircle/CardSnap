package com.cardscannerapp.ui.screens.scan

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardscannerapp.data.repository.ContactRepository
import com.cardscannerapp.data.repository.SettingsRepository
import com.cardscannerapp.domain.model.ContactCard
import com.cardscannerapp.domain.ocr.ImageCropper
import com.cardscannerapp.domain.ocr.OcrEngine
import com.cardscannerapp.domain.parser.ContactParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ScanUiState(
    val isProcessing: Boolean = false, val capturedImage: String? = null,
    val extractedText: String = "", val contact: ContactCard = ContactCard.empty(),
    val showResults: Boolean = false, val isContactSaved: Boolean = false,
    val torchOn: Boolean = false, val isOffline: Boolean = false,
    val errorMessage: String? = null, val showSuccess: Boolean = false
)

class ScanViewModel(
    private val contactRepository: ContactRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()
    private val ocrEngine = OcrEngine()

    fun processImage(imageUri: String, context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)
            try {
                val bitmap = ImageCropper.decodeBitmapWithRotation(imageUri)
                if (bitmap == null) {
                    _uiState.value = _uiState.value.copy(isProcessing = false, errorMessage = "Failed to load image")
                    return@launch
                }
                val croppedBitmap = ImageCropper.cropToCardGuide(bitmap)
                val ocrText = ocrEngine.recognizeText(croppedBitmap)
                val contact = ContactParser.parse(ocrText, imageUri)
                _uiState.value = _uiState.value.copy(isProcessing = false, capturedImage = imageUri,
                    extractedText = ocrText, contact = contact, showResults = true)
                val settings = settingsRepository.appSettings.first()
                if (settings.autoSave && contact.hasDetails()) saveContact(contact, context)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isProcessing = false, errorMessage = "Failed to process image: ${e.message}")
            }
        }
    }

    fun saveContact(contact: ContactCard, context: Context) {
        viewModelScope.launch {
            try {
                contactRepository.insertContact(contact)
                _uiState.value = _uiState.value.copy(isContactSaved = true, showSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to save contact: ${e.message}")
            }
        }
    }

    fun resetState() { _uiState.value = ScanUiState() }
    fun toggleTorch() { _uiState.value = _uiState.value.copy(torchOn = !_uiState.value.torchOn) }
    fun setOffline(offline: Boolean) { _uiState.value = _uiState.value.copy(isOffline = offline) }
    fun clearError() { _uiState.value = _uiState.value.copy(errorMessage = null) }
    fun dismissSuccess() { _uiState.value = _uiState.value.copy(showSuccess = false) }
    override fun onCleared() { super.onCleared(); ocrEngine.cleanup() }
}
