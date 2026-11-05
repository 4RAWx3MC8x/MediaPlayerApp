package com.example.wavora.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.RotateLeft
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wavora.MediaViewModel
import com.example.wavora.data.EqualizerState
import com.example.wavora.theme.*
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * basim-music / MelodyFlow Equalizer Screen.
 * Master DSP engine with crimson red accents, animated spectrum canvas,
 * 10-band touch sliders, tone shaping, reverb acoustics, and spatial matrix.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    viewModel: MediaViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val eqState by viewModel.equalizerState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()

    val tabTitles = listOf("EQ", "Tone", "Reverb", "Spatial")
    val tabIcons = listOf(
        Icons.Rounded.Tune,
        Icons.Rounded.GraphicEq,
        Icons.Rounded.AutoAwesome,
        Icons.Rounded.SurroundSound,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(bottom = 90.dp)
    ) {
        // --- Header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ChipBg)
                        .border(1.dp, BorderLight, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = AccentRed,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = "Equalizer",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }
                Text(
                    text = "Fine-tune frequency curves, soundstage, acoustics, and tone dynamics",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextSecondary,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- Audio Engine & DSP Master Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ChipBg),
            border = BorderStroke(1.dp, BorderLight),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (eqState.isEngineOn) AccentRed else Color(0xFF242429)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ElectricBolt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = "Audio Engine & DSP",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                )
                                if (eqState.isEngineOn) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AccentRed.copy(alpha = 0.2f))
                                            .border(1.dp, AccentRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    ) {
                                        Text(
                                            text = "Active",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                            color = AccentRed,
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "10-Band Equalizer, Bass Boost & Spatial Enhancements",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = TextSecondary,
                                maxLines = 1,
                            )
                        }
                    }

                    // Engine Master Switch
                    Switch(
                        checked = eqState.isEngineOn,
                        onCheckedChange = { viewModel.toggleEqualizerEngine() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentRed,
                            uncheckedThumbColor = Color(0xFF71717A),
                            uncheckedTrackColor = Color(0xFF242429),
                        )
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Spectrum Visualizer Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = null,
                            tint = AccentRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Real-time Spectrum",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                    }
                    Text(
                        text = "32 Hz - 16 kHz",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                        color = TextMuted,
                    )
                }

                Spacer(Modifier.height(8.dp))

                SpectrumCanvas(
                    isPlaying = playerState.isPlaying && eqState.isEngineOn,
                    bands = eqState.bands,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0C0C0E))
                        .border(1.dp, BorderLight, RoundedCornerShape(10.dp))
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- Tabs Row (basim-music tabs) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ChipBg)
                .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                .padding(3.dp),
        ) {
            tabTitles.forEachIndexed { index, title ->
                val isSelected = eqState.selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isSelected) AccentRed else Color.Transparent)
                        .clickable { viewModel.setEqualizerTab(index) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = tabIcons[index],
                            contentDescription = title,
                            tint = if (isSelected) Color.White else TextSecondary,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.5.sp,
                            ),
                            color = if (isSelected) Color.White else TextSecondary,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- Tab Contents ---
        when (eqState.selectedTab) {
            0 -> EqTabContent(eqState = eqState, viewModel = viewModel)
            1 -> ToneTabContent(eqState = eqState, viewModel = viewModel)
            2 -> ReverbTabContent(eqState = eqState, viewModel = viewModel)
            3 -> SpatialTabContent(eqState = eqState, viewModel = viewModel)
        }
    }
}

/**
 * Animated Spectrum canvas for audio visualization
 */
@Composable
fun SpectrumCanvas(
    isPlaying: Boolean,
    bands: List<Float>,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "spectrumAnim")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spectrumPhase"
    )

    Canvas(modifier = modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
        val barCount = 36
        val spacing = 3.dp.toPx()
        val totalSpacing = spacing * (barCount - 1)
        val barWidth = (size.width - totalSpacing) / barCount
        val maxHeight = size.height

        for (i in 0 until barCount) {
            val bandIdx = (i * bands.size / barCount).coerceIn(0, bands.size - 1)
            val boost = ((bands[bandIdx] + 12f) / 24f).coerceIn(0.1f, 1f)

            val animatedHeight = if (isPlaying) {
                val wave = (sin(phase + i * 0.35f) + 1f) / 2f
                (maxHeight * (0.25f + 0.7f * wave * boost)).coerceIn(4f, maxHeight)
            } else {
                (maxHeight * 0.15f * boost).coerceIn(4f, maxHeight)
            }

            val x = i * (barWidth + spacing)
            val y = maxHeight - animatedHeight

            drawRect(
                color = if (i % 2 == 0) AccentRed else Color(0xFFFF4D4D),
                topLeft = Offset(x, y),
                size = Size(barWidth, animatedHeight)
            )
        }
    }
}

