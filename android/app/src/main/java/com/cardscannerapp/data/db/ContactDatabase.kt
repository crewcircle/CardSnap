package com.cardscannerapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ContactEntity::class], version = 1, exportSchema = false)
abstract class ContactDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    companion object {
        @Volatile private var INSTANCE: ContactDatabase? = null
        fun getInstance(context: Context): ContactDatabase =
            INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, ContactDatabase::class.java, "card_scanner_database"
                ).build()
                INSTANCE = instance; instance
            }
    }
}
