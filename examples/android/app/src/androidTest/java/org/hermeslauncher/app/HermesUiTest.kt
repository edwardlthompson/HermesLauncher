package org.hermeslauncher.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test

class HermesUiTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun opensSettingsPanelWithThemeAndUpdateControls() {
        composeTestRule.skipFirstRunIfPresent()
        composeTestRule.onNodeWithContentDescription("Open settings").performClick()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Theme").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Ignore persistent notifications").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Keep notification photos").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark theme").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Close settings").performClick()
    }

    @Test
    fun showsInboxPlaceholderOnHome() {
        composeTestRule.skipFirstRunIfPresent()
        composeTestRule.onNodeWithText("Tap X to dismiss a card. Horizontal swipe changes pages.").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Open app drawer").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Open settings").assertIsDisplayed()
    }

    @Test
    fun opensAboutPanelWithVersion() {
        composeTestRule.skipFirstRunIfPresent()
        composeTestRule.onNodeWithContentDescription("Open app drawer").performClick()
        composeTestRule.onNodeWithContentDescription("About").performClick()
        composeTestRule.onNodeWithText("About").assertIsDisplayed()
        composeTestRule.onNodeWithText("Installed format: apk").assertIsDisplayed()
    }
}
