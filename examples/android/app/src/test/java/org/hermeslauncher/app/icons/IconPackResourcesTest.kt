package org.hermeslauncher.app.icons

import org.junit.Assert.assertEquals
import org.junit.Test

class IconPackResourcesTest {
    @Test
    fun drawableNameIsStable() {
        val app = LaunchableApp("com.mail.app", "com.mail.app.Inbox", "Mail")
        assertEquals("com_mail_app_inbox", IconPackResources.drawableName(app))
    }
}
