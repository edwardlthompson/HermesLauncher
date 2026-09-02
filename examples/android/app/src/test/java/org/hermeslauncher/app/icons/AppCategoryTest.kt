package org.hermeslauncher.app.icons

import org.junit.Assert.assertEquals
import org.junit.Test

class AppCategoryTest {
    @Test
    fun socialMapsFromManifestInt() {
        assertEquals(AppCategoryKind.SOCIAL, AppCategory.kind(4))
    }

    @Test
    fun undefinedIsOther() {
        assertEquals(AppCategoryKind.OTHER, AppCategory.kind(-1))
        assertEquals(AppCategoryKind.OTHER, AppCategory.kindOf("", 4))
    }

    @Test
    fun accessibilityAndGame() {
        assertEquals(AppCategoryKind.GAME, AppCategory.kind(0))
        assertEquals(AppCategoryKind.ACCESSIBILITY, AppCategory.kind(8))
    }
}
