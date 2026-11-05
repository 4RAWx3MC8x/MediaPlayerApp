package com.example.wavora.player

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.wavora.MainActivity
import com.example.wavora.R
import com.example.wavora.data.EqualizerState
import com.example.wavora.data.MediaRepository
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Android Auto & Media3 MediaLibraryService.
 * Implements in-car browsing hierarchy matching Android Auto standards:
 * - Top Tabs: Library, Playlists, Albums, Folders Hierarchy
 * - Library contains: Shuffle All, All Songs, Folders, Folders Hierarchy
 * - Full Now Playing metadata with Artwork, Title, Artist, Seekbar, Queue
 * - In-Car Voice/Text Search support
 */
class WavoraPlaybackService : MediaLibraryService() {

    private var mediaLibrarySession: MediaLibrarySession? = null
    private lateinit var player: ExoPlayer
    private lateinit var repository: MediaRepository
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        const val ROOT_ID = "root"

        // Top Tabs in Android Auto
        const val TAB_LIBRARY = "tab_library"
        const val TAB_BY_LETTER = "tab_by_letter"
        const val TAB_PLAYLISTS = "tab_playlists"
        const val TAB_ALBUMS = "tab_albums"
        const val TAB_FOLDERS = "tab_folders"

        // Items inside Library Tab
        const val ITEM_SHUFFLE_ALL = "item_shuffle_all"
        const val ITEM_BY_LETTER = "item_by_letter"
        const val ITEM_ALL_SONGS = "item_all_songs"
        const val ITEM_FOLDERS = "item_folders"
        const val ITEM_FOLDERS_HIERARCHY = "item_folders_hierarchy"
        const val TAB_VIDEOS = "tab_videos"

        // Custom Layout Actions for Android Auto (Ordered: Shuffle -> Close -> Repeat -> Overflow)
        const val CUSTOM_ACTION_SHUFFLE = "com.example.wavora.ACTION_SHUFFLE"
        const val CUSTOM_ACTION_CLOSE = "com.example.wavora.ACTION_CLOSE"
        const val CUSTOM_ACTION_REPEAT = "com.example.wavora.ACTION_REPEAT"
        const val CUSTOM_ACTION_OVERFLOW = "com.example.wavora.ACTION_OVERFLOW"

        val audioFxManager = AudioFxManager()

        var activePlayer: ExoPlayer? = null
        var activeSession: MediaLibrarySession? = null

