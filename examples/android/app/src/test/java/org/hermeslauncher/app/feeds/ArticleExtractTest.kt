package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleExtractTest {
    @Test
    fun prefersContentOverDescription() {
        val text = ArticleExtract.fromRss("<p>Full story here with enough words to keep.</p>", "Teaser")
        assertTrue(text.contains("Full story"))
        assertTrue(!text.contains("Teaser"))
    }

    @Test
    fun stripsTagsAndEntities() {
        assertEquals("A & B", ArticleExtract.htmlToText("<p>A &amp; B</p>"))
    }

    @Test
    fun fromPageUsesArticle() {
        val html = """
            <html><body>
              <nav><p>Skip this short nav</p></nav>
              <article><p>Android Authority published a long enough article paragraph for reading mode.</p></article>
            </body></html>
        """.trimIndent()
        val text = ArticleExtract.fromPage(html)
        assertTrue(text.contains("Android Authority"))
        assertTrue(!text.contains("Skip this"))
    }

    @Test
    fun fromPageFallsBackToParagraphs() {
        val html = """
            <html><body>
              <p>First paragraph is long enough to be kept in the reader body text.</p>
              <p>Second paragraph is also long enough so both survive extraction together.</p>
            </body></html>
        """.trimIndent()
        val text = ArticleExtract.fromPage(html)
        assertTrue(text.contains("First paragraph"))
        assertTrue(text.contains("Second paragraph"))
    }

    @Test
    fun blocksKeepArticleImagesAndSkipTinyUrls() {
        val html = """
            <article>
              <p>Lead paragraph stays in reading mode before the photo.</p>
              <img src="https://example.com/favicon.ico">
              <img src="https://cdn.example.com/hero.jpg">
              <p>Closing paragraph after the image.</p>
            </article>
        """.trimIndent()
        val blocks = ArticleExtract.blocks(html)
        val images = blocks.filterIsInstance<ArticleBlock.Image>()
        val texts = blocks.filterIsInstance<ArticleBlock.Text>()
        assertEquals(listOf("https://cdn.example.com/hero.jpg"), images.map { it.url })
        assertTrue(texts.any { it.value.contains("Lead paragraph") })
        assertTrue(texts.any { it.value.contains("Closing paragraph") })
    }

    @Test
    fun blocksKeepDataSrcAndOgImages() {
        val html = """
            <article>
              <p>Lead paragraph stays in reading mode before the photo.</p>
              <img data-src="https://cdn.example.com/second.jpg">
            </article>
            <meta property="og:image" content="https://cdn.example.com/og.jpg">
        """.trimIndent()
        val images = ArticleExtract.blocks(html).filterIsInstance<ArticleBlock.Image>().map { it.url }
        assertTrue(images.contains("https://cdn.example.com/second.jpg"))
        assertTrue(images.contains("https://cdn.example.com/og.jpg"))
    }

    @Test
    fun blocksKeepOneUrlPerSrcsetAndWordpressSize() {
        val html = """
            <article>
              <p>Lead paragraph stays in reading mode before the photo.</p>
              <img src="https://cdn.example.com/hero-300x169.jpg"
                   srcset="https://cdn.example.com/hero-300x169.jpg 300w,
                           https://cdn.example.com/hero-768x432.jpg 768w,
                           https://cdn.example.com/hero-1024x576.jpg 1024w,
                           https://cdn.example.com/hero.jpg 1920w">
              <img src="https://cdn.example.com/hero-1024x576.jpg">
              <img src="https://cdn.example.com/other.jpg">
            </article>
            <meta property="og:image" content="https://cdn.example.com/hero.jpg">
        """.trimIndent()
        val images = ArticleExtract.blocks(html).filterIsInstance<ArticleBlock.Image>().map { it.url }
        assertEquals(listOf("https://cdn.example.com/hero.jpg", "https://cdn.example.com/other.jpg"), images)
    }
}
