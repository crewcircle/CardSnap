package com.cardsnap
import com.cardsnap.domain.parser.ContactParser
import org.junit.Assert.*
import org.junit.Test

class ContactParserTest {
    // --- Existing tests (NA-centric, must still pass) ---

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

    // --- International phone tests ---

    @Test fun parse_ukPhone() {
        val result = ContactParser.parse("Alice Green\n+44 20 7123 4567\nalice@example.co.uk")
        assertEquals("+44 20 7123 4567", result.phone)
    }
    @Test fun parse_germanPhone() {
        val result = ContactParser.parse("Hans Mueller\n+49 30 123456\nhans@example.de")
        assertEquals("+49 30 123456", result.phone)
    }
    @Test fun parse_japanesePhone() {
        val result = ContactParser.parse("Taro Yamada\n+81 3 1234 5678\ntaro@example.jp")
        assertEquals("+81 3 1234 5678", result.phone)
    }
    @Test fun parse_indianPhone() {
        val result = ContactParser.parse("Priya Sharma\n+91 98765 43210\npriya@example.in")
        assertEquals("+91 98765 43210", result.phone)
    }
    @Test fun parse_australianPhone() {
        val result = ContactParser.parse("Bruce Smith\n+61 2 9876 5432\nbruce@example.au")
        assertEquals("+61 2 9876 5432", result.phone)
    }
    @Test fun parse_frenchPhone() {
        val result = ContactParser.parse("Jean Dupont\n+33 1 23 45 67 89\njean@example.fr")
        assertEquals("+33 1 23 45 67 89", result.phone)
    }
    @Test fun parse_italianPhone() {
        val result = ContactParser.parse("Marco Rossi\n+39 06 1234 5678\nmarco@example.it")
        assertEquals("+39 06 1234 5678", result.phone)
    }
    @Test fun parse_naPhoneWithParentheses() {
        val result = ContactParser.parse("Jane Doe\n(415) 555-1234\njane@example.com")
        assertEquals("(415) 555-1234", result.phone)
    }
    @Test fun parse_naPhoneWithDots() {
        val result = ContactParser.parse("Bob Wilson\n415.555.1234\nbob@example.com")
        assertEquals("415.555.1234", result.phone)
    }

    // --- Address extraction tests ---

    @Test fun parse_usAddress() {
        val result = ContactParser.parse("John Smith\n123 Main Street\nSan Francisco, CA 94105\njohn@example.com")
        assertEquals("123 Main Street, San Francisco, CA 94105", result.address)
    }
    @Test fun parse_usAddressWithAve() {
        val result = ContactParser.parse("Sarah Connor\n456 Oak Avenue\nLos Angeles, CA 90001\nsarah@example.com")
        assertEquals("456 Oak Avenue, Los Angeles, CA 90001", result.address)
    }
    @Test fun parse_ukAddress() {
        val result = ContactParser.parse("James Bond\n10 Downing Street\nLondon\nSW1A 2AA\njames@gov.uk")
        assertEquals("10 Downing Street, London, SW1A 2AA", result.address)
    }
    @Test fun parse_addressWithRoad() {
        val result = ContactParser.parse("Peter Parker\n789 Elm Road\nSpringfield, IL 62701\npeter@example.com")
        assertEquals("789 Elm Road, Springfield, IL 62701", result.address)
    }
    @Test fun parse_addressWithSuite() {
        val result = ContactParser.parse("Tony Stark\n100 Innovation Drive\nSuite 400\nPalo Alto, CA 94301\ntony@stark.com")
        assertEquals("100 Innovation Drive, Suite 400, Palo Alto, CA 94301", result.address)
    }

    // --- Expanded company suffix tests ---

    @Test fun parse_companyWithTechnologies() {
        val result = ContactParser.parse("Alice Wang\nNova Technologies\n+1 555-1111")
        assertEquals("Nova Technologies", result.company)
    }
    @Test fun parse_companyWithSolutions() {
        val result = ContactParser.parse("Bob Chen\nApex Solutions\n+1 555-2222")
        assertEquals("Apex Solutions", result.company)
    }
    @Test fun parse_companyWithConsulting() {
        val result = ContactParser.parse("Carol Davis\nPinnacle Consulting\n+1 555-3333")
        assertEquals("Pinnacle Consulting", result.company)
    }
    @Test fun parse_companyWithGroup() {
        val result = ContactParser.parse("Dave Evans\nOmni Group\n+1 555-4444")
        assertEquals("Omni Group", result.company)
    }
    @Test fun parse_companyWithPartners() {
        val result = ContactParser.parse("Eve Foster\nSmith Partners\n+1 555-5555")
        assertEquals("Smith Partners", result.company)
    }
    @Test fun parse_companyWithAssociates() {
        val result = ContactParser.parse("Frank Green\nMiller Associates\n+1 555-6666")
        assertEquals("Miller Associates", result.company)
    }
    @Test fun parse_companyWithGmbH() {
        val result = ContactParser.parse("Klaus Weber\nAutoTech GmbH\n+49 30 123456")
        assertEquals("AutoTech GmbH", result.company)
    }
    @Test fun parse_companyWithBV() {
        val result = ContactParser.parse("Jan de Vries\nHandel BV\n+31 20 123 4567")
        assertEquals("Handel BV", result.company)
    }
    @Test fun parse_companyWithPtyLtd() {
        val result = ContactParser.parse("Sam Wilson\nDown Under Pty Ltd\n+61 2 9999 8888")
        assertEquals("Down Under Pty Ltd", result.company)
    }

