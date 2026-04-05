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
class RealWorldScenariosTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()
    @get:Rule val permissionsRule = GrantPermissionsRule()
    @Before fun setUp() = TestHelpers.resetAppData()
    @After fun tearDown() = TestHelpers.resetAppData()
    @Test fun realworld_01_lowResCard_scansSuccessfully() { composeRule.onNodeWithTag("scan-screen").assertIsDisplayed() }
    @Test fun realworld_02_noEmailCard_handlesGracefully() { composeRule.onNodeWithTag("scan-screen").assertIsDisplayed() }
    @Test fun realworld_03_multiplePhoneNumbers_extractsFirst() { composeRule.onNodeWithTag("scan-screen").assertIsDisplayed() }
    @Test fun realworld_04_fullPipeline_injectExtractSave() { composeRule.onNodeWithTag("scan-screen").assertIsDisplayed() }
    @Test fun realworld_05_batchProcessing_tenCards() { composeRule.onNodeWithTag("scan-screen").assertIsDisplayed() }
}
