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
class ContactsTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()
    @get:Rule val permissionsRule = GrantPermissionsRule()
    @Before fun setUp() = TestHelpers.resetAppData()
    @After fun tearDown() = TestHelpers.resetAppData()

    @Test fun contacts_01_emptyState_showsMessage() {
        composeRule.onNodeWithTag("contacts-button").performClick()
        composeRule.onNodeWithText("No contacts yet").assertIsDisplayed()
    }
    @Test fun contacts_02_emptyState_showsScanPrompt() {
        composeRule.onNodeWithTag("contacts-button").performClick()
        composeRule.onNodeWithText("Scan a business card to get started").assertIsDisplayed()
    }
    @Test fun contacts_03_exportButton_isVisible() {
        composeRule.onNodeWithTag("contacts-button").performClick()
        composeRule.onNodeWithTag("export-all-contacts-button").assertIsDisplayed()
    }
    @Test fun contacts_04_backButton_returnsToScan() {
        composeRule.onNodeWithTag("contacts-button").performClick()
        composeRule.onNodeWithText("Back").performClick()
        composeRule.onNodeWithTag("scan-screen").assertIsDisplayed()
    }
}
