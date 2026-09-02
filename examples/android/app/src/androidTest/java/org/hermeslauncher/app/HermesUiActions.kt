package org.hermeslauncher.app

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick

fun ComposeTestRule.skipFirstRunIfPresent() {
    waitUntil(5_000) {
        onAllNodesWithText("Later").fetchSemanticsNodes().isNotEmpty() ||
            onAllNodesWithContentDescription("Open settings").fetchSemanticsNodes().isNotEmpty()
    }
    if (onAllNodesWithText("Later").fetchSemanticsNodes().isNotEmpty()) {
        onNodeWithText("Later").performClick()
        waitForIdle()
    }
}
