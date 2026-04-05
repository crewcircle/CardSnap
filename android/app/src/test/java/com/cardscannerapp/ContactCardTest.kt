package com.cardscannerapp
import com.cardscannerapp.domain.model.ContactCard
import org.junit.Assert.*
import org.junit.Test

class ContactCardTest {
    @Test fun emptyCard_hasNoDetails() { assertFalse(ContactCard.empty().hasDetails()) }
    @Test fun cardWithName_hasDetails() { assertTrue(ContactCard(name = "John Doe").hasDetails()) }
    @Test fun cardWithEmail_hasDetails() { assertTrue(ContactCard(email = "john@example.com").hasDetails()) }
    @Test fun cardWithPhone_hasDetails() { assertTrue(ContactCard(phone = "555-1234").hasDetails()) }
    @Test fun cardWithCompany_hasDetails() { assertTrue(ContactCard(company = "Acme Inc").hasDetails()) }
}
