package com.cardscannerapp
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
open class ComposeTestRule {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()
}
