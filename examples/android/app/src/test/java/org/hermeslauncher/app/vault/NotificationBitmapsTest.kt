package org.hermeslauncher.app.vault

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class NotificationBitmapsTest {
    @Test
    fun encodesPictureExtraWithoutStoragePermission() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        val notification = Notification.Builder(context, "inbox")
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("Ada")
            .setContentText("photo")
            .build()
        notification.extras.putParcelable(Notification.EXTRA_PICTURE, bitmap)
        val jpeg = NotificationBitmaps.jpeg(context, notification)
        assertTrue(jpeg.isNotEmpty())
        assertTrue(jpeg.size < ImageLimits.ORIGINAL_MAX_BYTES)
    }
}
