package com.cardscannerapp.domain.model

data class ContactCard(
    val id: String = "",
    val name: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val company: String = "",
    val title: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val website: String = "",
    val imageUri: String? = null,
    val scannedAt: String = "",
    val updatedAt: String? = null,
    val rawOcrText: String = ""
) {
    fun hasDetails(): Boolean = name.isNotBlank() || email.isNotBlank() || phone.isNotBlank() || company.isNotBlank()
    companion object { fun empty() = ContactCard() }
}
