package com.cardsnap.helpers
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.io.File

object TestHelpers {
    fun resetAppData() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().clear().apply()
        context.deleteDatabase("cardsnap_database")
    }
    fun copyTestAssetToCache(assetName: String): String {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inputStream = context.assets.open("business_cards/$assetName")
        val outputFile = File(context.cacheDir, "test_card.jpg")
        inputStream.use { input -> outputFile.outputStream().use { output -> input.copyTo(output) } }
        return outputFile.absolutePath
    }
}
