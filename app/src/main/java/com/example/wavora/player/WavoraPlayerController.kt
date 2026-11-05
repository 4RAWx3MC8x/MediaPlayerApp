package com.example.wavora.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.wavora.data.MediaItem
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayerState(
    val currentItem: MediaItem? = null,
    val isPlaying: Boolean = false,
    val position: Long = 0L,
    val duration: Long = 0L,
    val queue: List<MediaItem> = emptyList(),
    val currentIndex: Int = 0,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val shuffleEnabled: Boolean = false,
)

/**
 * Singleton-like player controller that wraps Media3 MediaController.
 */
class WavoraPlayerController(context: Context) {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>
    private var controller: MediaController? = null

    private val appContext = context.applicationContext

    init {
        val sessionToken = SessionToken(
            appContext,
            ComponentName(appContext, WavoraPlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(appContext, sessionToken).buildAsync()
        controllerFuture.addListener({
            controller = controllerFuture.get()
            controller?.addListener(playerListener)
        }, appContext.mainExecutor)
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.value = _state.value.copy(isPlaying = isPlaying)
        }

        override fun onMediaItemTransition(mediaItem: Media3Item?, reason: Int) {
            val queue = _state.value.queue
            val index = controller?.currentMediaItemIndex ?: 0
            val foundItem = queue.getOrNull(index) ?: mediaItem?.let { m3 ->
                val meta = m3.mediaMetadata
                MediaItem(
                    id = m3.mediaId.toLongOrNull() ?: 0L,
                    title = meta.title?.toString() ?: "Unknown Title",
                    artist = meta.artist?.toString() ?: "Unknown Artist",
                    album = meta.albumTitle?.toString() ?: "Unknown Album",
                    albumArtUri = meta.artworkUri,
                    contentUri = m3.requestMetadata.mediaUri ?: android.net.Uri.EMPTY,
                    duration = controller?.duration?.takeIf { it > 0 } ?: 0L,
                    mediaType = com.example.wavora.data.MediaType.AUDIO,
                    dateAdded = System.currentTimeMillis()
                )
            }
            _state.value = _state.value.copy(
                currentItem = foundItem,
                currentIndex = index,
            )
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val dur = controller?.duration?.takeIf { it > 0 } ?: 0L
            _state.value = _state.value.copy(duration = dur)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _state.value = _state.value.copy(repeatMode = repeatMode)
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _state.value = _state.value.copy(shuffleEnabled = shuffleModeEnabled)
        }
    }

    fun playQueue(items: List<MediaItem>, startIndex: Int = 0) {
        val ctrl = controller ?: return
        val media3Items = items.map { item ->
            Media3Item.Builder()
                .setUri(item.contentUri)
                .setMediaId(item.id.toString())
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(item.title)
                        .setArtist(item.artist)
                        .setAlbumTitle(item.album)
                        .setArtworkUri(item.albumArtUri)
                        .setMediaType(androidx.media3.common.MediaMetadata.MEDIA_TYPE_MUSIC)
                        .setIsPlayable(true)
                        .setIsBrowsable(false)
                        .build()
                )
                .build()
        }
        ctrl.setMediaItems(media3Items, startIndex, 0L)
        ctrl.prepare()
        ctrl.play()
        _state.value = _state.value.copy(
            queue = items,
            currentItem = items.getOrNull(startIndex),
            currentIndex = startIndex,
        )
    }

    fun play(item: MediaItem, queue: List<MediaItem> = listOf(item)) {
        val index = queue.indexOf(item).coerceAtLeast(0)
        playQueue(queue, index)
    }

    fun playSingle(item: MediaItem) = playQueue(listOf(item), 0)

    fun togglePlayPause() {
        val ctrl = controller ?: return
        if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
    }

    fun seekTo(position: Long) {
        controller?.seekTo(position)
        _state.value = _state.value.copy(position = position)
    }

    fun skipNext() { controller?.seekToNextMediaItem() }
    fun skipPrevious() { controller?.seekToPreviousMediaItem() }

    fun clearQueue() {
        val ctrl = controller ?: return
        ctrl.clearMediaItems()
        _state.value = _state.value.copy(
            queue = emptyList(),
            currentItem = null,
            currentIndex = 0,
            isPlaying = false,
            position = 0L,
            duration = 0L
        )
    }

    fun toggleRepeat() {
        val ctrl = controller ?: return
        val next = when (ctrl.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        ctrl.repeatMode = next
    }

    fun toggleShuffle() {
        val ctrl = controller ?: return
        ctrl.shuffleModeEnabled = !ctrl.shuffleModeEnabled
    }

    fun getCurrentPosition(): Long = controller?.currentPosition ?: 0L

    fun release() {
        MediaController.releaseFuture(controllerFuture)
    }
}
