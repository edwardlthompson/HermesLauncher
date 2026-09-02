package org.hermeslauncher.app.icons

import org.junit.Assert.assertEquals
import org.junit.Test

class UsageRankerTest {
    @Test
    fun recencyBeatsStaleHighTime() {
        val recent = LaunchableApp("com.recent", "A", "Recent")
        val stale = LaunchableApp("com.stale", "B", "Stale")
        val ranked = UsageRanker.rank(
            apps = listOf(stale, recent),
            rows = listOf(
                UsageRow("com.stale", lastTimeUsed = 10L, totalTimeInForeground = 50_000L),
                UsageRow("com.recent", lastTimeUsed = 100L, totalTimeInForeground = 10L),
            ),
            slotCount = 2,
        )
        assertEquals(listOf("com.recent", "com.stale"), ranked.map { it.packageName })
    }

    @Test
    fun fillsOnlySlotCount() {
        val apps = listOf(
            LaunchableApp("a", "A", "A"),
            LaunchableApp("b", "B", "B"),
            LaunchableApp("c", "C", "C"),
        )
        val ranked = UsageRanker.rank(apps, emptyList(), slotCount = 2)
        assertEquals(2, ranked.size)
    }
}
