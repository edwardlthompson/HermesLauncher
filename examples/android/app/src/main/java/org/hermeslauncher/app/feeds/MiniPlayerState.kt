package org.hermeslauncher.app.feeds

data class MiniPlayerState(
    val episode: FeedItem? = null,
    val playing: Boolean = false,
) {
    fun load(item: FeedItem): MiniPlayerState {
        return copy(episode = item, playing = true)
    }

    fun toggle(): MiniPlayerState {
        if (episode == null) {
            return this
        }
        return copy(playing = !playing)
    }

    fun stop(): MiniPlayerState {
        return MiniPlayerState()
    }
}
