package com.example.wavora.data

import android.net.Uri

/**
 * Represents a single media item (audio or video) on the device.
 */
data class MediaItem(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtUri: Uri?,
    val contentUri: Uri,
    val duration: Long,      // milliseconds
    val mediaType: MediaType,
    val dateAdded: Long,
    val folder: String = "Internal Storage",
    val path: String = "",
    val genre: String = "Various",
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayed: Long = 0L,
)

enum class MediaType {
    AUDIO, VIDEO
}

data class Album(
    val name: String,
    val artist: String,
    val artUri: Uri?,
    val items: List<MediaItem>,
    val songCount: Int = items.size,
)

data class Artist(
    val name: String,
    val artUri: Uri?,
    val items: List<MediaItem>,
    val albumCount: Int,
    val songCount: Int = items.size,
)

data class MediaFolder(
    val name: String,
    val path: String,
    val items: List<MediaItem>,
    val songCount: Int = items.size,
)

data class SelectableFolder(
    val name: String,
    val path: String,
    val storageLabel: String = "Internal Storage",
    val isSelected: Boolean = true,
    val songCount: Int = 0,
)

data class MediaPlaylist(
    val id: String,
    val name: String,
    val items: List<MediaItem>,
    val songCount: Int = items.size,
)

data class MediaGenre(
    val name: String,
    val items: List<MediaItem>,
    val songCount: Int = items.size,
)

/**
 * Equalizer and DSP Engine State
 */
data class EqualizerState(
    val isEngineOn: Boolean = true,
    val selectedTab: Int = 0, // 0: EQ, 1: Tone, 2: Reverb, 3: Spatial
    val activePreset: String = "Flat",
    val preGain: Float = 0f, // -12dB to +12dB
    // 10 bands: 32Hz, 64Hz, 125Hz, 250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz, 16kHz
    val bands: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
    
    // Tone & Balance
    val bassEnhancement: Float = 0f, // 0..100%
    val trebleClarity: Float = 0f,   // 0..100%
    val midRange: Float = 0f,        // -12..+12 dB
    val toneGainStage: Float = 0f,   // -12..+12 dB
    val stereoBalance: Float = 0f,   // -1.0 (Left) .. 0.0 (Center) .. 1.0 (Right)
    val subBassSynthesizer: Float = 0f, // 0..100%

    // Dynamics & Reverb
    val reverbWetPercent: Float = 70f, // 0..100%
    val reverbPreset: String = "Hall", // Off, Studio, Chamber, Hall, Cathedral
    val isCompressorOn: Boolean = false,
    val isLoudnessEqualizerOn: Boolean = true,

    // Spatial Audio
    val spatialWidthPercent: Float = 140f, // 0..200%
    val spatialPreset: String = "Wide", // Mono, Narrow, Standard, Wide, Ultra 3D
)

/**
 * Formats duration millis → "m:ss" or "h:mm:ss"
 */
fun Long.formatDuration(): String {
    val totalSeconds = this / 1000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) {
        "%d:%02d:%02d".format(h, m, s)
    } else {
        "%d:%02d".format(m, s)
    }
}
