package com.example.wavora.ui

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.wavora.MediaUiState
import com.example.wavora.MediaViewModel
import com.example.wavora.data.*
import com.example.wavora.theme.*

/**
 * Wavora Main Screen Layout.
 * Supports:
 *  - High-Fidelity Music & Video Library
 *  - Embedded Media3 PlayerView with complete playback controls (Play, Pause, Seek Bar, 10s skip)
 *  - Rich Customization (Accent themes, OLED true black backgrounds, layout modes)
 *  - Advanced Sorting & Arranging (By title, date, length, artist, album, most played, asc/desc)
 *  - Real-time Universal Search with category filters & history
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MediaViewModel,
    onOpenPlayer: () -> Unit,
    onOpenVideoPlayer: (MediaItem) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenAlbumDetail: (Album) -> Unit = {},
    onOpenFolderDetail: (MediaFolder) -> Unit = {},
    onOpenYouTube: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    var activeNavTab by remember { mutableIntStateOf(uiState.activeNavTab) }

    val activeAccent = remember(uiState.customization.accentColorIndex) {
        WavoraAccents.getOrElse(uiState.customization.accentColorIndex) { WavoraAccents[0] }.color
    }
    val activeBgTheme = remember(uiState.customization.bgThemeIndex) {
        WavoraBgThemes.getOrElse(uiState.customization.bgThemeIndex) { WavoraBgThemes[0] }
    }

    Scaffold(
        containerColor = activeBgTheme.bg,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // MiniPlayer Bar: floating above bottom navigation dock
                if (playerState.currentItem != null) {
                    MiniPlayerBar(
                        playerState = playerState,
                        controller = viewModel.playerController,
                        onClick = onOpenPlayer,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // Modern Floating Dock Navigation
                Box(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(activeBgTheme.card.copy(alpha = 0.95f))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(28.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("main_bottom_nav_dock")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FloatingNavTabItem(
                            icon = Icons.Rounded.LibraryMusic,
                            label = "Library",
                            selected = activeNavTab == 0,
                            accentColor = activeAccent,
                            onClick = { activeNavTab = 0; viewModel.setActiveNavTab(0) }
                        )
                        FloatingNavTabItem(
                            icon = Icons.Rounded.SmartDisplay,
                            label = "Video Player",
                            selected = activeNavTab == 1,
                            accentColor = activeAccent,
                            onClick = { activeNavTab = 1; viewModel.setActiveNavTab(1) }
                        )
                        FloatingNavTabItem(
                            icon = Icons.Rounded.Search,
                            label = "Search",
                            selected = activeNavTab == 2,
                            accentColor = activeAccent,
                            onClick = { activeNavTab = 2; viewModel.setActiveNavTab(2) }
                        )
                        FloatingNavTabItem(
                            icon = Icons.Rounded.GraphicEq,
                            label = "EQ",
                            selected = activeNavTab == 3,
                            accentColor = activeAccent,
                            onClick = { activeNavTab = 3; viewModel.setActiveNavTab(3) }
                        )
                        FloatingNavTabItem(
                            icon = Icons.Rounded.Tune,
                            label = "Customize",
                            selected = activeNavTab == 4,
                            accentColor = activeAccent,
                            onClick = { activeNavTab = 4; viewModel.setActiveNavTab(4) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (activeNavTab) {
                // ── 0. Modernized Library View ──
                0 -> MainLibraryView(
                    viewModel = viewModel,
                    uiState = uiState,
                    playerState = playerState,
                    accentColor = activeAccent,
                    cardBg = activeBgTheme.card,
                    onOpenPlayer = onOpenPlayer,
                    onOpenVideoTab = { activeNavTab = 1 },
                    onOpenAlbumDetail = onOpenAlbumDetail,
                    onOpenFolderDetail = onOpenFolderDetail
                )

                // ── 1. The Requested Media3 PlayerView Video Layout ──
                1 -> MainVideoPlayerView(
                    videos = uiState.videoItems,
                    selectedVideo = uiState.selectedMainVideo,
                    onSelectVideo = { video ->
                        viewModel.setSelectedMainVideo(video)
                    },
                    accentColor = activeAccent,
                    cardBg = activeBgTheme.card,
                    onOpenFullscreen = onOpenVideoPlayer
                )

                // ── 2. Universal Fast Real-Time Search ──
                2 -> UniversalSearchView(
                    viewModel = viewModel,
                    onOpenAudio = { track ->
                        viewModel.playerController.play(track, listOf(track))
                        onOpenPlayer()
                    },
                    onOpenVideo = { video ->
                        viewModel.setSelectedMainVideo(video)
                        activeNavTab = 1
                    },
                    onOpenAlbum = onOpenAlbumDetail,
                    onOpenFolder = onOpenFolderDetail,
                    accentColor = activeAccent,
                    cardBg = activeBgTheme.card
                )

                // ── 3. Parametric EQ Studio ──
                3 -> EqualizerScreen(
                    viewModel = viewModel,
                    onBack = { activeNavTab = 0 }
                )

                // ── 4. Settings & Customization Hub ──
                4 -> SettingsScreen(
                    viewModel = viewModel,
                    onBack = { activeNavTab = 0 },
                    onOpenEqualizer = { activeNavTab = 3 }
                )
            }
        }
    }

    // Modal: Customization Sheet (when triggered from top bar or settings)
    if (uiState.isCustomizationSheetVisible) {
        CustomizationModalSheet(
            viewModel = viewModel,
            customization = uiState.customization,
            onDismiss = { viewModel.closeCustomizationSheet() }
        )
    }

    // Modal: Sort & Arrange Sheet (when triggered from top bar)
    if (uiState.isSortSheetVisible) {
        SortAndArrangeModalSheet(
            viewModel = viewModel,
            customization = uiState.customization,
            onDismiss = { viewModel.closeSortSheet() }
        )
    }
}

/**
 * Floating Dock Navigation Item with animated pill background and label.
 */
