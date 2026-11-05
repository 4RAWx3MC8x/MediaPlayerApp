package com.example.wavora.ui

import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import com.example.wavora.player.PlayerState
import com.example.wavora.player.WavoraPlayerController
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.wavora.MediaUiState
import com.example.wavora.MediaViewModel
import com.example.wavora.data.*
import com.example.wavora.theme.*

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
    var activeNavTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // MiniPlayer: sleek floating card above the island dock
                if (playerState.currentItem != null) {
                    MiniPlayerBar(
                        playerState = playerState,
                        controller = viewModel.playerController,
                        onClick = onOpenPlayer,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // Floating Island (جزيرة طائرة) - Pure minimal icon-only dock
                Box(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .clip(CircleShape)
                        .background(IslandBg)
                        .border(1.dp, IslandBorder, CircleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FloatingNavIcon(
                            icon = Icons.Rounded.GridView,
                            contentDescription = "Library",
                            selected = activeNavTab == 0,
                            onClick = {
                                if (activeNavTab == 0) {
                                    viewModel.setLibrarySubCategory("MENU")
                                } else {
                                    activeNavTab = 0
                                }
                            }
                        )
                        FloatingNavIcon(
                            icon = Icons.Rounded.GraphicEq,
                            contentDescription = "Equalizer",
                            selected = activeNavTab == 1,
                            onClick = { activeNavTab = 1 }
                        )
                        FloatingNavIcon(
                            icon = Icons.Rounded.Search,
                            contentDescription = "Search",
                            selected = activeNavTab == 2,
                            onClick = { activeNavTab = 2 }
                        )
                        FloatingNavIcon(
                            icon = Icons.Rounded.Menu,
                            contentDescription = "More",
                            selected = activeNavTab == 3,
                            onClick = { activeNavTab = 3 }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (activeNavTab) {
                0 -> LibraryView(
                    viewModel = viewModel,
                    onOpenPlayer = onOpenPlayer,
                    onOpenSettings = onOpenSettings,
                    onOpenAlbumDetail = onOpenAlbumDetail,
                    onOpenFolderDetail = onOpenFolderDetail
                )
                1 -> EqualizerScreen(viewModel = viewModel, onBack = { activeNavTab = 0 })
                2 -> QuickSearchView(
                    viewModel = viewModel,
                    onOpenPlayer = onOpenPlayer,
                    onOpenAlbumDetail = onOpenAlbumDetail,
                    onOpenFolderDetail = onOpenFolderDetail
                )
                3 -> MoreHubView(
                    viewModel = viewModel,
                    onOpenSettings = onOpenSettings,
                    onOpenVideoPlayer = onOpenVideoPlayer,
                    onOpenYouTube = onOpenYouTube,
                )
            }
        }
    }
}

// Minimal Floating Island Icon-Only Nav Item
@Composable
fun FloatingNavIcon(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgAnim by animateColorAsState(
        targetValue = if (selected) AccentRed.copy(alpha = 0.22f) else Color.Transparent,
        label = "navIconBg"
    )
    val tintAnim by animateColorAsState(
        targetValue = if (selected) AccentRed else Color(0xFF71717A),
        label = "navIconTint"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(bgAnim)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tintAnim,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun LibraryView(
    viewModel: MediaViewModel,
    onOpenPlayer: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenAlbumDetail: (Album) -> Unit = {},
    onOpenFolderDetail: (MediaFolder) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()

    if (uiState.librarySubCategory == "MENU") {
        LibraryMenuView(
            viewModel = viewModel,
            queueSize = playerState.queue.size,
            onOpenSettings = onOpenSettings,
        )
    } else {
        LibrarySubCategoryView(
            viewModel = viewModel,
            uiState = uiState,
            playerState = playerState,
            onOpenSettings = onOpenSettings,
            onOpenAlbumDetail = onOpenAlbumDetail,
            onOpenFolderDetail = onOpenFolderDetail,
        )
    }
}

// -------------------------------------------------------------
// Library Menu View (Image 3: Clean Root Overview with Circular Badges)
// -------------------------------------------------------------
@Composable
fun LibraryMenuView(
    viewModel: MediaViewModel,
    queueSize: Int,
    onOpenSettings: () -> Unit,
) {
    var showOverflowMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
    ) {
        // Top Header matching Image 3
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Library",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                ),
                color = Color.White
            )

            Box {
                IconButton(
                    onClick = { showOverflowMenu = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF16161A))
                        .border(1.dp, Color(0xFF28282E), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showOverflowMenu,
                    onDismissRequest = { showOverflowMenu = false },
                    modifier = Modifier
                        .background(Color(0xFF1E1E24))
                        .border(1.dp, Color(0xFF33333C), RoundedCornerShape(12.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("Rescan Library", color = Color.White, fontSize = 13.5.sp) },
                        leadingIcon = { Icon(Icons.Rounded.Refresh, contentDescription = null, tint = AccentRed, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showOverflowMenu = false
                            viewModel.loadMedia()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Folders Selection", color = Color.White, fontSize = 13.5.sp) },
                        leadingIcon = { Icon(Icons.Rounded.FolderSpecial, contentDescription = null, tint = AccentRed, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showOverflowMenu = false
                            viewModel.openFoldersDialog()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings", color = Color.White, fontSize = 13.5.sp) },
                        leadingIcon = { Icon(Icons.Rounded.Settings, contentDescription = null, tint = Color(0xFFA1A1AA), modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showOverflowMenu = false
                            onOpenSettings()
                        }
                    )
                }
            }
        }

        // 10 Library Category Items (Exact Image 3 Layout & Circular Badge Colors)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 6.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LibraryMenuItem(
                    title = "All Songs",
                    icon = Icons.Rounded.MusicNote,
                    badgeBg = Color(0xFF4378DB),
                    iconTint = Color(0xFF0D1B3E),
                    onClick = { viewModel.setLibrarySubCategory("ALL_SONGS") }
                )
            }
            item {
                LibraryMenuItem(
                    title = "Folders",
                    icon = Icons.Rounded.Folder,
                    badgeBg = Color(0xFF3880FF),
                    iconTint = Color(0xFF0B192C),
                    onClick = { viewModel.setLibrarySubCategory("FOLDERS") }
                )
            }
            item {
                LibraryMenuItem(
                    title = "Folders Hierarchy",
                    icon = Icons.Rounded.FolderCopy,
                    badgeBg = Color(0xFF4B69FE),
                    iconTint = Color(0xFF0F1738),
                    onClick = { viewModel.setLibrarySubCategory("FOLDERS_HIERARCHY") }
                )
            }
            item {
                LibraryMenuItem(
                    title = "Albums",
                    icon = Icons.Rounded.Album,
                    badgeBg = Color(0xFF6B66DA),
                    iconTint = Color(0xFF1C1335),
                    onClick = { viewModel.setLibrarySubCategory("ALBUMS") }
                )
            }
            item {
                LibraryMenuItem(
                    title = "Artists",
                    icon = Icons.Rounded.Mic,
                    badgeBg = Color(0xFF5E6090),
                    iconTint = Color(0xFF16152B),
                    onClick = { viewModel.setLibrarySubCategory("ARTISTS") }
                )
            }
            item {
                LibraryMenuItem(
                    title = "Genres",
                    icon = Icons.Rounded.Audiotrack,
                    badgeBg = Color(0xFF7E4A88),
                    iconTint = Color(0xFF220E28),
                    onClick = { viewModel.setLibrarySubCategory("GENRES") }
                )
            }
            item {
                LibraryMenuItem(
                    title = "Playlists",
                    icon = Icons.AutoMirrored.Rounded.QueueMusic,
                    badgeBg = Color(0xFF9E6555),
                    iconTint = Color(0xFF2D160E),
                    onClick = { viewModel.setLibrarySubCategory("PLAYLISTS") }
                )
            }
            item {
                LibraryMenuItem(
                    title = "Queue",
                    subtitle = if (queueSize == 0) "empty" else "$queueSize tracks",
                    icon = Icons.Rounded.FastForward,
                    badgeBg = Color(0xFF7E8146),
                    iconTint = Color(0xFF20210E),
                    onClick = { viewModel.setLibrarySubCategory("QUEUE") }
                )
            }
            item {
                LibraryMenuItem(
                    title = "Bookmarks",
                    icon = Icons.Rounded.Bookmark,
                    badgeBg = Color(0xFF387CF0),
                    iconTint = Color(0xFF0C192E),
                    onClick = { viewModel.setLibrarySubCategory("BOOKMARKS") }
                )
            }
            item {
                LibraryMenuItem(
                    title = "Most Played",
                    icon = Icons.Rounded.PlayArrow,
                    badgeBg = Color(0xFF8869A6),
                    iconTint = Color(0xFF231431),
                    onClick = { viewModel.setLibrarySubCategory("MOST_PLAYED") }
                )
            }
        }
    }
}

@Composable
fun LibraryMenuItem(
    title: String,
    icon: ImageVector,
    badgeBg: Color,
    iconTint: Color,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Colored Circular Badge
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(badgeBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                ),
                color = Color.White
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color(0xFFA1A1AA)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Subcategory Browser View (< Library top header + category contents)
// -------------------------------------------------------------
@Composable
fun LibrarySubCategoryView(
    viewModel: MediaViewModel,
    uiState: MediaUiState,
    playerState: PlayerState,
    onOpenSettings: () -> Unit,
    onOpenAlbumDetail: (Album) -> Unit,
    onOpenFolderDetail: (MediaFolder) -> Unit,
) {
    var isSearchVisible by remember { mutableStateOf(false) }

    val subCategories = listOf(
        "All Songs" to "ALL_SONGS",
        "Folders" to "FOLDERS",
        "Hierarchy" to "FOLDERS_HIERARCHY",
        "Albums" to "ALBUMS",
        "Artists" to "ARTISTS",
        "Genres" to "GENRES",
        "Playlists" to "PLAYLISTS",
        "Queue" to "QUEUE",
        "Bookmarks" to "BOOKMARKS",
        "Most Played" to "MOST_PLAYED",
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Navigation Header: `< Library` with 🔍 Search & ✕ Close (Image 1 & 2 Header style)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBg)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back button to return to Library Menu (< Library)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.setLibrarySubCategory("MENU") }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back to Library",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Library",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { isSearchVisible = !isSearchVisible },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = if (isSearchVisible || uiState.searchQuery.isNotEmpty()) AccentRed else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                viewModel.setSearchQuery("")
                            } else {
                                viewModel.setLibrarySubCategory("MENU")
                            }
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expandable Search Capsule
            AnimatedVisibility(
                visible = isSearchVisible || uiState.searchQuery.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF141418))
                        .border(1.dp, BorderLight, RoundedCornerShape(22.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = Color(0xFF8E8E93), modifier = Modifier.size(18.dp))
                        BasicTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontSize = 13.5.sp),
                            modifier = Modifier.weight(1f),
                            decorationBox = { inner ->
                                Box {
                                    if (uiState.searchQuery.isEmpty()) {
                                        Text("Filter in this category...", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp), color = Color(0xFF63636E))
                                    }
                                    inner()
                                }
                            }
                        )
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Quick Category Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(subCategories) { (label, key) ->
                    val isSelected = uiState.librarySubCategory == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) AccentRed else Color(0xFF141418))
                            .border(1.dp, if (isSelected) AccentRed else BorderLight, RoundedCornerShape(20.dp))
                            .clickable { viewModel.setLibrarySubCategory(key) }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        when (uiState.librarySubCategory) {
            "ALL_SONGS" -> {
                val songs = viewModel.filteredAudio()
                if (songs.isEmpty()) {
                    EmptyStateView("No audio tracks found", Icons.Rounded.MusicOff, onRefresh = { viewModel.loadMedia() }, onManageFolders = { viewModel.openFoldersDialog() })
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        itemsIndexed(songs) { idx, item ->
                            SongRow(
                                item = item,
                                index = idx,
                                isPlaying = playerState.currentItem?.id == item.id,
                                onClick = { viewModel.playerController.play(item, songs) },
                                onFavoriteToggle = { viewModel.toggleFavorite(item.id) },
                                onOptionsClick = { viewModel.openSongOptions(item) }
                            )
                        }
                    }
                }
            }
            "FOLDERS" -> {
                val folders = uiState.folders
                if (folders.isEmpty()) {
                    EmptyStateView("No folders found", Icons.Rounded.FolderOff, onRefresh = { viewModel.loadMedia() }, onManageFolders = { viewModel.openFoldersDialog() })
                } else {
                    val rootFolders = folders.filter { !it.name.contains('/') }
                    val subFolders = folders.filter { it.name.contains('/') }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Music Folders",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${folders.size} directories · ${folders.sumOf { it.songCount }} tracks",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = TextMuted
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFF1C1C22))
                                        .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
                                        .clickable { viewModel.openFoldersDialog() }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Rounded.FolderSpecial, contentDescription = null, tint = AccentRed, modifier = Modifier.size(14.dp))
                                        Text("Manage", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold), color = Color.White)
                                    }
                                }
                            }
                        }
                        if (rootFolders.isNotEmpty()) {
                            item {
                                Text(
                                    text = "ROOT DIRECTORIES",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                    color = TextMuted,
                                    modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                                )
                            }
                            items(rootFolders) { folder ->
                                FolderCard(folder = folder, onPlayAll = { onOpenFolderDetail(folder) })
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                        if (subFolders.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "SUBFOLDERS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                    color = TextMuted,
                                    modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                                )
                            }
                            items(subFolders) { folder ->
                                FolderCard(folder = folder, onPlayAll = { onOpenFolderDetail(folder) }, isSubfolder = true)
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
            "FOLDERS_HIERARCHY" -> {
                FoldersHierarchyView(
                    viewModel = viewModel,
                    uiState = uiState,
                    playerState = playerState,
                    onOpenSong = { item, list -> viewModel.playerController.play(item, list) },
                    onOptionsClick = { viewModel.openSongOptions(it) }
                )
            }
            "ALBUMS" -> {
                val albums = viewModel.filteredAlbums()
                if (albums.isEmpty()) {
                    EmptyStateView("No albums found", Icons.Rounded.Album)
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(albums) { album ->
                            AlbumCard(album = album, onClick = { onOpenAlbumDetail(album) })
                        }
                    }
                }
            }
            "ARTISTS" -> {
                val artists = viewModel.filteredArtists()
                if (artists.isEmpty()) {
                    EmptyStateView("No artists found", Icons.Rounded.PersonOff)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(artists) { artist ->
                            ArtistCard(artist = artist, onClick = {
                                if (artist.items.isNotEmpty()) {
                                    viewModel.playerController.play(artist.items.first(), artist.items)
                                }
                            })
                        }
                    }
                }
            }
            "PLAYLISTS" -> {
                val playlists = uiState.playlists
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(playlists) { playlist ->
                        PlaylistCard(playlist = playlist, onPlay = {
                            if (playlist.items.isNotEmpty()) {
                                viewModel.playerController.play(playlist.items.first(), playlist.items)
                            }
                        })
                    }
                }
            }
            "QUEUE" -> {
                QueueView(
                    playerState = playerState,
                    controller = viewModel.playerController,
                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                    onOptionsClick = { viewModel.openSongOptions(it) }
                )
            }
            "BOOKMARKS" -> {
                val bookmarks = uiState.bookmarks
                if (bookmarks.isEmpty()) {
                    EmptyStateView("No bookmarked tracks yet", Icons.Rounded.FavoriteBorder)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        itemsIndexed(bookmarks) { idx, item ->
                            SongRow(
                                item = item,
                                index = idx,
                                isPlaying = playerState.currentItem?.id == item.id,
                                onClick = { viewModel.playerController.play(item, bookmarks) },
                                onFavoriteToggle = { viewModel.toggleFavorite(item.id) },
                                onOptionsClick = { viewModel.openSongOptions(item) }
                            )
                        }
                    }
                }
            }
            "MOST_PLAYED" -> {
                val mostPlayed = uiState.mostPlayed
                if (mostPlayed.isEmpty()) {
                    EmptyStateView("No most played tracks yet", Icons.Rounded.LocalFireDepartment)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        itemsIndexed(mostPlayed) { idx, item ->
                            SongRow(
                                item = item,
                                index = idx,
                                isPlaying = playerState.currentItem?.id == item.id,
                                onClick = { viewModel.playerController.play(item, mostPlayed) },
                                onFavoriteToggle = { viewModel.toggleFavorite(item.id) },
                                onOptionsClick = { viewModel.openSongOptions(item) },
                                badgeText = if (item.playCount > 0) "${item.playCount} ${if (item.playCount == 1) "play" else "plays"}" else null
                            )
                        }
                    }
                }
            }
            "GENRES" -> {
                val genres = uiState.genres
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(genres) { genre ->
                        GenreCard(genre = genre, onClick = {
                            if (genre.items.isNotEmpty()) {
                                viewModel.playerController.play(genre.items.first(), genre.items)
                            }
                        })
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Interactive Folders Hierarchy Navigator (Drill-down Explorer)
// -------------------------------------------------------------
@Composable
fun FoldersHierarchyView(
    viewModel: MediaViewModel,
    uiState: MediaUiState,
    playerState: PlayerState,
    onOpenSong: (MediaItem, List<MediaItem>) -> Unit,
    onOptionsClick: (MediaItem) -> Unit,
) {
    var currentPath by remember { mutableStateOf<String?>(null) }
    val deviceModel = remember { android.os.Build.MODEL ?: "Device Storage" }
    val folders = uiState.folders

    if (currentPath == null) {
        val rootFolders = remember(folders) {
            folders.map { if (it.name.contains('/')) it.name.substringBefore('/') else it.name }.distinct().sorted()
        }

        if (rootFolders.isEmpty()) {
            EmptyStateView("No folders discovered", Icons.Rounded.FolderOff, onRefresh = { viewModel.loadMedia() }, onManageFolders = { viewModel.openFoldersDialog() })
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Root Directories (${rootFolders.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF1E1E24))
                                .clickable { viewModel.openFoldersDialog() }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Manage", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold), color = AccentRed)
                        }
                    }
                }

                items(rootFolders) { rootName ->
                    val treeFolders = folders.filter { it.name == rootName || it.name.startsWith("$rootName/") }
                    val totalTracks = treeFolders.sumOf { it.songCount }
                    val subCount = treeFolders.size - 1

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF16161A))
                            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                            .clickable { currentPath = rootName }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF24242C)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Folder, contentDescription = null, tint = AccentRed, modifier = Modifier.size(24.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rootName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "$deviceModel · $totalTracks tracks" + (if (subCount > 0) " · $subCount subfolders" else ""),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                    color = TextMuted
                                )
                            }

                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Enter", tint = Color(0xFF6E6E78), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    } else {
        val activePath = currentPath!!
        val subFolders = remember(folders, activePath) {
            folders.filter { it.name.startsWith("$activePath/") }
                .map { folder ->
                    val relativeSub = folder.name.removePrefix("$activePath/")
                    relativeSub.substringBefore('/')
                }
                .distinct()
        }

        val folderItems = remember(folders, activePath) {
            folders.filter { it.name == activePath || it.name.startsWith("$activePath/") }
                .flatMap { it.items }
                .distinctBy { it.id }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (activePath.contains('/')) {
                                currentPath = activePath.substringBeforeLast('/')
                            } else {
                                currentPath = null
                            }
                        },
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF1E1E24))
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activePath.substringAfterLast('/'),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = activePath,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (folderItems.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(AccentRed)
                                .clickable { onOpenSong(folderItems.first(), folderItems) }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Text("Play All", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp), color = Color.White)
                            }
                        }
                    }
                }
            }

            if (subFolders.isNotEmpty()) {
                item {
                    Text(
                        text = "SUBFOLDERS (${subFolders.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp),
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                items(subFolders) { subName ->
                    val fullSubPath = "$activePath/$subName"
                    val subFolderObj = folders.firstOrNull { it.name == fullSubPath }
                    val count = subFolderObj?.songCount ?: 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF18181D))
                            .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                            .clickable { currentPath = fullSubPath }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Rounded.Folder, contentDescription = null, tint = AccentRed, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = subName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp), color = Color.White)
                            Text(text = "$count tracks", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextMuted)
                        }
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF6E6E78), modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (folderItems.isNotEmpty()) {
                item {
                    Text(
                        text = "TRACKS (${folderItems.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp),
                        color = TextMuted,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }

                itemsIndexed(folderItems) { idx, item ->
                    SongRow(
                        item = item,
                        index = idx,
                        isPlaying = playerState.currentItem?.id == item.id,
                        onClick = { onOpenSong(item, folderItems) },
                        onFavoriteToggle = { viewModel.toggleFavorite(item.id) },
                        onOptionsClick = { onOptionsClick(item) }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Interactive Live Queue View
// -------------------------------------------------------------
@Composable
fun QueueView(
    playerState: PlayerState,
    controller: WavoraPlayerController,
    onFavoriteToggle: (Long) -> Unit,
    onOptionsClick: (MediaItem) -> Unit,
) {
    val queue = playerState.queue
    if (queue.isEmpty()) {
        EmptyStateView("Queue is currently empty", Icons.Rounded.FastForward)
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${queue.size} Tracks in Queue",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF222228))
                        .clickable { controller.clearQueue() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Clear Queue",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold),
                        color = Color(0xFFEF4444)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                itemsIndexed(queue) { idx, item ->
                    val isPlaying = playerState.currentItem?.id == item.id
                    SongRow(
                        item = item,
                        index = idx,
                        isPlaying = isPlaying,
                        onClick = { controller.play(item, queue) },
                        onFavoriteToggle = { onFavoriteToggle(item.id) },
                        onOptionsClick = { onOptionsClick(item) },
                        badgeText = if (isPlaying) "Now Playing" else null
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Quick Search View (Nav Tab 2)
// -------------------------------------------------------------
@Composable
fun QuickSearchView(
    viewModel: MediaViewModel,
    onOpenPlayer: () -> Unit,
    onOpenAlbumDetail: (Album) -> Unit,
    onOpenFolderDetail: (MediaFolder) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF141418))
                .border(1.dp, BorderLight, RoundedCornerShape(24.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Rounded.Search, contentDescription = "Search", tint = AccentRed, modifier = Modifier.size(20.dp))
                BasicTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontSize = 14.sp),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        Box {
                            if (uiState.searchQuery.isEmpty()) {
                                Text("Search tracks, artists, albums, folders...", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp), color = Color(0xFF63636E))
                            }
                            inner()
                        }
                    }
                )
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        val songs = viewModel.filteredAudio()
        if (uiState.searchQuery.isEmpty()) {
            EmptyStateView("Type to search tracks, artists, and albums", Icons.Rounded.Search)
        } else if (songs.isEmpty()) {
            EmptyStateView("No results for \"${uiState.searchQuery}\"", Icons.Rounded.SearchOff)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                item {
                    Text(
                        text = "SEARCH RESULTS (${songs.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp),
                        color = TextMuted,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                    )
                }
                itemsIndexed(songs) { idx, item ->
                    SongRow(
                        item = item,
                        index = idx,
                        isPlaying = playerState.currentItem?.id == item.id,
                        onClick = { viewModel.playerController.play(item, songs) },
                        onFavoriteToggle = { viewModel.toggleFavorite(item.id) },
                        onOptionsClick = { viewModel.openSongOptions(item) }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// More Hub View (Nav Tab 3: Settings, Folders, Rescan, Videos)
// -------------------------------------------------------------
@Composable
fun MoreHubView(
    viewModel: MediaViewModel,
    onOpenSettings: () -> Unit,
    onOpenVideoPlayer: (MediaItem) -> Unit,
    onOpenYouTube: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "More",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp),
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            SettingsActionCard(
                icon = Icons.Rounded.FolderSpecial,
                title = "Manage Music Folders",
                subtitle = "Select storage directories and include/exclude subfolders",
                badge = "${uiState.folders.size} Folders",
                onClick = { viewModel.openFoldersDialog() }
            )
        }

        item {
            SettingsActionCard(
                icon = Icons.Rounded.Refresh,
                title = "Rescan Library",
                subtitle = "Rescan internal and external storage for newly added files",
                badge = "${uiState.audioItems.size} Audio",
                onClick = { viewModel.loadMedia() }
            )
        }

        item {
            SettingsActionCard(
                icon = Icons.Rounded.Settings,
                title = "Settings",
                subtitle = "Audio playback, themes, equalizer, and player settings",
                onClick = onOpenSettings
            )
        }

        item {
            SettingsActionCard(
                icon = Icons.Rounded.PlayCircle,
                title = "YouTube Manager",
                subtitle = "Open YouTube videos, save to device, and play on car screen",
                badge = "YouTube",
                onClick = onOpenYouTube
            )
        }

        if (uiState.videoItems.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "VIDEOS (${uiState.videoItems.size})",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp),
                    color = TextMuted
                )
            }
            items(uiState.videoItems) { video ->
                VideoCard(item = video, onClick = { onOpenVideoPlayer(video) })
            }
        }
    }
}

@Composable
fun AlbumsDirectView(viewModel: MediaViewModel, onOpenAlbumDetail: (Album) -> Unit) {
    val albums = viewModel.filteredAlbums()
    if (albums.isEmpty()) {
        EmptyStateView("No albums found", Icons.Rounded.Album)
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(albums) { album ->
                AlbumCard(album = album, onClick = { onOpenAlbumDetail(album) })
            }
        }
    }
}

@Composable
fun VideosView(viewModel: MediaViewModel, onOpenVideoPlayer: (MediaItem) -> Unit) {
    val videos = viewModel.filteredVideos()
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val ctx = viewModel.getApplication<android.app.Application>()
            ctx.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val name = android.provider.OpenableColumns.DISPLAY_NAME.let { col ->
                ctx.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val idx = cursor.getColumnIndex(col)
                    if (idx != -1 && cursor.moveToFirst()) cursor.getString(idx) else null
                }
            } ?: uri.lastPathSegment ?: "Custom Video"
            val customVideo = MediaItem(
                id = uri.toString().hashCode().toLong(),
                title = name,
                artist = "Local File",
                album = "Custom",
                albumArtUri = null,
                contentUri = uri,
                duration = 0L,
                mediaType = MediaType.VIDEO,
                dateAdded = System.currentTimeMillis() / 1000
            )
            onOpenVideoPlayer(customVideo)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Videos",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = Color.White
            )
            Button(
                onClick = { videoPickerLauncher.launch(arrayOf("video/*")) },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(Modifier.width(4.dp))
                Text("Open Video", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        if (videos.isEmpty()) {
            EmptyStateView("No videos found", Icons.Rounded.VideocamOff)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(videos) { video ->
                    VideoCard(item = video, onClick = { onOpenVideoPlayer(video) })
                }
            }
        }
    }
}

