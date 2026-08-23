package dev.foss.goldenpath.githubfeedback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IssueFormUrlTest {
    @Test
    fun placeholderRepoIsEmpty() {
        assertEquals("", IssueFormUrl.build("OWNER/REPO", "crash_report.yml", mapOf("description" to "x")).url)
    }

    @Test
    fun crashTitleFormats() {
        assertEquals("[crash] a1b2c3d4e5f6 TypeError", IssueFormUrl.crashTitle("A1B2C3D4E5F6ffff", "TypeError: x"))
    }

    @Test
    fun largeStackUsesClipboard() {
        val stack = "x".repeat(IssueFormUrl.MAX_QUERY_CHARS + 500)
        val built = IssueFormUrl.build("acme/app", "crash_report.yml", mapOf("stack" to stack, "title" to "[crash] ab"))
        assertTrue(built.bodyTooLarge)
        assertTrue(built.url.length < IssueFormUrl.MAX_QUERY_CHARS)
        assertFalse(built.url.contains(stack.take(40)))
        assertEquals(stack, built.clipboardMarkdown)
    }
}
