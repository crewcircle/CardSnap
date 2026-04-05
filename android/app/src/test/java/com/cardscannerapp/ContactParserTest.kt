package com.cardscannerapp
import com.cardscannerapp.domain.parser.ContactParser
import org.junit.Assert.*
import org.junit.Test

class ContactParserTest {
    @Test fun parse_extractsEmail() {
        val result = ContactParser.parse("John Doe\njohn@example.com\n555-1234")
        assertEquals("john@example.com", result.email)
    }
    @Test fun parse_extractsPhone() {
        val result = ContactParser.parse("John Doe\n555-1234\njohn@example.com")
        assertEquals("555-1234", result.phone)
    }
    @Test fun parse_extractsCompany() {
        val result = ContactParser.parse("John Doe\nAcme Inc\n555-1234")
        assertEquals("Acme Inc", result.company)
    }
    @Test fun parse_extractsName() {
        val result = ContactParser.parse("John Doe\njohn@example.com")
        assertEquals("John Doe", result.name)
    }
    @Test fun parse_handlesEmptyInput() {
        val result = ContactParser.parse("")
        assertEquals("", result.name); assertEquals("", result.email); assertEquals("", result.phone)
    }
    @Test fun parse_handlesComplexCard() {
        val input = "JOHN SMITH\nSenior Engineer\nTechCorp LLC\njohn.smith@techcorp.com\n+1 555-987-6543\nwww.techcorp.com"
        val result = ContactParser.parse(input)
        assertEquals("john.smith@techcorp.com", result.email)
        assertEquals("TechCorp LLC", result.company)
        assertTrue(result.name.contains("JOHN") || result.name.contains("SMITH"))
    }
}
