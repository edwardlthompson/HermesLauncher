package org.hermeslauncher.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class HermesUiTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun opensSettingsPanelWithThemeAndUpdateControls() {
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ignore persistent notifications").assertIsDisplayed()
        composeTestRule.onNodeWithText("Keep notification photos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark theme").performClick()
        composeTestRule.onNodeWithText("Close settings").performClick()
    }

    @Test
    fun showsInboxPlaceholderOnHome() {
        composeTestRule.onNodeWithText("Tap X to dismiss a card. Horizontal swipe changes pages.").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Open app drawer").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Settings").assertIsDisplayed()
    }

    @Test
    fun opensAboutPanelWithVersion() {
        composeTestRule.onNodeWithContentDescription("Open app drawer").performClick()
        composeTestRule.onNodeWithContentDescription("About").performClick()
        composeTestRule.onNodeWithText("About").assertIsDisplayed()
        composeTestRule.onNodeWithText("Installed format: apk").assertIsDisplayed()
    }
}
