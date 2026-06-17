package com.cardsnap.ui.screens.scan

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardsnap.data.repository.ContactRepository
import com.cardsnap.data.repository.SettingsRepository
import com.cardsnap.domain.model.ContactCard
import com.cardsnap.domain.model.ScanError
import com.cardsnap.domain.ocr.ImageCropper
import com.cardsnap.domain.ocr.OcrEngine
import com.cardsnap.domain.ContactConfidenceScorer
import com.cardsnap.domain.Confidence
import com.cardsnap.domain.parser.ContactParser
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
    val error: ScanError? = null, val showSuccess: Boolean = false,
    val confidence: Confidence? = null
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
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)
            try {
                val bitmap = ImageCropper.decodeBitmapWithRotation(imageUri)
                if (bitmap == null) {
                    _uiState.value = _uiState.value.copy(isProcessing = false, error = ScanError.ImageProcessingFailed("Could not decode image"))
                    return@launch
                }
                val croppedBitmap = ImageCropper.cropToCardGuide(bitmap)
                val ocrText = try {
                    ocrEngine.recognizeText(croppedBitmap)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(isProcessing = false, error = ScanError.OcrFailed(e.message ?: "OCR failed"))
                    return@launch
                }
                val contact = try {
                    ContactParser.parse(ocrText, imageUri)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(isProcessing = false, error = ScanError.ParserFailed(e.message ?: "Parse failed"))
                    return@launch
                }
                val confidence = ContactConfidenceScorer.score(contact, ocrText)
                _uiState.value = _uiState.value.copy(isProcessing = false, capturedImage = imageUri,
                    extractedText = ocrText, contact = contact, showResults = true, confidence = confidence)
                val settings = settingsRepository.appSettings.first()
                if (settings.autoSave && contact.hasDetails()) saveContact(contact, context)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isProcessing = false, error = ScanError.Unknown)
            }
        }
    }

    fun saveContact(contact: ContactCard, context: Context) {
        viewModelScope.launch {
            try {
                contactRepository.insertContact(contact)
                _uiState.value = _uiState.value.copy(isContactSaved = true, showSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = ScanError.SaveFailed)
            }
        }
    }

    fun resetState() { _uiState.value = ScanUiState() }
    fun toggleTorch() { _uiState.value = _uiState.value.copy(torchOn = !_uiState.value.torchOn) }
    fun setOffline(offline: Boolean) { _uiState.value = _uiState.value.copy(isOffline = offline) }
    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
    fun dismissSuccess() { _uiState.value = _uiState.value.copy(showSuccess = false) }
    override fun onCleared() { super.onCleared(); ocrEngine.cleanup() }
}
