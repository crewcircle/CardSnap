package com.cardsnap.util

import com.cardsnap.domain.model.ContactCard
import ezvcard.Ezvcard
import ezvcard.VCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object VCardParser {
    private val timestampFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    fun parse(vcfContent: String): ContactCard? {
        return try {
            val vCards: List<VCard> = Ezvcard.parse(vcfContent).all()
            vCards.firstOrNull()?.toContactCard()
        } catch (e: Exception) {
            null
        }
    }

    fun parseMultiple(vcfContent: String): List<ContactCard> {
        return try {
            val vCards: List<VCard> = Ezvcard.parse(vcfContent).all()
            vCards.mapNotNull { it.toContactCard() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun VCard.toContactCard(): ContactCard? {
        val name = formattedName?.value?.takeIf { it.isNotBlank() }
            ?: return null

        val email = emails.firstOrNull()?.value ?: ""
        val phone = telephoneNumbers.firstOrNull()?.text ?: ""
        val company = organization?.values?.firstOrNull() ?: ""
        val title = titles.firstOrNull()?.value ?: ""
        val address = addresses.firstOrNull()?.streetAddress ?: ""
        val website = urls.firstOrNull()?.value ?: ""
        val firstName = structuredName?.given ?: ""
        val lastName = structuredName?.family ?: ""

        val now = Date()
        val timestamp = timestampFormat.format(now)

        return ContactCard(
            id = UUID.randomUUID().toString(),
            name = name,
            firstName = firstName,
            lastName = lastName,
            company = company,
            title = title,
            email = email,
            phone = phone,
            address = address,
            website = website,
            scannedAt = timestamp,
            updatedAt = timestamp
        )
    }
}
