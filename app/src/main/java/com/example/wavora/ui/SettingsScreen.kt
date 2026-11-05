package com.example.wavora.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wavora.MediaViewModel
import com.example.wavora.theme.*

@Composable
fun SettingsScreen(viewModel: MediaViewModel, onBack: () -> Unit, onOpenEqualizer: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var isBackgroundVideoAudioEnabled by remember { mutableStateOf(true) }
    var isIgnoreShortClipsEnabled by remember { mutableStateOf(true) }
    var isPauseOnUnplugEnabled by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBack, modifier = Modifier.size(42.dp).clip(CircleShape).background(Color(0xFF111111)).border(1.dp, Color(0xFF1E1E1E), CircleShape)) {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text(text = "Settings", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp), color = Color.White)
                Spacer(Modifier.size(42.dp))
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { SettingsSectionHeader(title = "LIBRARY & SCANNING") }
            item { SettingsActionCard(icon = Icons.Rounded.FolderSpecial, title = "Manage Music Folders", subtitle = "Select scanned storage directories and exclude folders", badge = "${uiState.folders.size} Folders", onClick = { viewModel.openFoldersDialog() }) }
            item { SettingsActionCard(icon = Icons.Rounded.Refresh, title = "Rescan Device Media", subtitle = "Refresh library index and discover newly added audio files", badge = "Scan", onClick = { viewModel.loadMedia() }) }
            item { SettingsSwitchCard(icon = Icons.Rounded.Timer, title = "Ignore Short Audio Clips", subtitle = "Exclude notification chimes and clips under 30 seconds", checked = isIgnoreShortClipsEnabled, onCheckedChange = { isIgnoreShortClipsEnabled = it }) }
            item { Spacer(Modifier.height(4.dp)); SettingsSectionHeader(title = "AUDIO & PLAYBACK") }
            item { SettingsActionCard(icon = Icons.Rounded.GraphicEq, title = "Parametric EQ & DSP Studio", subtitle = "Multi-band EQ, Echo, Noise, Bass enhancement and Spatial audio", badge = "Open EQ", onClick = onOpenEqualizer) }
            item { SettingsSwitchCard(icon = Icons.Rounded.Headphones, title = "Background Video Audio", subtitle = "Continue playing audio when app is minimized or screen locked", checked = isBackgroundVideoAudioEnabled, onCheckedChange = { isBackgroundVideoAudioEnabled = it }) }
            item { SettingsSwitchCard(icon = Icons.AutoMirrored.Rounded.VolumeOff, title = "Pause on Headphone Disconnect", subtitle = "Automatically pause when headphones or Bluetooth unplugs", checked = isPauseOnUnplugEnabled, onCheckedChange = { isPauseOnUnplugEnabled = it }) }
            item { Spacer(Modifier.height(4.dp)); SettingsSectionHeader(title = "ANDROID AUTO") }
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ChipBg), border = BorderStroke(1.dp, BorderLight)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF111113)).border(1.dp, BorderLight, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Rounded.DirectionsCar, contentDescription = null, tint = AccentRed, modifier = Modifier.size(22.dp))
                            }
                            Column {
                                Text(text = "Android Auto Media Engine", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                                Text(text = "Connected & Active with MediaSession 3", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = EngineGreen)
                            }
                        }
                        HorizontalDivider(color = BorderLight)
                        Text(text = "Tabs: Library, Playlists, Albums, Folders, Videos\nCustom Controls: Shuffle, Close, Repeat, More\nFast A-Z Index Scroller enabled on car screen\nYouTube videos and local videos streamable to car", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 20.sp), color = TextSecondary)
                    }
                }
            }
            item { Spacer(Modifier.height(4.dp)); SettingsSectionHeader(title = "ABOUT") }
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ChipBg), border = BorderStroke(1.dp, BorderLight)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "Wavora Media Player", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                            Text(text = "High-Fidelity Audio & Video Engine  \u00b7  v1.2.0", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextMuted)
                        }
                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(AccentRed).padding(horizontal = 10.dp, vertical = 5.dp)) {
                            Text(text = "PRO", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp), color = Color.White)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(text = title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.2.sp), color = TextMuted, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
}

@Composable
fun SettingsActionCard(icon: ImageVector, title: String, subtitle: String, badge: String? = null, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = ChipBg), border = BorderStroke(1.dp, BorderLight)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF111113)).border(1.dp, BorderLight, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp), color = Color.White)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextSecondary)
            }
            if (badge != null) {
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF27272A)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(text = badge, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp), color = Color.White)
                }
            }
            Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun SettingsSwitchCard(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = ChipBg), border = BorderStroke(1.dp, BorderLight)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF111113)).border(1.dp, BorderLight, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = if (checked) AccentRed else Color.White, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp), color = Color.White)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextSecondary)
            }
            Switch(
                checked = checked, onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AccentRed,
                    uncheckedThumbColor = Color(0xFF71717A),
                    uncheckedTrackColor = Color(0xFF1E1E22),
                    uncheckedBorderColor = BorderLight
                )
            )
        }
    }
}
