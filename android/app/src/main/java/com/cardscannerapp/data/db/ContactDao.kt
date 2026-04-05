package com.cardscannerapp.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY scannedAt DESC")
    fun getAllContacts(): Flow<List<ContactEntity>>
    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getContactById(id: String): ContactEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)
    @Update suspend fun updateContact(contact: ContactEntity)
    @Delete suspend fun deleteContact(contact: ContactEntity)
    @Query("DELETE FROM contacts") suspend fun deleteAllContacts()
}
