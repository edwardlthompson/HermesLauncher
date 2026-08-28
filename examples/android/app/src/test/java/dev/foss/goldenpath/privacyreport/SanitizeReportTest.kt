package dev.foss.goldenpath.privacyreport

import java.nio.charset.StandardCharsets
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class SanitizeReportTest {
    private val fixture = loadFixture()

    @Test
    fun nullBecomesEmpty() {
        assertEquals("", SanitizeReport.text(null))
    }

    @Test
    fun redactsSecretsAndHome() {
        val out = SanitizeReport.text(fixture.stack, stack = true)
        for (leak in fixture.mustNotContain) {
            assertFalse(leak, out.contains(leak))
        }
        for (keep in fixture.mustContain) {
            assertTrue(keep, out.contains(keep))
        }
    }

    @Test
    fun fingerprintStableAcrossUsernames() {
        val a = FingerprintCrash.of("Error\n    at C:\\Users\\Ada\\app\\main.ts:1")
        val b = FingerprintCrash.of("Error\n    at C:\\Users\\Bob\\app\\main.ts:1")
        assertEquals(a, b)
        assertEquals(12, a.length)
    }

    @Test
    fun markdownStripsToken() {
        val md = ReportMarkdown.build("crash", "user ghp_abcdefghijklmnopqrstuvwxyz012345 leaked")
        assertFalse(md.contains("ghp_"))
        assertTrue(md.contains("crash"))
    }

    private data class Fixture(
        val stack: String,
        val mustNotContain: List<String>,
        val mustContain: List<String>,
    )

    private fun loadFixture(): Fixture {
        val text = readFixtureText()
        val obj = JSONObject(text)
        fun arr(key: String): List<String> {
            val json = obj.getJSONArray(key)
            return (0 until json.length()).map { json.getString(it) }
        }
        return Fixture(obj.getString("stack"), arr("must_not_contain"), arr("must_contain"))
    }

    private fun readFixtureText(): String {
        val loaders = listOfNotNull(
            SanitizeReportTest::class.java.getResourceAsStream("/sanitize-fixtures.json"),
            SanitizeReportTest::class.java.classLoader?.getResourceAsStream("sanitize-fixtures.json"),
            Thread.currentThread().contextClassLoader?.getResourceAsStream("sanitize-fixtures.json"),
        )
        val stream = loaders.firstOrNull()
        if (stream != null) {
            return stream.use { String(it.readBytes(), StandardCharsets.UTF_8) }
        }
        val files = listOf(
            java.io.File("src/test/resources/sanitize-fixtures.json"),
            java.io.File("app/src/test/resources/sanitize-fixtures.json"),
        )
        val hit = files.firstOrNull { it.isFile }
            ?: error("missing sanitize-fixtures.json")
        return hit.readText(Charsets.UTF_8)
    }
}
