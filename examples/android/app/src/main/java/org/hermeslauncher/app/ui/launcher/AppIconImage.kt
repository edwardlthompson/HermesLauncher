package org.hermeslauncher.app.ui.launcher

import android.content.ComponentName
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.icons.IconBitmapLoader
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.icons.IconPackResources
import org.hermeslauncher.app.icons.LaunchableApp

@Composable
fun AppIconImage(
    app: LaunchableApp,
    pack: IconPackId,
    modifier: Modifier = Modifier,
    contentDescription: String? = app.label,
) {
    val context = LocalContext.current
    val loader = (context.applicationContext as HermesApplication).iconLoader
    val key = IconBitmapLoader.key(pack, app)
    val image by produceState<ImageBitmap?>(null, key) {
        val bitmap: Bitmap? = loader.load(key) {
            runCatching {
                val drawable = IconPackResources.drawable(context, pack, app)
                    ?: context.packageManager.getActivityIcon(
                        ComponentName(app.packageName, app.activityName),
                    )
                drawable.toBitmap()
            }.getOrNull()
        }
        value = bitmap?.asImageBitmap()
    }
    if (image != null) {
        Image(bitmap = image!!, contentDescription = contentDescription, modifier = modifier)
    } else {
        Icon(Icons.Filled.Apps, contentDescription = contentDescription, modifier = modifier)
    }
}
