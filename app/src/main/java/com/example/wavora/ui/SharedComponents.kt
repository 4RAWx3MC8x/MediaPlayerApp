package com.example.wavora.ui

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.wavora.data.MediaItem
import com.example.wavora.data.formatDuration
import com.example.wavora.player.PlayerState
import com.example.wavora.player.WavoraPlayerController
import com.example.wavora.theme.*

@Composable
fun MiniPlayerBar(
    playerState: PlayerState,
    controller: WavoraPlayerController,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = playerState.currentItem ?: return
    val progress = if (playerState.duration > 0) {
        (playerState.position.toFloat() / playerState.duration).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(IslandBg)
            .border(1.dp, IslandBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF111114)),
                contentAlignment = Alignment.Center,
            ) {
                if (item.albumArtUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(item.albumArtUri).crossfade(true).build(),
                        contentDescription = "Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                    )
                } else {
                    Icon(imageVector = Icons.Rounded.MusicNote, contentDescription = null, tint = AccentRed, modifier = Modifier.size(22.dp))
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp),
                    maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (item.album.isNotBlank() && item.album != "<unknown>") "${item.album}  ·  ${item.artist}" else item.artist,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    maxLines = 1, overflow = TextOverflow.Ellipsis, color = TextSecondary,
                )
            }
            IconButton(onClick = { controller.skipPrevious() }, modifier = Modifier.size(36.dp)) {
                Icon(imageVector = Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = Color(0xFFCCCCCC), modifier = Modifier.size(22.dp))
            }
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(AccentRed).clickable { controller.togglePlayPause() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Play/Pause", tint = Color.White, modifier = Modifier.size(24.dp),
                )
            }
            IconButton(onClick = { controller.skipNext() }, modifier = Modifier.size(36.dp)) {
                Icon(imageVector = Icons.Rounded.SkipNext, contentDescription = "Next", tint = Color(0xFFCCCCCC), modifier = Modifier.size(22.dp))
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(2.5.dp).align(Alignment.BottomStart).background(Color(0xFF242429))) {
            Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(AccentRed))
        }
    }
}

@Composable
fun AlbumArtImage(uri: Uri?, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Crop) {
    val ctx = LocalContext.current
    if (uri != null) {
        AsyncImage(
            model = ImageRequest.Builder(ctx).data(uri).crossfade(true).build(),
            contentDescription = "Album Art", contentScale = contentScale, modifier = modifier, error = null,
        )
    } else {
        MusicFallbackIcon(modifier)
    }
}

@Composable
fun MusicFallbackIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color(0xFF0D1320), RoundedCornerShape(10.dp)).border(1.dp, Color(0xFF1A2840), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = Icons.Rounded.MusicNote, contentDescription = null, tint = AccentRed.copy(alpha = 0.8f), modifier = Modifier.size(24.dp))
    }
}

@Composable
fun VideoFallbackIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color(0xFF141418), RoundedCornerShape(10.dp)).border(1.dp, Color(0xFF27272A), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = Icons.Rounded.PlayCircle, contentDescription = null, tint = AccentRed.copy(alpha = 0.8f), modifier = Modifier.size(28.dp))
    }
}

/**
 * basim-music table-style track row (renderSongTable):
 * [  #/bars  |  art 40dp  |  title + artist (flex)  |  duration  |  heart  ]
 * Separated by a thin bottom divider, h-64dp, active row has red-tinted bg.
 */
@Composable
fun SongRow(
    item: MediaItem,
    index: Int = 0,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: (() -> Unit)? = null,
    onOptionsClick: (() -> Unit)? = null,
    showIndex: Boolean = true,
    badgeText: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(if (isPlaying) Color(0xFF0A1525) else Color.Transparent)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Col 1: index number OR animated bars
            if (showIndex) {
                Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                    if (isPlaying) {
                        PlayingBarsIndicator()
                    } else {
                        Text(text = "${index + 1}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = TextMuted)
                    }
                }
            }

            // Col 2: 40x40 album art
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF111114)),
                contentAlignment = Alignment.Center,
            ) {
                AlbumArtImage(uri = item.albumArtUri, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp)))
                if (isPlaying) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, tint = AccentRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Col 3: Title + Artist (flex)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isPlaying) FontWeight.SemiBold else FontWeight.Normal, fontSize = 14.sp
                    ),
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    color = if (isPlaying) AccentRed else Color.White,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = item.artist,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        maxLines = 1, overflow = TextOverflow.Ellipsis, color = TextSecondary,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (badgeText != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AccentRed.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentRed
                                )
                            )
                        }
                    }
                }
            }

            // Col 4: Duration
            Text(text = item.duration.formatDuration(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = TextMuted)

            // Col 5: Heart
            if (onFavoriteToggle != null) {
                IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) AccentRed else Color(0xFF525252),
                        modifier = Modifier.size(17.dp),
                    )
                }
            }

            // Col 6: Options
            if (onOptionsClick != null) {
                IconButton(onClick = onOptionsClick, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Options",
                        tint = Color(0xFF888892),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        // Bottom divider — border-b border-divider
        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF1E1E22), modifier = Modifier.padding(horizontal = 16.dp))
    }
}

