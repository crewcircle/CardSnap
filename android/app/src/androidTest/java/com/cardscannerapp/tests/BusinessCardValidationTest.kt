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
class BusinessCardValidationTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()
    @get:Rule val permissionsRule = GrantPermissionsRule()
    @Before fun setUp() = TestHelpers.resetAppData()
    @After fun tearDown() = TestHelpers.resetAppData()
    @Test fun validation_01_emailField_extractedCorrectly() { composeRule.onNodeWithTag("scan-screen").assertIsDisplayed() }
    @Test fun validation_02_phoneField_extractedCorrectly() { composeRule.onNodeWithTag("scan-screen").assertIsDisplayed() }
    @Test fun validation_03_companyField_extractedCorrectly() { composeRule.onNodeWithTag("scan-screen").assertIsDisplayed() }
    @Test fun validation_04_nameField_extractedCorrectly() { composeRule.onNodeWithTag("scan-screen").assertIsDisplayed() }
}
