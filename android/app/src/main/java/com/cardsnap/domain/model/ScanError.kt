package com.cardsnap.domain.model

sealed class ScanError(
    open val message: String,
    open val userFacingMessage: String
) {
    data object CameraPermissionDenied : ScanError(
        message = "Camera permission was denied by the user",
        userFacingMessage = "Camera permission is required to scan business cards. Please grant it in Settings."
    )

    data class OcrFailed(val cause: String) : ScanError(
        message = "OCR processing failed: $cause",
        userFacingMessage = "Could not read text from the image. Please try again with better lighting."
    )

    data class ParserFailed(val cause: String) : ScanError(
        message = "Contact parsing failed: $cause",
        userFacingMessage = "Could not extract contact details from the scanned text."
    )

    data object NoCardDetected : ScanError(
        message = "No business card was detected in the image",
        userFacingMessage = "No business card detected. Make sure the card is centered in the frame."
    )

    data class ImageProcessingFailed(val cause: String) : ScanError(
        message = "Image processing failed: $cause",
        userFacingMessage = "Failed to process the image. Please try again."
    )

    data object SaveFailed : ScanError(
        message = "Failed to save the scanned contact",
        userFacingMessage = "Could not save the contact. Please check your storage and try again."
    )

    data object Unknown : ScanError(
        message = "An unknown error occurred",
        userFacingMessage = "Something went wrong. Please try again."
    )
}
