package org.hermeslauncher.app.feeds

/** Deep-link from a feed notification into News or Podcasts. */
data class PendingFeedOpen(
    val id: String,
    val kind: SubKind,
)
