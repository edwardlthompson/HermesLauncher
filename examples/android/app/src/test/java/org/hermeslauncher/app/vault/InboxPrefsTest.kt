package org.hermeslauncher.app.vault

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hermeslauncher.app.clearPreferenceDataStores
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class InboxPrefsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun resetDataStore() {
        context.clearPreferenceDataStores()
    }

    @Test
    fun defaultsToIgnoringOngoing() = runBlocking {
        assertEquals(true, InboxPrefs(context).ignoreOngoing.first())
    }

    @Test
    fun persistsToggle() = runBlocking {
        val prefs = InboxPrefs(context)
        prefs.setIgnoreOngoing(false)
        assertEquals(false, prefs.ignoreOngoing.first())
    }

    @Test
    fun defaultsToStoringPhotos() = runBlocking {
        assertEquals(true, InboxPrefs(context).storePhotos.first())
    }

    @Test
    fun persistsStorePhotosToggle() = runBlocking {
        val prefs = InboxPrefs(context)
        prefs.setStorePhotos(false)
        assertEquals(false, prefs.storePhotos.first())
    }

    @Test
    fun defaultsRetention() = runBlocking {
        val prefs = InboxPrefs(context)
        assertEquals(2000, prefs.maxItems.first())
        assertEquals(30, prefs.autoDeleteDays.first())
        assertEquals(true, prefs.autoDelete.first())
    }
}
