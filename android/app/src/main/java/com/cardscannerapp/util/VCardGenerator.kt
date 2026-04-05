package com.cardscannerapp.util

import ezvcard.Ezvcard
import ezvcard.VCardVersion
import com.cardscannerapp.domain.model.ContactCard

object VCardGenerator {
    fun generateVCard(contact: ContactCard): Pair<String, String> {
        val vCard = ezvcard.VCard()
        if (contact.name.isNotBlank()) {
            vCard.setFormattedName(ezvcard.property.FormattedName(contact.name))
        }
        if (contact.email.isNotBlank()) vCard.addEmail(contact.email)
        if (contact.phone.isNotBlank()) vCard.addTelephoneNumber(contact.phone)
        if (contact.company.isNotBlank()) {
            val org = ezvcard.property.Organization(); org.values.add(contact.company); vCard.organization = org
        }
        if (contact.title.isNotBlank()) vCard.addTitle(contact.title)
        if (contact.address.isNotBlank()) {
            val addr = ezvcard.property.Address(); addr.streetAddress = contact.address; vCard.addAddress(addr)
        }
        if (contact.website.isNotBlank()) vCard.addUrl(contact.website)
        return Ezvcard.write(vCard).version(VCardVersion.V3_0).go() to "text/x-vcard"
    }
}
