package com.example.wavora.ui

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.wavora.MediaUiState
import com.example.wavora.MediaViewModel
import com.example.wavora.data.SelectableFolder

@Composable
fun FoldersSelectionDialog(viewModel: MediaViewModel, uiState: MediaUiState) {
    val context = LocalContext.current
    var expandedFolders by remember { mutableStateOf(setOf<String>()) }
    val deviceModel = remember { Build.MODEL.ifBlank { "Internal Storage" } }

    val openDocumentTreeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            val folderName = uri.lastPathSegment?.substringAfterLast(':') ?: uri.lastPathSegment ?: "Custom Folder"
            viewModel.addCustomFolder(folderName, uri.toString())
        }
    }

    // Group folders into root directories and nested subfolders
    val folderGroups = remember(uiState.discoveredFolders) {
        val groups = LinkedHashMap<String, MutableList<SelectableFolder>>()
        uiState.discoveredFolders.forEach { folder ->
            val root = if (folder.name.contains('/')) folder.name.substringBefore('/') else folder.name
            groups.getOrPut(root) { mutableListOf() }.add(folder)
        }
        groups
    }

    AlertDialog(
        onDismissRequest = { viewModel.closeFoldersDialog() },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFF19191C),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(24.dp)),
        title = {
            Text(
                text = "Folders Selection",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                ),
                color = Color.White,
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    folderGroups.forEach { (rootName, folderList) ->
                        val subfolders = folderList.filter { it.name.contains('/') }
                        val rootFolder = folderList.firstOrNull { !it.name.contains('/') }
                        val isExpanded = expandedFolders.contains(rootName)
                        val isRootSelected = if (subfolders.isNotEmpty()) {
                            subfolders.any { it.isSelected }
                        } else {
                            rootFolder?.isSelected == true
                        }

                        // Root Folder Row
                        item(key = "root_$rootName") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Expand / Collapse Arrow
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                expandedFolders = if (isExpanded) {
                                                    expandedFolders - rootName
                                                } else {
                                                    expandedFolders + rootName
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Rounded.ArrowDropDown else Icons.Rounded.PlayArrow,
                                            contentDescription = "Toggle",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Folder Icon
                                    Icon(
                                        imageVector = Icons.Rounded.Folder,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )

                                    // Folder Name & Device Model Label
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = rootName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp
                                            ),
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = deviceModel,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                            color = Color(0xFFA1A1AA)
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.removeRootFolderFromSelection(rootName) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Exclude",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Checkbox(
                                        checked = isRootSelected,
                                        onCheckedChange = { checked ->
                                            viewModel.toggleRootFolderSelection(rootName, checked)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color.White,
                                            uncheckedColor = Color(0xFF6B6B76),
                                            checkmarkColor = Color.Black
                                        )
                                    )
                                }
                            }
                        }

                        // Expanded Subfolders
                        if (isExpanded) {
                            items(subfolders, key = { "sub_${it.name}" }) { sub ->
                                val subDisplayName = sub.name.substringAfterLast('/')
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 28.dp, top = 2.dp, bottom = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = subDisplayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Checkbox(
                                        checked = sub.isSelected,
                                        onCheckedChange = {
                                            viewModel.toggleFolderSelection(sub.name)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color.White,
                                            uncheckedColor = Color(0xFF6B6B76),
                                            checkmarkColor = Color.Black
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF26262A))
                        .clickable { openDocumentTreeLauncher.launch(null) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Select Folder or Storage",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF26262A))
                        .clickable { viewModel.saveAndRescanFolders() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save and Scan",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    )
}
