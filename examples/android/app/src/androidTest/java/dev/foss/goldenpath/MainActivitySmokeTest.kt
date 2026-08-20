package dev.foss.goldenpath

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun launchesMainActivity() {
        activityRule.scenario.onActivity { activity ->
            check(!activity.isFinishing)
        }
    }

    @Test
    fun prefersFastestSameResolutionDisplayMode() {
        activityRule.scenario.onActivity { activity ->
            val display = activity.display ?: return@onActivity
            val current = display.mode
            val expected = display.supportedModes
                .filter {
                    it.physicalWidth == current.physicalWidth &&
                        it.physicalHeight == current.physicalHeight
                }
                .maxByOrNull { it.refreshRate }
                ?.modeId
                ?: return@onActivity
            assertEquals(expected, activity.window.attributes.preferredDisplayModeId)
        }
    }
}