/**
 * Tab 1: 10-Band Graphic Equalizer with fully interactive vertical sliders
 */
@Composable
fun EqTabContent(
    eqState: EqualizerState,
    viewModel: MediaViewModel,
) {
    val frequencies = listOf("32Hz", "64Hz", "125Hz", "250Hz", "500Hz", "1kHz", "2kHz", "4kHz", "8kHz", "16kHz")
    val presets = listOf(
        "Flat", "Bass Boost", "Treble Boost", "Acoustic", "Classical",
        "Dance", "Electronic", "Hip-Hop", "Jazz", "Latin",
        "Lounge", "Metal", "Piano", "Pop", "R&B", "Rock", "Vocal", "Small Speakers"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ChipBg),
        border = BorderStroke(1.dp, BorderLight),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header & Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "10-Band Graphic Equalizer",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    Text(
                        text = "Touch and drag vertically to adjust (-12 dB to +12 dB)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondary,
                    )
                }

                Button(
                    onClick = { viewModel.resetEq() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF242429),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp),
                ) {
                    Icon(Icons.AutoMirrored.Rounded.RotateLeft, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reset", fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Presets Horizontal list (basim-music chips)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presets) { preset ->
                    val isSelected = eqState.activePreset == preset
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) AccentRed else Color(0xFF1E1E24))
                            .border(1.dp, if (isSelected) AccentRed else BorderLight, RoundedCornerShape(16.dp))
                            .clickable { viewModel.setPreset(preset) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = Color.White,
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Sliders Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0C0C0E))
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    .padding(vertical = 12.dp, horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Pre-Gain Slider
                    TouchVerticalSlider(
                        label = "PRE",
                        unit = "Gain",
                        gain = eqState.preGain,
                        onGainChanged = { viewModel.setPreGain(it) },
                        isPre = true,
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(200.dp)
                            .background(BorderLight)
                    )

                    // 10 bands
                    frequencies.forEachIndexed { index, freq ->
                        val currentGain = eqState.bands.getOrElse(index) { 0f }
                        TouchVerticalSlider(
                            label = freq,
                            unit = "",
                            gain = currentGain,
                            onGainChanged = { viewModel.setBandGain(index, it) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * True Touch-Driven Vertical Slider for Graphic Equalizer.
 */
@Composable
fun TouchVerticalSlider(
    label: String,
    unit: String,
    gain: Float,
    onGainChanged: (Float) -> Unit,
    isPre: Boolean = false,
) {
    var trackHeightPx by remember { mutableFloatStateOf(160f) }
    val currentOnGainChanged by rememberUpdatedState(onGainChanged)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(48.dp)
    ) {
        // Quick + Step button
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFF242429))
                .clickable {
                    val next = (gain + 1.0f).coerceIn(-12f, 12f)
                    currentOnGainChanged((next * 10).roundToInt() / 10f)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "+", tint = Color.White, modifier = Modifier.size(14.dp))
        }

        Spacer(Modifier.height(4.dp))

        // Gain readout
        Text(
            text = "%+.1f".format(gain),
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = if (gain != 0f) AccentRed else TextMuted,
        )

        Spacer(Modifier.height(6.dp))

        val trackHeightDp = 140.dp
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(trackHeightDp)
                .onGloballyPositioned { coords ->
                    trackHeightPx = coords.size.height.toFloat().coerceAtLeast(10f)
                }
                .pointerInput(trackHeightPx) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val updateGain = { y: Float ->
                            val rawFraction = (1f - (y / trackHeightPx)).coerceIn(0f, 1f)
                            val targetGain = -12f + rawFraction * 24f
                            val roundedGain = (targetGain * 10f).roundToInt() / 10f
                            currentOnGainChanged(roundedGain)
                        }
                        updateGain(down.position.y)

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            change.consume()
                            updateGain(change.position.y)
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            // Background track
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF242429))
            )

            // Center 0 dB dashed line
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(1.5.dp)
                    .background(Color(0xFF555566))
            )

            // Active bar from 0dB to thumb
            val fraction = ((gain + 12f) / 24f).coerceIn(0f, 1f)
            val centerFraction = 0.5f

            if (fraction >= centerFraction) {
                val barHeightFrac = fraction - centerFraction
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight(barHeightFrac)
                        .align(Alignment.Center)
                        .offset(y = -(trackHeightDp * barHeightFrac / 2))
                        .background(AccentRed)
                )
            } else {
                val barHeightFrac = centerFraction - fraction
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight(barHeightFrac)
                        .align(Alignment.Center)
                        .offset(y = trackHeightDp * barHeightFrac / 2)
                        .background(Color(0xFF888894))
                )
            }

            // Draggable Red/White Thumb
            val thumbOffsetFraction = (1f - fraction) - 0.5f
            Box(
                modifier = Modifier
                    .offset(y = trackHeightDp * thumbOffsetFraction)
                    .width(28.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color.White)
                    .border(1.5.dp, AccentRed, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(2.dp)
                        .background(AccentRed)
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // Quick - Step button
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFF242429))
                .clickable {
                    val next = (gain - 1.0f).coerceIn(-12f, 12f)
                    currentOnGainChanged((next * 10).roundToInt() / 10f)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Remove, contentDescription = "-", tint = Color.White, modifier = Modifier.size(14.dp))
        }

        Spacer(Modifier.height(6.dp))

        // Frequency Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isPre) FontWeight.Bold else FontWeight.Medium,
                fontSize = 10.sp,
            ),
            color = if (isPre) Color.White else Color(0xFFDDDDDD),
        )
        if (unit.isNotEmpty()) {
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = TextMuted,
            )
        }
    }
}

