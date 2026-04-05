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
class UxFeaturesTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()
    @get:Rule val permissionsRule = GrantPermissionsRule()
    @Before fun setUp() = TestHelpers.resetAppData()
    @After fun tearDown() = TestHelpers.resetAppData()
    @Test fun ux_01_scanScreen_hasContent() { composeRule.onNodeWithTag("scan-screen").assertIsDisplayed() }
    @Test fun ux_02_galleryUpload_isAvailable() { composeRule.onNodeWithTag("gallery-button").assertIsDisplayed() }
    @Test fun ux_03_torchToggle_isAvailable() { composeRule.onNodeWithTag("torch-button").assertIsDisplayed() }
}
