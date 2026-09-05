package org.hermeslauncher.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.platform.app.InstrumentationRegistry
import org.hermeslauncher.app.ui.insets.NavigationMode
import org.hermeslauncher.app.ui.insets.readNavigationMode
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NavBarInsetUiTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun setNavigationMode(mode: Int) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
            "settings put secure navigation_mode $mode",
        )
        composeTestRule.activityRule.scenario.recreate()
        composeTestRule.waitForIdle()
    }

    @Test
    fun closeButtonClearsNavigationBar_threeButton() {
        setNavigationMode(0)

        val context = composeTestRule.activity
        assertTrue(context.readNavigationMode() == NavigationMode.ThreeButton)

        composeTestRule.skipFirstRunIfPresent()
        composeTestRule.onNodeWithContentDescription("Open settings").performClick()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Backup").performScrollTo().assertIsDisplayed()

        val decorView = context.window.decorView
        val navInset = ViewCompat.getRootWindowInsets(decorView)
            ?.getInsets(WindowInsetsCompat.Type.navigationBars())
            ?.bottom ?: 0
        val screenHeight = decorView.height
        val buttonBottom = composeTestRule.onNodeWithText("Backup")
            .fetchSemanticsNode()
            .boundsInRoot
            .bottom

        val minClearance = if (navInset > 0) navInset else 48
        assertTrue(
            "Backup row bottom ($buttonBottom) should be above nav bar (screen=$screenHeight inset=$navInset)",
            buttonBottom <= screenHeight - minClearance + 8,
        )
    }

    @Test
    fun closeButtonClearsNavigationBar_gesture() {
        setNavigationMode(2)

        composeTestRule.skipFirstRunIfPresent()
        composeTestRule.onNodeWithContentDescription("Open settings").performClick()
        composeTestRule.onNodeWithText("Backup").performScrollTo().assertIsDisplayed()

        val decorView = composeTestRule.activity.window.decorView
        val navInset = ViewCompat.getRootWindowInsets(decorView)
            ?.getInsets(WindowInsetsCompat.Type.navigationBars())
            ?.bottom ?: 0
        val screenHeight = decorView.height
        val buttonBottom = composeTestRule.onNodeWithText("Backup")
            .fetchSemanticsNode()
            .boundsInRoot
            .bottom

        assertTrue(
            "Backup row bottom ($buttonBottom) should clear gesture nav inset ($navInset)",
            buttonBottom <= screenHeight - navInset + 8,
        )
    }
}
