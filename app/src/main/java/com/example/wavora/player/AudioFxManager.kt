package com.example.wavora.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.example.wavora.data.EqualizerState

/**
 * Manages native Android AudioFx applied to the ExoPlayer audio session.
 */
class AudioFxManager {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private var currentSessionId: Int = 0

    fun initSession(sessionId: Int) {
        if (sessionId == 0 || sessionId == currentSessionId) return
        release()
        currentSessionId = sessionId

        try {
            equalizer = Equalizer(0, sessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.w("AudioFxManager", "Equalizer init error: ${e.message}")
        }

        try {
            bassBoost = BassBoost(0, sessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.w("AudioFxManager", "BassBoost init error: ${e.message}")
        }

        try {
            virtualizer = Virtualizer(0, sessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.w("AudioFxManager", "Virtualizer init error: ${e.message}")
        }

        try {
            loudnessEnhancer = LoudnessEnhancer(sessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.w("AudioFxManager", "LoudnessEnhancer init error: ${e.message}")
        }
    }

    fun applyState(state: EqualizerState) {
        val isEnabled = state.isEngineOn

        // 1. Equalizer Bands
        equalizer?.let { eq ->
            try {
                eq.enabled = isEnabled
                if (isEnabled) {
                    val numBands = eq.numberOfBands.toInt()
                    val range = eq.bandLevelRange // e.g. -1500 to 1500 mB (-15dB to +15dB)
                    val minLevel = range?.getOrNull(0) ?: -1200
                    val maxLevel = range?.getOrNull(1) ?: 1200

                    for (i in 0 until numBands) {
                        val centerFreq = try { eq.getCenterFreq(i.toShort()) / 1000 } catch (_: Exception) { 1000 } // Hz
                        val srcIdx = (i * state.bands.size / numBands).coerceIn(0, state.bands.size - 1)
                        var dbGain = state.bands[srcIdx] + state.preGain + state.toneGainStage

                        // Tone shaping additions based on frequency
                        if (centerFreq <= 250) {
                            dbGain += (state.bassEnhancement / 100f) * 6f
                        } else if (centerFreq in 500..2000) {
                            dbGain += state.midRange
                        } else if (centerFreq >= 3000) {
                            dbGain += (state.trebleClarity / 100f) * 6f
                        }

                        // Loudness shelf compensation
                        if (state.isLoudnessEqualizerOn) {
                            if (centerFreq <= 250) dbGain += 3.5f
                            if (centerFreq >= 4000) dbGain += 2f
                        }

                        // 1 dB = 100 millibels
                        val targetMb = (dbGain * 100).toInt().coerceIn(minLevel.toInt(), maxLevel.toInt()).toShort()
                        eq.setBandLevel(i.toShort(), targetMb)
                    }
                }
            } catch (e: Exception) {
                Log.w("AudioFxManager", "Failed applying EQ band: ${e.message}")
            }
        }

        // 2. Bass Boost
        bassBoost?.let { bb ->
            try {
                bb.enabled = isEnabled && (state.bassEnhancement > 0f || state.subBassSynthesizer > 0f)
                if (bb.enabled) {
                    val strength = ((state.bassEnhancement * 0.7f + state.subBassSynthesizer * 0.3f) * 10)
                        .toInt().coerceIn(0, 1000).toShort()
                    bb.setStrength(strength)
                }
            } catch (e: Exception) {
                Log.w("AudioFxManager", "Failed applying BassBoost: ${e.message}")
            }
        }

        // 3. Virtualizer (Spatial Audio)
        virtualizer?.let { virt ->
            try {
                virt.enabled = isEnabled && (state.spatialWidthPercent > 100f)
                if (virt.enabled) {
                    val strength = ((state.spatialWidthPercent - 100f) * 10)
                        .toInt().coerceIn(0, 1000).toShort()
                    virt.setStrength(strength)
                }
            } catch (e: Exception) {
                Log.w("AudioFxManager", "Failed applying Virtualizer: ${e.message}")
            }
        }

        // 4. Loudness Enhancer
        loudnessEnhancer?.let { le ->
            try {
                le.enabled = isEnabled && state.isLoudnessEqualizerOn
                if (le.enabled) {
                    le.setTargetGain(1200) // +1.2 dB target loudness
                }
            } catch (e: Exception) {
                Log.w("AudioFxManager", "Failed applying Loudness: ${e.message}")
            }
        }
    }

    fun release() {
        try { equalizer?.release() } catch (_: Exception) {}
        try { bassBoost?.release() } catch (_: Exception) {}
        try { virtualizer?.release() } catch (_: Exception) {}
        try { loudnessEnhancer?.release() } catch (_: Exception) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
        currentSessionId = 0
    }
}
