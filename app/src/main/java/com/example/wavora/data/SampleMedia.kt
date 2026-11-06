package com.example.wavora.data

import android.net.Uri

object SampleMedia {
    val sampleVideos = listOf(
        MediaItem(
            id = -101L,
            title = "Big Buck Bunny Cinema Showcase",
            artist = "Blender Open Animation",
            album = "Cinema 4K",
            albumArtUri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg"),
            contentUri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
            duration = 596000L,
            mediaType = MediaType.VIDEO,
            dateAdded = System.currentTimeMillis() - 86400000L,
            folder = "Cinema Samples",
            genre = "Animation",
            path = "sample/BigBuckBunny.mp4"
        ),
        MediaItem(
            id = -102L,
            title = "Elephants Dream Sci-Fi 3D",
            artist = "Orange Open Studio",
            album = "Short Films",
            albumArtUri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg"),
            contentUri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
            duration = 653000L,
            mediaType = MediaType.VIDEO,
            dateAdded = System.currentTimeMillis() - 172800000L,
            folder = "Sci-Fi",
            genre = "3D CGI",
            path = "sample/ElephantsDream.mp4"
        ),
        MediaItem(
            id = -103L,
            title = "For Bigger Blazes HDR Showcase",
            artist = "Chromecast HDR",
            album = "UltraHD Stream",
            albumArtUri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerBlazes.jpg"),
            contentUri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"),
            duration = 15000L,
            mediaType = MediaType.VIDEO,
            dateAdded = System.currentTimeMillis() - 259200000L,
            folder = "Showcase",
            genre = "Demo",
            path = "sample/ForBiggerBlazes.mp4"
        ),
        MediaItem(
            id = -104L,
            title = "Tears of Steel Futuristic VFX",
            artist = "Mango Open Movie",
            album = "VFX Reel",
            albumArtUri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/TearsOfSteel.jpg"),
            contentUri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"),
            duration = 734000L,
            mediaType = MediaType.VIDEO,
            dateAdded = System.currentTimeMillis() - 345600000L,
            folder = "VFX Reel",
            genre = "Sci-Fi",
            path = "sample/TearsOfSteel.mp4"
        )
    )

    val sampleAudio = listOf(
        MediaItem(
            id = -201L,
            title = "Midnight Odyssey (Synthwave Drive)",
            artist = "Kavinsky Waves",
            album = "Neon Velocity",
            albumArtUri = Uri.parse("https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400&q=80"),
            contentUri = Uri.parse("https://commondatastorage.googleapis.com/codeskulptor-demos/DDR_assets/Kangaroo_MusiQue_-_The_Neverending_Story.mp3"),
            duration = 215000L,
            mediaType = MediaType.AUDIO,
            dateAdded = System.currentTimeMillis() - 100000L,
            folder = "Electronic",
            genre = "Synthwave",
            path = "sample/MidnightOdyssey.mp3",
            playCount = 18,
            isFavorite = true
        ),
        MediaItem(
            id = -202L,
            title = "Cyber Horizon (Bassline)",
            artist = "Aura Matrix",
            album = "Cyberpunk 2099",
            albumArtUri = Uri.parse("https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=400&q=80"),
            contentUri = Uri.parse("https://commondatastorage.googleapis.com/codeskulptor-demos/DDR_assets/Sevish_-__nbsp_.mp3"),
            duration = 184000L,
            mediaType = MediaType.AUDIO,
            dateAdded = System.currentTimeMillis() - 200000L,
            folder = "Electronic",
            genre = "Cyberpunk",
            path = "sample/CyberHorizon.mp3",
            playCount = 12
        ),
        MediaItem(
            id = -203L,
            title = "Atmospheric Echoes (Ambient)",
            artist = "Solar Resonance",
            album = "Deep Space Ambience",
            albumArtUri = Uri.parse("https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400&q=80"),
            contentUri = Uri.parse("https://commondatastorage.googleapis.com/codeskulptor-assets/Epoq-Lepidoptera.ogg"),
            duration = 260000L,
            mediaType = MediaType.AUDIO,
            dateAdded = System.currentTimeMillis() - 300000L,
            folder = "Ambient",
            genre = "Ambient",
            path = "sample/AtmosphericEchoes.ogg",
            playCount = 24,
            isFavorite = true
        ),
        MediaItem(
            id = -204L,
            title = "Acoustic Sunrise Harmony",
            artist = "Luna Strings",
            album = "Golden Hour",
            albumArtUri = Uri.parse("https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400&q=80"),
            contentUri = Uri.parse("https://commondatastorage.googleapis.com/codeskulptor-demos/pyman_assets/ateapill.ogg"),
            duration = 172000L,
            mediaType = MediaType.AUDIO,
            dateAdded = System.currentTimeMillis() - 400000L,
            folder = "Acoustic",
            genre = "Acoustic",
            path = "sample/AcousticSunrise.ogg",
            playCount = 8
        )
    )
}
