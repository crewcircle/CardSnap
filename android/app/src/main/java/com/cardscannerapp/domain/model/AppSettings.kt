package com.cardscannerapp.domain.model

enum class DataUsagePreference { WIFI_ONLY, CELLULAR }

data class AppSettings(
    val ocrLanguages: List<String> = listOf("eng"),
    val autoSave: Boolean = false,
    val notifications: Boolean = true,
    val dataUsage: DataUsagePreference = DataUsagePreference.WIFI_ONLY,
    val hapticEnabled: Boolean = true
)

val DEFAULT_APP_SETTINGS = AppSettings()
