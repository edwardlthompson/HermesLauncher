package org.hermeslauncher.app.workspace

import org.hermeslauncher.app.widgets.WidgetBinding
import org.hermeslauncher.app.widgets.WidgetGridSpec
import org.hermeslauncher.app.widgets.WidgetHostCodec
import org.hermeslauncher.app.widgets.WidgetHostState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceModelTest {
    @Test
    fun migrateTwoPagesKeepsWidgetIdsAndHomeInbox() {
        val host = WidgetHostState(grid = WidgetGridSpec.DEFAULT)
            .withBinding(1, WidgetBinding(9, "org.example/.A", 2, 2, 0, 0))
            .withBinding(2, WidgetBinding(8, "org.example/.B", 2, 2, 0, 0))
        val model = WorkspaceModel.migrate(host)
        assertEquals(WorkspaceModel.INBOX_SCREEN_ID, model.homeScreenId)
        assertEquals(WorkspaceKind.FEEDS, model.screens.first().kind)
        assertEquals(WorkspaceKind.INBOX, model.screens[1].kind)
        assertEquals(
            listOf(
                WorkspaceModel.FEEDS_SCREEN_ID,
                WorkspaceModel.INBOX_SCREEN_ID,
                WorkspaceModel.desktopId(1),
                WorkspaceModel.desktopId(2),
                WorkspaceModel.desktopId(3),
            ),
            model.screenIds,
        )
        assertEquals(1, model.pagerIndex(WorkspaceModel.INBOX_SCREEN_ID))
        assertEquals(0, model.pagerIndex(WorkspaceModel.FEEDS_SCREEN_ID))
        assertEquals(1, model.desktopPageIndex(WorkspaceModel.desktopId(1)))
        assertEquals(null, model.desktopPageIndex(WorkspaceModel.INBOX_SCREEN_ID))
        assertEquals(1, model.widgetPageAt(2))
        assertEquals(0, model.widgetPageAt(1))
        assertEquals(9, host.page(1).bindings[0].appWidgetId)
        assertEquals(8, host.page(2).bindings[0].appWidgetId)
    }

    @Test
    fun missingIdFallsBackToHome() {
        val model = WorkspaceModel.defaults()
        assertEquals(model.homePagerIndex(), model.pagerIndex(99L))
    }
}

class WorkspaceCodecTest {
    @Test
    fun v5RoundTripPreservesHomeAndBindings() {
        val host = WidgetHostState(grid = WidgetGridSpec.DEFAULT)
            .withBinding(1, WidgetBinding(9, "org.example/.A", 2, 2, 0, 0))
            .withBinding(2, WidgetBinding(8, "org.example/.B", 2, 2, 0, 0))
        val original = WorkspaceBundle(host, WorkspaceModel.migrate(host))
        val restored = WorkspaceCodec.decode(WorkspaceCodec.encode(original))
        assertTrue(WorkspaceCodec.encode(original).startsWith("v5|"))
        assertEquals(original.model.homeScreenId, restored.model.homeScreenId)
        assertEquals(original.model.screenIds, restored.model.screenIds)
        assertEquals(9, restored.host.page(1).bindings[0].appWidgetId)
        assertEquals(8, restored.host.page(2).bindings[0].appWidgetId)
        assertEquals(WidgetGridSpec.DEFAULT, restored.host.grid)
    }

    @Test
    fun v4PayloadMigratesThroughWorkspaceCodec() {
        val host = WidgetHostState().withBinding(1, WidgetBinding(3, "org.example/.W", 2, 2, 1, 1))
        val v4 = WidgetHostCodec.encode(host)
        val bundle = WorkspaceCodec.decode(v4)
        assertEquals(WorkspaceModel.INBOX_SCREEN_ID, bundle.model.homeScreenId)
        assertEquals(3, bundle.host.page(1).bindings[0].appWidgetId)
    }

    @Test
    fun corruptV5YieldsDefaults() {
        val bundle = WorkspaceCodec.decode("v5|")
        assertEquals(WorkspaceModel.INBOX_SCREEN_ID, bundle.model.homeScreenId)
        assertTrue(bundle.host.page(1).bindings.isEmpty())
    }

    @Test
    fun missingHomeUsesFirstInbox() {
        val packed = "v5|nogood|1:FEEDS,2:INBOX,1001:DESKTOP|" +
            WidgetHostCodec.encode(WidgetHostState()).removePrefix("v4|")
        val bundle = WorkspaceCodec.decode(packed)
        assertEquals(WorkspaceModel.INBOX_SCREEN_ID, bundle.model.homeScreenId)
    }

    @Test
    fun widgetHostCodecReadsV5Host() {
        val host = WidgetHostState().withBinding(1, WidgetBinding(4, "org.example/.Z"))
        val encoded = WorkspaceCodec.encode(WorkspaceBundle(host, WorkspaceModel.migrate(host)))
        val restored = WidgetHostCodec.decode(encoded)
        assertEquals(4, restored.page(1).bindings[0].appWidgetId)
    }
}
