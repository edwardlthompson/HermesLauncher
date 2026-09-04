package org.hermeslauncher.app.icons

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hermeslauncher.app.clearPreferenceDataStores
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class DrawerPrefsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun resetDataStore() {
        context.clearPreferenceDataStores()
    }

    @Test
    fun defaultsShowAllFiveColumnGrid() = runBlocking {
        val prefs = DrawerPrefs(context)
        val snap = prefs.snapshot.first()
        assertTrue(snap.hidden.isEmpty())
        assertEquals(5, snap.columns)
        assertFalse(snap.listMode)
        assertTrue(snap.showRail)
    }

    @Test
    fun persistsHideAndColumns() = runBlocking {
        val prefs = DrawerPrefs(context)
        prefs.hide("  ")
        prefs.hide("com.mail")
        prefs.setColumns(4)
        prefs.setListMode(true)
        prefs.setShowRail(false)
        val snap = prefs.snapshot.first()
        assertEquals(setOf("com.mail"), snap.hidden)
        assertEquals(4, snap.columns)
        assertTrue(snap.listMode)
        assertFalse(snap.showRail)
        prefs.show("com.mail")
        assertTrue(prefs.snapshot.first().hidden.isEmpty())
    }
}
