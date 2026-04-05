package com.cardscannerapp.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.cardscannerapp.domain.model.ContactCard
import java.io.File

object ShareHelper {
    fun shareVCard(context: Context, contact: ContactCard) {
        val (vCardString, mimeType) = VCardGenerator.generateVCard(contact)
        val fileName = "${(contact.name.ifBlank { "contact" }).replace(Regex("\\s"), "_")}.vcf"
        val file = File(context.cacheDir, fileName); file.writeText(vCardString, Charsets.UTF_8)
        val uri = FileProvider.getUriForFile(context, "com.cardscannerapp.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType; putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share contact"))
    }

    fun shareCsv(context: Context, contacts: List<ContactCard>) {
        val header = "Name,Email,Phone,Company,Address,Website,Scanned At"
        val rows = contacts.joinToString("\n") { c ->
            listOf(c.name, c.email, c.phone, c.company, c.address, c.website, c.scannedAt)
                .joinToString(",") { f -> if (f.containsAny(',', '"', '\n')) "\"${f.replace("\"", "\"\"")}\"" else f }
        }
        val file = File(context.cacheDir, "contacts.csv"); file.writeText("$header\n$rows", Charsets.UTF_8)
        val uri = FileProvider.getUriForFile(context, "com.cardscannerapp.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"; putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share contacts"))
    }
    private fun String.containsAny(vararg chars: Char) = chars.any { this.contains(it) }
}