@Composable
fun FloatingNavTabItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val bgAnim by animateColorAsState(
        targetValue = if (selected) accentColor.copy(alpha = 0.22f) else Color.Transparent,
        label = "tabBg"
    )
    val tintAnim by animateColorAsState(
        targetValue = if (selected) accentColor else Color(0xFF94A3B8),
        label = "tabTint"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgAnim)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("nav_tab_$label"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tintAnim,
                modifier = Modifier.size(20.dp)
            )
            if (selected) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Main Library View with:
 *  - Top bar with Logo, ViewMode toggle, Sort button, Customization button
 *  - Category Chips: All, Songs, Videos, Favorites, Playlists, Albums, Artists, Folders
 *  - Featured Video Player card banner
 *  - Dynamic Media List in GRID, LIST, or COMPACT mode!
 */
@Composable
fun MainLibraryView(
    viewModel: MediaViewModel,
    uiState: MediaUiState,
    playerState: com.example.wavora.player.PlayerState,
    accentColor: Color,
    cardBg: Color,
    onOpenPlayer: () -> Unit,
    onOpenVideoTab: () -> Unit,
    onOpenAlbumDetail: (Album) -> Unit,
    onOpenFolderDetail: (MediaFolder) -> Unit,
    modifier: Modifier = Modifier
) {
    val audioList = remember(uiState.audioItems, uiState.customization) {
        viewModel.getFilteredAndSortedAudio()
    }
    val videoList = remember(uiState.videoItems, uiState.customization) {
        viewModel.getFilteredAndSortedVideos()
    }
    val currentFilter = uiState.customization.filter
    val currentSort = uiState.customization.sortBy
    val sortAscending = uiState.customization.sortAscending
    val viewMode = uiState.customization.viewMode

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_library_list"),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 1. Top App Bar ──
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Branding Logo & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(accentColor, accentColor.copy(alpha = 0.6f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "WAVORA",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                fontSize = 18.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Media Player • ${audioList.size} tracks",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }

                // Action Controls: ViewMode Toggle, Sort, Theme Customization
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // View Mode Toggle (List -> Grid -> Compact)
                    IconButton(
                        onClick = {
                            val nextMode = when (viewMode) {
                                ViewMode.LIST -> ViewMode.GRID
                                ViewMode.GRID -> ViewMode.COMPACT
                                ViewMode.COMPACT -> ViewMode.LIST
                            }
                            viewModel.setViewMode(nextMode)
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(cardBg)
                            .border(1.dp, Color(0xFF1E293B), CircleShape)
                            .testTag("view_mode_toggle")
                    ) {
                        Icon(
                            imageVector = when (viewMode) {
                                ViewMode.LIST -> Icons.Rounded.ViewList
                                ViewMode.GRID -> Icons.Rounded.GridView
                                ViewMode.COMPACT -> Icons.Rounded.DensitySmall
                            },
                            contentDescription = "Toggle View Mode",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Sort & Arrange Button
                    IconButton(
                        onClick = { viewModel.openSortSheet() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(cardBg)
                            .border(1.dp, Color(0xFF1E293B), CircleShape)
                            .testTag("open_sort_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Sort,
                            contentDescription = "Sort Options",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Customization Palette Button
                    IconButton(
                        onClick = { viewModel.openCustomizationSheet() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(cardBg)
                            .border(1.dp, Color(0xFF1E293B), CircleShape)
                            .testTag("open_customization_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = "Customize Themes",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // ── 2. Quick Category Filter Chips ──
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(MediaFilter.values()) { filter ->
                    val isSelected = currentFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) accentColor else cardBg)
                            .border(1.dp, if (isSelected) accentColor else Color(0xFF1E293B), RoundedCornerShape(12.dp))
                            .clickable { viewModel.setMediaFilter(filter) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("category_chip_${filter.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.5.sp
                            ),
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }
        }

        // ── 3. Featured Video Player Banner ──
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable(onClick = onOpenVideoTab)
                    .testTag("featured_video_banner"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SmartDisplay,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Media3 Video Player",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "PlayerView with Seek bar, Play, Pause & Controls",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "WATCH",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // ── 4. Active Sort & Order Status Bar ──
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sorted by: ${currentSort.label} (${if (sortAscending) "A-Z / Oldest" else "Z-A / Newest"})",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextMuted
                )

                Text(
                    text = "${audioList.size} items",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = accentColor
                )
            }
        }

        // ── 5. Media Content based on chosen ViewMode & Filter ──
        if (currentFilter == MediaFilter.VIDEOS) {
            // Video collection
            if (videoList.isEmpty()) {
                item {
                    EmptyMediaView(message = "No videos found", onReset = { viewModel.setMediaFilter(MediaFilter.ALL) })
                }
            } else {
                items(videoList, key = { it.id }) { videoItem ->
                    VideoQueueCard(
                        item = videoItem,
                        isActive = false,
                        accentColor = accentColor,
                        cardBg = cardBg,
                        onClick = {
                            viewModel.setSelectedMainVideo(videoItem)
                            onOpenVideoTab()
                        }
                    )
                }
            }
        } else {
            // Audio collection
            if (audioList.isEmpty()) {
                item {
                    EmptyMediaView(message = "No audio tracks match this filter", onReset = { viewModel.setMediaFilter(MediaFilter.ALL) })
                }
            } else {
                when (viewMode) {
                    ViewMode.LIST -> {
                        itemsIndexed(audioList, key = { _, item -> item.id }) { index, item ->
                            val isPlaying = playerState.currentItem?.id == item.id && playerState.isPlaying
                            SongRow(
                                item = item,
                                index = index,
                                isPlaying = isPlaying,
                                onClick = {
                                    viewModel.playerController.play(item, audioList)
                                    onOpenPlayer()
                                },
                                onFavoriteToggle = { viewModel.toggleFavorite(item.id) },
                                onOptionsClick = { viewModel.openSongOptions(item) }
                            )
                        }
                    }

                    ViewMode.COMPACT -> {
                        itemsIndexed(audioList, key = { _, item -> item.id }) { index, item ->
                            val isPlaying = playerState.currentItem?.id == item.id && playerState.isPlaying
                            CompactSongRow(
                                item = item,
                                index = index,
                                isPlaying = isPlaying,
                                accentColor = accentColor,
                                onClick = {
                                    viewModel.playerController.play(item, audioList)
                                    onOpenPlayer()
                                }
                            )
                        }
                    }

                    ViewMode.GRID -> {
                        // 2-Column Grid Rows
                        val chunked = audioList.chunked(2)
                        items(chunked) { pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                for (track in pair) {
                                    val isPlaying = playerState.currentItem?.id == track.id && playerState.isPlaying
                                    GridSongCard(
                                        item = track,
                                        isPlaying = isPlaying,
                                        accentColor = accentColor,
                                        cardBg = cardBg,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.playerController.play(track, audioList)
                                            onOpenPlayer()
                                        },
                                        onFavoriteToggle = { viewModel.toggleFavorite(track.id) }
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2-Column Grid Card for tracks in Grid Mode
 */
@Composable
fun GridSongCard(
    item: MediaItem,
    isPlaying: Boolean,
    accentColor: Color,
    cardBg: Color,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("track_grid_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(
            width = if (isPlaying) 1.5.dp else 1.dp,
            color = if (isPlaying) accentColor else Color(0xFF1E293B)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Album artwork container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0D1320)),
                contentAlignment = Alignment.Center
            ) {
                AlbumArtImage(uri = item.albumArtUri, modifier = Modifier.fillMaxSize())

                // Favorite Heart Button in top corner
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) accentColor else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Duration badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.duration.formatDuration(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = if (isPlaying) accentColor else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = item.artist,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Compact list row for dense scanning.
 */
@Composable
fun CompactSongRow(
    item: MediaItem,
    index: Int,
    isPlaying: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .testTag("compact_row_${item.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "${index + 1}",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = if (isPlaying) accentColor else TextMuted,
            modifier = Modifier.width(22.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = if (isPlaying) accentColor else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.artist,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = item.duration.formatDuration(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = TextMuted
        )
    }
}

@Composable
fun EmptyMediaView(message: String, onReset: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MusicOff,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Color.White
            )
            Button(
                onClick = onReset,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Show All Media")
            }
        }
    }
}
