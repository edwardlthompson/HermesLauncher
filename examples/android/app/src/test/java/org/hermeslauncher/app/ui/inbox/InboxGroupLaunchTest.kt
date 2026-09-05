package org.hermeslauncher.app.ui.inbox

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InboxGroupLaunchTest {
    @Test
    fun launchOnlyWhenExpandedAppGroup() {
        assertTrue(showGroupLaunch(expanded = true, packageName = "eu.faircode.email"))
        assertFalse(showGroupLaunch(expanded = false, packageName = "eu.faircode.email"))
        assertFalse(showGroupLaunch(expanded = true, packageName = ""))
        assertFalse(showGroupLaunch(expanded = true, packageName = "   "))
    }
}