/**
 * Tab 2: Tone & Balance
 */
@Composable
fun ToneTabContent(
    eqState: EqualizerState,
    viewModel: MediaViewModel,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ChipBg),
        border = BorderStroke(1.dp, BorderLight),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Tone & Soundstage",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    Text(
                        text = "Bass enhancement, treble brilliance, and stereo imaging",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondary,
                    )
                }

                Button(
                    onClick = {
                        viewModel.setBassEnhancement(0f)
                        viewModel.setTrebleClarity(0f)
                        viewModel.setMidRange(0f)
                        viewModel.setStereoBalance(0f)
                        viewModel.setSubBass(0f)
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF242429),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp),
                ) {
                    Icon(Icons.AutoMirrored.Rounded.RotateLeft, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reset", fontSize = 11.sp)
                }
            }

            ToneControlCard(
                title = "Bass Enhancement",
                subtitle = "Low-end punch & warmth",
                valueText = "%.0f%%".format(eqState.bassEnhancement),
                sliderValue = eqState.bassEnhancement,
                valueRange = 0f..100f,
                step = 5f,
                onValueChange = { viewModel.setBassEnhancement(it) },
                minLabel = "Flat (0%)",
                maxLabel = "Max (+12dB)",
            )

            ToneControlCard(
                title = "Treble Clarity",
                subtitle = "High-frequency brilliance & air",
                valueText = "%.0f%%".format(eqState.trebleClarity),
                sliderValue = eqState.trebleClarity,
                valueRange = 0f..100f,
                step = 5f,
                onValueChange = { viewModel.setTrebleClarity(it) },
                minLabel = "Flat (0%)",
                maxLabel = "Max (+12dB)",
            )

            ToneControlCard(
                title = "Mid-Range (1 kHz)",
                subtitle = "Vocals & acoustic presence",
                valueText = "%+.1f dB".format(eqState.midRange),
                sliderValue = eqState.midRange,
                valueRange = -12f..12f,
                step = 1f,
                onValueChange = { viewModel.setMidRange(it) },
                minLabel = "-12 dB",
                maxLabel = "+12 dB",
            )

            ToneControlCard(
                title = "Stereo Balance",
                subtitle = "Left & Right speaker balance",
                valueText = when {
                    eqState.stereoBalance < -0.1f -> "Left %.0f%%".format(-eqState.stereoBalance * 100)
                    eqState.stereoBalance > 0.1f -> "Right %.0f%%".format(eqState.stereoBalance * 100)
                    else -> "Center"
                },
                sliderValue = eqState.stereoBalance,
                valueRange = -1f..1f,
                step = 0.1f,
                onValueChange = { viewModel.setStereoBalance(it) },
                minLabel = "Left",
                maxLabel = "Right",
            )

            ToneControlCard(
                title = "Sub-Bass Synthesizer (60 Hz)",
                subtitle = "Deep subharmonic low-frequency reinforcement",
                valueText = "%.0f%%".format(eqState.subBassSynthesizer),
                sliderValue = eqState.subBassSynthesizer,
                valueRange = 0f..100f,
                step = 5f,
                onValueChange = { viewModel.setSubBass(it) },
                minLabel = "Disabled (0%)",
                maxLabel = "Deep Resonance",
            )
        }
    }
}

