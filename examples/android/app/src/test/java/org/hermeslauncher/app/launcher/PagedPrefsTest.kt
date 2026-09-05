package org.hermeslauncher.app.launcher

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hermeslauncher.app.clearPreferenceDataStores
import org.hermeslauncher.app.workspace.LabsFlags
import org.hermeslauncher.app.workspace.QsbPlacement
import org.hermeslauncher.app.workspace.ScrollMode
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class PagedPrefsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun resetDataStore() {
        context.clearPreferenceDataStores()
    }

    @Test
    fun defaultsMatchLauncher3() = runBlocking {
        val prefs = PagedPrefs(context)
        assertEquals(LabsFlags(), prefs.labs.first())
        assertEquals(QsbPlacement.NONE, prefs.qsb.first())
        assertEquals(ScrollMode.ADJACENT, prefs.scrollMode.first())
    }
}
