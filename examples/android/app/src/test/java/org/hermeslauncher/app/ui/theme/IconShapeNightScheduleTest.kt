package org.hermeslauncher.app.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IconShapeTest {
    @Test
    fun parseKnownAndUnknown() {
        assertEquals(IconShape.CIRCLE, IconShape.parse("circle"))
        assertEquals(IconShape.SQUIRCLE, IconShape.parse("SQUIRCLE"))
        assertEquals(IconShape.SYSTEM, IconShape.parse(null))
        assertEquals(IconShape.SYSTEM, IconShape.parse("nope"))
    }
}

class NightScheduleTest {
    @Test
    fun parseTimeValidAndInvalid() {
        assertEquals(22 * 60 + 30, NightSchedule.parseTime("22:30"))
        assertEquals(7 * 60, NightSchedule.parseTime("7:00"))
        assertNull(NightSchedule.parseTime(""))
        assertNull(NightSchedule.parseTime("25:00"))
        assertNull(NightSchedule.parseTime("abc"))
    }

    @Test
    fun invalidScheduleBecomesOff() {
        assertEquals(NightSchedule.OFF, NightSchedule.parse(true, "bad", "07:00"))
        assertEquals(NightSchedule.OFF, NightSchedule.parse(true, "22:00", null))
    }

    @Test
    fun overnightWindowContains() {
        val night = NightSchedule(enabled = true, startMinute = 22 * 60, endMinute = 7 * 60)
        assertTrue(night.contains(23 * 60))
        assertTrue(night.contains(3 * 60))
        assertFalse(night.contains(12 * 60))
        assertFalse(night.contains(7 * 60))
    }

    @Test
    fun sameDayWindow() {
        val day = NightSchedule(enabled = true, startMinute = 9 * 60, endMinute = 17 * 60)
        assertTrue(day.contains(12 * 60))
        assertFalse(day.contains(8 * 60))
        assertFalse(day.contains(17 * 60))
    }

    @Test
    fun scheduleForcesDarkInsideWindow() {
        val schedule = NightSchedule(enabled = true, startMinute = 22 * 60, endMinute = 7 * 60)
        assertTrue(
            ThemeResolve.isDark(
                mode = ThemeMode.System,
                schedule = schedule,
                systemDark = false,
                nowMinute = 23 * 60,
            ),
        )
        assertFalse(
            ThemeResolve.isDark(
                mode = ThemeMode.System,
                schedule = schedule,
                systemDark = false,
                nowMinute = 12 * 60,
            ),
        )
    }

    @Test
    fun disabledScheduleFollowsMode() {
        assertFalse(
            ThemeResolve.isDark(
                ThemeMode.Light,
                NightSchedule.OFF,
                systemDark = true,
                nowMinute = 23 * 60,
            ),
        )
        assertTrue(
            ThemeResolve.isDark(
                ThemeMode.Dark,
                NightSchedule.OFF,
                systemDark = false,
                nowMinute = 12 * 60,
            ),
        )
    }
}
