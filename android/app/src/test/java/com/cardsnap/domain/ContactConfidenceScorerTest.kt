package com.cardsnap.domain

import com.cardsnap.domain.model.ContactCard
import org.junit.Assert.*
import org.junit.Test

class ContactConfidenceScorerTest {

    // ── HIGH confidence ──

    @Test fun namePlusEmail_returnsHigh() {
        val card = ContactCard(name = "Jane Smith", email = "jane@example.com")
        assertEquals(Confidence.HIGH, ContactConfidenceScorer.score(card, "Jane Smith\njane@example.com"))
    }

    @Test fun namePlusPhone_returnsHigh() {
        val card = ContactCard(name = "Bob Jones", phone = "555-123-4567")
        assertEquals(Confidence.HIGH, ContactConfidenceScorer.score(card, "Bob Jones\n555-123-4567"))
    }

    @Test fun namePlusCompanyAndTitle_returnsHigh() {
        val card = ContactCard(name = "Alice Wang", company = "TechCorp", title = "CEO")
        assertEquals(Confidence.HIGH, ContactConfidenceScorer.score(card, "Alice Wang\nTechCorp\nCEO"))
    }

    @Test fun namePlusCompany_returnsHigh() {
        val card = ContactCard(name = "John Doe", company = "Acme Inc")
        assertEquals(Confidence.HIGH, ContactConfidenceScorer.score(card, "John Doe\nAcme Inc"))
    }

    // ── MEDIUM confidence ──

    @Test fun onlyEmail_returnsMedium() {
        val card = ContactCard(email = "test@example.com")
        assertEquals(Confidence.MEDIUM, ContactConfidenceScorer.score(card, ""))
    }

    @Test fun onlyPhone_returnsMedium() {
        val card = ContactCard(phone = "555-9876")
        assertEquals(Confidence.MEDIUM, ContactConfidenceScorer.score(card, ""))
    }

    @Test fun garbageNameWithValidPhoneAndEmail_returnsMedium() {
        val card = ContactCard(name = "ZZZ!!123", email = "real@email.com", phone = "555-0000")
        assertEquals(Confidence.MEDIUM, ContactConfidenceScorer.score(card, "ZZZ!!123\n555-0000\nreal@email.com"))
    }

    @Test fun onlyCompany_returnsMedium() {
        val card = ContactCard(company = "Startup Inc")
        assertEquals(Confidence.MEDIUM, ContactConfidenceScorer.score(card, ""))
    }

    @Test fun onlyTitle_returnsMedium() {
        val card = ContactCard(title = "Engineer")
        assertEquals(Confidence.MEDIUM, ContactConfidenceScorer.score(card, ""))
    }

    // ── LOW confidence ──

    @Test fun completelyEmpty_returnsLow() {
        assertEquals(Confidence.LOW, ContactConfidenceScorer.score(ContactCard.empty(), ""))
    }

    @Test fun ocrTextIsSingleWordOfSymbols_returnsLow() {
        val card = ContactCard()
        assertEquals(Confidence.LOW, ContactConfidenceScorer.score(card, "@@@@!!!!"))
    }

    @Test fun ocrTextIsEmpty_returnsLow() {
        val card = ContactCard()
        assertEquals(Confidence.LOW, ContactConfidenceScorer.score(card, ""))
    }

    @Test fun nameIsOcrGarbage_returnsLow() {
        val card = ContactCard(name = "ABC123!!@#")
        assertEquals(Confidence.LOW, ContactConfidenceScorer.score(card, ""))
    }

    @Test fun nameIsJustSymbols_returnsLow() {
        val card = ContactCard(name = "!!!###")
        assertEquals(Confidence.LOW, ContactConfidenceScorer.score(card, "!!!###"))
    }

    // ── Edge cases ──

    @Test fun veryLongCompanyName_doesNotAffectScoringNegatively() {
        val card = ContactCard(
            name = "Sarah Connor",
            company = "A".repeat(200)
        )
        assertEquals(Confidence.HIGH, ContactConfidenceScorer.score(card, "Sarah Connor"))
    }

    @Test fun unicodeNameWithContactInfo_returnsHigh() {
        val card = ContactCard(
            name = "José García",
            email = "jose@example.com"
        )
        assertEquals(Confidence.HIGH, ContactConfidenceScorer.score(card, "José García\njose@example.com"))
    }

    @Test fun nameWithAccentsAndPhone_returnsHigh() {
        val card = ContactCard(
            name = "François Müller",
            phone = "+33 6 12 34 56 78"
        )
        assertEquals(Confidence.HIGH, ContactConfidenceScorer.score(card, "François Müller\n+33 6 12 34 56 78"))
    }

    @Test fun singleNameWithPhone_returnsHigh() {
        val card = ContactCard(name = "Madonna", phone = "555-1111")
        assertEquals(Confidence.HIGH, ContactConfidenceScorer.score(card, "Madonna\n555-1111"))
    }

    @Test fun nameIsSingleCharacter_returnsMedium() {
        val card = ContactCard(name = "X", email = "x@test.com")
        assertEquals(Confidence.MEDIUM, ContactConfidenceScorer.score(card, ""))
    }

    @Test fun allFieldsPresent_returnsHigh() {
        val card = ContactCard(
            name = "Michael Scott", firstName = "Michael", lastName = "Scott",
            company = "Dunder Mifflin", title = "Regional Manager",
            email = "michael@dundermifflin.com", phone = "555-9999",
            address = "1725 Slough Ave", website = "dundermifflin.com"
        )
        assertEquals(Confidence.HIGH, ContactConfidenceScorer.score(card, "Michael Scott\nDunder Mifflin\nRegional Manager\nmichael@dundermifflin.com\n555-9999"))
    }

    // ── isValid helper ──

    @Test fun isValid_returnsTrueForHigh() {
        val card = ContactCard(name = "Valid Name", email = "v@example.com")
        assertTrue(ContactConfidenceScorer.isValid(card, "Valid Name\nv@example.com"))
    }

    @Test fun isValid_returnsFalseForMedium() {
        val card = ContactCard(email = "only@email.com")
        assertFalse(ContactConfidenceScorer.isValid(card, ""))
    }

    @Test fun isValid_returnsFalseForLow() {
        assertFalse(ContactConfidenceScorer.isValid(ContactCard.empty(), ""))
    }
}
