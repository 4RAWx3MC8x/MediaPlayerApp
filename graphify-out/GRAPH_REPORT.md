# Graph Report - MediaPlayerApp  (2026-10-21)

## Corpus Check
- 26 files · ~24,171 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 30 file(s) not represented in the graph (top: .xml 14, .ttf 8, (none) 2)

## Summary
- 361 nodes · 800 edges · 18 communities (9 shown, 9 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 23 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Detail Screens & UI Components
- Playback Service & Media3 Session
- Media ViewModel State & Actions
- Media Data Repository
- Player Controller & State Management
- Equalizer & Audio FX UI
- App Navigation & Screen Composables
- Main Activity & Lifecycle
- ViewModel Unit Tests
- Audio Effects Engine
- HomeScreen.kt
- Screen Navigation Destinations
- Frontend Design

## God Nodes (most connected - your core abstractions)
1. `MediaViewModel` - 75 edges
2. `MediaItem` - 30 edges
3. `WavoraPlayerController` - 20 edges
4. `MediaRepository` - 17 edges
5. `WavoraPlaybackService` - 15 edges
6. `Callback` - 13 edges
7. `MainNavigation()` - 12 edges
8. `EqualizerState` - 12 edges
9. `MediaUiState` - 11 edges
10. `Album` - 11 edges

## Surprising Connections (you probably didn't know these)
- `MediaViewModel` --calls--> `MediaRepository`  [INFERRED]
  app/src/main/java/com/example/wavora/MediaViewModel.kt → app/src/main/java/com/example/wavora/data/MediaRepository.kt
- `MainNavigation()` --calls--> `EqualizerScreen()`  [INFERRED]
  app/src/main/java/com/example/wavora/Navigation.kt → app/src/main/java/com/example/wavora/ui/EqualizerScreen.kt
- `MainNavigation()` --calls--> `FoldersSelectionDialog()`  [INFERRED]
  app/src/main/java/com/example/wavora/Navigation.kt → app/src/main/java/com/example/wavora/ui/HomeScreen.kt
- `MainNavigation()` --calls--> `HomeScreen()`  [INFERRED]
  app/src/main/java/com/example/wavora/Navigation.kt → app/src/main/java/com/example/wavora/ui/HomeScreen.kt
- `MainNavigation()` --calls--> `PermissionScreen()`  [INFERRED]
  app/src/main/java/com/example/wavora/Navigation.kt → app/src/main/java/com/example/wavora/ui/PermissionScreen.kt

## Import Cycles
- None detected.

## Communities (18 total, 9 thin omitted)

### Community 0 - "Detail Screens & UI Components"
Cohesion: 0.10
Nodes (5): formatDuration(), SettingsActionCard(), SettingsScreen(), SettingsSectionHeader(), SettingsSwitchCard()

### Community 1 - "Playback Service & Media3 Session"
Cohesion: 0.10
Nodes (3): WavoraPlaybackService, Callback, Listener

### Community 3 - "Media Data Repository"
Cohesion: 0.11
Nodes (12): MediaRepository, Album, Artist, MediaFolder, MediaGenre, MediaItem, MediaPlaylist, MediaType (+4 more)

### Community 5 - "Equalizer & Audio FX UI"
Cohesion: 0.12
Nodes (9): EqualizerState, EqTabContent(), EqualizerScreen(), ReverbTabContent(), SpatialTabContent(), SpectrumCanvas(), ToneControlCard(), ToneTabContent() (+1 more)

### Community 6 - "App Navigation & Screen Composables"
Cohesion: 0.13
Nodes (14): MainNavigation(), PlayerState, AlbumDetailScreen(), FolderDetailScreen(), TrackRowItem(), PlayerScreen(), AlbumArtImage(), MiniPlayerBar() (+6 more)

### Community 7 - "Main Activity & Lifecycle"
Cohesion: 0.08
Nodes (3): MainScreenTest, MainActivity, WavoraTheme()

### Community 10 - "HomeScreen.kt"
Cohesion: 0.18
Nodes (13): AlbumCard(), AlbumsDirectView(), ArtistCard(), EmptyStateView(), FloatingNavIcon(), FolderCard(), FoldersSelectionDialog(), GenreCard() (+5 more)

### Community 11 - "Screen Navigation Destinations"
Cohesion: 0.25
Nodes (8): Screen, ALBUM_DETAIL, EQUALIZER, FOLDER_DETAIL, HOME, PLAYER, SETTINGS, VIDEO_PLAYER

### Community 16 - "Frontend Design"
Cohesion: 0.29
Nodes (6): Design principles, Frontend Design, Ground your designs in the subject matter, More on writing in design, Process: plan, review against the brief, build, critique, Restraint and self-critique

## Knowledge Gaps
- **9 isolated node(s):** `HOME`, `PLAYER`, `VIDEO_PLAYER`, `EQUALIZER`, `ALBUM_DETAIL` (+4 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 106 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **9 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MediaViewModel` connect `Media ViewModel State & Actions` to `Detail Screens & UI Components`, `Media Data Repository`, `Player Controller & State Management`, `Equalizer & Audio FX UI`, `App Navigation & Screen Composables`, `HomeScreen.kt`?**
  _High betweenness centrality (0.321) - this node is a cross-community bridge._
- **What connects `HOME`, `PLAYER`, `VIDEO_PLAYER` to the rest of the system?**
  _9 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Detail Screens & UI Components` be split into smaller, more focused modules?**
  _Cohesion score 0.09951690821256039 - nodes in this community are weakly interconnected._
- **Why does `WavoraPlaybackService` connect `Playback Service & Media3 Session` to `Detail Screens & UI Components`, `Audio Effects Engine`, `Media Data Repository`, `Player Controller & State Management`?**
  _High betweenness centrality (0.103) - this node is a cross-community bridge._
- **Should `Playback Service & Media3 Session` be split into smaller, more focused modules?**
  _Cohesion score 0.10404040404040404 - nodes in this community are weakly interconnected._
- **Why does `MediaRepository` connect `Media Data Repository` to `Playback Service & Media3 Session`, `Media ViewModel State & Actions`, `Main Activity & Lifecycle`?**
  _High betweenness centrality (0.098) - this node is a cross-community bridge._
- **Should `Media ViewModel State & Actions` be split into smaller, more focused modules?**
  _Cohesion score 0.07610993657505286 - nodes in this community are weakly interconnected._