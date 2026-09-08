package org.hermeslauncher.app.icons

import org.junit.Assert.assertEquals
import org.junit.Test

class UsageRankerTest {
    @Test
    fun foregroundTimeBeatsRecentLowTime() {
        val recent = LaunchableApp("com.recent", "A", "Recent")
        val heavy = LaunchableApp("com.stale", "B", "Stale")
        val ranked = UsageRanker.rank(
            apps = listOf(recent, heavy),
            rows = listOf(
                UsageRow("com.stale", lastTimeUsed = 10L, totalTimeInForeground = 50_000L),
                UsageRow("com.recent", lastTimeUsed = 100L, totalTimeInForeground = 10L),
            ),
            slotCount = 2,
        )
        assertEquals(listOf("com.stale", "com.recent"), ranked.map { it.packageName })
    }

    @Test
    fun recencyBreaksForegroundTie() {
        val older = LaunchableApp("com.old", "A", "Old")
        val newer = LaunchableApp("com.new", "B", "New")
        val ranked = UsageRanker.rank(
            apps = listOf(older, newer),
            rows = listOf(
                UsageRow("com.old", lastTimeUsed = 10L, totalTimeInForeground = 50_000L),
                UsageRow("com.new", lastTimeUsed = 100L, totalTimeInForeground = 50_000L),
            ),
            slotCount = 2,
        )
        assertEquals(listOf("com.new", "com.old"), ranked.map { it.packageName })
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
