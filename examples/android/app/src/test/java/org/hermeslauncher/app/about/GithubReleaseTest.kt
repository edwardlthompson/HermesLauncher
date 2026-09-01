package org.hermeslauncher.app.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class GithubReleaseTest {
    @Test
    fun parseIgnoresEmptyOrMalformedPayloads() {
        assertNull(GithubRelease.parse("not-json"))
        val empty = GithubRelease.parse("""{"html_url":"https://example.com/r","assets":[]}""")
        assertEquals("https://example.com/r", empty?.htmlUrl)
        assertTrue(empty?.assets?.isEmpty() == true)
    }

    @Test
    fun parseKeepsNamedDownloadUrls() {
        val parsed = GithubRelease.parse(
            """
            {
              "html_url": "https://example.com/r",
              "assets": [
                {"name":"golden-path-0.18.0-foss.apk","browser_download_url":"https://example.com/a.apk"}
              ]
            }
            """.trimIndent(),
        )
        assertEquals("https://example.com/r", parsed?.htmlUrl)
        assertEquals("https://example.com/a.apk", parsed?.assets?.first()?.url)
    }
}
