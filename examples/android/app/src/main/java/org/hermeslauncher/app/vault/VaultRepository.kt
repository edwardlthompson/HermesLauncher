package org.hermeslauncher.app.vault

import android.content.Context
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

class VaultRepository(
    context: Context,
    private val inboxPrefs: InboxPrefs = InboxPrefs(context),
) {
    private val appContext = context.applicationContext
    private val filesDir = appContext.filesDir
    private val opened = VaultOpener.open(context)
    private val db = when (opened) {
        is VaultOpenResult.Ok -> opened.db
        is VaultOpenResult.Rebuild -> opened.db
    }
    private val dao = db.vaultDao()
    val rebuildRequired: StateFlow<Boolean> = MutableStateFlow(opened is VaultOpenResult.Rebuild)
    @Volatile
    private var lastPruneAt: Long = 0L

    val visibleItems: Flow<List<VaultItem>> = dao.visibleItemsFlow()
    val archivedItems: Flow<List<VaultItem>> = dao.archivedItemsFlow()
    val policies: Flow<List<AppStorePolicy>> = dao.policiesFlow()

    suspend fun onPosted(context: Context, sbn: StatusBarNotification) {
        val ignoreOngoing = inboxPrefs.ignoreOngoing.first()
        if (ignoreOngoing && sbn.isOngoing) {
            return
        }
        onPosted(StatusBarNotificationMapper.map(context, sbn))
    }

    suspend fun onPosted(posted: PostedNotification) {
        val ignoreOngoing = inboxPrefs.ignoreOngoing.first()
        if (ignoreOngoing && posted.ongoing) {
            return
        }
        persist(posted, dao.policyFor(posted.packageName), ignoreOngoing)
    }

    suspend fun archive(id: String) {
        val item = dao.itemById(id) ?: return
        if (!item.archived) {
            dao.updateItem(item.copy(archived = true, unread = false))
        }
        ShadeBridge.dismiss(item.sbnKey)
    }

    suspend fun archiveBySbnKey(key: String) {
        if (key.isBlank()) {
            return
        }
        dao.visibleBySbnKey(key).forEach { item ->
            dao.updateItem(item.copy(archived = true, unread = false))
        }
    }

    suspend fun open(id: String) {
        val item = dao.itemById(id) ?: return
        ShadeBridge.open(item, appContext)
    }

    fun actions(sbnKey: String): List<ShadeAction> {
        return ShadeBridge.actions(sbnKey)
    }

    suspend fun runAction(id: String, index: Int) {
        val item = dao.itemById(id) ?: return
        ShadeBridge.runAction(item.sbnKey, index)
    }

    suspend fun togglePin(id: String) {
        val item = dao.itemById(id) ?: return
        dao.updateItem(item.copy(pinned = !item.pinned))
    }

    suspend fun blacklist(packageName: String) {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) {
            return
        }
        dao.insertPolicy(AppStorePolicy(pkg, storeContent = false, storeImages = false))
    }

    suspend fun unblacklist(packageName: String) {
        dao.deletePolicy(packageName)
    }

    private suspend fun persist(
        posted: PostedNotification,
        policy: AppStorePolicy?,
        ignoreOngoing: Boolean,
    ) {
        val storePhotos = inboxPrefs.storePhotos.first()
        val decision = VaultMapper.decide(posted, policy, ignoreOngoing, storePhotos)
        val item = VaultMapper.toItem(posted, decision) ?: return
        if (dao.itemById(item.id) != null) {
            return
        }
        dao.deleteParts(item.id)
        var stored = item
        if (decision.action == PersistAction.PERSIST_TEXT_AND_IMAGES) {
            val ref = VaultImageStore.write(filesDir, item.id, posted.imageBytes)
            stored = if (ref != null) {
                item.copy(extrasJson = VaultPreview.parse(item.extrasJson).withImage(ref).encode())
            } else {
                item.copy(imagesStored = false)
            }
        }
        Log.i(
            VaultImageStore.TAG,
            "persist pkg=${posted.packageName} action=${decision.action} images=${stored.imagesStored}",
        )
        dao.insertItem(stored)
        VaultMapper.toParts(posted, item.id).forEach { part ->
            dao.insertPart(part)
        }
        prune(force = false)
    }

    suspend fun prune(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!VaultPrune.shouldRun(now, lastPruneAt, force)) {
            return
        }
        lastPruneAt = now
        VaultPrune.run(
            dao = dao,
            filesDir = filesDir,
            maxItems = inboxPrefs.maxItems.first(),
            autoDeleteDays = inboxPrefs.autoDeleteDays.first(),
            autoDelete = inboxPrefs.autoDelete.first(),
        )
    }
}
