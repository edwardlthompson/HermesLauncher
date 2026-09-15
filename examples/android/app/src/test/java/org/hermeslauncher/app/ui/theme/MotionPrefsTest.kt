package org.hermeslauncher.app.ui.theme

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionPrefsTest {
    @Test
    fun reducedWhenAnimatorScaleIsZero() {
        assertTrue(MotionPrefs.reduced(0f))
        assertFalse(MotionPrefs.reduced(1f))
        assertFalse(MotionPrefs.reduced(0.5f))
    }

    @Test
    fun motionFlagsOffWhenReduced() {
        assertFalse(MotionPrefs.animateScroll(reduced = true))
        assertFalse(MotionPrefs.animateSize(reduced = true))
        assertFalse(MotionPrefs.animateDismiss(reduced = true))
        assertTrue(MotionPrefs.animateScroll(reduced = false))
        assertTrue(MotionPrefs.animateSize(reduced = false))
        assertTrue(MotionPrefs.animateDismiss(reduced = false))
    }
}
