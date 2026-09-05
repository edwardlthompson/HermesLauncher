package org.hermeslauncher.app.about

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class DonationsLoaderTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun loadsDonationsFromAssets() {
        val cfg = DonationsLoader.load(context)
        assertTrue(cfg.enabled)
        assertEquals("If Hermes is your home, a Venmo tip keeps this FOSS work going. Optional, never required.", cfg.message)
        assertEquals(1, cfg.links.size)
        assertEquals("Donate via Venmo", cfg.links[0].label)
        assertEquals(DonationsLoader.DEFAULT_VENMO_URL, cfg.links[0].url)
        assertEquals(DonationsLoader.DEFAULT_VENMO_URL, DonationsLoader.primaryUrl(cfg))
    }

    @Test
    fun defaultsExposeVenmoWhenAssetMissing() {
        val cfg = DonationsLoader.defaults()
        assertTrue(cfg.enabled)
        assertEquals(DonationsLoader.DEFAULT_VENMO_URL, DonationsLoader.primaryUrl(cfg))
        assertEquals(DonationsLoader.DEFAULT_LABEL, cfg.links[0].label)
    }
}