@Composable
fun ToneControlCard(
    title: String,
    subtitle: String,
    valueText: String,
    sliderValue: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    step: Float,
    onValueChange: (Float) -> Unit,
    minLabel: String,
    maxLabel: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F0F12))
            .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextSecondary)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentRed.copy(alpha = 0.15f))
                        .border(1.dp, AccentRed.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(valueText, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold), color = AccentRed)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF242429))
                        .clickable {
                            val next = (sliderValue - step).coerceIn(valueRange.start, valueRange.endInclusive)
                            onValueChange((next * 10).roundToInt() / 10f)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Remove, contentDescription = "-", tint = Color.White, modifier = Modifier.size(16.dp))
                }

                Slider(
                    value = sliderValue,
                    onValueChange = onValueChange,
                    valueRange = valueRange,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = AccentRed,
                        inactiveTrackColor = Color(0xFF242429),
                    )
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF242429))
                        .clickable {
                            val next = (sliderValue + step).coerceIn(valueRange.start, valueRange.endInclusive)
                            onValueChange((next * 10).roundToInt() / 10f)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "+", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(minLabel, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextMuted)
                Text(maxLabel, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextMuted)
            }
        }
    }
}

/**
 * Tab 3: Dynamics & Reverb
 */
@Composable
fun ReverbTabContent(
    eqState: EqualizerState,
    viewModel: MediaViewModel,
) {
    val presets = listOf("Off", "Studio", "Chamber", "Hall", "Cathedral")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ChipBg),
        border = BorderStroke(1.dp, BorderLight),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Dynamics & Reverb",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )

            // Reverb Simulation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F0F12))
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Acoustic Reverb Simulation", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                            Text("${eqState.reverbPreset} Ambience", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextSecondary)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentRed)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                "%.0f%% Wet".format(eqState.reverbWetPercent),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Slider(
                        value = eqState.reverbWetPercent,
                        onValueChange = { viewModel.setReverbWet(it) },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = AccentRed,
                            inactiveTrackColor = Color(0xFF242429),
                        )
                    )

                    // Presets pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        presets.forEach { preset ->
                            val isSelected = eqState.reverbPreset == preset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentRed else Color(0xFF242429))
                                    .clickable { viewModel.setReverbPreset(preset) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = preset,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }

            // Dynamic Range Compressor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F0F12))
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dynamic Range Compressor", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                        Text("Smooths out volume spikes & enhances quiet musical details", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextSecondary)
                    }
                    Switch(
                        checked = eqState.isCompressorOn,
                        onCheckedChange = { viewModel.toggleCompressor() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentRed,
                            uncheckedThumbColor = Color(0xFF71717A),
                            uncheckedTrackColor = Color(0xFF242429),
                        )
                    )
                }
            }

            // Loudness Equalizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F0F12))
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Loudness Enhancer", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                        Text("Increases perceived loudness with psychoacoustic bass reinforcement", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextSecondary)
                    }
                    Switch(
                        checked = eqState.isLoudnessEqualizerOn,
                        onCheckedChange = { viewModel.toggleLoudnessEqualizer() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentRed,
                            uncheckedThumbColor = Color(0xFF71717A),
                            uncheckedTrackColor = Color(0xFF242429),
                        )
                    )
                }
            }
        }
    }
}

/**
 * Tab 4: Spatial Audio & Soundstage
 */
@Composable
fun SpatialTabContent(
    eqState: EqualizerState,
    viewModel: MediaViewModel,
) {
    val presets = listOf("Mono", "Narrow", "Standard", "Wide", "Ultra 3D")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ChipBg),
        border = BorderStroke(1.dp, BorderLight),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Spatial Audio & Soundstage",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
            Text(
                text = "Expand or focus acoustic soundstage using mid-side stereo matrix",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextSecondary,
            )

            // Stereo Field Width Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F0F12))
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Stereo Field Width", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                            Text("${eqState.spatialPreset} Soundstage", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextSecondary)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentRed)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                "%.0f%%".format(eqState.spatialWidthPercent),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Slider(
                        value = eqState.spatialWidthPercent,
                        onValueChange = { viewModel.setSpatialWidth(it) },
                        valueRange = 0f..200f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = AccentRed,
                            inactiveTrackColor = Color(0xFF242429),
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        presets.forEach { preset ->
                            val isSelected = eqState.spatialPreset == preset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentRed else Color(0xFF242429))
                                    .clickable { viewModel.setSpatialPreset(preset) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = preset,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }

            // Spatial Field Visualizer Arc
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F0F12))
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.height(48.dp),
                    ) {
                        val barCount = 21
                        for (i in 0 until barCount) {
                            val centerDist = kotlin.math.abs(i - 10) / 10f
                            val heightFraction = (1f - centerDist * 0.5f) * (eqState.spatialWidthPercent / 150f).coerceIn(0.4f, 1.3f)
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .fillMaxHeight(heightFraction.coerceIn(0.15f, 1f))
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (i == 10) AccentRed else AccentRed.copy(alpha = (1f - centerDist * 0.5f).coerceIn(0.35f, 1f)))
                            )
                        }
                    }
                    Text(
                        text = "3D Acoustic Mid-Side Matrix",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextMuted,
                    )
                }
            }
        }
    }
}
