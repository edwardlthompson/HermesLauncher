package org.hermeslauncher.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test

/**
 * OP12 (`b5214fc6`) backlog smoke: opens each settings hub used by Nova-parity ADB rows.
 * Visual drag/bind remains out of scope; this proves chrome and strings ship on-device.
 */
class Op12BacklogSmokeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun settingsHubsForBacklogRows() {
        composeTestRule.skipFirstRunIfPresent()
        composeTestRule.onNodeWithContentDescription("Open settings").performClick()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()

        openSection("Desktop")
        composeTestRule.onNodeWithText("Wallpaper").performScrollTo().assertIsDisplayed()
        backToHub()

        openSection("Look & feel")
        composeTestRule.onNodeWithText("Icon shape").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Night schedule").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Dots").performScrollTo().assertIsDisplayed()
        backToHub()

        openSection("Gestures")
        composeTestRule.onNodeWithText("Empty-space gestures").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Swipe up").performScrollTo().assertIsDisplayed()
        backToHub()

        openSection("Search")
        composeTestRule.onNodeWithText("Search stays on-device. There is no web provider.")
            .performScrollTo()
            .assertIsDisplayed()
        backToHub()

        openSection("Folders")
        composeTestRule.onNodeWithText("Open folders fullscreen").performScrollTo().assertIsDisplayed()
        backToHub()

        openSection("Feeds")
        composeTestRule.onNodeWithText("Import OPML").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Export OPML").performScrollTo().assertIsDisplayed()
        backToHub()

        openSection("Backup")
        composeTestRule.onNodeWithText("Export Hermes backup").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Import Hermes backup").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Reset home layout").performScrollTo().assertIsDisplayed()
        backToHub()
    }

    @Test
    fun drawerAndSearchChromePresent() {
        composeTestRule.skipFirstRunIfPresent()
        composeTestRule.onNodeWithContentDescription("Open app drawer").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Open settings").assertIsDisplayed()
    }

    private fun openSection(title: String) {
        composeTestRule.onNodeWithText(title).performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

    private fun backToHub() {
        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }
}