@Composable
fun FolderCard(folder: MediaFolder, onPlayAll: () -> Unit, isSubfolder: Boolean = folder.name.contains('/')) {
    val displayName = if (isSubfolder) folder.name.substringAfterLast('/') else folder.name
    val parentPath = if (isSubfolder) folder.name.substringBeforeLast('/') else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isSubfolder) Modifier.padding(start = 14.dp) else Modifier)
            .clickable(onClick = onPlayAll),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSubfolder) Color(0xFF0F0F13) else Color(0xFF121217)
        ),
        border = BorderStroke(
            1.dp,
            if (isSubfolder) Color(0xFF1E1E28) else BorderLight
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (isSubfolder) 38.dp else 44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSubfolder) Color(0xFF1A1A22) else Color(0xFF1C1C24)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSubfolder) Icons.Rounded.FolderCopy else Icons.Rounded.Folder,
                    contentDescription = null,
                    tint = if (isSubfolder) Color(0xFF71717A) else AccentRed,
                    modifier = Modifier.size(if (isSubfolder) 18.dp else 22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (isSubfolder) FontWeight.Medium else FontWeight.SemiBold,
                        fontSize = if (isSubfolder) 13.sp else 14.sp
                    ),
                    color = if (isSubfolder) Color(0xFFB0B0B8) else Color.White
                )
                if (parentPath != null) {
                    Text(
                        text = "in $parentPath",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${folder.songCount} tracks · ${folder.path}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1C1C24)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "Play All", tint = AccentRed, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun AlbumCard(album: Album, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ChipBg),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF141418))
            ) {
                AlbumArtImage(uri = album.artUri, modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = album.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${album.artist}  •  ${album.songCount}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ArtistCard(artist: Artist, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(ChipBg)
                .border(1.dp, BorderLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Rounded.Person, contentDescription = null, tint = AccentRed, modifier = Modifier.size(26.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = artist.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
            Text(text = "${artist.albumCount} albums  •  ${artist.songCount} songs", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp), color = TextSecondary)
        }
        Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
    }
    HorizontalDivider(color = BorderLight, thickness = 1.dp)
}

@Composable
fun PlaylistCard(playlist: MediaPlaylist, onPlay: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onPlay),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ChipBg),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF242429)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.QueueMusic, contentDescription = null, tint = AccentRed, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = playlist.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                Text(text = "${playlist.songCount} tracks", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp), color = TextSecondary)
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(AccentRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
fun GenreCard(genre: MediaGenre, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ChipBg),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF242429))
                    .border(1.dp, BorderLight, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Rounded.GraphicEq, contentDescription = null, tint = AccentRed, modifier = Modifier.size(18.dp))
            }
            Text(text = genre.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
            Text(text = "${genre.songCount} songs", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextSecondary)
        }
    }
}

