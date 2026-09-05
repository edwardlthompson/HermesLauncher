package org.hermeslauncher.app.workspace

import org.hermeslauncher.app.widgets.WidgetBinding
import org.hermeslauncher.app.widgets.WidgetGridSpec
import org.hermeslauncher.app.widgets.WidgetHostState
import org.hermeslauncher.app.widgets.WidgetPageState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class BackupCodecTest {
    @Test
    fun roundTripJson() {
        val backup = HermesBackup(
            workspace = "v5|1|1:INBOX|4x5|",
            desktop = "desktop",
            dock = "dock",
            feedUrls = listOf("https://example.com/feed.xml"),
            hiddenApps = listOf("com.hide"),
            blacklist = listOf("com.ignore"),
        )
        val encoded = BackupCodec.encode(backup)
        val decoded = BackupCodec.decode(encoded)!!
        assertEquals(backup.feedUrls, decoded.feedUrls)
        assertEquals(backup.hiddenApps, decoded.hiddenApps)
        assertEquals(backup.blacklist, decoded.blacklist)
        assertEquals(backup.workspace, decoded.workspace)
    }

    @Test
    fun invalidJsonIsNull() {
        assertNull(BackupCodec.decode("{nope"))
        assertNull(BackupCodec.decode(""))
        assertTrue(BackupCodec.import("not-json") { 1 } is BackupResult.Invalid)
    }

    @Test
    fun remapNeverKeepsForeignIds() {
        val foreign = WidgetHostState(
            pages = listOf(
                WidgetPageState(
                    pageIndex = 1,
                    bindings = listOf(
                        WidgetBinding(999, "com.ex/.W", 2, 2, 0, 0),
                        WidgetBinding(1000, "com.missing/.W", 2, 2, 2, 0),
                    ),
                ),
            ),
            grid = WidgetGridSpec.DEFAULT,
        )
        var next = 40
        val (remapped, skipped) = BackupCodec.remapWidgets(foreign) { provider ->
            if (provider.startsWith("com.missing")) null else ++next
        }
        val ids = remapped.pages.flatMap { page -> page.bindings.map { it.appWidgetId } }
        assertEquals(listOf(41), ids)
        assertFalse(ids.contains(999))
        assertFalse(ids.contains(1000))
        assertEquals(1, skipped)
        assertEquals("com.ex/.W", remapped.pages.first().bindings.first().providerFlattened)
    }

    @Test
    fun emptyFeedsAllowedInBackup() {
        val encoded = BackupCodec.encode(HermesBackup())
        val decoded = BackupCodec.decode(encoded)!!
        assertTrue(decoded.feedUrls.isEmpty())
    }
}
