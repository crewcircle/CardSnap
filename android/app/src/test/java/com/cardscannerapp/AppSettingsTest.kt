package com.cardscannerapp
import com.cardscannerapp.domain.model.AppSettings
import com.cardscannerapp.domain.model.DEFAULT_APP_SETTINGS
import com.cardscannerapp.domain.model.DataUsagePreference
import org.junit.Assert.*
import org.junit.Test

class AppSettingsTest {
    @Test fun defaultSettings_hasEnglishOcr() { assertEquals(listOf("eng"), DEFAULT_APP_SETTINGS.ocrLanguages) }
    @Test fun defaultSettings_autoSaveIsFalse() { assertFalse(DEFAULT_APP_SETTINGS.autoSave) }
    @Test fun defaultSettings_notificationsIsTrue() { assertTrue(DEFAULT_APP_SETTINGS.notifications) }
    @Test fun defaultSettings_wifiOnlyDataUsage() { assertEquals(DataUsagePreference.WIFI_ONLY, DEFAULT_APP_SETTINGS.dataUsage) }
    @Test fun customSettings_canBeCreated() {
        val settings = AppSettings(ocrLanguages = listOf("eng", "spa"), autoSave = true, notifications = false, dataUsage = DataUsagePreference.CELLULAR, hapticEnabled = false)
        assertEquals(listOf("eng", "spa"), settings.ocrLanguages)
        assertTrue(settings.autoSave); assertFalse(settings.notifications)
        assertEquals(DataUsagePreference.CELLULAR, settings.dataUsage); assertFalse(settings.hapticEnabled)
    }
}