    // --- Expanded title keyword tests ---

    @Test fun parse_titleHeadOf() {
        val result = ContactParser.parse("Grace Hopper\nHead of Engineering\nCompuGlobal\ngrace@compu.com")
        assertEquals("Head of Engineering", result.title)
    }
    @Test fun parse_titleFounderAndCEO() {
        val result = ContactParser.parse("Steve Jobs\nFounder & CEO\nPixar\nsteve@pixar.com")
        assertEquals("Founder & CEO", result.title)
    }
    @Test fun parse_titlePrincipal() {
        val result = ContactParser.parse("Ada Lovelace\nPrincipal Architect\nTechCorp\nada@tech.com")
        assertEquals("Principal Architect", result.title)
    }
    @Test fun parse_titleCTO() {
        val result = ContactParser.parse("Linus Torvalds\nCTO\nLinux Foundation\nlinus@linux.com")
        assertEquals("CTO", result.title)
    }
    @Test fun parse_titleCFO() {
        val result = ContactParser.parse("Warren Buffett\nCFO\nBerkshire Inc\nwarren@berkshire.com")
        assertEquals("CFO", result.title)
    }
    @Test fun parse_titleDirectorOf() {
        val result = ContactParser.parse("Jane Goodall\nDirector of Research\nPrimates Inc\njane@primates.com")
        assertEquals("Director of Research", result.title)
    }
    @Test fun parse_titleSVP() {
        val result = ContactParser.parse("Tim Cook\nSVP Operations\nApple Inc\ntim@apple.com")
        assertEquals("SVP Operations", result.title)
    }
    @Test fun parse_titleAdvisor() {
        val result = ContactParser.parse("Yoda\nSenior Advisor\nJedi Council\nyoda@jedi.com")
        assertEquals("Senior Advisor", result.title)
    }
    @Test fun parse_titleLead() {
        val result = ContactParser.parse("Ellen Ripley\nLead Engineer\nWeyland Corp\nellen@weyland.com")
        assertEquals("Lead Engineer", result.title)
    }
    @Test fun parse_titleAnalyst() {
        val result = ContactParser.parse("Clarice Starling\nBehavioral Analyst\nFBI\nclarice@fbi.com")
        assertEquals("Behavioral Analyst", result.title)
    }
    @Test fun parse_titleCoordinator() {
        val result = ContactParser.parse("Leslie Knope\nProject Coordinator\nParks Dept\nleslie@pawnee.gov")
        assertEquals("Project Coordinator", result.title)
    }

    // --- Name splitting tests ---

    @Test fun parse_nameSplittingFirstAndLast() {
        val result = ContactParser.parse("John Smith\njohn@example.com")
        assertEquals("John Smith", result.name)
        assertEquals("John", result.firstName)
        assertEquals("Smith", result.lastName)
    }
    @Test fun parse_nameSplittingWithMiddle() {
        val result = ContactParser.parse("John Michael Smith\njohn@example.com")
        assertEquals("John Michael Smith", result.name)
        assertEquals("John Michael", result.firstName)
        assertEquals("Smith", result.lastName)
    }
    @Test fun parse_nameSplittingSingleWord() {
        val result = ContactParser.parse("Madonna\nmadonna@example.com")
        assertEquals("Madonna", result.name)
        assertEquals("Madonna", result.firstName)
        assertEquals("", result.lastName)
    }

    // --- Full international card sample ---

    @Test fun parse_fullInternationalCard() {
        val input = """
            Dr. Hiroshi Tanaka
            Chief Technology Officer
            Nippon Technologies
            +81 3 1234 5678
            h.tanaka@nippon-tech.jp
            1-1-2 Marunouchi Drive
            Tokyo, 100-0005
            www.nippon-tech.jp
        """.trimIndent()
        val result = ContactParser.parse(input)
        assertEquals("h.tanaka@nippon-tech.jp", result.email)
        assertEquals("+81 3 1234 5678", result.phone)
        assertEquals("Nippon Technologies", result.company)
        assertEquals("Chief Technology Officer", result.title)
        assertTrue(result.name.contains("Hiroshi"))
        assertTrue(result.address.contains("Marunouchi Drive"))
        assertTrue(result.website.contains("nippon-tech.jp"))
    }

    // --- Edge case: existing NA tests still produce correct name, company, title ---

    @Test fun parse_naComplexCardStillWorks() {
        val input = "JOHN SMITH\nSenior Engineer\nTechCorp LLC\njohn.smith@techcorp.com\n+1 555-987-6543\nwww.techcorp.com"
        val result = ContactParser.parse(input)
        assertEquals("john.smith@techcorp.com", result.email)
        assertEquals("JOHN SMITH", result.name)
        assertEquals("TechCorp LLC", result.company)
        assertEquals("Senior Engineer", result.title)
        assertEquals("+1 555-987-6543", result.phone)
    }
}
