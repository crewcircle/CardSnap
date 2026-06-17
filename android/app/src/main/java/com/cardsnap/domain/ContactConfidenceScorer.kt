package com.cardsnap.domain

import com.cardsnap.domain.model.ContactCard

sealed interface Confidence {
    object HIGH : Confidence
    object MEDIUM : Confidence
    object LOW : Confidence
}

object ContactConfidenceScorer {

    fun score(contact: ContactCard, ocrRawText: String): Confidence {
        val hasRealName = contact.name.isNotBlank() && looksLikeRealName(contact.name)
        val hasEmail = contact.email.isNotBlank()
        val hasPhone = contact.phone.isNotBlank()
        val hasCompany = contact.company.isNotBlank()
        val hasTitle = contact.title.isNotBlank()
        val hasMeaningfulField = hasRealName || hasEmail || hasPhone || hasCompany || hasTitle

        if (isGarbageOcr(ocrRawText) && !hasMeaningfulField) return Confidence.LOW

        if (hasRealName && (hasEmail || hasPhone || hasCompany)) return Confidence.HIGH

        if (hasMeaningfulField) return Confidence.MEDIUM

        return Confidence.LOW
    }

    fun isValid(contact: ContactCard, ocrRawText: String): Boolean =
        score(contact, ocrRawText) == Confidence.HIGH

    internal fun looksLikeRealName(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.length < 2 || trimmed.length > 80) return false

        val letterCount = trimmed.count { it.isLetter() }
        if (letterCount < 2) return false

        val alphaRatio = letterCount.toFloat() / trimmed.length
        if (alphaRatio < 0.4f) return false

        val words = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return false

        return true
    }

    internal fun isGarbageOcr(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return true

        val words = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.size < 2 && words.all { it.length < 3 }) return true

        val symbolCount = trimmed.count { !it.isLetterOrDigit() && !it.isWhitespace() }
        val symbolRatio = symbolCount.toFloat() / trimmed.length
        if (symbolRatio > 0.5f) return true

        val letterCount = trimmed.count { it.isLetter() }
        if (letterCount < 3) return true

        return false
    }
}