/** 3 animated red bars — basim-music equalizer.gif equivalent */
@Composable
fun PlayingBarsIndicator() {
    val inf = rememberInfiniteTransition(label = "eq")
    val b1 by inf.animateFloat(initialValue = 0.3f, targetValue = 1f, label = "b1",
        animationSpec = infiniteRepeatable(tween(420, easing = EaseInOutSine), RepeatMode.Reverse))
    val b2 by inf.animateFloat(initialValue = 1f, targetValue = 0.3f, label = "b2",
        animationSpec = infiniteRepeatable(tween(320, easing = EaseInOutSine), RepeatMode.Reverse))
    val b3 by inf.animateFloat(initialValue = 0.5f, targetValue = 1f, label = "b3",
        animationSpec = infiniteRepeatable(tween(500, easing = EaseInOutSine), RepeatMode.Reverse))

    Row(
        modifier = Modifier.size(18.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        listOf(b1, b2, b3).forEach { h ->
            Box(modifier = Modifier.width(3.dp).fillMaxHeight(h).clip(RoundedCornerShape(2.dp)).background(AccentRed))
        }
    }
}

/**
 * Track Options Bottom Sheet matching Image 4.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackOptionsMenuSheet(
    item: MediaItem,
    onDismiss: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    onNavigateToFolder: (String) -> Unit,
    onNavigateToGenre: (String) -> Unit,
    onAddToPlaylist: () -> Unit = {},
    onDelete: () -> Unit = {},
    onShowInfo: () -> Unit = {},
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF161619),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF3A3A42))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Track Card (Image 4)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E24))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Album Art
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF111114)),
                    contentAlignment = Alignment.Center
                ) {
                    AlbumArtImage(uri = item.albumArtUri, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)))
                }

                // Info & Thumbs
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (item.album.isNotBlank() && item.album != "<unknown>") "${item.artist} - ${item.album}" else item.artist,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Format badge & Thumbs Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "♪ ${item.duration.formatDuration()} | mp3",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                color = Color(0xFFA1A1AA)
                            )
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Thumbs Up
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (item.isFavorite) AccentRed.copy(alpha = 0.25f) else Color(0xFF2B2B33))
                                    .clickable(onClick = onFavoriteToggle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ThumbUp,
                                    contentDescription = "Like",
                                    tint = if (item.isFavorite) AccentRed else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            // Thumbs Down
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2B2B33))
                                    .clickable { onDismiss() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ThumbDown,
                                    contentDescription = "Dislike",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2-Column Action Grid (matching Image 4)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Row 1: Delete
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrackOptionButton(icon = Icons.Rounded.Delete, label = "Delete", onClick = onDelete, modifier = Modifier.weight(1f))
                    Spacer(Modifier.weight(1f))
                }

                // Row 2: Playlist & Bookmark
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrackOptionButton(icon = Icons.Rounded.CheckCircleOutline, label = "Playlist", onClick = onAddToPlaylist, modifier = Modifier.weight(1f))
                    TrackOptionButton(icon = if (item.isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder, label = "Bookmark", onClick = onFavoriteToggle, modifier = Modifier.weight(1f))
                }

                // Row 3: Album Art
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrackOptionButton(icon = Icons.Rounded.Image, label = "Album Art", onClick = onShowInfo, modifier = Modifier.weight(1f))
                    Spacer(Modifier.weight(1f))
                }

                // Row 4: Info/Tags & Lyrics
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrackOptionButton(icon = Icons.Rounded.Info, label = "Info/Tags", onClick = onShowInfo, modifier = Modifier.weight(1f))
                    TrackOptionButton(icon = Icons.AutoMirrored.Rounded.Notes, label = "Lyrics", onClick = onShowInfo, modifier = Modifier.weight(1f))
                }

                // Row 5: Artist & Album
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrackOptionButton(icon = Icons.Rounded.Mic, label = "Artist", onClick = { onNavigateToArtist(item.artist) }, modifier = Modifier.weight(1f))
                    TrackOptionButton(icon = Icons.Rounded.Album, label = "Album", onClick = { onNavigateToAlbum(item.album) }, modifier = Modifier.weight(1f))
                }

                // Row 6: Folder & Genre
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrackOptionButton(icon = Icons.Rounded.Folder, label = "Folder", onClick = { onNavigateToFolder(item.folder) }, modifier = Modifier.weight(1f))
                    TrackOptionButton(icon = Icons.Rounded.MusicNote, label = "Genre", onClick = { onNavigateToGenre(item.genre) }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun TrackOptionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = Color.White
        )
    }
}
