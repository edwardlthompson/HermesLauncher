package dev.foss.goldenpath.privacyreport

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SanitizeReportTest {
    private val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIn0.signaturepart"
    private val stack = """
        TypeError: boom
            at C:\Users\Ada\secret.env:1
        token=ghp_abcdefghijklmnopqrstuvwxyz012345
        $jwt
        AKIAIOSFODNN7EXAMPLE
    """.trimIndent()

    @Test
    fun nullBecomesEmpty() {
        assertEquals("", SanitizeReport.text(null))
    }

    @Test
    fun redactsSecretsAndHome() {
        val out = SanitizeReport.text(stack, stack = true)
        assertFalse(out.contains("Ada"))
        assertFalse(out.contains("ghp_"))
        assertFalse(out.contains("eyJ"))
        assertFalse(out.contains("AKIA"))
        assertTrue(out.contains("<redacted-secret>"))
        assertTrue(out.contains("<redacted-home>"))
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
}
