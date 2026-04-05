package com.cardscannerapp.tests
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cardscannerapp.GrantPermissionsRule
import com.cardscannerapp.MainActivity
import com.cardscannerapp.helpers.TestHelpers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()
    @get:Rule val permissionsRule = GrantPermissionsRule()
    @Before fun setUp() = TestHelpers.resetAppData()
    @After fun tearDown() = TestHelpers.resetAppData()

    @Test fun settings_01_screen_isDisplayed() {
        composeRule.onNodeWithTag("settings-button").performClick()
        composeRule.onNodeWithTag("settings-screen").assertIsDisplayed()
    }
    @Test fun settings_02_autoSaveToggle_isDisplayed() {
        composeRule.onNodeWithTag("settings-button").performClick()
        composeRule.onNodeWithText("Auto-save contacts").assertIsDisplayed()
    }
    @Test fun settings_03_ocrLanguages_isDisplayed() {
        composeRule.onNodeWithTag("settings-button").performClick()
        composeRule.onNodeWithText("OCR Languages").assertIsDisplayed()
    }
    @Test fun settings_04_resetButton_isDisplayed() {
        composeRule.onNodeWithTag("settings-button").performClick()
        composeRule.onNodeWithText("Reset All Data").assertIsDisplayed()
    }
    @Test fun settings_05_backButton_returnsToScan() {
        composeRule.onNodeWithTag("settings-button").performClick()
        composeRule.onNodeWithText("Back").performClick()
        composeRule.onNodeWithTag("scan-screen").assertIsDisplayed()
    }
}
