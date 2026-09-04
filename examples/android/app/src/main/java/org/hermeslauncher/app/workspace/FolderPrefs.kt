package org.hermeslauncher.app.workspace

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.folderDataStore by preferencesDataStore(name = "folder_prefs")

private val PREVIEW = stringPreferencesKey("preview")
private val FULLSCREEN = booleanPreferencesKey("fullscreen")

object FolderCodec {
    fun encodePreview(kind: FolderPreviewKind): String = kind.name

    fun decodePreview(raw: String?): FolderPreviewKind {
        return FolderPreviewKind.entries.firstOrNull { it.name == raw } ?: FolderPreviewKind.STACK
    }
}

data class FolderSnapshot(
    val preview: FolderPreviewKind = FolderPreviewKind.STACK,
    val fullscreen: Boolean = false,
)

class FolderPrefs(private val context: Context) {
    val snapshot: Flow<FolderSnapshot> = context.folderDataStore.data.map { prefs ->
        FolderSnapshot(
            preview = FolderCodec.decodePreview(prefs[PREVIEW]),
            fullscreen = prefs[FULLSCREEN] ?: false,
        )
    }

    suspend fun setPreview(kind: FolderPreviewKind) {
        context.folderDataStore.edit { prefs -> prefs[PREVIEW] = FolderCodec.encodePreview(kind) }
    }

    suspend fun setFullscreen(value: Boolean) {
        context.folderDataStore.edit { prefs -> prefs[FULLSCREEN] = value }
    }
}
