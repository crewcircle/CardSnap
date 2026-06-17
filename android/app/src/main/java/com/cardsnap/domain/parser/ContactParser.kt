package com.cardsnap.domain.parser

import com.cardsnap.domain.model.ContactCard

object ContactParser {
    private val EMAIL_REGEX = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    private val PHONE_REGEX = Regex("""(?:\+?\d{1,3}[-.\s]?)?(?:\(?\d{1,4}\)?[-.\s]?)?\d{2,6}[-.\s]?\d{2,6}(?:[-.\s]?\d{2,6})*""")
    private val WEBSITE_REGEX = Regex("(?:https?://)?(?:www\\.)?[a-zA-Z0-9][a-zA-Z0-9-]+\\.[a-zA-Z]{2,}(?:/[^\\s]*)?")
    private val COMPANY_SUFFIXES = Regex(
        "\\b(?:Pty\\s+Ltd|Inc|LLC|Ltd|Corp|Corporation|Company|Co\\.|GmbH|AG|SA|SAS|SL|BV|NV|PLC|LLP|Group|Technologies|Solutions|Consulting|Associates|Partners)\\b",
        RegexOption.IGNORE_CASE
    )
    private val TITLE_KEYWORDS = Regex(
        "\\b(?:CEO|CTO|CFO|COO|CMO|President|Director|Manager|Engineer|Founder|Co-Founder|VP\\s+of|SVP\\s+of|Director\\s+of|Head\\s+of|VP|SVP|EVP|Chief|Head|Lead|Senior|Junior|Associate|Consultant|Specialist|Coordinator|Administrator|Analyst|Developer|Architect|Officer|Partner|Owner|Principal|Advisor)\\b",
        RegexOption.IGNORE_CASE
    )
    private val ADDRESS_STREET_REGEX = Regex(
        "\\d+[A-Za-z]?\\s+.*(?:Street|St\\.?|Road|Rd\\.?|Avenue|Ave\\.?|Drive|Dr\\.?|Lane|Ln\\.?|Boulevard|Blvd\\.?|Way|Court|Ct\\.?|Plaza|Square|Highway|Hwy\\.?|Parkway|Pkwy\\.?|Circle|Cir\\.?|Terrace|Ter\\.?|Place|Loop)",
        RegexOption.IGNORE_CASE
    )
    private val CITY_STATE_ZIP_REGEX = Regex(
        "[A-Za-z\\s]+,\\s*[A-Z]{2}\\s+\\d{5}(?:-\\d{4})?",
        RegexOption.IGNORE_CASE
    )
    private val POSTCODE_REGEX = Regex("\\b\\d{5}(?:-\\d{4})?\\b")
    private val UK_POSTCODE_REGEX = Regex(
        "[A-Z]{1,2}\\d{1,2}[A-Z]?\\s*\\d[A-Z]{2}",
        RegexOption.IGNORE_CASE
    )

    fun parse(ocrText: String, imageUri: String? = null): ContactCard {
        val lines = ocrText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        val emails = EMAIL_REGEX.findAll(ocrText).map { it.value }.toList()
        val phones = PHONE_REGEX.findAll(ocrText).map { it.value.trim() }.toList()
        val websites = WEBSITE_REGEX.findAll(ocrText).map { it.value }.filter { !it.contains("@") }.toList()
        var name = ""; var company = ""; var title = ""
        val addressLines = mutableListOf<String>()
        var inAddressBlock = false
        for (line in lines) {
            if (line.length < 50 && !line.contains("@") && !line.any { it.isDigit() } && name.isBlank()) name = line
            if (COMPANY_SUFFIXES.containsMatchIn(line) && company.isBlank()) company = line
            if (TITLE_KEYWORDS.containsMatchIn(line) && title.isBlank()) title = line
            val isStreet = ADDRESS_STREET_REGEX.containsMatchIn(line)
            val isCityState = CITY_STATE_ZIP_REGEX.containsMatchIn(line)
            val isUKPostcode = UK_POSTCODE_REGEX.containsMatchIn(line)
            val isUSZip = POSTCODE_REGEX.containsMatchIn(line) && line.length > 5 && line.length < 60
            if (isStreet || isCityState || isUKPostcode || isUSZip) {
                addressLines.add(line)
                inAddressBlock = true
            } else if (inAddressBlock && line.length < 50 && !line.contains("@") &&
                !PHONE_REGEX.matches(line) && !WEBSITE_REGEX.containsMatchIn(line) &&
                !COMPANY_SUFFIXES.containsMatchIn(line) && !TITLE_KEYWORDS.containsMatchIn(line)) {
                addressLines.add(line)
            } else {
                inAddressBlock = false
            }
        }
        val address = if (addressLines.isNotEmpty()) addressLines.joinToString(", ") else ""
        val nameParts = name.split(" ")
        val firstName = if (nameParts.size > 1) nameParts.dropLast(1).joinToString(" ") else name
        val lastName = if (nameParts.size > 1) nameParts.last() else ""
        return ContactCard(
            name = name, firstName = firstName, lastName = lastName,
            company = company, title = title, email = emails.firstOrNull() ?: "",
            phone = phones.firstOrNull() ?: "", website = websites.firstOrNull { it.isNotBlank() } ?: "",
            address = address, imageUri = imageUri, rawOcrText = ocrText
        )
    }
}
