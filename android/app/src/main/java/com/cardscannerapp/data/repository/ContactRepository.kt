package com.cardscannerapp.data.repository

import com.cardscannerapp.data.db.ContactDao
import com.cardscannerapp.data.db.toDomain
import com.cardscannerapp.data.db.toEntity
import com.cardscannerapp.domain.model.ContactCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContactRepository(private val dao: ContactDao) {
    fun getAllContacts(): Flow<List<ContactCard>> = dao.getAllContacts().map { it.map { e -> e.toDomain() } }
    suspend fun getContactById(id: String): ContactCard? = dao.getContactById(id)?.toDomain()
    suspend fun insertContact(contact: ContactCard) = dao.insertContact(contact.toEntity())
    suspend fun updateContact(contact: ContactCard) = dao.updateContact(contact.toEntity())
    suspend fun deleteContact(contact: ContactCard) = dao.deleteContact(contact.toEntity())
    suspend fun deleteAllContacts() = dao.deleteAllContacts()
}
