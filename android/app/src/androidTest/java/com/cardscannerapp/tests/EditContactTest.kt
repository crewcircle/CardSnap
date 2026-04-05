package com.cardscannerapp.tests
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
class EditContactTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()
    @get:Rule val permissionsRule = GrantPermissionsRule()
    @Before fun setUp() = TestHelpers.resetAppData()
    @After fun tearDown() = TestHelpers.resetAppData()
    @Test fun edit_01_saveButton_isDisplayed() { composeRule.onNodeWithTag("save-contact-button").assertIsDisplayed() }
    @Test fun edit_02_exportButton_isDisplayed() { composeRule.onNodeWithTag("export-contact-button").assertIsDisplayed() }
    @Test fun edit_03_retakeButton_isDisplayed() { composeRule.onNodeWithTag("retake-button").assertIsDisplayed() }
}
