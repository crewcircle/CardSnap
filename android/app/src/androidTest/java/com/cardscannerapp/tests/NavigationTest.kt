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
class NavigationTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()
    @get:Rule val permissionsRule = GrantPermissionsRule()
    @Before fun setUp() = TestHelpers.resetAppData()
    @After fun tearDown() = TestHelpers.resetAppData()

    @Test fun nav_01_scanScreen_isDefaultScreen() { composeRule.onNodeWithTag("scan-screen").assertIsDisplayed() }
    @Test fun nav_02_navigateToContacts_showsContactsScreen() {
        composeRule.onNodeWithTag("contacts-button").performClick()
        composeRule.onNodeWithTag("contacts-screen").assertIsDisplayed()
    }
    @Test fun nav_03_navigateToSettings_showsSettingsScreen() {
        composeRule.onNodeWithTag("settings-button").performClick()
        composeRule.onNodeWithTag("settings-screen").assertIsDisplayed()
    }
    @Test fun nav_04_backFromContacts_returnsToScan() {
        composeRule.onNodeWithTag("contacts-button").performClick()
        composeRule.onNodeWithTag("contacts-screen").assertIsDisplayed()
        composeRule.onNodeWithText("Back").performClick()
        composeRule.onNodeWithTag("scan-screen").assertIsDisplayed()
    }
    @Test fun nav_05_backFromSettings_returnsToScan() {
        composeRule.onNodeWithTag("settings-button").performClick()
        composeRule.onNodeWithTag("settings-screen").assertIsDisplayed()
        composeRule.onNodeWithText("Back").performClick()
        composeRule.onNodeWithTag("scan-screen").assertIsDisplayed()
    }
}
