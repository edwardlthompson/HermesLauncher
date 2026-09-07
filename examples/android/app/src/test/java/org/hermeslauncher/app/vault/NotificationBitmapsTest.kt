package org.hermeslauncher.app.vault

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.io.File

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
        assertTrue(jpeg.bytes.isNotEmpty())
        assertTrue(jpeg.bytes.size < ImageLimits.ORIGINAL_MAX_BYTES)
        assertEquals(8, jpeg.width)
        assertEquals(8, jpeg.height)
        assertFalse(jpeg.fromLargeIcon)
    }

    @Test
    fun largeIconIsMarkedAsAvatar() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        val notification = Notification.Builder(context, "inbox")
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("Ada")
            .build()
        notification.extras.putParcelable(Notification.EXTRA_LARGE_ICON, bitmap)
        val jpeg = NotificationBitmaps.jpeg(context, notification)
        assertTrue(jpeg.fromLargeIcon)
        assertEquals(64, jpeg.width)
    }

    @Test
    fun uriPhotoBytesIncludeDecodedBounds() {
        val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
        val encoded = ByteArrayOutputStream()
        assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 85, encoded))
        val jpeg = NotificationBitmaps.jpegFromUriBytes(encoded.toByteArray())
        assertTrue(jpeg.bytes.isNotEmpty())
        assertEquals(400, jpeg.width)
        assertEquals(300, jpeg.height)
        assertFalse(jpeg.fromLargeIcon)
    }

    @Test
    fun messagingStyleUriPhotoKeepsDecodedBounds() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
        val encoded = ByteArrayOutputStream()
        assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 85, encoded))
        val file = File(context.cacheDir, "uri-photo.jpg")
        file.writeBytes(encoded.toByteArray())
        val person = Person.Builder().setName("Ada").build()
        val style = NotificationCompat.MessagingStyle(person).addMessage(
            NotificationCompat.MessagingStyle.Message("photo", 1L, person)
                .setData("image/jpeg", Uri.fromFile(file)),
        )
        val notification = NotificationCompat.Builder(context, "inbox")
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("Ada")
            .setStyle(style)
            .build()
        val jpeg = NotificationBitmaps.jpeg(context, notification)
        assertTrue(jpeg.bytes.isNotEmpty())
        assertEquals(400, jpeg.width)
        assertEquals(300, jpeg.height)
        assertFalse(jpeg.fromLargeIcon)
    }
}
