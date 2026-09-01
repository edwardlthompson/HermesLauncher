package org.hermeslauncher.app.push

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UnifiedPushConfigTest {
    @Test
    fun staysFossUntilDistributorExists() {
        assertNull(UnifiedPushConfig.endpointUrl())
        assertFalse(UnifiedPushConfig.usesProprietaryPush())
        assertTrue(UnifiedPushConfig.CONNECTOR_ACTION.startsWith("org.unifiedpush."))
    }
}
