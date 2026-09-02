package org.hermeslauncher.app

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick

fun ComposeTestRule.skipFirstRunIfPresent() {
    waitForIdle()
    if (onAllNodesWithText("Later").fetchSemanticsNodes().isNotEmpty()) {
        onNodeWithText("Later").performClick()
        waitForIdle()
    }
}
