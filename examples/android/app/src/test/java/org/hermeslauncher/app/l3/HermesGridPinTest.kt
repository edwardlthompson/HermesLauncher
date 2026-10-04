package org.hermeslauncher.app.l3

import org.hermeslauncher.app.widgets.WidgetGridSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class HermesGridPinTest {
    @Test
    fun unsetPrefsReturnNull() {
        val context = RuntimeEnvironment.getApplication()
        assertNull(HermesGridPin.read(context))
    }

    @Test
    fun writeThenReadRoundTrip() {
        val context = RuntimeEnvironment.getApplication()
        HermesGridPin.write(context, WidgetGridSpec(6, 6))
        assertEquals(WidgetGridSpec(6, 6), HermesGridPin.read(context))
    }
}
