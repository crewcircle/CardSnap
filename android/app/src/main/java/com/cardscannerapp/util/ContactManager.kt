package com.cardscannerapp.util

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import com.cardscannerapp.domain.model.ContactCard

object ContactManager {
    fun openContactForm(context: Context, contact: ContactCard) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            type = ContactsContract.Contacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.NAME, contact.name)
            putExtra(ContactsContract.Intents.Insert.COMPANY, contact.company)
            putExtra(ContactsContract.Intents.Insert.JOB_TITLE, contact.title)
            if (contact.email.isNotBlank()) putExtra(ContactsContract.Intents.Insert.EMAIL, contact.email)
            if (contact.phone.isNotBlank()) putExtra(ContactsContract.Intents.Insert.PHONE, contact.phone)
            if (contact.address.isNotBlank()) putExtra(ContactsContract.Intents.Insert.POSTAL, contact.address)
        }
        context.startActivity(intent)
    }
}
