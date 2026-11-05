# Graph Report - MediaPlayerApp  (2026-10-19)

## Corpus Check
- 25 files · ~20,823 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 22 file(s) not represented in the graph (top: .xml 14, (none) 2, .properties 2)

## Summary
- 346 nodes · 780 edges · 16 communities (8 shown, 8 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 21 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Player UI & Compose Screens
- Media3 Playback Service
- State Management & ViewModel
- Audio Effects & Equalizer
- State Management & ViewModel
- Audio Effects & Equalizer
- Design System & Theme
- Module Homescreen Albumcard Artistcard
- Module Mainscreenviewmodeltest Fakemymodelrepository Test
- Audio Effects & Equalizer
- Audio Effects & Equalizer
- MainNavigation

## God Nodes (most connected - your core abstractions)
1. `MediaViewModel` - 68 edges
2. `MediaItem` - 30 edges
3. `WavoraPlayerController` - 21 edges
4. `MediaRepository` - 17 edges
5. `WavoraPlaybackService` - 15 edges
6. `Callback` - 13 edges
7. `EqualizerState` - 12 edges
8. `HomeScreen()` - 12 edges
9. `LibraryView()` - 12 edges
10. `MediaUiState` - 11 edges

## Surprising Connections (you probably didn't know these)
- `MediaViewModel` --calls--> `MediaRepository`  [INFERRED]
  app/src/main/java/com/example/wavora/MediaViewModel.kt → app/src/main/java/com/example/wavora/data/MediaRepository.kt
- `MainNavigation()` --calls--> `EqualizerScreen()`  [INFERRED]
  app/src/main/java/com/example/wavora/Navigation.kt → app/src/main/java/com/example/wavora/ui/EqualizerScreen.kt
- `MainNavigation()` --calls--> `HomeScreen()`  [INFERRED]
  app/src/main/java/com/example/wavora/Navigation.kt → app/src/main/java/com/example/wavora/ui/HomeScreen.kt
- `MainNavigation()` --calls--> `PermissionScreen()`  [INFERRED]
  app/src/main/java/com/example/wavora/Navigation.kt → app/src/main/java/com/example/wavora/ui/PermissionScreen.kt
- `MainNavigation()` --calls--> `SettingsScreen()`  [INFERRED]
  app/src/main/java/com/example/wavora/Navigation.kt → app/src/main/java/com/example/wavora/ui/SettingsScreen.kt

## Import Cycles
- None detected.

## Communities (16 total, 8 thin omitted)

### Community 0 - "Player UI & Compose Screens"
Cohesion: 0.06
Nodes (11): formatDuration(), PlayerState, SettingsActionCard(), SettingsScreen(), SettingsSectionHeader(), SettingsSwitchCard(), AlbumArtImage(), MiniPlayerBar() (+3 more)

### Community 1 - "Media3 Playback Service"
Cohesion: 0.10
Nodes (3): WavoraPlaybackService, Callback, Listener

### Community 4 - "State Management & ViewModel"
Cohesion: 0.12
Nodes (12): MediaRepository, Album, Artist, MediaFolder, MediaGenre, MediaItem, MediaPlaylist, MediaType (+4 more)

### Community 5 - "Audio Effects & Equalizer"
Cohesion: 0.13
Nodes (9): EqualizerState, EqTabContent(), EqualizerScreen(), ReverbTabContent(), SpatialTabContent(), SpectrumCanvas(), ToneControlCard(), ToneTabContent() (+1 more)

### Community 6 - "Design System & Theme"
Cohesion: 0.08
Nodes (3): MainScreenTest, MainActivity, WavoraTheme()

### Community 7 - "Module Homescreen Albumcard Artistcard"
Cohesion: 0.24
Nodes (13): AlbumCard(), AlbumsDirectView(), ArtistCard(), BottomNavItem(), EmptyStateView(), FolderCard(), FoldersSelectionDialog(), GenreCard() (+5 more)

### Community 10 - "Audio Effects & Equalizer"
Cohesion: 0.25
Nodes (8): Screen, ALBUM_DETAIL, EQUALIZER, FOLDER_DETAIL, HOME, PLAYER, SETTINGS, VIDEO_PLAYER

### Community 12 - "MainNavigation"
Cohesion: 0.16
Nodes (8): MainNavigation(), AlbumDetailScreen(), FolderDetailScreen(), TrackRowItem(), PlayerScreen(), QuickNavPill(), VideoPlayerScreen(), Listener

## Knowledge Gaps
- **9 isolated node(s):** `HOME`, `PLAYER`, `VIDEO_PLAYER`, `EQUALIZER`, `ALBUM_DETAIL` (+4 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 97 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **8 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MediaViewModel` connect `Audio Effects & Equalizer` to `Player UI & Compose Screens`, `State Management & ViewModel`, `State Management & ViewModel`, `Audio Effects & Equalizer`, `Module Homescreen Albumcard Artistcard`, `MainNavigation`?**
  _High betweenness centrality (0.307) - this node is a cross-community bridge._
- **What connects `HOME`, `PLAYER`, `VIDEO_PLAYER` to the rest of the system?**
  _9 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Player UI & Compose Screens` be split into smaller, more focused modules?**
  _Cohesion score 0.0629399585921325 - nodes in this community are weakly interconnected._
- **Why does `WavoraPlaybackService` connect `Media3 Playback Service` to `Player UI & Compose Screens`, `State Management & ViewModel`, `State Management & ViewModel`, `Audio Effects & Equalizer`, `Audio Effects & Equalizer`?**
  _High betweenness centrality (0.112) - this node is a cross-community bridge._
- **Should `Media3 Playback Service` be split into smaller, more focused modules?**
  _Cohesion score 0.0971322849213691 - nodes in this community are weakly interconnected._
- **Why does `MediaItem` connect `State Management & ViewModel` to `Player UI & Compose Screens`, `State Management & ViewModel`, `MainNavigation`, `Module Homescreen Albumcard Artistcard`?**
  _High betweenness centrality (0.103) - this node is a cross-community bridge._
- **Should `State Management & ViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.08866995073891626 - nodes in this community are weakly interconnected._