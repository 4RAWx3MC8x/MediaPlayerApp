package com.example.wavora.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wavora.data.MediaItem
import com.example.wavora.data.formatDuration
import com.example.wavora.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Wavora YouTube Manager Screen.
 * Allows users to:
 *  1. Paste a YouTube URL and open it in the YouTube app
 *  2. Open YouTube videos in an external downloader
 *  3. View already-saved YouTube videos from local storage
 *  4. Cast / share YouTube videos to car screen
 *
 * NOTE: YouTube's terms of service restrict direct downloading via their API.
 * This screen routes through system share/intent to trusted third-party tools.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeManagerScreen(
    savedYouTubeVideos: List<MediaItem> = emptyList(),
    onBack: () -> Unit,
    onOpenVideoPlayer: (MediaItem) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var youtubeUrl by remember { mutableStateOf("") }
    var isUrlValid by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Validate URL as user types
    LaunchedEffect(youtubeUrl) {
        isUrlValid = youtubeUrl.isNotBlank() &&
            (youtubeUrl.contains("youtube.com/watch") ||
             youtubeUrl.contains("youtu.be/") ||
             youtubeUrl.contains("youtube.com/shorts"))
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DarkCard)
                        .border(1.dp, BorderLight, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(YouTubeRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "YouTube",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color.White
                    )
                }

                Spacer(Modifier.size(44.dp))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── URL Input Card ───────────────────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Open YouTube Video",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = Color.White
                        )

                        // URL field
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkSurface)
                                .border(
                                    1.dp,
                                    if (isUrlValid) AccentBlue.copy(alpha = 0.6f) else BorderLight,
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Link,
                                    contentDescription = null,
                                    tint = if (isUrlValid) AccentBlue else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                BasicTextField(
                                    value = youtubeUrl,
                                    onValueChange = { youtubeUrl = it },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontSize = 13.sp
                                    ),
                                    modifier = Modifier.weight(1f),
                                    decorationBox = { inner ->
                                        Box {
                                            if (youtubeUrl.isEmpty()) {
                                                Text(
                                                    "Paste YouTube link here…",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                                    color = TextMuted
                                                )
                                            }
                                            inner()
                                        }
                                    }
                                )
                                if (youtubeUrl.isNotEmpty()) {
                                    IconButton(
                                        onClick = { youtubeUrl = "" },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Close,
                                            contentDescription = "Clear",
                                            tint = TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Open in YouTube app
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isUrlValid) YouTubeRed else DarkSurface
                                    )
                                    .clickable(enabled = isUrlValid) {
                                        openYouTubeUrl(context, youtubeUrl)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.PlayArrow,
                                        contentDescription = null,
                                        tint = if (isUrlValid) Color.White else TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "Open",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        ),
                                        color = if (isUrlValid) Color.White else TextMuted
                                    )
                                }
                            }

                            // Save via external downloader
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isUrlValid) AccentBlue else DarkSurface
                                    )
                                    .clickable(enabled = isUrlValid) {
                                        scope.launch {
                                            isLoading = true
                                            statusMessage = null
                                            delay(400)
                                            // Route through system downloader / share
                                            shareToDownloader(context, youtubeUrl)
                                            statusMessage = "Sent to downloader"
                                            isLoading = false
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Download,
                                            contentDescription = null,
                                            tint = if (isUrlValid) Color.White else TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            "Save",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            ),
                                            color = if (isUrlValid) Color.White else TextMuted
                                        )
                                    }
                                }
                            }
                        }

                        // Status message
                        AnimatedVisibility(visible = statusMessage != null) {
                            statusMessage?.let { msg ->
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = EngineGreen,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // ── How it works info ────────────────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E35)),
                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp).padding(top = 1.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "How to save YouTube videos",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Tap Save to open your video in a compatible downloader app (like YT-DLP, Snaptube, or VidMate). Once saved, it will appear in your Videos library automatically.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    lineHeight = 17.sp
                                ),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // ── Car Screen section ───────────────────────────────────────────────
            item {
                Text(
                    text = "Play on Car Screen",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = Color.White
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CarActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.DirectionsCar,
                        title = "Android Auto",
                        subtitle = "Connect car display",
                        iconTint = CarBlue,
                        onClick = {
                            try {
                                val intent = context.packageManager
                                    .getLaunchIntentForPackage("com.google.android.projection.gearhead")
                                    ?: Intent("android.intent.action.VIEW").apply {
                                        data = Uri.parse("androidauto://")
                                    }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    )
                    CarActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Cast,
                        title = "Cast Screen",
                        subtitle = "Stream to TV / display",
                        iconTint = Color(0xFF34D399),
                        onClick = {
                            try {
                                val intent = Intent("android.settings.CAST_SETTINGS")
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    )
                }
            }

            // ── Saved YouTube Videos ─────────────────────────────────────────────
            if (savedYouTubeVideos.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Saved videos (${savedYouTubeVideos.size})",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = Color.White
                    )
                }
                items(savedYouTubeVideos) { video ->
                    SavedVideoRow(
                        item = video,
                        onClick = { onOpenVideoPlayer(video) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun CarActionCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun SavedVideoRow(item: MediaItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkCard)
            .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.VideoFile, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(26.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = item.duration.formatDuration(),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextSecondary
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AccentBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────
private fun openYouTubeUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.setPackage("com.google.android.youtube")
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) {}
    }
}

private fun shareToDownloader(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        context.startActivity(Intent.createChooser(intent, "Save video with…"))
    } catch (_: Exception) {}
}
