package org.hermeslauncher.app.icons

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class IconBitmapLoaderTest {
    @Test
    fun cacheHitSkipsSecondDecode() = runBlocking {
        var calls = 0
        val loader = IconBitmapLoader<String>()
        val first = loader.load("sys|pkg|Act") {
            calls += 1
            "icon"
        }
        val second = loader.load("sys|pkg|Act") {
            calls += 1
            "other"
        }
        assertEquals("icon", first)
        assertSame(first, second)
        assertEquals(1, calls)
    }

    @Test
    fun blankKeyIsNullWithoutDecode() = runBlocking {
        var calls = 0
        val loader = IconBitmapLoader<String>()
        val out = loader.load("") {
            calls += 1
            "x"
        }
        assertEquals(null, out)
        assertEquals(0, calls)
    }

    @Test
    fun clearByPackPrefixKeepsOthers() = runBlocking {
        val loader = IconBitmapLoader<String>()
        loader.load("pack.a|one|A") { "a" }
        loader.load("pack.b|two|B") { "b" }
        loader.clear("pack.a")
        assertEquals(null, loader.peek("pack.a|one|A"))
        assertEquals("b", loader.peek("pack.b|two|B"))
    }

    @Test
    fun keyJoinsPackPackageActivity() {
        val pack = IconPackId("org.pack")
        val app = LaunchableApp("com.app", "com.app.Main", "App")
        assertEquals("org.pack|com.app|com.app.Main", IconBitmapLoader.key(pack, app))
    }
}
