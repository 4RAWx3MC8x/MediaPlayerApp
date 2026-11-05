package com.example.wavora.ui

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.BoxScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.wavora.MainActivity
import com.example.wavora.data.MediaItem
import com.example.wavora.data.formatDuration
import com.example.wavora.player.WavoraPlaybackService
import com.example.wavora.theme.AccentBlue
import com.example.wavora.theme.CarBlue
import kotlinx.coroutines.delay

/**
 * Wavora 2.0 — Cinema-grade OLED Video Player.
 * Features:
 *  - Car screen sharing (Android Auto / Cast intent)
 *  - Resize mode toggle (fit / zoom)
 *  - Picture-in-Picture
 *  - Auto-hide controls
 */
@Composable
fun VideoPlayerScreen(
    mediaItem: MediaItem,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var showControls by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var position by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(1L) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var showCarDialog by remember { mutableStateOf(false) }

    val servicePlayer = WavoraPlaybackService.activePlayer
    val player = remember {
        val media3Item = Media3Item.Builder()
            .setMediaId("video_${mediaItem.id}")
            .setUri(mediaItem.contentUri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(mediaItem.title)
                    .setArtist(mediaItem.folder)
                    .setAlbumTitle("Videos")
                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    .setIsPlayable(true)
                    .setIsBrowsable(false)
                    .build()
            )
            .build()

        if (servicePlayer != null) {
            servicePlayer.apply {
                setMediaItem(media3Item)
                prepare()
                playWhenReady = true
            }
        } else {
            ExoPlayer.Builder(context).build().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                        .setUsage(C.USAGE_MEDIA)
                        .build(),
                    true
                )
                setHandleAudioBecomingNoisy(true)
                setMediaItem(media3Item)
                prepare()
                playWhenReady = true
            }
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(player) {
        while (true) {
            position = player.currentPosition
            duration = player.duration.coerceAtLeast(1L)
            delay(500)
        }
    }

    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(3500)
            showControls = false
        }
    }

    DisposableEffect(Unit) {
        MainActivity.isVideoPlaying = true
        onDispose {
            MainActivity.isVideoPlaying = false
            if (servicePlayer == null) {
                player.release()
            }
        }
    }

    // ─── Car Screen Share Dialog ──────────────────────────────────────────────
    if (showCarDialog) {
        CarShareDialog(
            mediaItem = mediaItem,
            context = context,
            onDismiss = { showCarDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { showControls = !showControls },
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    this.resizeMode = resizeMode
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
            },
            update = { playerView ->
                playerView.player = player
                playerView.resizeMode = resizeMode
            },
            modifier = Modifier.fillMaxSize(),
        )

        // ─── Control Overlay ────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.50f))
            ) {
                // ── Top Bar ──────────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        VideoControlButton(
                            onClick = onBack,
                            size = 42,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = mediaItem.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // ── Car Screen Share ──────────────────────────────────────
                        VideoControlButton(
                            onClick = { showCarDialog = true },
                            size = 40,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DirectionsCar,
                                contentDescription = "Share to Car",
                                tint = CarBlue,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        // ── Aspect Ratio Toggle ───────────────────────────────────
                        VideoControlButton(
                            onClick = {
                                resizeMode = if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                } else {
                                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                            },
                            size = 40,
                        ) {
                            Icon(
                                imageVector = if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT)
                                    Icons.Rounded.Fullscreen else Icons.Rounded.FullscreenExit,
                                contentDescription = "Resize Mode",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        // ── PiP ──────────────────────────────────────────────────
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            VideoControlButton(
                                onClick = {
                                    (context as? Activity)?.enterPictureInPictureMode(
                                        PictureInPictureParams.Builder().build()
                                    )
                                },
                                size = 40,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PictureInPictureAlt,
                                    contentDescription = "Picture in Picture",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }

                // ── Center Playback Controls ──────────────────────────────────────
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    VideoControlButton(
                        onClick = { player.seekTo((player.currentPosition - 10_000L).coerceAtLeast(0L)) },
                        size = 54,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Replay10,
                            contentDescription = "Back 10s",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp),
                        )
                    }

                    // Main play/pause with accent glow
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AccentBlue)
                            .clickable { if (isPlaying) player.pause() else player.play() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp),
                        )
                    }

                    VideoControlButton(
                        onClick = { player.seekTo(player.currentPosition + 10_000L) },
                        size = 54,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Forward10,
                            contentDescription = "Forward 10s",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }

                // ── Bottom Seek Bar ───────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                ) {
                    val progress = (position.toFloat() / duration).coerceIn(0f, 1f)
                    Slider(
                        value = progress,
                        onValueChange = { v ->
                            val newPos = (v * duration).toLong()
                            position = newPos
                            player.seekTo(newPos)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = AccentBlue,
                            activeTrackColor = AccentBlue,
                            inactiveTrackColor = Color.White.copy(alpha = 0.20f),
                        ),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = position.formatDuration(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Color.White.copy(alpha = 0.9f),
                        )
                        Text(
                            text = duration.formatDuration(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Color.White.copy(alpha = 0.55f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoControlButton(
    onClick: () -> Unit,
    size: Int,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

// ─── Car Screen Share Dialog ──────────────────────────────────────────────────
@Composable
fun CarShareDialog(
    mediaItem: MediaItem,
    context: Context,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1320),
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DirectionsCar,
                        contentDescription = null,
                        tint = CarBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Share to Car Screen",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color.White
                    )
                }
                Text(
                    text = mediaItem.title,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFF7B93B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CarShareOption(
                    icon = Icons.Rounded.DirectionsCar,
                    title = "Android Auto",
                    subtitle = "Play on your car's built-in screen via USB or wireless",
                    iconTint = CarBlue,
                    onClick = {
                        // Android Auto launches via the Media Session — the WavoraPlaybackService
                        // handles it. Here we surface the system's media output chooser.
                        try {
                            val intent = Intent("android.media.action.MEDIA_PLAY_FROM_SEARCH")
                            intent.putExtra("query", mediaItem.title)
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                        onDismiss()
                    }
                )
                CarShareOption(
                    icon = Icons.Rounded.Cast,
                    title = "Cast to Screen",
                    subtitle = "Stream to a Chromecast, smart TV or car display",
                    iconTint = Color(0xFF34D399),
                    onClick = {
                        try {
                            val shareIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(mediaItem.contentUri, "video/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Cast to Car Screen"))
                        } catch (_: Exception) {}
                        onDismiss()
                    }
                )
                CarShareOption(
                    icon = Icons.Rounded.Share,
                    title = "Share Video File",
                    subtitle = "Send to another device, app or Bluetooth receiver",
                    iconTint = Color(0xFFA78BFA),
                    onClick = {
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "video/*"
                                putExtra(Intent.EXTRA_STREAM, mediaItem.contentUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
                        } catch (_: Exception) {}
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF7B93B8), fontSize = 14.sp)
            }
        }
    )
}

@Composable
private fun CarShareOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF111827))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = Color.White)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp), color = Color(0xFF7B93B8))
        }
        Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null, tint = Color(0xFF3D5480), modifier = Modifier.size(18.dp))
    }
}
