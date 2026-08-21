package dev.foss.goldenpath.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductUpdateTest {

    @Test
    fun dailyCheckWaitsAFullDay() {
        assertTrue(ProductUpdate.shouldCheckDaily(null, 0L))
        assertFalse(ProductUpdate.shouldCheckDaily(0L, ProductUpdate.MS_DAY - 1))
        assertTrue(ProductUpdate.shouldCheckDaily(0L, ProductUpdate.MS_DAY))
    }

    @Test
    fun assetVersionIgnoresTemplateTags() {
        assertEquals(
            "1.2.3",
            ProductUpdate.parseAssetVersion("Golden-Path-1.2.3-x64-setup.exe", "Golden-Path", apk = false),
        )
        assertEquals(
            "1.2.3",
            ProductUpdate.parseAssetVersion("golden-path-1.2.3-foss.apk", "Golden-Path", apk = true),
        )
        assertEquals(null, ProductUpdate.parseAssetVersion("v0.22.1", "Golden-Path", apk = false))
    }

    @Test
    fun donateNudgeOnlyAfterVersionChange() {
        assertFalse(ProductUpdate.shouldNudgeDonate(null, "1.10.7"))
        assertFalse(ProductUpdate.shouldNudgeDonate("1.10.7", "1.10.7"))
        assertTrue(ProductUpdate.shouldNudgeDonate("1.10.7", "1.10.8"))
    }

    @Test
    fun selectApkAssetReadsFossFilename() {
        val picked = ProductUpdate.selectProductAsset(
            listOf(
                ProductUpdate.NamedAsset("sbom.cyclonedx.json", "https://example.com/sbom"),
                ProductUpdate.NamedAsset("golden-path-1.10.8-foss.apk", "https://example.com/a.apk"),
            ),
            "Golden-Path",
            apk = true,
        )
        assertEquals("1.10.8", picked?.version)
        assertEquals("https://example.com/a.apk", picked?.url)
    }

    @Test
    fun updatePromptSkipsDismissedVersion() {
        assertTrue(ProductUpdate.shouldPromptUpdate("1.10.7", "1.10.8", null))
        assertFalse(ProductUpdate.shouldPromptUpdate("1.10.7", "1.10.8", "1.10.8"))
        assertFalse(ProductUpdate.shouldPromptUpdate("1.10.8", "1.10.8", null))
    }
}
