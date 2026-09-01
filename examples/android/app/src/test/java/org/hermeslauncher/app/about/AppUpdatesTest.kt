package org.hermeslauncher.app.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdatesTest {
    @Test
    fun decideReturnsDonateOnVersionChangeWithoutUsingRelease() {
        val prompt = AppUpdates.decide(
            current = "1.1.0",
            lastSeen = "1.0.0",
            lastCheckAt = 0L,
            dismissed = null,
            now = ProductUpdate.MS_DAY,
            release = GithubRelease.Parsed(
                "https://example.com/r",
                listOf(ProductUpdate.NamedAsset("golden-path-9.9.9-foss.apk", "https://example.com/a.apk")),
            ),
            prefix = "Golden-Path",
            fallbackUrl = "https://example.com/latest",
        )
        assertEquals(AppUpdates.LaunchPrompt.Donate, prompt)
    }

    @Test
    fun decideReturnsUpdateForNewerUndismissedApk() {
        val prompt = AppUpdates.decide(
            current = "1.0.0",
            lastSeen = "1.0.0",
            lastCheckAt = 0L,
            dismissed = null,
            now = ProductUpdate.MS_DAY,
            release = GithubRelease.Parsed(
                "https://example.com/r",
                listOf(ProductUpdate.NamedAsset("golden-path-1.1.0-foss.apk", "https://example.com/a.apk")),
            ),
            prefix = "Golden-Path",
            fallbackUrl = "https://example.com/latest",
        )
        assertTrue(prompt is AppUpdates.LaunchPrompt.Update)
        assertEquals("1.1.0", (prompt as AppUpdates.LaunchPrompt.Update).version)
        assertEquals("https://example.com/a.apk", prompt.url)
    }

    @Test
    fun decideStaysSilentWhenReleaseMissing() {
        assertNull(
            AppUpdates.decide(
                current = "1.0.0",
                lastSeen = "1.0.0",
                lastCheckAt = 0L,
                dismissed = null,
                now = ProductUpdate.MS_DAY,
                release = null,
                prefix = "Golden-Path",
                fallbackUrl = "",
            ),
        )
    }
}
