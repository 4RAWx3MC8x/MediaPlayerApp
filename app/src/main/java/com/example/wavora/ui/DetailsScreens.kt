package com.example.wavora.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wavora.MediaViewModel
import com.example.wavora.data.Album
import com.example.wavora.data.MediaFolder
import com.example.wavora.data.MediaItem
import com.example.wavora.data.formatDuration
import com.example.wavora.theme.*

/**
 * Dedicated Album Detail Screen.
 * basim-music / MelodyFlow style with crimson Play All button, rounded artwork,
 * favorite heart icons, and high-contrast typography.
 */
@Composable
fun AlbumDetailScreen(
    album: Album,
    viewModel: MediaViewModel,
    onBack: () -> Unit,
    onOpenPlayer: () -> Unit,
) {
    val playerState by viewModel.playerState.collectAsState()
    val totalDuration = album.items.sumOf { it.duration }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            if (playerState.currentItem != null) {
                MiniPlayerBar(
                    playerState = playerState,
                    controller = viewModel.playerController,
                    onClick = onOpenPlayer,
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // --- Header & Navigation ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ChipBg)
                            .border(1.dp, BorderLight, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = "ALBUM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                        ),
                        color = TextMuted,
                    )

                    Spacer(Modifier.size(42.dp))
                }
            }

            // --- Big Album Header ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(210.dp)
                            .shadow(28.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black, spotColor = AccentRedGlow)
                            .clip(RoundedCornerShape(20.dp))
                            .background(ChipBg)
                            .border(1.5.dp, BorderLight, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        AlbumArtImage(
                            uri = album.artUri,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = album.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = album.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "${album.items.size} tracks   •   ${totalDuration.formatDuration()}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = TextMuted,
                    )

                    Spacer(Modifier.height(20.dp))

                    // Play All & Shuffle Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Play All Button (Vibrant Crimson Red)
                        Button(
                            onClick = {
                                if (album.items.isNotEmpty()) {
                                    viewModel.playerController.play(album.items.first(), album.items)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentRed,
                                contentColor = Color.White,
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = "Play All", modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Play All", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        // Shuffle Button
                        OutlinedButton(
                            onClick = {
                                if (album.items.isNotEmpty()) {
                                    val shuffled = album.items.shuffled()
                                    viewModel.playerController.play(shuffled.first(), shuffled)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = ChipBg,
                                contentColor = Color.White,
                            ),
                            border = BorderStroke(1.dp, BorderLight)
                        ) {
                            Icon(Icons.Rounded.Shuffle, contentDescription = "Shuffle", modifier = Modifier.size(18.dp), tint = AccentRed)
                            Spacer(Modifier.width(8.dp))
                            Text("Shuffle", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "TRACKS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    ),
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                )
            }

            // --- Track List ---
            itemsIndexed(album.items) { index, item ->
                val isPlaying = playerState.currentItem?.id == item.id
                TrackRowItem(
                    index = index + 1,
                    item = item,
                    isPlaying = isPlaying,
                    onClick = {
                        viewModel.playerController.play(item, album.items)
                    },
                    onFavoriteToggle = {
                        viewModel.toggleFavorite(item.id)
                    }
                )
            }
        }
    }
}

/**
 * Dedicated Folder Detail Screen.
 */
@Composable
fun FolderDetailScreen(
    folder: MediaFolder,
    viewModel: MediaViewModel,
    onBack: () -> Unit,
    onOpenPlayer: () -> Unit,
) {
    val playerState by viewModel.playerState.collectAsState()
    val totalDuration = folder.items.sumOf { it.duration }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            if (playerState.currentItem != null) {
                MiniPlayerBar(
                    playerState = playerState,
                    controller = viewModel.playerController,
                    onClick = onOpenPlayer,
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // --- Header & Navigation ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ChipBg)
                            .border(1.dp, BorderLight, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = "FOLDER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                        ),
                        color = TextMuted,
                    )

                    Spacer(Modifier.size(42.dp))
                }
            }

            // --- Big Folder Header ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .shadow(20.dp, CircleShape, ambientColor = Color.Black, spotColor = AccentRedGlow)
                            .clip(CircleShape)
                            .background(ChipBg)
                            .border(1.5.dp, BorderLight, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Folder,
                            contentDescription = "Folder",
                            tint = AccentRed,
                            modifier = Modifier.size(54.dp),
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = folder.path,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "${folder.items.size} audio files   •   ${totalDuration.formatDuration()}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = TextMuted,
                    )

                    Spacer(Modifier.height(20.dp))

                    // Play All & Shuffle Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Play All Button (Crimson Red)
                        Button(
                            onClick = {
                                if (folder.items.isNotEmpty()) {
                                    viewModel.playerController.play(folder.items.first(), folder.items)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentRed,
                                contentColor = Color.White,
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = "Play All", modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Play All", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        // Shuffle Button
                        OutlinedButton(
                            onClick = {
                                if (folder.items.isNotEmpty()) {
                                    val shuffled = folder.items.shuffled()
                                    viewModel.playerController.play(shuffled.first(), shuffled)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = ChipBg,
                                contentColor = Color.White,
                            ),
                            border = BorderStroke(1.dp, BorderLight)
                        ) {
                            Icon(Icons.Rounded.Shuffle, contentDescription = "Shuffle", modifier = Modifier.size(18.dp), tint = AccentRed)
                            Spacer(Modifier.width(8.dp))
                            Text("Shuffle", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "FILES IN FOLDER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    ),
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                )
            }

            // --- Track List ---
            itemsIndexed(folder.items) { index, item ->
                val isPlaying = playerState.currentItem?.id == item.id
                TrackRowItem(
                    index = index + 1,
                    item = item,
                    isPlaying = isPlaying,
                    onClick = {
                        viewModel.playerController.play(item, folder.items)
                    },
                    onFavoriteToggle = {
                        viewModel.toggleFavorite(item.id)
                    }
                )
            }
        }
    }
}

/**
 * Reusable Track Item for Album & Folder Detail Views.
 */
@Composable
private fun TrackRowItem(
    index: Int,
    item: MediaItem,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isPlaying) Color(0xFF1E1E26) else Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Track number or playing equalizer animation
        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (isPlaying) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = "Playing",
                    tint = AccentRed,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = index.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = TextMuted,
                )
            }
        }

        // Title and Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isPlaying) AccentRed else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${item.artist}   •   ${item.duration.formatDuration()}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Favorite Heart
        IconButton(
            onClick = onFavoriteToggle,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (item.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (item.isFavorite) AccentRed else Color(0xFF71717A),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
