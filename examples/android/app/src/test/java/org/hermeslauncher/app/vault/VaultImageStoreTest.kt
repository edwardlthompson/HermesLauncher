package org.hermeslauncher.app.vault

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.io.path.createTempDirectory

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class VaultImageStoreTest {
    @Test
    fun writesJpegUnderCap() {
        val dir = createTempDirectory("hermes-vault-img").toFile()
        val rel = VaultImageStore.write(dir, "pkg|0|1", byteArrayOf(1, 2, 3, 4))
        assertEquals("vault/images/pkg_0_1/preview.jpg", rel)
        val file = VaultImageStore.file(dir, rel)!!
        assertTrue(file.isFile)
        assertEquals(4, file.length())
    }

    @Test
    fun deleteRemovesItemDir() {
        val dir = createTempDirectory("hermes-vault-del").toFile()
        val rel = VaultImageStore.write(dir, "gone", byteArrayOf(1, 2, 3))
        assertTrue(VaultImageStore.file(dir, rel)!!.isFile)
        VaultImageStore.delete(dir, "gone")
        assertNull(VaultImageStore.file(dir, rel))
    }

    @Test
    fun skipsEmptyAndOversize() {
        val dir = File("build/tmp-vault-img").apply { mkdirs() }
        assertNull(VaultImageStore.write(dir, "id", byteArrayOf()))
        val huge = ByteArray((ImageLimits.ORIGINAL_MAX_BYTES + 1).toInt()) { 1 }
        assertNull(VaultImageStore.write(dir, "id", huge))
    }
}
