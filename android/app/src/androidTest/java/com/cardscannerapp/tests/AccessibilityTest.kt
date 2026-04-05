package com.cardscannerapp.tests
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
class AccessibilityTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()
    @get:Rule val permissionsRule = GrantPermissionsRule()
    @Before fun setUp() = TestHelpers.resetAppData()
    @After fun tearDown() = TestHelpers.resetAppData()
    @Test fun a11y_01_scanScreen_hasContent() { composeRule.onNodeWithTag("scan-screen").assertIsDisplayed() }
    @Test fun a11y_02_contactsScreen_isAccessible() {
        composeRule.onNodeWithTag("contacts-button").performClick()
        composeRule.onNodeWithTag("contacts-screen").assertIsDisplayed()
    }
    @Test fun a11y_03_settingsScreen_isAccessible() {
        composeRule.onNodeWithTag("settings-button").performClick()
        composeRule.onNodeWithTag("settings-screen").assertIsDisplayed()
    }
}
