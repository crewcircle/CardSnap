package com.cardsnap.domain

import com.cardsnap.domain.model.ContactCard
import org.junit.Assert.*
import org.junit.Test

class ContactDeduplicatorTest {

    // ── normalizedSimilarity ──

    @Test fun identicalStrings_returnsOne() {
        assertEquals(1.0, ContactDeduplicator.normalizedSimilarity("Hello", "Hello"), 0.001)
    }

    @Test fun completelyDifferent_returnsZero() {
        assertEquals(0.0, ContactDeduplicator.normalizedSimilarity("abc", "xyz"), 0.001)
    }

    @Test fun caseInsensitive_returnsOne() {
        assertEquals(1.0, ContactDeduplicator.normalizedSimilarity("Hello", "hello"), 0.001)
    }

    @Test fun partiallySimilar_returnsBetweenZeroAndOne() {
        val sim = ContactDeduplicator.normalizedSimilarity("John", "Jon")
        assertTrue(sim > 0.0 && sim < 1.0)
    }

    // ── findExactDuplicates ──

    @Test fun sameEmail_returnsPair() {
        val a = ContactCard(id = "1", name = "Alice", email = "alice@test.com")
        val b = ContactCard(id = "2", name = "Bob", email = "alice@test.com")
        val result = ContactDeduplicator.findExactDuplicates(listOf(a, b))
        assertEquals(1, result.size)
    }

    @Test fun samePhone_returnsPair() {
        val a = ContactCard(id = "1", name = "Alice", phone = "555-1234")
        val b = ContactCard(id = "2", name = "Bob", phone = "555-1234")
        val result = ContactDeduplicator.findExactDuplicates(listOf(a, b))
        assertEquals(1, result.size)
    }

    @Test fun threeWithSameEmail_returnsFirstPairOnly() {
        val a = ContactCard(id = "1", name = "Alice", email = "same@test.com")
        val b = ContactCard(id = "2", name = "Bob", email = "same@test.com")
        val c = ContactCard(id = "3", name = "Carol", email = "same@test.com")
        val result = ContactDeduplicator.findExactDuplicates(listOf(a, b, c))
        assertEquals(1, result.size)
    }

    @Test fun matchingEmailButBlankName_skipBlanks() {
        val a = ContactCard(id = "1", name = "Alice", email = "same@test.com")
        val b = ContactCard(id = "2")
        val c = ContactCard(id = "3", email = "same@test.com")
        val result = ContactDeduplicator.findExactDuplicates(listOf(a, b, c))
        assertEquals(1, result.size)
        assertEquals("1", result[0].first.id)
        assertEquals("3", result[0].second.id)
    }

    @Test fun noMatch_returnsEmpty() {
        val a = ContactCard(id = "1", name = "Alice", email = "a@test.com")
        val b = ContactCard(id = "2", name = "Bob", email = "b@test.com")
        assertTrue(ContactDeduplicator.findExactDuplicates(listOf(a, b)).isEmpty())
    }

    @Test fun phoneFormattingDifferences_stillMatches() {
        val a = ContactCard(id = "1", name = "Alice", phone = "555-1234")
        val b = ContactCard(id = "2", name = "Bob", phone = "5551234")
        val result = ContactDeduplicator.findExactDuplicates(listOf(a, b))
        assertEquals(1, result.size)
    }

    @Test fun allFieldsBlank_filteredOut() {
        val a = ContactCard(id = "1", name = "Alice", email = "a@test.com")
        val b = ContactCard(id = "2")
        val c = ContactCard(id = "3")
        assertTrue(ContactDeduplicator.findExactDuplicates(listOf(a, b, c)).isEmpty())
    }

    // ── findFuzzyDuplicates ──

    @Test fun verySimilarNames_returnsPair() {
        val a = ContactCard(id = "1", name = "John Doe", email = "john@test.com")
        val b = ContactCard(id = "2", name = "John Doe", email = "johndoe@test.com")
        val result = ContactDeduplicator.findFuzzyDuplicates(listOf(a, b))
        assertEquals(1, result.size)
    }

    @Test fun sameCompanyAndModeratelySimilarName_returnsPair() {
        val a = ContactCard(id = "1", name = "John", company = "Acme", email = "john@acme.com")
        val b = ContactCard(id = "2", name = "Johnny", company = "Acme", email = "johnny@acme.com")
        val result = ContactDeduplicator.findFuzzyDuplicates(listOf(a, b))
        assertEquals(1, result.size)
    }

    @Test fun prefiltersExactMatchedPairs() {
        val a = ContactCard(id = "1", email = "same@test.com", name = "John Smith")
        val b = ContactCard(id = "2", email = "same@test.com", name = "John Smith")
        val c = ContactCard(id = "3", email = "c@test.com", name = "Jon Smythe", company = "Acme")
        val d = ContactCard(id = "4", email = "d@test.com", name = "John Smith", company = "Acme")
        val result = ContactDeduplicator.findFuzzyDuplicates(listOf(a, b, c, d))
        assertEquals(1, result.size)
        assertEquals("3", result[0].first.id)
        assertEquals("4", result[0].second.id)
    }

    @Test fun noFuzzyMatch_returnsEmpty() {
        val a = ContactCard(id = "1", name = "Alice", company = "Acme")
        val b = ContactCard(id = "2", name = "Bob", company = "Acme")
        assertTrue(ContactDeduplicator.findFuzzyDuplicates(listOf(a, b)).isEmpty())
    }

    @Test fun differentNamesAndCompanies_noMatch() {
        val a = ContactCard(id = "1", name = "Alice", company = "Acme")
        val b = ContactCard(id = "2", name = "Bob", company = "Beta")
        assertTrue(ContactDeduplicator.findFuzzyDuplicates(listOf(a, b)).isEmpty())
    }
}
