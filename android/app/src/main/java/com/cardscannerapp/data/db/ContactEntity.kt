package com.cardscannerapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cardscannerapp.domain.model.ContactCard
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String, val firstName: String, val lastName: String,
    val company: String, val title: String, val email: String,
    val phone: String, val address: String, val website: String,
    val imageUri: String?, val scannedAt: String, val updatedAt: String?
)

fun ContactEntity.toDomain() = ContactCard(
    id = id, name = name, firstName = firstName, lastName = lastName,
    company = company, title = title, email = email, phone = phone,
    address = address, website = website, imageUri = imageUri,
    scannedAt = scannedAt, updatedAt = updatedAt
)

fun ContactCard.toEntity() = ContactEntity(
    id = id.ifBlank { UUID.randomUUID().toString() },
    name = name, firstName = firstName, lastName = lastName,
    company = company, title = title, email = email, phone = phone,
    address = address, website = website, imageUri = imageUri,
    scannedAt = scannedAt.ifBlank { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date()) },
    updatedAt = updatedAt
)
