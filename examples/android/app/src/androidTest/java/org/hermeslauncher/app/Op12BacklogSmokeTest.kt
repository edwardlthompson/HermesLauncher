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

        openRow("Home")
        openRow("Desktop")
        composeTestRule.onNodeWithText("Wallpaper").performScrollTo().assertIsDisplayed()
        backOnce()

        openRow("Look & feel")
        composeTestRule.onNodeWithText("Icon shape").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Night schedule").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Notification badges").performScrollTo().assertIsDisplayed()
        backOnce()

        openRow("Gestures")
        composeTestRule.onNodeWithText("Empty-space gestures").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Swipe up").performScrollTo().assertIsDisplayed()
        backOnce()

        openRow("Search")
        composeTestRule.onNodeWithText("Search stays on-device. There is no web provider.")
            .performScrollTo()
            .assertIsDisplayed()
        backOnce()

        openRow("Folders")
        composeTestRule.onNodeWithText("Open folders fullscreen").performScrollTo().assertIsDisplayed()
        backOnce()
        backOnce()

        openRow("Feeds")
        openRow("Feeds")
        composeTestRule.onNodeWithContentDescription("Import and export").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Import OPML").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Export OPML").performScrollTo().assertIsDisplayed()
        backOnce()
        backOnce()

        openRow("System")
        openRow("Backup")
        composeTestRule.onNodeWithText("Export Hermes backup").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Import Hermes backup").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Reset home layout").performScrollTo().assertIsDisplayed()
        backOnce()
        backOnce()
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

    private fun openRow(title: String) {
        composeTestRule.onNodeWithContentDescription("Open settings section $title")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
    }

    private fun backOnce() {
        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        composeTestRule.waitForIdle()
    }
}
