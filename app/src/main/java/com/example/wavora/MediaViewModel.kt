package com.example.wavora

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavora.data.*
import com.example.wavora.player.PlayerState
import com.example.wavora.player.WavoraPlaybackService
import com.example.wavora.player.WavoraPlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MediaUiState(
    val hasPermission: Boolean = false,
    val isLoading: Boolean = true,
    val audioItems: List<MediaItem> = emptyList(),
    val videoItems: List<MediaItem> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val folders: List<MediaFolder> = emptyList(),
    val genres: List<MediaGenre> = emptyList(),
    val playlists: List<MediaPlaylist> = emptyList(),
    val bookmarks: List<MediaItem> = emptyList(),
    val mostPlayed: List<MediaItem> = emptyList(),
    val recentlyPlayed: List<MediaItem> = emptyList(),
    val searchQuery: String = "",
    val searchCategory: SearchCategory = SearchCategory.ALL,
    val recentSearches: List<String> = emptyList(),
    val librarySubCategory: String = "MENU",
    val activeNavTab: Int = 0,
    val isFoldersDialogVisible: Boolean = false,
    val discoveredFolders: List<SelectableFolder> = emptyList(),
    val selectedSongForOptions: MediaItem? = null,
    val customization: AppCustomization = AppCustomization(),
    val isSortSheetVisible: Boolean = false,
    val isCustomizationSheetVisible: Boolean = false,
    val selectedMainVideo: MediaItem? = null,
)

class MediaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)
    val playerController = WavoraPlayerController(application)
    val playerState: StateFlow<PlayerState> = playerController.state

    private val userDataPrefs by lazy {
        getApplication<Application>().getSharedPreferences("wavora_user_data", Context.MODE_PRIVATE)
    }

    private fun loadSavedCustomization(): AppCustomization {
        val accent = userDataPrefs.getInt("accent_index", 0)
        val bg = userDataPrefs.getInt("bg_index", 0)
        val viewModeName = userDataPrefs.getString("view_mode", ViewMode.LIST.name) ?: ViewMode.LIST.name
        val sortOptionName = userDataPrefs.getString("sort_option", SortOption.TITLE.name) ?: SortOption.TITLE.name
        val sortAsc = userDataPrefs.getBoolean("sort_asc", true)
        val viewMode = try { ViewMode.valueOf(viewModeName) } catch (_: Exception) { ViewMode.LIST }
        val sortOption = try { SortOption.valueOf(sortOptionName) } catch (_: Exception) { SortOption.TITLE }
        return AppCustomization(
            accentColorIndex = accent,
            bgThemeIndex = bg,
            viewMode = viewMode,
            sortBy = sortOption,
            sortAscending = sortAsc,
        )
    }

    private fun getSavedRecentSearches(): List<String> {
        val raw = userDataPrefs.getString("recent_searches", "") ?: ""
        return if (raw.isBlank()) listOf("Bunny", "Synthwave", "Ambient", "Cinema") else raw.split("|||")
    }

    private val _uiState = MutableStateFlow(
        MediaUiState(
            customization = loadSavedCustomization(),
            recentSearches = getSavedRecentSearches()
        )
    )
    val uiState: StateFlow<MediaUiState> = _uiState.asStateFlow()

    private val _equalizerState = MutableStateFlow(loadSavedEqSettings())
    val equalizerState: StateFlow<EqualizerState> = _equalizerState.asStateFlow()

    private fun getPlayCount(id: Long): Int = userDataPrefs.getInt("count_$id", 0)
    private fun getLastPlayed(id: Long): Long = userDataPrefs.getLong("last_$id", 0L)
    private fun getFavoriteIds(): Set<String> = userDataPrefs.getStringSet("favorites", emptySet()) ?: emptySet()

    init {
        checkPermissionsAndLoad()
        notifyAudioFx()
        observePlaybackForStats()
    }

    private fun observePlaybackForStats() {
        viewModelScope.launch {
            var lastTrackId: Long? = null
            playerState.collect { state ->
                val current = state.currentItem
                if (current != null && state.isPlaying && current.id != 0L && current.id != lastTrackId) {
                    lastTrackId = current.id
                    recordTrackPlay(current.id)
                }
            }
        }
    }

    private fun recordTrackPlay(id: Long) {
        val currentCount = getPlayCount(id) + 1
        val now = System.currentTimeMillis()
        userDataPrefs.edit()
            .putInt("count_$id", currentCount)
            .putLong("last_$id", now)
            .apply()

        val updatedAudio = _uiState.value.audioItems.map { item ->
            if (item.id == id) {
                item.copy(playCount = currentCount, lastPlayed = now)
            } else {
                item
            }
        }
        val mostPlayed = updatedAudio.filter { it.playCount > 0 }.sortedByDescending { it.playCount }
        val recentlyPlayed = updatedAudio.filter { it.lastPlayed > 0 }.sortedByDescending { it.lastPlayed }

        _uiState.value = _uiState.value.copy(
            audioItems = updatedAudio,
            mostPlayed = mostPlayed,
            recentlyPlayed = recentlyPlayed,
        )
    }

    fun checkPermissionsAndLoad() {
        val ctx = getApplication<Application>()
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.READ_MEDIA_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
        }
        _uiState.value = _uiState.value.copy(hasPermission = hasPermission)
        if (hasPermission) {
            loadMedia()
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun loadMedia() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val favSet = getFavoriteIds()
            val audio = repository.getAudioItems().map { item ->
                item.copy(
                    isFavorite = favSet.contains(item.id.toString()),
                    playCount = getPlayCount(item.id),
                    lastPlayed = getLastPlayed(item.id)
                )
            }
            val video = repository.getVideoItems()
            val albums = repository.groupByAlbum(audio)
            val artists = repository.groupByArtist(audio)
            val folders = repository.groupByFolder(audio)
            val genres = repository.groupByGenre(audio)
            val bookmarks = audio.filter { it.isFavorite }
            val mostPlayed = audio.filter { it.playCount > 0 }.sortedByDescending { it.playCount }
            val recentlyPlayed = audio.filter { it.lastPlayed > 0 }.sortedByDescending { it.lastPlayed }

            val playlists = if (audio.isNotEmpty()) {
                listOf(
                    MediaPlaylist("1", "All Audio Tracks", audio),
                    MediaPlaylist("2", "Bookmarked Tracks", bookmarks),
                )
            } else emptyList()

            val firstVideo = video.firstOrNull() ?: SampleMedia.sampleVideos.firstOrNull()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                audioItems = audio,
                videoItems = video,
                albums = albums,
                artists = artists,
                folders = folders,
                genres = genres,
                bookmarks = bookmarks,
                mostPlayed = mostPlayed,
                recentlyPlayed = recentlyPlayed,
                playlists = playlists,
                selectedMainVideo = _uiState.value.selectedMainVideo ?: firstVideo,
            )
        }
    }

    fun setSelectedMainVideo(item: MediaItem) {
        _uiState.value = _uiState.value.copy(selectedMainVideo = item)
    }

    fun openSortSheet() {
        _uiState.value = _uiState.value.copy(isSortSheetVisible = true)
    }

    fun closeSortSheet() {
        _uiState.value = _uiState.value.copy(isSortSheetVisible = false)
    }

    fun openCustomizationSheet() {
        _uiState.value = _uiState.value.copy(isCustomizationSheetVisible = true)
    }

    fun closeCustomizationSheet() {
        _uiState.value = _uiState.value.copy(isCustomizationSheetVisible = false)
    }

    fun setAccentColorIndex(index: Int) {
        userDataPrefs.edit().putInt("accent_index", index).apply()
        val updated = _uiState.value.customization.copy(accentColorIndex = index)
        _uiState.value = _uiState.value.copy(customization = updated)
    }

    fun setBgThemeIndex(index: Int) {
        userDataPrefs.edit().putInt("bg_index", index).apply()
        val updated = _uiState.value.customization.copy(bgThemeIndex = index)
        _uiState.value = _uiState.value.copy(customization = updated)
    }

    fun setViewMode(mode: ViewMode) {
        userDataPrefs.edit().putString("view_mode", mode.name).apply()
        val updated = _uiState.value.customization.copy(viewMode = mode)
        _uiState.value = _uiState.value.copy(customization = updated)
    }

    fun setSortOption(option: SortOption) {
        userDataPrefs.edit().putString("sort_option", option.name).apply()
        val updated = _uiState.value.customization.copy(sortBy = option)
        _uiState.value = _uiState.value.copy(customization = updated)
    }

    fun toggleSortDirection() {
        val newAsc = !_uiState.value.customization.sortAscending
        userDataPrefs.edit().putBoolean("sort_asc", newAsc).apply()
        val updated = _uiState.value.customization.copy(sortAscending = newAsc)
        _uiState.value = _uiState.value.copy(customization = updated)
    }

    fun setMediaFilter(filter: MediaFilter) {
        val updated = _uiState.value.customization.copy(filter = filter)
        _uiState.value = _uiState.value.copy(customization = updated)
    }

    fun setSearchCategory(category: SearchCategory) {
        _uiState.value = _uiState.value.copy(searchCategory = category)
    }

    fun addRecentSearch(query: String) {
        if (query.isBlank()) return
        val current = _uiState.value.recentSearches.toMutableList()
        current.remove(query)
        current.add(0, query)
        val limited = current.take(8)
        userDataPrefs.edit().putString("recent_searches", limited.joinToString("|||")).apply()
        _uiState.value = _uiState.value.copy(recentSearches = limited)
    }

    fun clearRecentSearches() {
        userDataPrefs.edit().remove("recent_searches").apply()
        _uiState.value = _uiState.value.copy(recentSearches = emptyList())
    }

    fun getFilteredAndSortedAudio(): List<MediaItem> {
        val state = _uiState.value
        val items = when (state.customization.filter) {
            MediaFilter.ALL -> state.audioItems
            MediaFilter.SONGS -> state.audioItems
            MediaFilter.VIDEOS -> emptyList()
            MediaFilter.FAVORITES -> state.bookmarks
            MediaFilter.RECENT -> if (state.recentlyPlayed.isNotEmpty()) state.recentlyPlayed else state.audioItems
            MediaFilter.MOST_PLAYED -> if (state.mostPlayed.isNotEmpty()) state.mostPlayed else state.audioItems
        }

        val sorted = when (state.customization.sortBy) {
            SortOption.TITLE -> items.sortedBy { it.title.lowercase() }
            SortOption.DATE_ADDED -> items.sortedByDescending { it.dateAdded }
            SortOption.DURATION -> items.sortedByDescending { it.duration }
            SortOption.ARTIST -> items.sortedBy { it.artist.lowercase() }
            SortOption.ALBUM -> items.sortedBy { it.album.lowercase() }
            SortOption.MOST_PLAYED -> items.sortedByDescending { it.playCount }
        }

        return if (state.customization.sortAscending) sorted else sorted.reversed()
    }

    fun getFilteredAndSortedVideos(): List<MediaItem> {
        val state = _uiState.value
        val items = state.videoItems.ifEmpty { SampleMedia.sampleVideos }
        val sorted = when (state.customization.sortBy) {
            SortOption.TITLE -> items.sortedBy { it.title.lowercase() }
            SortOption.DATE_ADDED -> items.sortedByDescending { it.dateAdded }
            SortOption.DURATION -> items.sortedByDescending { it.duration }
            SortOption.ARTIST -> items.sortedBy { it.artist.lowercase() }
            SortOption.ALBUM -> items.sortedBy { it.album.lowercase() }
            SortOption.MOST_PLAYED -> items.sortedByDescending { it.playCount }
        }
        return if (state.customization.sortAscending) sorted else sorted.reversed()
    }

    fun onPermissionGranted() {
        _uiState.value = _uiState.value.copy(hasPermission = true)
        loadMedia()
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setLibrarySubCategory(category: String) {
        _uiState.value = _uiState.value.copy(librarySubCategory = category)
    }

    fun setActiveNavTab(tab: Int) {
        _uiState.value = _uiState.value.copy(activeNavTab = tab)
    }

    fun toggleFavorite(itemId: Long) {
        val currentFavs = getFavoriteIds().toMutableSet()
        val idStr = itemId.toString()
        val isFavNow = if (currentFavs.contains(idStr)) {
            currentFavs.remove(idStr)
            false
        } else {
            currentFavs.add(idStr)
            true
        }
        userDataPrefs.edit().putStringSet("favorites", currentFavs).apply()

        val updatedList = _uiState.value.audioItems.map {
            if (it.id == itemId) it.copy(isFavorite = isFavNow) else it
        }
        val bookmarks = updatedList.filter { it.isFavorite }
        _uiState.value = _uiState.value.copy(
            audioItems = updatedList,
            bookmarks = bookmarks,
        )
    }

    fun openSongOptions(item: MediaItem) {
        _uiState.value = _uiState.value.copy(selectedSongForOptions = item)
    }

    fun closeSongOptions() {
        _uiState.value = _uiState.value.copy(selectedSongForOptions = null)
    }

    fun findAlbum(albumName: String): Album? {
        val state = _uiState.value
        val existing = state.albums.find { it.name.equals(albumName, ignoreCase = true) }
        if (existing != null) return existing
        val matchingSongs = state.audioItems.filter { it.album.equals(albumName, ignoreCase = true) }
        if (matchingSongs.isNotEmpty()) {
            return Album(
                name = albumName,
                artist = matchingSongs.firstOrNull()?.artist ?: "Unknown Artist",
                artUri = matchingSongs.firstOrNull()?.albumArtUri,
                items = matchingSongs
            )
        }
        return null
    }

    fun findFolder(folderName: String): MediaFolder? {
        val state = _uiState.value
        val existing = state.folders.find { 
            it.name.equals(folderName, ignoreCase = true) || it.path.equals(folderName, ignoreCase = true) 
        }
        if (existing != null) return existing
        val matchingSongs = state.audioItems.filter { 
            it.folder.equals(folderName, ignoreCase = true) || it.folder.endsWith(folderName, ignoreCase = true) 
        }
        if (matchingSongs.isNotEmpty()) {
            return MediaFolder(
                name = folderName,
                path = folderName,
                items = matchingSongs
            )
        }
        return null
    }

    // --- Folders Selection Management ---
    fun openFoldersDialog() {
        viewModelScope.launch {
            val folders = repository.getAllDiscoveredFolders()
            _uiState.value = _uiState.value.copy(
                isFoldersDialogVisible = true,
                discoveredFolders = folders
            )
        }
    }

    fun closeFoldersDialog() {
        _uiState.value = _uiState.value.copy(isFoldersDialogVisible = false)
    }

    fun toggleFolderSelection(folderName: String) {
        val current = _uiState.value.discoveredFolders
        val updated = current.map {
            if (it.name == folderName) it.copy(isSelected = !it.isSelected) else it
        }
        _uiState.value = _uiState.value.copy(discoveredFolders = updated)
    }

    fun toggleRootFolderSelection(rootName: String, isSelected: Boolean) {
        val current = _uiState.value.discoveredFolders
        val updated = current.map {
            if (it.name == rootName || it.name.startsWith("$rootName/")) {
                it.copy(isSelected = isSelected)
            } else it
        }
        _uiState.value = _uiState.value.copy(discoveredFolders = updated)
    }

    fun removeFolderFromSelection(folderName: String) {
        val current = _uiState.value.discoveredFolders
        val updated = current.filter { it.name != folderName }
        _uiState.value = _uiState.value.copy(discoveredFolders = updated)
    }

    fun removeRootFolderFromSelection(rootName: String) {
        val current = _uiState.value.discoveredFolders
        val updated = current.filterNot {
            it.name == rootName || it.name.startsWith("$rootName/")
        }
        _uiState.value = _uiState.value.copy(discoveredFolders = updated)
    }

    fun addCustomFolder(folderName: String, path: String) {
        val current = _uiState.value.discoveredFolders
        if (current.none { it.name.equals(folderName, ignoreCase = true) }) {
            val newFolder = SelectableFolder(
                name = folderName,
                path = path,
                storageLabel = android.os.Build.MODEL ?: "Internal Storage",
                isSelected = true,
                songCount = 0
            )
            _uiState.value = _uiState.value.copy(discoveredFolders = listOf(newFolder) + current)
        }
    }

    fun selectAllFolders() {
        val current = _uiState.value.discoveredFolders
        _uiState.value = _uiState.value.copy(discoveredFolders = current.map { it.copy(isSelected = true) })
    }

    fun deselectAllFolders() {
        val current = _uiState.value.discoveredFolders
        _uiState.value = _uiState.value.copy(discoveredFolders = current.map { it.copy(isSelected = false) })
    }

    fun saveAndRescanFolders() {
        viewModelScope.launch {
            val current = _uiState.value.discoveredFolders
            val selected = current.filter { it.isSelected }.flatMap { listOf(it.name, it.path) }.filter { it.isNotBlank() }.toSet()
            val unselectedOrRemoved = current.filter { !it.isSelected }.flatMap { listOf(it.name, it.path) }.filter { it.isNotBlank() }.toSet()
            repository.saveSelectedFolders(selected, unselectedOrRemoved)
            closeFoldersDialog()
            loadMedia()
        }
    }

    // --- Equalizer Persistence & Controls ---
    private fun loadSavedEqSettings(): EqualizerState {
        val prefs = getApplication<Application>().getSharedPreferences("melodyflow_eq_settings", android.content.Context.MODE_PRIVATE)
        val rawBands = prefs.getString("bands", null)
        val bands = rawBands?.split(",")?.mapNotNull { it.toFloatOrNull() }?.takeIf { it.size == 10 }
            ?: listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        return EqualizerState(
            isEngineOn = prefs.getBoolean("enabled", false),
            activePreset = prefs.getString("activePreset", "Flat") ?: "Flat",
            preGain = prefs.getFloat("preGain", 0f),
            bands = bands,
            bassEnhancement = prefs.getFloat("bass", 0f),
            trebleClarity = prefs.getFloat("treble", 0f),
            midRange = prefs.getFloat("mid", 0f),
            toneGainStage = prefs.getFloat("toneGain", 0f),
            stereoBalance = prefs.getFloat("balance", 0f),
            subBassSynthesizer = prefs.getFloat("subBass", 0f),
            reverbWetPercent = prefs.getFloat("reverbWet", 0f),
            reverbPreset = prefs.getString("reverbPreset", "Off") ?: "Off",
            isCompressorOn = prefs.getBoolean("compressor", false),
            isLoudnessEqualizerOn = prefs.getBoolean("loudness", false),
            spatialWidthPercent = prefs.getFloat("stereoWidth", 100f),
            spatialPreset = prefs.getString("spatialPreset", "Standard") ?: "Standard",
        )
    }

    private fun saveEqSettings() {
        val prefs = getApplication<Application>().getSharedPreferences("melodyflow_eq_settings", android.content.Context.MODE_PRIVATE)
        val s = _equalizerState.value
        prefs.edit().apply {
            putBoolean("enabled", s.isEngineOn)
            putString("activePreset", s.activePreset)
            putFloat("preGain", s.preGain)
            putString("bands", s.bands.joinToString(","))
            putFloat("bass", s.bassEnhancement)
            putFloat("treble", s.trebleClarity)
            putFloat("mid", s.midRange)
            putFloat("toneGain", s.toneGainStage)
            putFloat("balance", s.stereoBalance)
            putFloat("subBass", s.subBassSynthesizer)
            putFloat("reverbWet", s.reverbWetPercent)
            putString("reverbPreset", s.reverbPreset)
            putBoolean("compressor", s.isCompressorOn)
            putBoolean("loudness", s.isLoudnessEqualizerOn)
            putFloat("stereoWidth", s.spatialWidthPercent)
            putString("spatialPreset", s.spatialPreset)
            apply()
        }
    }

    private fun notifyAudioFx() {
        saveEqSettings()
        WavoraPlaybackService.applyEqualizerState(_equalizerState.value)
    }

    fun toggleEqualizerEngine() {
        _equalizerState.value = _equalizerState.value.copy(
            isEngineOn = !_equalizerState.value.isEngineOn
        )
        notifyAudioFx()
    }

    fun setEqualizerTab(tab: Int) {
        _equalizerState.value = _equalizerState.value.copy(selectedTab = tab)
    }

    fun setBandGain(bandIndex: Int, gain: Float) {
        val currentBands = _equalizerState.value.bands.toMutableList()
        if (bandIndex in currentBands.indices) {
            currentBands[bandIndex] = ((gain * 10).toInt() / 10f)
            _equalizerState.value = _equalizerState.value.copy(
                bands = currentBands,
                activePreset = "Custom"
            )
            notifyAudioFx()
        }
    }

    fun setPreGain(gain: Float) {
        _equalizerState.value = _equalizerState.value.copy(
            preGain = ((gain * 10).toInt() / 10f)
        )
        notifyAudioFx()
    }

    fun setPreset(name: String) {
        val bands = when (name) {
            "Bass Boost" -> listOf(6f, 5f, 4f, 2f, 0f, 0f, 0f, 0f, 0f, 0f)
            "Treble Boost" -> listOf(0f, 0f, 0f, 0f, 0f, 2f, 4f, 5f, 6f, 6f)
            "Acoustic" -> listOf(4f, 4f, 3f, 1f, 1f, 1f, 2f, 3f, 3f, 2f)
            "Classical" -> listOf(4f, 3f, 3f, 2f, -1f, -1f, 0f, 2f, 3f, 4f)
            "Dance" -> listOf(6f, 4f, 2f, 0f, 0f, -3f, -4f, -4f, 0f, 0f)
            "Electronic" -> listOf(5f, 4f, 2f, 0f, -2f, -1f, 1f, 4f, 5f, 4f)
            "Hip-Hop" -> listOf(5f, 4f, 1f, 3f, -1f, -1f, 1f, -1f, 2f, 3f)
            "Jazz" -> listOf(3f, 2f, 1f, 2f, -2f, -2f, 0f, 1f, 2f, 3f)
            "Latin" -> listOf(4f, 3f, 0f, 0f, -2f, -2f, 0f, 0f, 3f, 4f)
            "Lounge" -> listOf(-2f, -2f, 0f, 2f, 4f, 3f, 2f, 0f, -2f, -2f)
            "Metal" -> listOf(6f, 4f, 0f, -2f, -2f, 0f, 0f, 4f, 5f, 6f)
            "Piano" -> listOf(-2f, 0f, 2f, 4f, 3f, 2f, 0f, 1f, 3f, 4f)
            "Pop" -> listOf(-1f, -1f, 0f, 2f, 4f, 4f, 2f, 0f, -1f, -1f)
            "R&B" -> listOf(3f, 7f, 6f, 1f, -2f, -2f, 1f, 2f, 3f, 4f)
            "Rock" -> listOf(5f, 4f, 3f, 1f, -1f, -2f, 0f, 2f, 4f, 5f)
            "Vocal" -> listOf(-2f, -2f, -1f, 1f, 3f, 4f, 3f, 1f, -1f, -2f)
            "Small Speakers" -> listOf(5f, 4f, 3f, 2f, 1f, 0f, -1f, -2f, -3f, -4f)
            else -> listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f) // Flat
        }
        _equalizerState.value = _equalizerState.value.copy(
            activePreset = name,
            bands = bands,
            preGain = 0f,
            isEngineOn = true
        )
        notifyAudioFx()
    }

    fun resetEq() {
        setPreset("Flat")
        _equalizerState.value = _equalizerState.value.copy(
            preGain = 0f,
            bassEnhancement = 0f,
            trebleClarity = 0f,
            midRange = 0f,
            toneGainStage = 0f,
            stereoBalance = 0f,
            subBassSynthesizer = 0f,
            reverbWetPercent = 0f,
            reverbPreset = "Off",
            spatialWidthPercent = 100f,
            spatialPreset = "Standard",
            isCompressorOn = false,
            isLoudnessEqualizerOn = false,
        )
        notifyAudioFx()
    }

    fun setBassEnhancement(value: Float) {
        _equalizerState.value = _equalizerState.value.copy(bassEnhancement = value)
        notifyAudioFx()
    }

    fun setTrebleClarity(value: Float) {
        _equalizerState.value = _equalizerState.value.copy(trebleClarity = value)
        notifyAudioFx()
    }

    fun setMidRange(value: Float) {
        _equalizerState.value = _equalizerState.value.copy(midRange = value)
        notifyAudioFx()
    }

    fun setToneGainStage(value: Float) {
        _equalizerState.value = _equalizerState.value.copy(toneGainStage = value)
        notifyAudioFx()
    }

    fun setStereoBalance(value: Float) {
        _equalizerState.value = _equalizerState.value.copy(stereoBalance = value)
        notifyAudioFx()
    }

    fun setSubBass(value: Float) {
        _equalizerState.value = _equalizerState.value.copy(subBassSynthesizer = value)
        notifyAudioFx()
    }

    fun setReverbWet(value: Float) {
        _equalizerState.value = _equalizerState.value.copy(reverbWetPercent = value)
        notifyAudioFx()
    }

    fun setReverbPreset(preset: String) {
        val wet = when (preset) {
            "Off" -> 0f
            "Studio" -> 20f
            "Chamber" -> 45f
            "Hall" -> 70f
            "Cathedral" -> 100f
            else -> 50f
        }
        _equalizerState.value = _equalizerState.value.copy(
            reverbPreset = preset,
            reverbWetPercent = wet
        )
        notifyAudioFx()
    }

    fun toggleCompressor() {
        _equalizerState.value = _equalizerState.value.copy(
            isCompressorOn = !_equalizerState.value.isCompressorOn
        )
        notifyAudioFx()
    }

    fun toggleLoudnessEqualizer() {
        _equalizerState.value = _equalizerState.value.copy(
            isLoudnessEqualizerOn = !_equalizerState.value.isLoudnessEqualizerOn
        )
        notifyAudioFx()
    }

    fun setSpatialWidth(value: Float) {
        _equalizerState.value = _equalizerState.value.copy(spatialWidthPercent = value)
        notifyAudioFx()
    }

    fun setSpatialPreset(preset: String) {
        val width = when (preset) {
            "Mono" -> 0f
            "Narrow" -> 70f
            "Standard" -> 100f
            "Wide" -> 140f
            "Ultra 3D" -> 180f
            else -> 100f
        }
        _equalizerState.value = _equalizerState.value.copy(
            spatialPreset = preset,
            spatialWidthPercent = width
        )
        notifyAudioFx()
    }

    fun filteredAudio(): List<MediaItem> {
        val q = _uiState.value.searchQuery.lowercase()
        if (q.isEmpty()) return _uiState.value.audioItems
        return _uiState.value.audioItems.filter {
            it.title.lowercase().contains(q) ||
            it.artist.lowercase().contains(q) ||
            it.album.lowercase().contains(q)
        }
    }

    fun filteredVideos(): List<MediaItem> {
        val q = _uiState.value.searchQuery.lowercase()
        if (q.isEmpty()) return _uiState.value.videoItems
        return _uiState.value.videoItems.filter { it.title.lowercase().contains(q) }
    }

    fun filteredAlbums(): List<Album> {
        val q = _uiState.value.searchQuery.lowercase()
        if (q.isEmpty()) return _uiState.value.albums
        return _uiState.value.albums.filter {
            it.name.lowercase().contains(q) || it.artist.lowercase().contains(q)
        }
    }

    fun filteredArtists(): List<Artist> {
        val q = _uiState.value.searchQuery.lowercase()
        if (q.isEmpty()) return _uiState.value.artists
        return _uiState.value.artists.filter { it.name.lowercase().contains(q) }
    }

    override fun onCleared() {
        playerController.release()
        super.onCleared()
    }
}
