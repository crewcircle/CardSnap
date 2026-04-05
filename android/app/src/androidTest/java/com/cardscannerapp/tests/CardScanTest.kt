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
class CardScanTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()
    @get:Rule val permissionsRule = GrantPermissionsRule()
    @Before fun setUp() = TestHelpers.resetAppData()
    @After fun tearDown() = TestHelpers.resetAppData()

    @Test fun scan_01_captureButton_isDisplayed() { composeRule.onNodeWithTag("capture-button").assertIsDisplayed() }
    @Test fun scan_02_galleryButton_isDisplayed() { composeRule.onNodeWithTag("gallery-button").assertIsDisplayed() }
    @Test fun scan_03_torchButton_isDisplayed() { composeRule.onNodeWithTag("torch-button").assertIsDisplayed() }
    @Test fun scan_04_contactsButton_navigatesToContacts() {
        composeRule.onNodeWithTag("contacts-button").performClick()
        composeRule.onNodeWithTag("contacts-screen").assertIsDisplayed()
    }
    @Test fun scan_05_settingsButton_navigatesToSettings() {
        composeRule.onNodeWithTag("settings-button").performClick()
        composeRule.onNodeWithTag("settings-screen").assertIsDisplayed()
    }
    @Test fun scan_06_torchToggle_togglesTorch() {
        composeRule.onNodeWithTag("torch-button").performClick()
        composeRule.onNodeWithTag("torch-button").assertIsDisplayed()
    }
}
