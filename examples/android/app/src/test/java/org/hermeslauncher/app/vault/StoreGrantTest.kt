package org.hermeslauncher.app.vault

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreGrantTest {
    @Test
    fun skipWritesNothing() {
        assertNull(StoreGrant.policy("com.chat", storeContent = false, storeImages = false))
    }

    @Test
    fun textGrantDoesNotStoreImages() {
        val policy = StoreGrant.policy("com.chat", storeContent = true, storeImages = false)!!
        assertTrue(policy.storeContent)
        assertEquals(false, policy.storeImages)
    }

    @Test
    fun imagesGrantRequiresContent() {
        val policy = StoreGrant.policy("com.chat", storeContent = true, storeImages = true)!!
        assertTrue(policy.storeImages)
    }
}