        fun applyEqualizerState(state: EqualizerState) {
            audioFxManager.applyState(state)
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = MediaRepository(this)

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        activePlayer = player

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    audioFxManager.initSession(player.audioSessionId)
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                mediaLibrarySession?.let { session ->
                    session.connectedControllers.forEach { controller ->
                        session.setCustomLayout(controller, buildCustomLayout())
                    }
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                mediaLibrarySession?.let { session ->
                    session.connectedControllers.forEach { controller ->
                        session.setCustomLayout(controller, buildCustomLayout())
                    }
                }
            }
        })

        val sessionIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val callback = object : MediaLibrarySession.Callback {

            override fun onConnect(
                session: MediaSession,
                controller: MediaSession.ControllerInfo
            ): MediaSession.ConnectionResult {
                val connectionResult = super.onConnect(session, controller)
                val sessionCommands = connectionResult.availableSessionCommands.buildUpon()
                    .add(SessionCommand(SessionCommand.COMMAND_CODE_LIBRARY_GET_LIBRARY_ROOT))
                    .add(SessionCommand(SessionCommand.COMMAND_CODE_LIBRARY_GET_CHILDREN))
                    .add(SessionCommand(SessionCommand.COMMAND_CODE_LIBRARY_GET_ITEM))
                    .add(SessionCommand(SessionCommand.COMMAND_CODE_LIBRARY_SEARCH))
                    .add(SessionCommand(SessionCommand.COMMAND_CODE_LIBRARY_GET_SEARCH_RESULT))
                    .add(SessionCommand(CUSTOM_ACTION_SHUFFLE, Bundle.EMPTY))
                    .add(SessionCommand(CUSTOM_ACTION_CLOSE, Bundle.EMPTY))
                    .add(SessionCommand(CUSTOM_ACTION_REPEAT, Bundle.EMPTY))
                    .add(SessionCommand(CUSTOM_ACTION_OVERFLOW, Bundle.EMPTY))
                    .build()
                val playerCommands = connectionResult.availablePlayerCommands.buildUpon()
                    .addAll(
                        Player.COMMAND_PLAY_PAUSE,
                        Player.COMMAND_PREPARE,
                        Player.COMMAND_STOP,
                        Player.COMMAND_SEEK_TO_NEXT,
                        Player.COMMAND_SEEK_TO_PREVIOUS,
                        Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                        Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                        Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM,
                        Player.COMMAND_SET_MEDIA_ITEM,
                        Player.COMMAND_SET_SHUFFLE_MODE,
                        Player.COMMAND_SET_REPEAT_MODE,
                        Player.COMMAND_GET_CURRENT_MEDIA_ITEM,
                        Player.COMMAND_GET_TIMELINE,
                    )
                    .build()
                return MediaSession.ConnectionResult.accept(sessionCommands, playerCommands)
            }

            override fun onPostConnect(
                session: MediaSession,
                controller: MediaSession.ControllerInfo
            ) {
                super.onPostConnect(session, controller)
                session.setCustomLayout(controller, buildCustomLayout())
            }

            override fun onCustomCommand(
                session: MediaSession,
                controller: MediaSession.ControllerInfo,
                customCommand: SessionCommand,
                args: Bundle
            ): ListenableFuture<SessionResult> {
                when (customCommand.customAction) {
                    CUSTOM_ACTION_SHUFFLE -> {
                        player.shuffleModeEnabled = !player.shuffleModeEnabled
                        session.setCustomLayout(controller, buildCustomLayout())
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                    CUSTOM_ACTION_CLOSE -> {
                        player.stop()
                        player.clearMediaItems()
                        stopSelf()
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                    CUSTOM_ACTION_REPEAT -> {
                        val nextMode = when (player.repeatMode) {
                            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                            else -> Player.REPEAT_MODE_OFF
                        }
                        player.repeatMode = nextMode
                        session.setCustomLayout(controller, buildCustomLayout())
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                    CUSTOM_ACTION_OVERFLOW -> {
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                }
                return super.onCustomCommand(session, controller, customCommand, args)
            }

            override fun onGetLibraryRoot(
                session: MediaLibrarySession,
                browser: MediaSession.ControllerInfo,
                params: LibraryParams?
            ): ListenableFuture<LibraryResult<Media3Item>> {
                val rootItem = Media3Item.Builder()
                    .setMediaId(ROOT_ID)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setIsBrowsable(true)
                            .setIsPlayable(false)
                            .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                            .setTitle("Wavora Music")
                            .build()
                    )
                    .build()

                val rootExtras = Bundle().apply {
                    putBoolean("android.media.browse.SEARCH_SUPPORTED", true)
                    putInt("android.media.browse.CONTENT_STYLE_BROWSABLE_HINT", 1) // 1 = List, 2 = Grid
                    putInt("android.media.browse.CONTENT_STYLE_PLAYABLE_HINT", 1)
                    putBoolean("android.media.playback.ALWAYS_RESERVE_SPACE_FOR.ACTION_SKIP_TO_PREVIOUS", true)
                    putBoolean("android.media.playback.ALWAYS_RESERVE_SPACE_FOR.ACTION_SKIP_TO_NEXT", true)
                }

                val libraryParams = LibraryParams.Builder()
                    .setExtras(rootExtras)
                    .build()

                return Futures.immediateFuture(LibraryResult.ofItem(rootItem, libraryParams))
            }

            override fun onGetChildren(
                session: MediaLibrarySession,
                browser: MediaSession.ControllerInfo,
                parentId: String,
                page: Int,
                pageSize: Int,
                params: LibraryParams?
            ): ListenableFuture<LibraryResult<ImmutableList<Media3Item>>> {
                val future = com.google.common.util.concurrent.SettableFuture.create<LibraryResult<ImmutableList<Media3Item>>>()

                serviceScope.launch(Dispatchers.IO) {
                    try {
                        val children = mutableListOf<Media3Item>()
                        when (parentId) {
                            ROOT_ID -> {
                                // Top Tabs displayed in the Android Auto Header
                                children.add(
                                    createBrowsableFolder(
                                        id = TAB_LIBRARY,
                                        title = "Library",
                                        subtitle = "Audio collections",
                                        folderType = MediaMetadata.FOLDER_TYPE_MIXED
                                    )
                                )
                                children.add(
                                    createBrowsableFolder(
                                        id = TAB_PLAYLISTS,
                                        title = "Playlists",
                                        subtitle = "Custom playlists",
                                        folderType = MediaMetadata.FOLDER_TYPE_PLAYLISTS
                                    )
                                )
                                children.add(
                                    createBrowsableFolder(
                                        id = TAB_ALBUMS,
                                        title = "Albums",
                                        subtitle = "Albums by artist",
                                        folderType = MediaMetadata.FOLDER_TYPE_ALBUMS
                                    )
                                )
                                children.add(
                                    createBrowsableFolder(
                                        id = TAB_FOLDERS,
                                        title = "Folders",
                                        subtitle = "Device directory structure",
                                        folderType = MediaMetadata.FOLDER_TYPE_MIXED
                                    )
                                )
                                children.add(
                                    createBrowsableFolder(
                                        id = TAB_VIDEOS,
                                        title = "Videos",
                                        subtitle = "Video files on device",
                                        folderType = MediaMetadata.FOLDER_TYPE_MIXED
                                    )
                                )
                            }

                            TAB_LIBRARY -> {
                                // Inside Library Tab (as shown in Android Auto UI)
                                // "Shuffle All" playable action
                                children.add(
                                    Media3Item.Builder()
                                        .setMediaId(ITEM_SHUFFLE_ALL)
                                        .setMediaMetadata(
                                            MediaMetadata.Builder()
                                                .setTitle("Shuffle All")
                                                .setSubtitle("Play all tracks randomly")
                                                .setIsPlayable(true)
                                                .setIsBrowsable(false)
                                                .build()
                                        )
                                        .build()
                                )
                                children.add(
                                    createBrowsableFolder(
                                        id = ITEM_ALL_SONGS,
                                        title = "All Songs",
                                        subtitle = "Every song on device (A - Z)",
                                        folderType = MediaMetadata.FOLDER_TYPE_TITLES
                                    )
                                )
                                children.add(
                                    createBrowsableFolder(
                                        id = ITEM_FOLDERS,
                                        title = "Folders",
                                        subtitle = "Device storage folders",
                                        folderType = MediaMetadata.FOLDER_TYPE_MIXED
                                    )
                                )
                                children.add(
                                    createBrowsableFolder(
                                        id = ITEM_FOLDERS_HIERARCHY,
                                        title = "Folders Hierarchy",
                                        subtitle = "Nested directories",
                                        folderType = MediaMetadata.FOLDER_TYPE_MIXED
                                    )
                                )
                            }

                            TAB_BY_LETTER, ITEM_BY_LETTER -> {
                                val songs = repository.getAudioItems()
                                val letterCounts = mutableMapOf<String, Int>()
                                songs.forEach { song ->
                                    val firstChar = song.title.trim().firstOrNull()?.toString()?.uppercase() ?: "#"
                                    val key = when {
                                        firstChar in listOf("أ", "إ", "آ", "ا") -> "أ"
                                        firstChar in "بتثجحخدذرزسشصضطظعغفقكلمنهوي" -> firstChar
                                        firstChar.firstOrNull() in 'A'..'Z' -> firstChar
                                        else -> "#"
                                    }
                                    letterCounts[key] = (letterCounts[key] ?: 0) + 1
                                }
                                letterCounts.keys.sortedWith { a, b ->
                                    when {
                                        a == "#" -> 1
                                        b == "#" -> -1
                                        else -> a.compareTo(b)
                                    }
                                }.forEach { letter ->
                                    val count = letterCounts[letter] ?: 0
                                    children.add(
                                        createBrowsableFolder(
                                            id = "letter_$letter",
                                            title = "Letter $letter ($letter)",
                                            subtitle = "$count songs",
                                            folderType = MediaMetadata.FOLDER_TYPE_TITLES
                                        )
                                    )
                                }
                            }

                            ITEM_ALL_SONGS -> {
                                val songs = repository.getAudioItems().sortedBy { it.title.lowercase() }
                                songs.forEach { children.add(convertSongToMedia3Item(it)) }
                            }

                            TAB_ALBUMS -> {
                                val songs = repository.getAudioItems()
                                val albums = repository.groupByAlbum(songs)
                                albums.forEach { album ->
                                    children.add(
                                        Media3Item.Builder()
                                            .setMediaId("album_${album.name}")
                                            .setMediaMetadata(
                                                MediaMetadata.Builder()
                                                    .setTitle(album.name)
                                                    .setArtist(album.artist)
                                                    .setArtworkUri(album.artUri)
                                                    .setIsBrowsable(true)
                                                    .setIsPlayable(false)
                                                    .setFolderType(MediaMetadata.FOLDER_TYPE_ALBUMS)
                                                    .build()
                                            )
                                            .build()
                                    )
                                }
                            }

                            TAB_PLAYLISTS -> {
                                val songs = repository.getAudioItems()
                                val bookmarks = songs.filter { it.isFavorite }
                                children.add(
                                    createBrowsableFolder(
                                        id = "playlist_all",
                                        title = "All Songs Queue",
                                        subtitle = "${songs.size} songs",
                                        folderType = MediaMetadata.FOLDER_TYPE_PLAYLISTS
                                    )
                                )
                                children.add(
                                    createBrowsableFolder(
                                        id = "playlist_bookmarks",
                                        title = "Favorites & Bookmarks",
                                        subtitle = "${bookmarks.size} songs",
                                        folderType = MediaMetadata.FOLDER_TYPE_PLAYLISTS
                                    )
                                )
                            }

                            TAB_FOLDERS, ITEM_FOLDERS, ITEM_FOLDERS_HIERARCHY -> {
                                val songs = repository.getAudioItems()
                                val folders = repository.groupByFolder(songs)
                                folders.forEach { folder ->
                                    children.add(
                                        createBrowsableFolder(
                                            id = "folder_${folder.name}",
                                            title = folder.name,
                                            subtitle = "${folder.songCount} songs",
                                            folderType = MediaMetadata.FOLDER_TYPE_MIXED
                                        )
                                    )
                                }
                            }

                            else -> {
                                val songs = repository.getAudioItems()
                                if (parentId.startsWith("letter_")) {
                                    val targetLetter = parentId.removePrefix("letter_")
                                    val letterSongs = songs.filter { song ->
                                        val firstChar = song.title.trim().firstOrNull()?.toString()?.uppercase() ?: "#"
                                        val key = when {
                                            firstChar in listOf("أ", "إ", "آ", "ا") -> "أ"
                                            firstChar in "بتثجحخدذرزسشصضطظعغفقكلمنهوي" -> firstChar
                                            firstChar.firstOrNull() in 'A'..'Z' -> firstChar
                                            else -> "#"
                                        }
                                        key == targetLetter
                                    }.sortedBy { it.title.lowercase() }
                                    letterSongs.forEach { children.add(convertSongToMedia3Item(it)) }
                                } else if (parentId.startsWith("album_")) {
                                    val albumName = parentId.removePrefix("album_")
                                    val albumSongs = songs.filter { it.album == albumName }
                                    albumSongs.forEach { children.add(convertSongToMedia3Item(it)) }
                                } else if (parentId.startsWith("folder_")) {
                                    val folderName = parentId.removePrefix("folder_")
                                    val folderSongs = songs.filter { it.folder == folderName }
                                    folderSongs.forEach { children.add(convertSongToMedia3Item(it)) }
                                } else if (parentId == "playlist_all") {
                                    songs.forEach { children.add(convertSongToMedia3Item(it)) }
                                } else if (parentId == "playlist_bookmarks") {
                                    songs.filter { it.isFavorite }.forEach { children.add(convertSongToMedia3Item(it)) }
                                } else if (parentId == TAB_VIDEOS) {
                                    // Videos in Android Auto — playable list
                                    val videos = repository.getVideoItems()
                                    videos.forEach { video ->
                                        children.add(
                                            Media3Item.Builder()
                                                .setMediaId("video_${video.id}")
                                                .setUri(video.contentUri)
                                                .setMediaMetadata(
                                                    MediaMetadata.Builder()
                                                        .setTitle(video.title)
                                                        .setArtist(video.folder)
                                                        .setAlbumTitle("Videos")
                                                        .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                                                        .setIsPlayable(true)
                                                        .setIsBrowsable(false)
                                                        .build()
                                                )
                                                .build()
                                        )
                                    }
                                }
                            }
                        }

                        future.set(LibraryResult.ofItemList(ImmutableList.copyOf(children), params))
                    } catch (e: Exception) {
                        future.set(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
                    }
                }
                return future
            }

            override fun onGetItem(
                session: MediaLibrarySession,
                browser: MediaSession.ControllerInfo,
                mediaId: String
            ): ListenableFuture<LibraryResult<Media3Item>> {
                val future = com.google.common.util.concurrent.SettableFuture.create<LibraryResult<Media3Item>>()
                serviceScope.launch(Dispatchers.IO) {
                    val item = when (mediaId) {
                        ROOT_ID -> Media3Item.Builder()
                            .setMediaId(ROOT_ID)
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setIsBrowsable(true)
                                    .setIsPlayable(false)
                                    .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                                    .setTitle("Wavora Music")
                                    .build()
                            )
                            .build()
                        TAB_LIBRARY -> createBrowsableFolder(TAB_LIBRARY, "Library", "Audio collections", MediaMetadata.FOLDER_TYPE_MIXED)
                        TAB_BY_LETTER, ITEM_BY_LETTER -> createBrowsableFolder(TAB_BY_LETTER, "Jump to Letter", "Browse by initial letter A-Z / أ-ي", MediaMetadata.FOLDER_TYPE_TITLES)
                        TAB_PLAYLISTS -> createBrowsableFolder(TAB_PLAYLISTS, "Playlists", "Custom playlists", MediaMetadata.FOLDER_TYPE_PLAYLISTS)
                        TAB_ALBUMS -> createBrowsableFolder(TAB_ALBUMS, "Albums", "Albums by artist", MediaMetadata.FOLDER_TYPE_ALBUMS)
                        TAB_FOLDERS, ITEM_FOLDERS, ITEM_FOLDERS_HIERARCHY -> createBrowsableFolder(TAB_FOLDERS, "Folders", "Device directory structure", MediaMetadata.FOLDER_TYPE_MIXED)
                        TAB_VIDEOS -> createBrowsableFolder(TAB_VIDEOS, "Videos", "Video files on device", MediaMetadata.FOLDER_TYPE_MIXED)
                        ITEM_SHUFFLE_ALL -> Media3Item.Builder()
                            .setMediaId(ITEM_SHUFFLE_ALL)
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle("Shuffle All")
                                    .setSubtitle("Play all tracks randomly")
                                    .setIsPlayable(true)
                                    .setIsBrowsable(false)
                                    .build()
                            )
                            .build()
                        ITEM_ALL_SONGS -> createBrowsableFolder(ITEM_ALL_SONGS, "All Songs", "Every song on device (A - Z)", MediaMetadata.FOLDER_TYPE_TITLES)
                        else -> {
                            val songs = repository.getAudioItems()
                            if (mediaId.startsWith("letter_")) {
                                val letter = mediaId.removePrefix("letter_")
                                createBrowsableFolder(mediaId, "Letter $letter ($letter)", "Songs starting with $letter", MediaMetadata.FOLDER_TYPE_TITLES)
                            } else if (mediaId.startsWith("album_")) {
                                val albumName = mediaId.removePrefix("album_")
                                val album = repository.groupByAlbum(songs).find { it.name == albumName }
                                Media3Item.Builder()
                                    .setMediaId(mediaId)
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle(albumName)
                                            .setArtist(album?.artist ?: "")
                                            .setArtworkUri(album?.artUri)
                                            .setIsBrowsable(true)
                                            .setIsPlayable(false)
                                            .setFolderType(MediaMetadata.FOLDER_TYPE_ALBUMS)
                                            .build()
                                    )
                                    .build()
                            } else if (mediaId.startsWith("folder_")) {
                                val folderName = mediaId.removePrefix("folder_")
                                createBrowsableFolder(mediaId, folderName, "Folder", MediaMetadata.FOLDER_TYPE_MIXED)
                            } else if (mediaId == "playlist_all") {
                                createBrowsableFolder("playlist_all", "All Songs Queue", "${songs.size} songs", MediaMetadata.FOLDER_TYPE_PLAYLISTS)
                            } else if (mediaId == "playlist_bookmarks") {
                                val count = songs.count { it.isFavorite }
                                createBrowsableFolder("playlist_bookmarks", "Favorites & Bookmarks", "$count songs", MediaMetadata.FOLDER_TYPE_PLAYLISTS)
                            } else if (mediaId.startsWith("video_")) {
                                val vidId = mediaId.removePrefix("video_")
                                repository.getVideoItems().find { it.id.toString() == vidId }?.let { video ->
                                    Media3Item.Builder()
                                        .setMediaId(mediaId)
                                        .setUri(video.contentUri)
                                        .setMediaMetadata(
                                            MediaMetadata.Builder()
                                                .setTitle(video.title)
                                                .setArtist(video.folder)
                                                .setAlbumTitle("Videos")
                                                .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                                                .setIsPlayable(true)
                                                .setIsBrowsable(false)
                                                .build()
                                        )
                                        .build()
                                }
                            } else {
                                songs.find { it.id.toString() == mediaId }?.let { convertSongToMedia3Item(it) }
                                    ?: repository.getVideoItems().find { it.id.toString() == mediaId }?.let { video ->
                                        Media3Item.Builder()
                                            .setMediaId("video_${video.id}")
                                            .setUri(video.contentUri)
                                            .setMediaMetadata(
                                                MediaMetadata.Builder()
                                                    .setTitle(video.title)
                                                    .setArtist(video.folder)
                                                    .setAlbumTitle("Videos")
                                                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                                                    .setIsPlayable(true)
                                                    .setIsBrowsable(false)
                                                    .build()
                                            )
                                            .build()
                                    }
                            }
                        }
                    }

                    if (item != null) {
                        future.set(LibraryResult.ofItem(item, null))
                    } else {
                        future.set(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
                    }
                }
                return future
            }

            override fun onPlaybackResumption(
                mediaSession: MediaSession,
                controller: MediaSession.ControllerInfo
            ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
                val future = com.google.common.util.concurrent.SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()
                serviceScope.launch(Dispatchers.IO) {
                    val songs = repository.getAudioItems()
                    if (songs.isNotEmpty()) {
                        val media3Items = songs.map { convertSongToMedia3Item(it) }
                        future.set(
                            MediaSession.MediaItemsWithStartPosition(
                                media3Items,
                                0,
                                0L
                            )
                        )
                    } else {
                        future.setException(IllegalStateException("No audio items found on device"))
                    }
                }
                return future
            }

            override fun onSearch(
                session: MediaLibrarySession,
                browser: MediaSession.ControllerInfo,
                query: String,
                params: LibraryParams?
            ): ListenableFuture<LibraryResult<Void>> {
                session.notifySearchResultChanged(browser, query, 50, params)
                return Futures.immediateFuture(LibraryResult.ofVoid())
            }

            override fun onGetSearchResult(
                session: MediaLibrarySession,
                browser: MediaSession.ControllerInfo,
                query: String,
                page: Int,
                pageSize: Int,
                params: LibraryParams?
            ): ListenableFuture<LibraryResult<ImmutableList<Media3Item>>> {
                val future = com.google.common.util.concurrent.SettableFuture.create<LibraryResult<ImmutableList<Media3Item>>>()
                serviceScope.launch(Dispatchers.IO) {
                    val songs = repository.getAudioItems()
                    val q = query.lowercase()
                    val matches = songs.filter {
                        it.title.lowercase().contains(q) ||
                        it.artist.lowercase().contains(q) ||
                        it.album.lowercase().contains(q)
                    }.map { convertSongToMedia3Item(it) }
                    future.set(LibraryResult.ofItemList(ImmutableList.copyOf(matches), params))
                }
                return future
            }

            override fun onAddMediaItems(
                mediaSession: MediaSession,
                controller: MediaSession.ControllerInfo,
                mediaItems: List<Media3Item>
            ): ListenableFuture<List<Media3Item>> {
                val future = com.google.common.util.concurrent.SettableFuture.create<List<Media3Item>>()
                serviceScope.launch(Dispatchers.IO) {
                    val allAudio = repository.getAudioItems()

                    // Check if Shuffle All was clicked
                    if (mediaItems.any { it.mediaId == ITEM_SHUFFLE_ALL }) {
                        val shuffled = allAudio.shuffled().map { convertSongToMedia3Item(it) }
                        serviceScope.launch(Dispatchers.Main) {
                            player.shuffleModeEnabled = true
                        }
                        future.set(shuffled)
                        return@launch
                    }

                    // Play all playlist
                    if (mediaItems.any { it.mediaId == "playlist_all" || it.mediaId == ITEM_ALL_SONGS }) {
                        future.set(allAudio.map { convertSongToMedia3Item(it) })
                        return@launch
                    }

                    // Play bookmarks playlist
                    if (mediaItems.any { it.mediaId == "playlist_bookmarks" }) {
                        val favs = allAudio.filter { it.isFavorite }.ifEmpty { allAudio }
                        future.set(favs.map { convertSongToMedia3Item(it) })
                        return@launch
                    }

                    // Letter clicked to play
                    val letterItem = mediaItems.find { it.mediaId.startsWith("letter_") }
                    if (letterItem != null) {
                        val letter = letterItem.mediaId.removePrefix("letter_")
                        val letterSongs = allAudio.filter { song ->
                            val firstChar = song.title.trim().firstOrNull()?.toString()?.uppercase() ?: "#"
                            val key = when {
                                firstChar in listOf("أ", "إ", "آ", "ا") -> "أ"
                                firstChar in "بتثجحخدذرزسشصضطظعغفقكلمنهوي" -> firstChar
                                firstChar.firstOrNull() in 'A'..'Z' -> firstChar
                                else -> "#"
                            }
                            key == letter
                        }.sortedBy { it.title.lowercase() }
                        if (letterSongs.isNotEmpty()) {
                            future.set(letterSongs.map { convertSongToMedia3Item(it) })
                            return@launch
                        }
                    }

                    // Album clicked
                    val albumItem = mediaItems.find { it.mediaId.startsWith("album_") }
                    if (albumItem != null) {
                        val albumName = albumItem.mediaId.removePrefix("album_")
                        val albumSongs = allAudio.filter { it.album == albumName }
                        if (albumSongs.isNotEmpty()) {
                            future.set(albumSongs.map { convertSongToMedia3Item(it) })
                            return@launch
                        }
                    }

                    // Folder clicked
                    val folderItem = mediaItems.find { it.mediaId.startsWith("folder_") }
                    if (folderItem != null) {
                        val folderName = folderItem.mediaId.removePrefix("folder_")
                        val folderSongs = allAudio.filter { it.folder == folderName }
                        if (folderSongs.isNotEmpty()) {
                            future.set(folderSongs.map { convertSongToMedia3Item(it) })
                            return@launch
                        }
                    }

                    // Video clicked in Android Auto or played from app
                    val videoItem = mediaItems.find { it.mediaId.startsWith("video_") }
                    if (videoItem != null) {
                        val videoId = videoItem.mediaId.removePrefix("video_")
                        val allVideos = repository.getVideoItems()
                        val clickedVideo = allVideos.find { it.id.toString() == videoId }
                        if (clickedVideo != null) {
                            val media3 = Media3Item.Builder()
                                .setMediaId("video_${clickedVideo.id}")
                                .setUri(clickedVideo.contentUri)
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(clickedVideo.title)
                                        .setArtist(clickedVideo.folder)
                                        .setAlbumTitle("Videos")
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                                        .setIsPlayable(true)
                                        .setIsBrowsable(false)
                                        .build()
                                )
                                .build()
                            future.set(listOf(media3))
                            return@launch
                        }
                    }

                    // Single song clicked in Android Auto: provide queue starting from this song so next/prev works!
                    if (mediaItems.size == 1) {
                        val targetId = mediaItems[0].mediaId
                        val clickedIdx = allAudio.indexOfFirst { it.id.toString() == targetId }
                        if (clickedIdx != -1) {
                            val reordered = allAudio.subList(clickedIdx, allAudio.size) + allAudio.subList(0, clickedIdx)
                            future.set(reordered.map { convertSongToMedia3Item(it) })
                            return@launch
                        }
                    }

                    val allVideos = repository.getVideoItems()
                    val updatedItems = mediaItems.map { item ->
                        val foundAudio = allAudio.find { it.id.toString() == item.mediaId }
                        if (foundAudio != null) {
                            convertSongToMedia3Item(foundAudio)
                        } else {
                            val foundVideo = allVideos.find {
                                it.id.toString() == item.mediaId || "video_${it.id}" == item.mediaId
                            }
                            if (foundVideo != null) {
                                Media3Item.Builder()
                                    .setMediaId("video_${foundVideo.id}")
                                    .setUri(foundVideo.contentUri)
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle(foundVideo.title)
                                            .setArtist(foundVideo.folder)
                                            .setAlbumTitle("Videos")
                                            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                                            .setIsPlayable(true)
                                            .setIsBrowsable(false)
                                            .build()
                                    )
                                    .build()
                            } else {
                                item
                            }
                        }
                    }
                    future.set(updatedItems)
                }
                return future
            }
        }

        mediaLibrarySession = MediaLibrarySession.Builder(this, player, callback)
            .setSessionActivity(sessionIntent)
            .build()
        activeSession = mediaLibrarySession
    }

    private fun createBrowsableFolder(
        id: String,
        title: String,
        subtitle: String,
        folderType: Int
    ): Media3Item {
        return Media3Item.Builder()
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .setFolderType(folderType)
                    .build()
            )
            .build()
    }

    private fun convertSongToMedia3Item(song: com.example.wavora.data.MediaItem): Media3Item {
        return Media3Item.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.contentUri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(song.albumArtUri)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    .setIsPlayable(true)
                    .setIsBrowsable(false)
                    .build()
            )
            .build()
    }

    /**
     * Builds custom layout for Android Auto & MediaSession in exact order:
     * 1. Shuffle (زر الخلط)
     * 2. Close/Dismiss (زر الإغلاق / إيقاف الجلسة)
     * 3. Repeat (زر التكرار)
     * 4. Overflow Menu (زر القائمة الإضافية)
     */
    private fun buildCustomLayout(): ImmutableList<CommandButton> {
        val shuffleBtn = CommandButton.Builder()
            .setDisplayName(if (player.shuffleModeEnabled) "Shuffle: On" else "Shuffle: Off")
            .setIconResId(R.drawable.ic_shuffle)
            .setSessionCommand(SessionCommand(CUSTOM_ACTION_SHUFFLE, Bundle.EMPTY))
            .setEnabled(true)
            .build()

        val closeBtn = CommandButton.Builder()
            .setDisplayName("Dismiss Session")
            .setIconResId(R.drawable.ic_close)
            .setSessionCommand(SessionCommand(CUSTOM_ACTION_CLOSE, Bundle.EMPTY))
            .setEnabled(true)
            .build()

        val repeatTitle = when (player.repeatMode) {
            Player.REPEAT_MODE_ONE -> "Repeat: One"
            Player.REPEAT_MODE_ALL -> "Repeat: All"
            else -> "Repeat: Off"
        }
        val repeatBtn = CommandButton.Builder()
            .setDisplayName(repeatTitle)
            .setIconResId(R.drawable.ic_repeat)
            .setSessionCommand(SessionCommand(CUSTOM_ACTION_REPEAT, Bundle.EMPTY))
            .setEnabled(true)
            .build()

        val overflowBtn = CommandButton.Builder()
            .setDisplayName("More Options")
            .setIconResId(R.drawable.ic_more_vert)
            .setSessionCommand(SessionCommand(CUSTOM_ACTION_OVERFLOW, Bundle.EMPTY))
            .setEnabled(true)
            .build()

        return ImmutableList.of(shuffleBtn, closeBtn, repeatBtn, overflowBtn)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        mediaLibrarySession

    override fun onDestroy() {
        activePlayer = null
        activeSession = null
        audioFxManager.release()
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }
        super.onDestroy()
    }
}
