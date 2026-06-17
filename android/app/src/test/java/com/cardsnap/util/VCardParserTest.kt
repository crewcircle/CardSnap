package com.cardsnap.util
import com.cardsnap.domain.model.ContactCard
import org.junit.Assert.*
import org.junit.Test

class VCardParserTest {
    // --- parse() tests ---

    @Test fun parse_validVcf_returnsContactCardWithAllFields() {
        val vcf = "BEGIN:VCARD\nVERSION:3.0\nFN:John Doe\nN:Doe;John;;;\nEMAIL:john@example.com\nTEL:555-1234\nORG:Acme Inc\nTITLE:Engineer\nADR:;;123 Main St;;;\nURL:https://example.com\nEND:VCARD"
        val result = VCardParser.parse(vcf)
        assertNotNull(result)
        assertEquals("John Doe", result!!.name)
        assertEquals("John", result.firstName)
        assertEquals("Doe", result.lastName)
        assertEquals("john@example.com", result.email)
        assertEquals("555-1234", result.phone)
        assertEquals("Acme Inc", result.company)
        assertEquals("Engineer", result.title)
        assertEquals("123 Main St", result.address)
        assertEquals("https://example.com", result.website)
        assertNotNull(result.id)
        assertNotNull(result.scannedAt)
        assertNotNull(result.updatedAt)
    }
    @Test fun parse_validVcfWithOnlyName_returnsContactCard() {
        val vcf = "BEGIN:VCARD\nVERSION:3.0\nFN:Jane Smith\nEND:VCARD"
        val result = VCardParser.parse(vcf)
        assertNotNull(result)
        assertEquals("Jane Smith", result!!.name)
        assertEquals("", result.firstName)
        assertEquals("", result.lastName)
        assertEquals("", result.email)
        assertEquals("", result.phone)
        assertEquals("", result.company)
        assertEquals("", result.title)
        assertEquals("", result.address)
        assertEquals("", result.website)
    }
    @Test fun parse_emptyString_returnsNull() {
        assertNull(VCardParser.parse(""))
    }
    @Test fun parse_invalidGarbage_returnsNullWithoutThrowing() {
        assertNull(VCardParser.parse("sdfjsdklfj sdf sdklfj sdklfj"))
    }
    @Test fun parse_vcfWithoutNameField_returnsNull() {
        val vcf = "BEGIN:VCARD\nVERSION:3.0\nEMAIL:john@example.com\nEND:VCARD"
        assertNull(VCardParser.parse(vcf))
    }
    @Test fun parse_vcfWithBlankName_returnsNull() {
        val vcf = "BEGIN:VCARD\nVERSION:3.0\nFN:\nEND:VCARD"
        assertNull(VCardParser.parse(vcf))
    }

    // --- parseMultiple() tests ---

    @Test fun parseMultiple_twoValidVCards_returnsBothContacts() {
        val vcf = "BEGIN:VCARD\nVERSION:3.0\nFN:Alice\nEND:VCARD\nBEGIN:VCARD\nVERSION:3.0\nFN:Bob\nEND:VCARD"
        val results = VCardParser.parseMultiple(vcf)
        assertEquals(2, results.size)
        assertEquals("Alice", results[0].name)
        assertEquals("Bob", results[1].name)
    }
    @Test fun parseMultiple_emptyString_returnsEmptyList() {
        assertTrue(VCardParser.parseMultiple("").isEmpty())
    }
    @Test fun parseMultiple_invalidGarbage_returnsEmptyListWithoutThrowing() {
        assertTrue(VCardParser.parseMultiple("sdfsdf sdf sdf sdf sdf").isEmpty())
    }
    @Test fun parseMultiple_oneMissingName_returnsOnlyValidContacts() {
        val vcf = "BEGIN:VCARD\nVERSION:3.0\nFN:Alice\nEND:VCARD\nBEGIN:VCARD\nVERSION:3.0\nEMAIL:no-name@example.com\nEND:VCARD"
        val results = VCardParser.parseMultiple(vcf)
        assertEquals(1, results.size)
        assertEquals("Alice", results[0].name)
    }
    @Test fun parseMultiple_allMissingName_returnsEmptyList() {
        val vcf = "BEGIN:VCARD\nVERSION:3.0\nEMAIL:a@x.com\nEND:VCARD\nBEGIN:VCARD\nVERSION:3.0\nEMAIL:b@x.com\nEND:VCARD"
        assertTrue(VCardParser.parseMultiple(vcf).isEmpty())
    }
}
