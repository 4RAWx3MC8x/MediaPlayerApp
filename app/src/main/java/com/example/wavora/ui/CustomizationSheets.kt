package com.example.wavora.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wavora.MediaViewModel
import com.example.wavora.data.AppCustomization
import com.example.wavora.data.MediaFilter
import com.example.wavora.data.SortOption
import com.example.wavora.data.ViewMode
import com.example.wavora.theme.*

/**
 * Bottom sheet for customizing app appearance: accent colors, OLED black themes, and view modes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationModalSheet(
    viewModel: MediaViewModel,
    customization: AppCustomization,
    onDismiss: () -> Unit,
) {
    val activeAccent = WavoraAccents.getOrElse(customization.accentColorIndex) { WavoraAccents[0] }.color
    val activeBgTheme = WavoraBgThemes.getOrElse(customization.bgThemeIndex) { WavoraBgThemes[0] }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = activeBgTheme.surface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f))
        },
        modifier = Modifier.testTag("customization_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(activeAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = null,
                            tint = activeAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "App Customization (التخصيص)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = Color.White
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            HorizontalDivider(color = Color(0xFF1E293B))

            // ── Section 1: Accent Color ──
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ACCENT COLOR THEME",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    ),
                    color = TextMuted
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(WavoraAccents) { index, paletteItem ->
                        val isSelected = customization.accentColorIndex == index
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.clickable { viewModel.setAccentColorIndex(index) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(paletteItem.color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .testTag("color_accent_${index}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = paletteItem.name.split(" ").first(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }

            // ── Section 2: Background Theme ──
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "BACKGROUND MODE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    ),
                    color = TextMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WavoraBgThemes.forEachIndexed { index, themeItem ->
                        val isSelected = customization.bgThemeIndex == index
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setBgThemeIndex(index) }
                                .testTag("bg_theme_${index}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = themeItem.bg),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) activeAccent else Color(0xFF1E293B)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = themeItem.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isSelected) activeAccent else Color.White
                                )
                            }
                        }
                    }
                }
            }

            // ── Section 3: View Layout Mode ──
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "MEDIA LIST VIEW MODE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    ),
                    color = TextMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val modes = listOf(
                        Triple(ViewMode.LIST, "Standard List", Icons.Rounded.ViewList),
                        Triple(ViewMode.GRID, "2-Col Grid", Icons.Rounded.GridView),
                        Triple(ViewMode.COMPACT, "Compact List", Icons.Rounded.DensitySmall)
                    )

                    modes.forEach { (mode, label, icon) ->
                        val isSelected = customization.viewMode == mode
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setViewMode(mode) }
                                .testTag("view_mode_${mode.name}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) activeAccent.copy(alpha = 0.15f) else Color(0xFF141F30)
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) activeAccent else Color(0xFF1E293B)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) activeAccent else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bottom sheet for sorting & arranging media files: Title, Date, Duration, Artist, Album, Most Played.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortAndArrangeModalSheet(
    viewModel: MediaViewModel,
    customization: AppCustomization,
    onDismiss: () -> Unit,
) {
    val activeAccent = WavoraAccents.getOrElse(customization.accentColorIndex) { WavoraAccents[0] }.color
    val activeBgTheme = WavoraBgThemes.getOrElse(customization.bgThemeIndex) { WavoraBgThemes[0] }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = activeBgTheme.surface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f))
        },
        modifier = Modifier.testTag("sort_and_arrange_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(activeAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Sort,
                            contentDescription = null,
                            tint = activeAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Sort & Arrange (الترتيب والفرز)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = Color.White
                    )
                }

                // Ascending / Descending toggle
                Button(
                    onClick = { viewModel.toggleSortDirection() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = activeAccent),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("sort_direction_toggle")
                ) {
                    Icon(
                        imageVector = if (customization.sortAscending) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (customization.sortAscending) "Ascending (A-Z)" else "Descending (Z-A)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF1E293B))

            // Sort Options List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ORDER BY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    ),
                    color = TextMuted
                )

                val sortOptions = listOf(
                    Pair(SortOption.TITLE, "Track / Video Title"),
                    Pair(SortOption.DATE_ADDED, "Date Added (Recency)"),
                    Pair(SortOption.DURATION, "Duration (Length)"),
                    Pair(SortOption.ARTIST, "Artist Name"),
                    Pair(SortOption.ALBUM, "Album Name"),
                    Pair(SortOption.MOST_PLAYED, "Play Count (Popularity)"),
                )

                sortOptions.forEach { (option, description) ->
                    val isSelected = customization.sortBy == option
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setSortOption(option) }
                            .testTag("sort_option_${option.name}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) activeAccent.copy(alpha = 0.12f) else Color(0xFF141F30)
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) activeAccent else Color(0xFF1E293B)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 13.5.sp
                                    ),
                                    color = if (isSelected) activeAccent else Color.White
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextSecondary
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = activeAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Category Filter
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "CATEGORY FILTER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    ),
                    color = TextMuted
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MediaFilter.values().size) { index ->
                        val filter = MediaFilter.values()[index]
                        val isSelected = customization.filter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) activeAccent else Color(0xFF141F30))
                                .clickable { viewModel.setMediaFilter(filter) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("filter_chip_${filter.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter.label,
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
    }
}
