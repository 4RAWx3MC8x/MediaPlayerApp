package com.example.wavora

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wavora.data.Album
import com.example.wavora.data.MediaFolder
import com.example.wavora.data.MediaItem
import com.example.wavora.ui.*

/**
 * App navigation — simple animated screen transitions.
 */
enum class Screen {
    HOME, PLAYER, VIDEO_PLAYER, EQUALIZER,
    ALBUM_DETAIL, FOLDER_DETAIL, SETTINGS, YOUTUBE_MANAGER
}

@Composable
fun MainNavigation() {
    val viewModel: MediaViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var videoItem by remember { mutableStateOf<MediaItem?>(null) }
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }
    var selectedFolder by remember { mutableStateOf<MediaFolder?>(null) }

    if (!uiState.hasPermission) {
        PermissionScreen(onPermissionGranted = { viewModel.onPermissionGranted() })
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                when (targetState) {
                    Screen.PLAYER -> slideInVertically { it } togetherWith slideOutVertically { -it / 3 }
                    Screen.EQUALIZER -> slideInVertically { it } togetherWith slideOutVertically { -it / 3 }
                    Screen.SETTINGS -> slideInHorizontally { it } togetherWith slideOutHorizontally { -it / 3 }
                    Screen.VIDEO_PLAYER -> fadeIn() togetherWith fadeOut()
                    Screen.ALBUM_DETAIL -> slideInHorizontally { it } togetherWith slideOutHorizontally { -it / 3 }
                    Screen.FOLDER_DETAIL -> slideInHorizontally { it } togetherWith slideOutHorizontally { -it / 3 }
                    Screen.YOUTUBE_MANAGER -> slideInHorizontally { it } togetherWith slideOutHorizontally { -it / 3 }
                    Screen.HOME -> slideInVertically { -it / 3 } togetherWith slideOutVertically { it }
                }
            },
            label = "ScreenTransition",
        ) { screen ->
            when (screen) {
                Screen.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onOpenPlayer = { currentScreen = Screen.PLAYER },
                    onOpenVideoPlayer = { item ->
                        if (viewModel.playerState.value.isPlaying) {
                            viewModel.playerController.togglePlayPause()
                        }
                        videoItem = item
                        currentScreen = Screen.VIDEO_PLAYER
                    },
                    onOpenEqualizer = { currentScreen = Screen.EQUALIZER },
                    onOpenSettings = { currentScreen = Screen.SETTINGS },
                    onOpenAlbumDetail = { album ->
                        selectedAlbum = album
                        currentScreen = Screen.ALBUM_DETAIL
                    },
                    onOpenFolderDetail = { folder ->
                        selectedFolder = folder
                        currentScreen = Screen.FOLDER_DETAIL
                    },
                    onOpenYouTube = { currentScreen = Screen.YOUTUBE_MANAGER },
                )
                Screen.PLAYER -> PlayerScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = Screen.HOME },
                    onOpenEqualizer = { currentScreen = Screen.EQUALIZER },
                    onGoToAlbum = { albumName ->
                        val album = viewModel.findAlbum(albumName)
                        if (album != null) {
                            selectedAlbum = album
                            currentScreen = Screen.ALBUM_DETAIL
                        }
                    },
                    onGoToFolder = { folderName ->
                        val folder = viewModel.findFolder(folderName)
                        if (folder != null) {
                            selectedFolder = folder
                            currentScreen = Screen.FOLDER_DETAIL
                        }
                    },
                )
                Screen.EQUALIZER -> EqualizerScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = Screen.HOME },
                )
                Screen.VIDEO_PLAYER -> {
                    val item = videoItem
                    if (item != null) {
                        VideoPlayerScreen(
                            mediaItem = item,
                            onBack = { currentScreen = Screen.HOME },
                        )
                    } else {
                        currentScreen = Screen.HOME
                    }
                }
                Screen.ALBUM_DETAIL -> {
                    val album = selectedAlbum
                    if (album != null) {
                        AlbumDetailScreen(
                            album = album,
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.HOME },
                            onOpenPlayer = { currentScreen = Screen.PLAYER },
                        )
                    } else {
                        currentScreen = Screen.HOME
                    }
                }
                Screen.FOLDER_DETAIL -> {
                    val folder = selectedFolder
                    if (folder != null) {
                        FolderDetailScreen(
                            folder = folder,
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.HOME },
                            onOpenPlayer = { currentScreen = Screen.PLAYER },
                        )
                    } else {
                        currentScreen = Screen.HOME
                    }
                }
                Screen.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = Screen.HOME },
                    onOpenEqualizer = { currentScreen = Screen.EQUALIZER },
                )
                Screen.YOUTUBE_MANAGER -> YouTubeManagerScreen(
                    savedYouTubeVideos = uiState.videoItems,
                    onBack = { currentScreen = Screen.HOME },
                    onOpenVideoPlayer = { item ->
                        if (viewModel.playerState.value.isPlaying) {
                            viewModel.playerController.togglePlayPause()
                        }
                        videoItem = item
                        currentScreen = Screen.VIDEO_PLAYER
                    },
                )
            }
        }

        // Global Dialog: Displayed over any active screen
        if (uiState.isFoldersDialogVisible) {
            FoldersSelectionDialog(viewModel = viewModel, uiState = uiState)
        }

        // Global Track Options Sheet
        if (uiState.selectedSongForOptions != null) {
            val song = uiState.selectedSongForOptions!!
            TrackOptionsMenuSheet(
                item = song,
                onDismiss = { viewModel.closeSongOptions() },
                onFavoriteToggle = { viewModel.toggleFavorite(song.id) },
                onNavigateToArtist = {
                    viewModel.closeSongOptions()
                    viewModel.setActiveNavTab(0)
                    viewModel.setLibrarySubCategory("ARTISTS")
                    currentScreen = Screen.HOME
                },
                onNavigateToAlbum = { albumName ->
                    viewModel.closeSongOptions()
                    val found = viewModel.findAlbum(albumName)
                    if (found != null) {
                        selectedAlbum = found
                        currentScreen = Screen.ALBUM_DETAIL
                    }
                },
                onNavigateToFolder = { folderName ->
                    viewModel.closeSongOptions()
                    val found = viewModel.findFolder(folderName)
                    if (found != null) {
                        selectedFolder = found
                        currentScreen = Screen.FOLDER_DETAIL
                    }
                },
                onNavigateToGenre = {
                    viewModel.closeSongOptions()
                    viewModel.setActiveNavTab(0)
                    viewModel.setLibrarySubCategory("GENRES")
                    currentScreen = Screen.HOME
                },
                onAddToPlaylist = {
                    viewModel.closeSongOptions()
                },
                onDelete = {
                    viewModel.closeSongOptions()
                },
                onShowInfo = {
                    viewModel.closeSongOptions()
                }
            )
        }
    }
}
