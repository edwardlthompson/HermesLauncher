package org.hermeslauncher.app.l3

import android.content.ContentValues
import android.util.Log
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherSettings
import com.android.launcher3.model.ModelDbController
import org.hermeslauncher.app.widgets.WidgetGridSpec

object DesktopReflowDb {
    private const val TAG = "HermesDesktopReflow"

    fun applyToDb(launcher: Launcher, spec: WidgetGridSpec) {
        val grid = spec.clamped()
        val db = launcher.model.modelDbController
        val items = loadDesktop(db)
        if (items.isEmpty()) {
            return
        }
        val next = DesktopReflow.reflow(items, grid.columns, grid.rows)
        val beforeById = items.associateBy { it.id }
        for (cell in next) {
            val prev = beforeById[cell.id] ?: continue
            if (prev == cell) {
                continue
            }
            val values = ContentValues().apply {
                put(LauncherSettings.Favorites.SCREEN, cell.screenId)
                put(LauncherSettings.Favorites.CELLX, cell.cellX)
                put(LauncherSettings.Favorites.CELLY, cell.cellY)
                put(LauncherSettings.Favorites.SPANX, cell.spanX)
                put(LauncherSettings.Favorites.SPANY, cell.spanY)
            }
            db.update(
                LauncherSettings.Favorites.TABLE_NAME,
                values,
                "${LauncherSettings.Favorites._ID}=?",
                arrayOf(cell.id.toString()),
            )
        }
        Log.i(TAG, "reflowed ${next.size} desktop cells to ${grid.columns}x${grid.rows}")
    }

    private fun loadDesktop(db: ModelDbController): List<DesktopCell> {
        val out = ArrayList<DesktopCell>()
        val cursor = db.query(
            LauncherSettings.Favorites.TABLE_NAME,
            arrayOf(
                LauncherSettings.Favorites._ID,
                LauncherSettings.Favorites.SCREEN,
                LauncherSettings.Favorites.CELLX,
                LauncherSettings.Favorites.CELLY,
                LauncherSettings.Favorites.SPANX,
                LauncherSettings.Favorites.SPANY,
            ),
            "${LauncherSettings.Favorites.CONTAINER}=?",
            arrayOf(LauncherSettings.Favorites.CONTAINER_DESKTOP.toString()),
            null,
        )
        cursor.use { c ->
            val idIdx = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID)
            val screenIdx = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN)
            val xIdx = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX)
            val yIdx = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY)
            val sxIdx = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANX)
            val syIdx = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANY)
            while (c.moveToNext()) {
                out += DesktopCell(
                    id = c.getLong(idIdx),
                    screenId = c.getInt(screenIdx),
                    cellX = c.getInt(xIdx),
                    cellY = c.getInt(yIdx),
                    spanX = c.getInt(sxIdx).coerceAtLeast(1),
                    spanY = c.getInt(syIdx).coerceAtLeast(1),
                )
            }
        }
        return out
    }
}
