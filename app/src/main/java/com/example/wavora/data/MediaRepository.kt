package com.example.wavora.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Reads real audio and video files from the device's MediaStore.
 */
class MediaRepository(private val context: Context) {

    suspend fun getAudioItems(): List<MediaItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<MediaItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
        ) + if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) arrayOf("genre") else emptyArray()

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collection, projection, "${MediaStore.Audio.Media.DURATION} >= ?",
                arrayOf("1000"), sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val genreCol = cursor.getColumnIndex("genre")

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val albumId = cursor.getLong(albumIdCol)
                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"), albumId
                    )
                    val filePath = if (dataCol != -1) cursor.getString(dataCol) ?: "" else ""
                    val fileObj = if (filePath.isNotBlank()) File(filePath) else null
                    val parentFile = fileObj?.parentFile
                    val dirPath = parentFile?.absolutePath ?: ""
                    val relFolder = if (dirPath.startsWith("/storage/emulated/0/")) {
                        dirPath.removePrefix("/storage/emulated/0/")
                    } else if (dirPath.startsWith("/storage/emulated/0")) {
                        dirPath.removePrefix("/storage/emulated/0")
                    } else {
                        parentFile?.name ?: "Music"
                    }
                    val genre = if (genreCol != -1) (cursor.getString(genreCol)?.takeIf { it.isNotBlank() } ?: "Unknown") else "Unknown"

                    items.add(
                        MediaItem(
                            id = id,
                            title = cursor.getString(titleCol) ?: "Unknown",
                            artist = cursor.getString(artistCol) ?: "Unknown Artist",
                            album = cursor.getString(albumCol) ?: "Unknown Album",
                            albumArtUri = albumArtUri,
                            contentUri = ContentUris.withAppendedId(collection, id),
                            duration = cursor.getLong(durationCol),
                            mediaType = MediaType.AUDIO,
                            dateAdded = cursor.getLong(dateCol),
                            folder = relFolder,
                            path = filePath,
                            genre = genre,
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        val selected = getSelectedFolders()
        val removed = getRemovedFolders()

        items.filter { item ->
            // Exclude item if its relative folder or full file path matches any excluded rule
            val isExcluded = removed.any { r ->
                r.isNotBlank() && (
                    item.folder.equals(r, ignoreCase = true) ||
                    item.folder.startsWith("$r/") ||
                    item.path.startsWith(r) ||
                    item.path.contains("/$r/")
                )
            }
            if (isExcluded) return@filter false

            if (selected.isEmpty()) {
                true
            } else {
                selected.any { s ->
                    s.isNotBlank() && (
                        item.folder.equals(s, ignoreCase = true) ||
                        item.folder.startsWith("$s/") ||
                        item.path.startsWith(s) ||
                        item.path.contains("/$s/")
                    )
                }
            }
        }
    }

    suspend fun getVideoItems(): List<MediaItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<MediaItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.ARTIST,
            MediaStore.Video.Media.ALBUM,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATA,
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                collection, projection, null, null, sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val dataCol = cursor.getColumnIndex(MediaStore.Video.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val thumbUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                    )
                    val filePath = if (dataCol != -1) cursor.getString(dataCol) ?: "" else ""
                    val folderName = if (filePath.isNotEmpty()) {
                        File(filePath).parentFile?.name ?: "Movies"
                    } else "Movies"

                    items.add(
                        MediaItem(
                            id = id,
                            title = cursor.getString(titleCol) ?: "Unknown",
                            artist = cursor.getString(artistCol) ?: "Unknown",
                            album = cursor.getString(albumCol) ?: "Unknown",
                            albumArtUri = thumbUri,
                            contentUri = ContentUris.withAppendedId(collection, id),
                            duration = cursor.getLong(durationCol),
                            mediaType = MediaType.VIDEO,
                            dateAdded = cursor.getLong(dateCol),
                            folder = folderName,
                            genre = "Video",
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        items
    }

    fun groupByAlbum(items: List<MediaItem>): List<Album> {
        return items.groupBy { it.album }
            .map { (albumName, songs) ->
                Album(
                    name = albumName,
                    artist = songs.firstOrNull()?.artist ?: "Unknown Artist",
                    artUri = songs.firstOrNull()?.albumArtUri,
                    items = songs,
                )
            }
            .sortedBy { it.name }
    }

    fun groupByArtist(items: List<MediaItem>): List<Artist> {
        return items.groupBy { it.artist }
            .map { (artistName, songs) ->
                Artist(
                    name = artistName,
                    artUri = songs.firstOrNull()?.albumArtUri,
                    items = songs,
                    albumCount = songs.map { it.album }.distinct().size,
                )
            }
            .sortedBy { it.name }
    }

    /**
     * Resolves the top-level directory containing the media file under Android storage.
     * Prevents nested subfolders (e.g. Music/Artist/Album) from appearing as independent root folders.
     */
    fun resolveTopLevelFolder(filePath: String): File {
        if (filePath.isEmpty()) return File("/storage/emulated/0/Music")
        val file = File(filePath)
        val parent = file.parentFile ?: return File("/storage/emulated/0/Music")
        val path = parent.absolutePath

        // Standard Android primary storage (/storage/emulated/0/...)
        val emulatedPrefix = "/storage/emulated/0"
        if (path.startsWith(emulatedPrefix)) {
            val rel = path.removePrefix(emulatedPrefix).trimStart('/')
            val firstSegment = rel.split('/').firstOrNull()
            if (!firstSegment.isNullOrBlank()) {
                return File(emulatedPrefix, firstSegment)
            }
        }

        // Removable storage / SD cards (/storage/XXXX-XXXX/...)
        val parts = path.split('/').filter { it.isNotBlank() }
        if (parts.size >= 3 && parts[0] == "storage") {
            val volumePath = "/storage/${parts[1]}"
            val rel = path.removePrefix(volumePath).trimStart('/')
            val firstSegment = rel.split('/').firstOrNull()
            if (!firstSegment.isNullOrBlank()) {
                return File(volumePath, firstSegment)
            }
        }

        return parent
    }

    fun groupByFolder(items: List<MediaItem>): List<MediaFolder> {
        // Group by top-level folder name (e.g. Music, Download, Podcasts)
        return items.groupBy { it.folder }
            .map { (folderName, songs) ->
                val samplePath = "/storage/emulated/0/$folderName"
                MediaFolder(
                    name = folderName,
                    path = samplePath,
                    items = songs,
                )
            }
            .filter { it.items.isNotEmpty() }
            .sortedBy { it.name }
    }

    fun groupByGenre(items: List<MediaItem>): List<MediaGenre> {
        return items.groupBy { it.genre }
            .map { (genreName, songs) ->
                MediaGenre(
                    name = genreName,
                    items = songs,
                )
            }
            .sortedBy { it.name }
    }

    fun getSelectedFolders(): Set<String> {
        val prefs = context.getSharedPreferences("music_folders_selection", Context.MODE_PRIVATE)
        return prefs.getStringSet("selected_folders", null) ?: emptySet()
    }

    fun getRemovedFolders(): Set<String> {
        val prefs = context.getSharedPreferences("music_folders_selection", Context.MODE_PRIVATE)
        return prefs.getStringSet("removed_folders", null) ?: emptySet()
    }

    fun saveSelectedFolders(selected: Set<String>, removed: Set<String>) {
        val prefs = context.getSharedPreferences("music_folders_selection", Context.MODE_PRIVATE)
        prefs.edit()
            .putStringSet("selected_folders", selected)
            .putStringSet("removed_folders", removed)
            .apply()
    }

    suspend fun getAllDiscoveredFolders(): List<SelectableFolder> = withContext(Dispatchers.IO) {
        val folderCounts = mutableMapOf<String, Triple<String, String, Int>>() // path -> (displayName, fullPath, count)
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayOf(MediaStore.Audio.Media.DATA)

        try {
            context.contentResolver.query(
                collection, projection, "${MediaStore.Audio.Media.DURATION} >= ?",
                arrayOf("1000"), null
            )?.use { cursor ->
                val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                while (cursor.moveToNext()) {
                    val filePath = if (dataCol != -1) cursor.getString(dataCol) ?: "" else ""
                    if (filePath.isBlank()) continue
                    val parentFile = File(filePath).parentFile ?: continue
                    val path = parentFile.absolutePath
                    val relPath = if (path.startsWith("/storage/emulated/0/")) {
                        path.removePrefix("/storage/emulated/0/")
                    } else if (path.startsWith("/storage/emulated/0")) {
                        path.removePrefix("/storage/emulated/0")
                    } else {
                        parentFile.name
                    }
                    val current = folderCounts[path]
                    val count = (current?.third ?: 0) + 1
                    folderCounts[path] = Triple(relPath, path, count)
                }
            }
        } catch (_: Exception) {}

        // Fallback standard default folders if none yet discovered in MediaStore
        val standardDirs = listOf("Music", "Download", "Podcasts", "Audiobooks", "Recordings")
        for (dirName in standardDirs) {
            val p = "/storage/emulated/0/$dirName"
            if (!folderCounts.containsKey(p)) {
                folderCounts[p] = Triple(dirName, p, 0)
            }
        }

        // Add any previously saved custom folders
        val savedSelected = getSelectedFolders()
        val savedRemoved = getRemovedFolders()
        for (f in savedSelected + savedRemoved) {
            if (f.isNotBlank() && !folderCounts.containsKey(f) && !folderCounts.values.any { it.first == f }) {
                val name = if (f.contains('/')) f.substringAfterLast('/') else f
                folderCounts[f] = Triple(name, f, 0)
            }
        }

        folderCounts.values
            .sortedBy { it.first }
            .map { (relPath, fullPath, count) ->
                val isExcluded = savedRemoved.any { r ->
                    r.isNotBlank() && (relPath.equals(r, ignoreCase = true) || fullPath.equals(r, ignoreCase = true))
                }
                val isSelected = if (isExcluded) {
                    false
                } else if (savedSelected.isNotEmpty()) {
                    savedSelected.any { s ->
                        s.isNotBlank() && (relPath.equals(s, ignoreCase = true) || fullPath.equals(s, ignoreCase = true))
                    }
                } else {
                    true
                }

                SelectableFolder(
                    name = relPath,
                    path = fullPath,
                    storageLabel = if (fullPath.startsWith("/storage/emulated/0")) "Internal Storage" else "Device Storage",
                    isSelected = isSelected,
                    songCount = count
                )
            }
    }
}
