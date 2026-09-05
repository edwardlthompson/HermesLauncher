package org.hermeslauncher.app.l3

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeAgainSearchTest {
    @Test
    fun secondHomeOnInboxOpensSearch() {
        assertTrue(
            HomeAgainSearch.shouldOpen(
                alreadyOnHome = true,
                inNormal = true,
                onInbox = true,
                floatingOpen = false,
                actionMain = true,
            ),
        )
        assertTrue(HomeAgainSearch.fromPulse(page = 1, homeIndex = 1))
    }

    @Test
    fun firstHomeOrDesktopDoesNotOpenSearch() {
        assertFalse(
            HomeAgainSearch.shouldOpen(
                alreadyOnHome = false,
                inNormal = true,
                onInbox = true,
                floatingOpen = false,
                actionMain = true,
            ),
        )
        assertFalse(
            HomeAgainSearch.shouldOpen(
                alreadyOnHome = true,
                inNormal = true,
                onInbox = false,
                floatingOpen = false,
                actionMain = true,
            ),
        )
        assertFalse(HomeAgainSearch.fromPulse(page = 0, homeIndex = 1))
    }
}

class L3GridTest {
    @Test
    fun pickPrefersMatchingColumns() {
        val options = listOf(
            GridChoice("4_by_4", 4, 4),
            GridChoice("5_by_5", 5, 5),
        )
        assertEquals("4_by_4", L3Grid.pick(4, 5, options)?.name)
        assertEquals("5_by_5", L3Grid.pick(5, 5, options)?.name)
        assertEquals(null, L3Grid.pick(4, 4, emptyList()))
    }

    @Test
    fun searchCapFollowsAppRowToggle() {
        assertEquals(5, L3Grid.previewCap(true, 5))
        assertEquals(Int.MAX_VALUE, L3Grid.previewCap(false, 5))
    }
}

class L3NightModeTest {
    @Test
    fun scheduleForcesDarkOtherwiseFollowsTheme() {
        val night = org.hermeslauncher.app.ui.theme.NightSchedule(
            enabled = true,
            startMinute = 22 * 60,
            endMinute = 7 * 60,
        )
        assertEquals(
            L3NightMode.YES,
            L3NightMode.delegateMode(org.hermeslauncher.app.ui.theme.ThemeMode.Light, night, 23 * 60),
        )
        assertEquals(
            L3NightMode.NO,
            L3NightMode.delegateMode(org.hermeslauncher.app.ui.theme.ThemeMode.Light, night, 12 * 60),
        )
        assertEquals(
            L3NightMode.FOLLOW,
            L3NightMode.delegateMode(
                org.hermeslauncher.app.ui.theme.ThemeMode.System,
                org.hermeslauncher.app.ui.theme.NightSchedule.OFF,
                0,
            ),
        )
    }

    @Test
    fun applyNullManagerDoesNotThrow() {
        L3NightMode.apply(
            org.hermeslauncher.app.ui.theme.ThemeMode.System,
            org.hermeslauncher.app.ui.theme.NightSchedule.OFF,
            null,
        )
    }
}

class L3DockTest {
    @Test
    fun pageCountMapsToHotseatIcons() {
        assertEquals(5, L3Dock.hotseatIcons(1))
        assertEquals(8, L3Dock.hotseatIcons(4))
        assertEquals(4, L3Dock.hotseatIcons(0))
    }
}

class HermesSwipeGateTest {
    @Test
    fun inboxScrollDoesNotOpenDrawerDockStillCan() {
        assertFalse(HermesSwipeGate.intercept(reserved = true, onHotseat = false, opensApps = true))
        assertTrue(HermesSwipeGate.intercept(reserved = true, onHotseat = true, opensApps = true))
        assertTrue(HermesSwipeGate.intercept(reserved = false, onHotseat = false, opensApps = true))
        assertFalse(HermesSwipeGate.intercept(reserved = false, onHotseat = false, opensApps = false))
    }
}
