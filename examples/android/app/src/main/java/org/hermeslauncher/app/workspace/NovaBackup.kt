package org.hermeslauncher.app.workspace

import android.content.Context
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.provider.MediaStore
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

data class NovaImport(
    val desktop: DesktopLayout,
    val dock: org.hermeslauncher.app.icons.DockLayout,
    val shortcuts: Int,
)

object NovaBackup {
    const val NOVA_PACKAGE: String = "com.teslacoilsw.launcher"
    const val NOVA_PRIME: String = "com.teslacoilsw.launcher.prime"

    fun installed(pm: PackageManager): Boolean {
        return listOf(NOVA_PACKAGE, NOVA_PRIME).any { pkg ->
            runCatching { pm.getPackageInfo(pkg, 0) }.isSuccess
        }
    }

    fun findLatest(context: Context): ByteArray? {
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED)
        val picked = context.contentResolver.query(
            uri,
            projection,
            "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?",
            arrayOf("%.novabackup"),
            "${MediaStore.MediaColumns.DATE_MODIFIED} DESC",
        ) ?: return null
        picked.use { cursor ->
            if (!cursor.moveToFirst()) {
                return null
            }
            val id = cursor.getLong(0)
            val fileUri = android.content.ContentUris.withAppendedId(uri, id)
            return context.contentResolver.openInputStream(fileUri)?.use { it.readBytes() }
        }
    }

    fun read(input: InputStream): NovaImport? {
        val dbFile = File.createTempFile("nova", ".db")
        try {
            ZipInputStream(input).use { zip ->
                var entry = zip.nextEntry
                var copied = false
                while (entry != null) {
                    val name = entry.name.substringAfterLast('/').lowercase()
                    if (name == "nova.db" || name.endsWith(".db")) {
                        dbFile.outputStream().use { zip.copyTo(it) }
                        copied = true
                        break
                    }
                    entry = zip.nextEntry
                }
                if (!copied) {
                    return null
                }
            }
            val rows = queryFavorites(dbFile) ?: return null
            val desktop = NovaLayout.desktop(rows)
            val dock = NovaLayout.dock(rows)
            val count = desktop.byPage.values.sumOf { it.size } + dock.assigned.size
            if (count == 0) {
                return null
            }
            return NovaImport(desktop, dock, count)
        } finally {
            dbFile.delete()
        }
    }

    private fun queryFavorites(dbFile: File): List<NovaFavorite>? {
        val db = runCatching {
            SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        }.getOrNull() ?: return null
        db.use { sqlite ->
            val cursor = runCatching {
                sqlite.query("favorites", null, null, null, null, null, null)
            }.getOrNull() ?: return null
            cursor.use {
                val rows = mutableListOf<NovaFavorite>()
                val title = it.getColumnIndex("title")
                val intent = it.getColumnIndex("intent")
                val container = it.getColumnIndex("container")
                val screen = it.getColumnIndex("screen")
                val cellX = it.getColumnIndex("cellX")
                val cellY = it.getColumnIndex("cellY")
                val itemType = it.getColumnIndex("itemType")
                if (intent < 0 || container < 0 || screen < 0 || cellX < 0 || cellY < 0 || itemType < 0) {
                    return null
                }
                while (it.moveToNext()) {
                    rows.add(
                        NovaFavorite(
                            title = if (title >= 0) it.getString(title).orEmpty() else "",
                            intent = it.getString(intent).orEmpty(),
                            container = it.getInt(container),
                            screen = it.getInt(screen),
                            cellX = it.getInt(cellX),
                            cellY = it.getInt(cellY),
                            itemType = it.getInt(itemType),
                        ),
                    )
                }
                return rows
            }
        }
    }
}
