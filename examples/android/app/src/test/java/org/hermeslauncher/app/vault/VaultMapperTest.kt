package org.hermeslauncher.app.vault

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VaultMapperTest {
    private val posted = PostedNotification(
        sbnKey = "pkg|0|1|null|1000",
        packageName = "com.example.chat",
        postedAt = 1_700_000_000_000L,
        title = "Ada",
        text = "hello",
        type = VaultItemType.MESSAGE,
        imageByteSize = 1024,
    )

    @Test
    fun missingPolicyKeeps() {
        val decision = VaultMapper.decide(posted, null)
        assertEquals(PersistAction.PERSIST_TEXT, decision.action)
        val item = VaultMapper.toItem(posted, decision)!!
        assertTrue(item.contentStored)
        assertEquals(false, item.imagesStored)
    }

    @Test
    fun missingPolicyStoresImagesWhenPhotosOn() {
        val decision = VaultMapper.decide(posted, null, storePhotos = true)
        assertEquals(PersistAction.PERSIST_TEXT_AND_IMAGES, decision.action)
    }

    @Test
    fun defaultOffPolicySkips() {
        val decision = VaultMapper.decide(posted, AppStorePolicy("com.example.chat"))
        assertEquals(PersistAction.SKIP, decision.action)
    }

    @Test
    fun contentOnlyPersistsText() {
        val policy = AppStorePolicy("com.example.chat", storeContent = true, storeImages = false)
        val decision = VaultMapper.decide(posted, policy)
        assertEquals(PersistAction.PERSIST_TEXT, decision.action)
        val item = VaultMapper.toItem(posted, decision)!!
        assertTrue(item.contentStored)
        assertEquals(false, item.imagesStored)
    }

    @Test
    fun oversizeImageKeepsText() {
        val big = posted.copy(imageByteSize = ImageLimits.ORIGINAL_MAX_BYTES + 1)
        val policy = AppStorePolicy("com.example.chat", storeContent = true, storeImages = true)
        val decision = VaultMapper.decide(big, policy)
        assertEquals(PersistAction.PERSIST_TEXT, decision.action)
        assertEquals("original_over_cap", decision.skipImageReason)
        val item = VaultMapper.toItem(big, decision)!!
        assertEquals(false, item.imagesStored)
    }

    @Test
    fun grantedImagesPersistWhenUnderCap() {
        val policy = AppStorePolicy("com.example.chat", storeContent = true, storeImages = true)
        val decision = VaultMapper.decide(posted, policy)
        assertEquals(PersistAction.PERSIST_TEXT_AND_IMAGES, decision.action)
        val item = VaultMapper.toItem(posted, decision)!!
        assertTrue(item.imagesStored)
        assertEquals("${posted.sbnKey}:${posted.postedAt}", item.id)
    }

    @Test
    fun globalPhotosStoresImagesWhenPolicyTextOnly() {
        val policy = AppStorePolicy("com.example.chat", storeContent = true, storeImages = false)
        val decision = VaultMapper.decide(posted, policy, storePhotos = true)
        assertEquals(PersistAction.PERSIST_TEXT_AND_IMAGES, decision.action)
    }

    @Test
    fun ignoreOngoingSkipsPersistent() {
        val ongoing = posted.copy(ongoing = true)
        val policy = AppStorePolicy("com.example.chat", storeContent = true, storeImages = true)
        val decision = VaultMapper.decide(ongoing, policy, ignoreOngoing = true)
        assertEquals(PersistAction.SKIP, decision.action)
        assertEquals("ongoing", decision.skipImageReason)
        assertNull(VaultMapper.toItem(ongoing, decision))
    }

    @Test
    fun ignoreOngoingOffKeepsPersistent() {
        val ongoing = posted.copy(ongoing = true)
        val policy = AppStorePolicy("com.example.chat", storeContent = true, storeImages = false)
        val decision = VaultMapper.decide(ongoing, policy, ignoreOngoing = false)
        assertEquals(PersistAction.PERSIST_TEXT, decision.action)
    }

    @Test
    fun messagingPartsMapWhenGranted() {
        val withParts = posted.copy(
            messageParts = listOf(
                PostedMessagePart(sender = "Ada", text = "hello", timestamp = 9L),
            ),
        )
        val parts = VaultMapper.toParts(withParts, "item-1")
        assertEquals(1, parts.size)
        assertEquals("Ada", parts[0].sender)
        assertEquals("hello", parts[0].text)
        assertEquals(9L, parts[0].timestamp)
    }
}
