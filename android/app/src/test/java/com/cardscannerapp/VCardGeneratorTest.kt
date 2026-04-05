package com.cardscannerapp
import com.cardscannerapp.domain.model.ContactCard
import com.cardscannerapp.util.VCardGenerator
import org.junit.Assert.*
import org.junit.Test

class VCardGeneratorTest {
    @Test fun generateVCard_includesName() {
        val contact = ContactCard(name = "John Doe", firstName = "John", lastName = "Doe")
        val (vcard, mimeType) = VCardGenerator.generateVCard(contact)
        assertTrue(vcard.contains("John Doe")); assertEquals("text/x-vcard", mimeType)
    }
    @Test fun generateVCard_includesEmail() {
        val contact = ContactCard(name = "John Doe", email = "john@example.com")
        val (vcard, _) = VCardGenerator.generateVCard(contact)
        assertTrue(vcard.contains("john@example.com"))
    }
    @Test fun generateVCard_includesPhone() {
        val contact = ContactCard(name = "John Doe", phone = "555-1234")
        val (vcard, _) = VCardGenerator.generateVCard(contact)
        assertTrue(vcard.contains("555-1234"))
    }
    @Test fun generateVCard_includesCompany() {
        val contact = ContactCard(name = "John Doe", company = "Acme Inc")
        val (vcard, _) = VCardGenerator.generateVCard(contact)
        assertTrue(vcard.contains("Acme Inc"))
    }
}