@Composable
fun VideoCard(item: MediaItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ChipBg),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF111114)),
                contentAlignment = Alignment.Center
            ) {
                AlbumArtImage(uri = item.albumArtUri, modifier = Modifier.fillMaxSize())
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AccentRed.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.85f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(text = item.duration.formatDuration(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color.White)
                }
            }
            Spacer(Modifier.height(7.dp))
            Text(text = item.title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = item.folder, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextMuted)
        }
    }
}

@Composable
fun EmptyStateView(
    message: String,
    icon: ImageVector,
    onRefresh: (() -> Unit)? = null,
    onManageFolders: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF121217))
                    .border(1.dp, Color(0xFF26262E), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AccentRed,
                    modifier = Modifier.size(38.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = "Ensure music files exist in storage, or select the exact folders to scan.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 17.sp
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onRefresh != null) {
                    Button(
                        onClick = onRefresh,
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Scan Device", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                if (onManageFolders != null) {
                    OutlinedButton(
                        onClick = onManageFolders,
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.dp, BorderLight),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF141418)),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Rounded.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Music Folders", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun FoldersSelectionDialog(viewModel: MediaViewModel, uiState: MediaUiState) {
    val context = LocalContext.current
    var expandedFolders by remember { mutableStateOf(setOf<String>()) }
    val deviceModel = remember { Build.MODEL.ifBlank { "SM-A576B" } }

    val openDocumentTreeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            val folderName = uri.lastPathSegment?.substringAfterLast(':') ?: uri.lastPathSegment ?: "Custom Folder"
            viewModel.addCustomFolder(folderName, uri.toString())
        }
    }

    // Group folders into root directories and nested subfolders
    val folderGroups = remember(uiState.discoveredFolders) {
        val groups = LinkedHashMap<String, MutableList<SelectableFolder>>()
        uiState.discoveredFolders.forEach { folder ->
            val root = if (folder.name.contains('/')) folder.name.substringBefore('/') else folder.name
            groups.getOrPut(root) { mutableListOf() }.add(folder)
        }
        groups
    }

    AlertDialog(
        onDismissRequest = { viewModel.closeFoldersDialog() },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFF19191C),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(24.dp)),
        title = {
            Text(
                text = "Folders Selection",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                ),
                color = Color.White,
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    folderGroups.forEach { (rootName, folderList) ->
                        val subfolders = folderList.filter { it.name.contains('/') }
                        val rootFolder = folderList.firstOrNull { !it.name.contains('/') }
                        val isExpanded = expandedFolders.contains(rootName)
                        val isRootSelected = if (subfolders.isNotEmpty()) {
                            subfolders.any { it.isSelected }
                        } else {
                            rootFolder?.isSelected == true
                        }

                        // Root Folder Row (Image 1)
                        item(key = "root_$rootName") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Expand / Collapse Arrow
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                expandedFolders = if (isExpanded) {
                                                    expandedFolders - rootName
                                                } else {
                                                    expandedFolders + rootName
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Rounded.ArrowDropDown else Icons.Rounded.PlayArrow,
                                            contentDescription = "Toggle",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Folder Icon
                                    Icon(
                                        imageVector = Icons.Rounded.Folder,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )

                                    // Folder Name & Device Model Label
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = rootName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp
                                            ),
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = deviceModel,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                            color = Color(0xFFA1A1AA)
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Remove / Exclude X
                                    IconButton(
                                        onClick = { viewModel.removeRootFolderFromSelection(rootName) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Exclude",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Checkbox
                                    Checkbox(
                                        checked = isRootSelected,
                                        onCheckedChange = { checked ->
                                            viewModel.toggleRootFolderSelection(rootName, checked)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color.White,
                                            uncheckedColor = Color(0xFF6B6B76),
                                            checkmarkColor = Color.Black
                                        )
                                    )
                                }
                            }
                        }

                        // Expanded Subfolders (Image 2)
                        if (isExpanded) {
                            items(subfolders, key = { "sub_${it.name}" }) { sub ->
                                val subDisplayName = sub.name.substringAfterLast('/')
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 28.dp, top = 2.dp, bottom = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = subDisplayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Checkbox(
                                        checked = sub.isSelected,
                                        onCheckedChange = {
                                            viewModel.toggleFolderSelection(sub.name)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color.White,
                                            uncheckedColor = Color(0xFF6B6B76),
                                            checkmarkColor = Color.Black
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button 1: Select Folder or Storage
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF26262A))
                        .clickable { openDocumentTreeLauncher.launch(null) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Select Folder or Storage",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = Color.White
                    )
                }

                // Button 2: Save and Scan
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF26262A))
                        .clickable { viewModel.saveAndRescanFolders() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save and Scan",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    )
}
