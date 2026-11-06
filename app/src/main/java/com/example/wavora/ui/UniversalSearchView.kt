package com.example.wavora.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wavora.MediaViewModel
import com.example.wavora.data.*
import com.example.wavora.theme.*

/**
 * Universal fast real-time search interface with Category chips, Search history,
 * and instant audio and video result filtering.
 */
@Composable
fun UniversalSearchView(
    viewModel: MediaViewModel,
    onOpenAudio: (MediaItem) -> Unit,
    onOpenVideo: (MediaItem) -> Unit,
    onOpenAlbum: (Album) -> Unit,
    onOpenFolder: (MediaFolder) -> Unit,
    accentColor: Color,
    cardBg: Color,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchInput by remember { mutableStateOf(uiState.searchQuery) }
    var selectedCategory by remember { mutableStateOf(SearchCategory.ALL) }

    LaunchedEffect(searchInput) {
        viewModel.setSearchQuery(searchInput)
    }

    // Filtered results
    val matchingAudio = remember(uiState.audioItems, searchInput) {
        if (searchInput.isBlank()) emptyList() else {
            uiState.audioItems.filter {
                it.title.contains(searchInput, ignoreCase = true) ||
                it.artist.contains(searchInput, ignoreCase = true) ||
                it.album.contains(searchInput, ignoreCase = true)
            }
        }
    }

    val matchingVideos = remember(uiState.videoItems, searchInput) {
        val allVideos = uiState.videoItems.ifEmpty { SampleMedia.sampleVideos }
        if (searchInput.isBlank()) emptyList() else {
            allVideos.filter {
                it.title.contains(searchInput, ignoreCase = true) ||
                it.artist.contains(searchInput, ignoreCase = true) ||
                it.folder.contains(searchInput, ignoreCase = true)
            }
        }
    }

    val matchingAlbums = remember(uiState.albums, searchInput) {
        if (searchInput.isBlank()) emptyList() else {
            uiState.albums.filter {
                it.name.contains(searchInput, ignoreCase = true) ||
                it.artist.contains(searchInput, ignoreCase = true)
            }
        }
    }

    val matchingFolders = remember(uiState.folders, searchInput) {
        if (searchInput.isBlank()) emptyList() else {
            uiState.folders.filter {
                it.name.contains(searchInput, ignoreCase = true)
            }
        }
    }

    val totalMatches = matchingAudio.size + matchingVideos.size + matchingAlbums.size + matchingFolders.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("universal_search_layout"),
        contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── 1. Search Bar ──
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchInput,
                    onValueChange = {
                        searchInput = it
                        if (it.isNotBlank()) {
                            viewModel.addRecentSearch(it)
                        }
                    },
                    placeholder = {
                        Text(
                            text = "Search tracks, artists, albums, or videos...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchInput.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchInput = ""
                                    viewModel.setSearchQuery("")
                                },
                                modifier = Modifier.size(28.dp).testTag("clear_search_button")
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg,
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color(0xFF1E293B),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("universal_search_input")
                )

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val categories = listOf(
                        Pair(SearchCategory.ALL, "All"),
                        Pair(SearchCategory.SONGS, "Songs (${matchingAudio.size})"),
                        Pair(SearchCategory.VIDEOS, "Videos (${matchingVideos.size})"),
                        Pair(SearchCategory.ALBUMS, "Albums (${matchingAlbums.size})"),
                        Pair(SearchCategory.FOLDERS, "Folders (${matchingFolders.size})")
                    )

                    items(categories) { (cat, label) ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) accentColor else Color(0xFF141F30))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .testTag("search_cat_${cat.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // ── 2. Recent Searches (when query is blank) ──
        if (searchInput.isBlank()) {
            if (uiState.recentSearches.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "RECENT SEARCHES (عمليات البحث الأخيرة)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 10.sp
                                ),
                                color = TextMuted
                            )

                            TextButton(
                                onClick = { viewModel.clearRecentSearches() },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.testTag("clear_recent_searches_button")
                            ) {
                                Text("Clear", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = accentColor)
                            }
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.recentSearches) { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFF141F30))
                                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp))
                                        .clickable { searchInput = tag }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("recent_search_chip_$tag"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.History,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Friendly Discovery Prompt
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TravelExplore,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = "Search across your local library",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Type any track title, artist name, album, or video keyword",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            // ── 3. Results Header ──
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "FOUND $totalMatches RESULTS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 11.sp
                        ),
                        color = accentColor
                    )
                }
            }

            // Videos Results
            if ((selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.VIDEOS) && matchingVideos.isNotEmpty()) {
                item {
                    Text(
                        text = "VIDEOS (${matchingVideos.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                items(matchingVideos) { video ->
                    VideoQueueCard(
                        item = video,
                        isActive = false,
                        accentColor = accentColor,
                        cardBg = cardBg,
                        onClick = { onOpenVideo(video) }
                    )
                }
            }

            // Audio Tracks Results
            if ((selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.SONGS) && matchingAudio.isNotEmpty()) {
                item {
                    Text(
                        text = "SONGS (${matchingAudio.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                items(matchingAudio) { song ->
                    SongRow(
                        item = song,
                        isPlaying = false,
                        onClick = { onOpenAudio(song) },
                        onFavoriteToggle = { viewModel.toggleFavorite(song.id) },
                        onOptionsClick = { viewModel.openSongOptions(song) },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Albums Results
            if ((selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.ALBUMS) && matchingAlbums.isNotEmpty()) {
                item {
                    Text(
                        text = "ALBUMS (${matchingAlbums.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                items(matchingAlbums) { album ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { onOpenAlbum(album) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF141F30)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Album, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                            }
                            Column {
                                Text(text = album.name, style = MaterialTheme.typography.titleSmall, color = Color.White)
                                Text(text = "${album.artist}  •  ${album.songCount} tracks", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // Empty state if 0 results
            if (totalMatches == 0) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SearchOff,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "No results for \"$searchInput\"",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Try searching for a different artist or keyword",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
