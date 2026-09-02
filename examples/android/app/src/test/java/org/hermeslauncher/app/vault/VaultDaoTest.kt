package org.hermeslauncher.app.vault

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class VaultDaoTest {
    private lateinit var db: VaultDatabase
    private lateinit var dao: VaultDao

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, VaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.vaultDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertRoundTrip() = runBlocking {
        val posted = PostedNotification(
            sbnKey = "key",
            packageName = "com.example.chat",
            postedAt = 42L,
            title = "Ada",
            text = "hello",
            type = VaultItemType.MESSAGE,
        )
        val policy = AppStorePolicy("com.example.chat", storeContent = true)
        val item = VaultMapper.toItem(posted, VaultMapper.decide(posted, policy))!!
        dao.insertPolicy(policy)
        dao.insertItem(item)
        val stored = dao.itemById(item.id)!!
        assertEquals("Ada", stored.title)
        assertTrue(dao.visibleItems().any { it.id == item.id })
        assertEquals(true, dao.policyFor("com.example.chat")?.storeContent)
    }

    @Test
    fun archiveBySbnKeyHidesVisibleRow() = runBlocking {
        val posted = PostedNotification(
            sbnKey = "shade-key",
            packageName = "com.example.chat",
            postedAt = 7L,
            title = "Ada",
            type = VaultItemType.MESSAGE,
        )
        val item = VaultMapper.toItem(posted, VaultMapper.decide(posted, null))!!
        dao.insertItem(item)
        assertEquals(1, dao.visibleBySbnKey("shade-key").size)
        val stored = dao.visibleBySbnKey("shade-key").first()
        dao.updateItem(stored.copy(archived = true, unread = false))
        assertTrue(dao.visibleBySbnKey("shade-key").isEmpty())
    }
}
