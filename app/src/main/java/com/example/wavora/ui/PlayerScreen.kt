package com.example.wavora.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.example.wavora.MediaViewModel
import com.example.wavora.data.formatDuration
import com.example.wavora.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Full-screen audio player with:
 * - Queue bottom sheet (tap Queue icon in top-right)
 * - Album & Folder context chips below track info for quick navigation
 * - EQ shortcut, Favorite, Shuffle, Repeat, Volume controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: MediaViewModel,
    onBack: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onGoToAlbum: (String) -> Unit = {},
    onGoToFolder: (String) -> Unit = {},
) {
    val playerState by viewModel.playerState.collectAsState()
    val controller = viewModel.playerController
    val item = playerState.currentItem

    var position by remember { mutableStateOf(0L) }
    LaunchedEffect(playerState.isPlaying) {
        while (playerState.isPlaying) {
            position = controller.getCurrentPosition()
            delay(350)
        }
    }
    LaunchedEffect(playerState.currentItem) { position = 0L }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showQueue by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Queue Bottom Sheet
    if (showQueue) {
        ModalBottomSheet(
            onDismissRequest = { showQueue = false },
            sheetState = sheetState,
            containerColor = Color(0xFF0D0D12),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3A3A45))
                )
            }
        ) {
            Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Now Playing Queue", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Text("${playerState.queue.size} tracks", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextMuted)
                    }
                    IconButton(
                        onClick = { scope.launch { sheetState.hide(); showQueue = false } },
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF1E1E26))
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(playerState.queue) { idx, qItem ->
                        val isCurrent = playerState.currentItem?.id == qItem.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isCurrent) Color(0xFF0A1525) else Color.Transparent)
                                .clickable { controller.play(qItem, playerState.queue); scope.launch { sheetState.hide(); showQueue = false } }
                                .padding(horizontal = 20.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                if (isCurrent) PlayingBarsIndicator()
                                else Text("${idx + 1}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = TextMuted)
                            }
                            Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF141418)), contentAlignment = Alignment.Center) {
                                AlbumArtImage(uri = qItem.albumArtUri, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = qItem.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal, fontSize = 13.sp), color = if (isCurrent) AccentRed else Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = qItem.artist, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text(text = qItem.duration.formatDuration(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextMuted)
                        }
                        HorizontalDivider(color = Color(0xFF1A1A20), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 20.dp))
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // TOP BAR
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                    Icon(imageVector = Icons.Rounded.KeyboardArrowDown, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Text("NOW PLAYING", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold, fontSize = 11.sp), color = TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isFav = item?.isFavorite == true
                    IconButton(onClick = { item?.id?.let { viewModel.toggleFavorite(it) } }, modifier = Modifier.size(44.dp)) {
                        Icon(imageVector = if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, contentDescription = "Favorite", tint = if (isFav) AccentRed else TextSecondary, modifier = Modifier.size(22.dp))
                    }
                    IconButton(onClick = { showQueue = true }, modifier = Modifier.size(44.dp)) {
                        Icon(imageVector = Icons.Rounded.QueueMusic, contentDescription = "Queue", tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onOpenEqualizer, modifier = Modifier.size(44.dp)) {
                        Icon(imageVector = Icons.Rounded.Tune, contentDescription = "EQ", tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // ALBUM ART
            Box(
                modifier = Modifier.fillMaxWidth(0.88f).aspectRatio(1f)
                    .shadow(elevation = 32.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color.Black, spotColor = AccentRedGlow)
                    .clip(RoundedCornerShape(16.dp)).background(ChipBg),
                contentAlignment = Alignment.Center,
            ) {
                AlbumArtImage(uri = item?.albumArtUri, modifier = Modifier.fillMaxSize())
            }

            // TRACK INFO + CONTEXT CHIPS
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item?.title ?: "No track playing",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                    color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                if (item != null) {
                    val sub = buildString {
                        if (item.album.isNotBlank() && item.album != "<unknown>") { append(item.album); append("  ·  ") }
                        append(item.artist)
                    }
                    Text(text = sub, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp), color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (item.album.isNotBlank() && item.album != "<unknown>") {
                            Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xFF141418)).border(1.dp, BorderLight, RoundedCornerShape(20.dp)).clickable { onGoToAlbum(item.album) }.padding(horizontal = 10.dp, vertical = 5.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Rounded.Album, null, tint = AccentRed, modifier = Modifier.size(12.dp))
                                    Text(text = item.album, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFFCCCCCC), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                        if (item.folder.isNotBlank()) {
                            Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xFF141418)).border(1.dp, BorderLight, RoundedCornerShape(20.dp)).clickable { onGoToFolder(item.folder) }.padding(horizontal = 10.dp, vertical = 5.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Rounded.Folder, null, tint = Color(0xFF71717A), modifier = Modifier.size(12.dp))
                                    Text(text = item.folder.substringAfterLast('/').ifBlank { item.folder }, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFFAAAAAA), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }

            // SEEK BAR
            val duration = playerState.duration.coerceAtLeast(1L)
            val progress = (position.toFloat() / duration).coerceIn(0f, 1f)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = position.formatDuration(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = TextSecondary, modifier = Modifier.widthIn(min = 32.dp))
                    Slider(value = progress, onValueChange = { v -> val newPos = (v * duration).toLong(); position = newPos; controller.seekTo(newPos) }, modifier = Modifier.weight(1f), colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = AccentRed, inactiveTrackColor = Color(0xFF242429)))
                    Text(text = duration.formatDuration(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = TextMuted, modifier = Modifier.widthIn(min = 32.dp))
                }
            }

            // PLAYBACK CONTROLS
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = { controller.toggleShuffle() }, modifier = Modifier.size(48.dp)) {
                    Icon(imageVector = Icons.Rounded.Shuffle, contentDescription = "Shuffle", tint = if (playerState.shuffleEnabled) AccentRed else Color(0xFF71717A), modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = { controller.skipPrevious() }, modifier = Modifier.size(48.dp)) {
                    Icon(imageVector = Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Box(
                    modifier = Modifier.size(64.dp).shadow(elevation = 18.dp, shape = CircleShape, ambientColor = AccentRedGlow, spotColor = AccentRed).clip(CircleShape).background(AccentRed).clickable { controller.togglePlayPause() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = "Play/Pause", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = { controller.skipNext() }, modifier = Modifier.size(48.dp)) {
                    Icon(imageVector = Icons.Rounded.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                val isRepeat = playerState.repeatMode != Player.REPEAT_MODE_OFF
                IconButton(onClick = { controller.toggleRepeat() }, modifier = Modifier.size(48.dp)) {
                    Icon(imageVector = if (playerState.repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat, contentDescription = "Repeat", tint = if (isRepeat) AccentRed else Color(0xFF71717A), modifier = Modifier.size(24.dp))
                }
            }

            // VOLUME
            var volume by remember { mutableStateOf(0.85f) }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { volume = if (volume > 0f) 0f else 0.85f }, modifier = Modifier.size(40.dp)) {
                    Icon(imageVector = when { volume == 0f -> Icons.AutoMirrored.Rounded.VolumeOff; volume < 0.5f -> Icons.AutoMirrored.Rounded.VolumeDown; else -> Icons.AutoMirrored.Rounded.VolumeUp }, contentDescription = "Volume", tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
                Slider(value = volume, onValueChange = { volume = it }, modifier = Modifier.weight(1f), colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = AccentRed, inactiveTrackColor = Color(0xFF242429)))
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}
