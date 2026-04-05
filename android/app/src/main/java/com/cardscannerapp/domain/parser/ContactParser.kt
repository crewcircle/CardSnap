package com.cardscannerapp.domain.parser

import com.cardscannerapp.domain.model.ContactCard

object ContactParser {
    private val EMAIL_REGEX = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    private val PHONE_REGEX = Regex("(?:\\+?1[-.\\s]?)?(?:\\(?\\d{3}\\)?[-.\\s]?)?\\d{3}[-.\\s]?\\d{4}")
    private val WEBSITE_REGEX = Regex("(?:https?://)?(?:www\\.)?[a-zA-Z0-9][a-zA-Z0-9-]+\\.[a-zA-Z]{2,}(?:/[^\\s]*)?")
    private val COMPANY_SUFFIXES = Regex("(?:Inc|LLC|Ltd|Corp|Corporation|Company|Co\\.)", RegexOption.IGNORE_CASE)
    private val TITLE_KEYWORDS = Regex("(?:CEO|CTO|President|Director|Manager|Engineer|Founder|VP|Chief|Head|Lead|Senior|Junior|Associate|Consultant|Specialist|Coordinator|Administrator|Analyst|Developer|Architect|Officer|Partner|Owner|Principal)", RegexOption.IGNORE_CASE)

    fun parse(ocrText: String, imageUri: String? = null): ContactCard {
        val lines = ocrText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        val emails = EMAIL_REGEX.findAll(ocrText).map { it.value }.toList()
        val phones = PHONE_REGEX.findAll(ocrText).map { it.value.trim() }.toList()
        val websites = WEBSITE_REGEX.findAll(ocrText).map { it.value }.filter { !it.contains("@") }.toList()
        var name = ""; var company = ""; var title = ""
        for (line in lines) {
            if (line.length < 50 && !line.contains("@") && !line.any { it.isDigit() } && name.isBlank()) name = line
            if (COMPANY_SUFFIXES.containsMatchIn(line) && company.isBlank()) company = line
            if (TITLE_KEYWORDS.containsMatchIn(line) && title.isBlank()) title = line
        }
        val nameParts = name.split(" ")
        return ContactCard(
            name = name, firstName = nameParts.firstOrNull() ?: "", lastName = nameParts.drop(1).joinToString(" "),
            company = company, title = title, email = emails.firstOrNull() ?: "",
            phone = phones.firstOrNull() ?: "", website = websites.firstOrNull { it.isNotBlank() } ?: "",
            imageUri = imageUri, rawOcrText = ocrText
        )
    }
}
