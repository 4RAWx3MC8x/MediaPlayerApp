package com.example.wavora

import com.example.wavora.data.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class WavoraUnitTest {

    @Test
    fun formatDuration_underOneHour() {
        assertEquals("0:00", 0L.formatDuration())
        assertEquals("0:05", 5000L.formatDuration())
        assertEquals("1:05", 65000L.formatDuration())
        assertEquals("59:59", (59 * 60 * 1000L + 59000L).formatDuration())
    }

    @Test
    fun formatDuration_overOneHour() {
        assertEquals("1:00:00", 3600000L.formatDuration())
        assertEquals("1:01:05", (3600000L + 65000L).formatDuration())
    }

    @Test
    fun equalizerState_defaultValues() {
        val state = EqualizerState()
        assertEquals("Wide", state.spatialPreset)
        assertEquals("Hall", state.reverbPreset)
        assertEquals(true, state.isLoudnessEqualizerOn)
    }

    @Test
    fun customization_defaultValues() {
        val customization = AppCustomization()
        assertEquals(ViewMode.LIST, customization.viewMode)
        assertEquals(SortOption.TITLE, customization.sortBy)
        assertEquals(true, customization.sortAscending)
        assertEquals(MediaFilter.ALL, customization.filter)
    }

    @Test
    fun mediaType_enumCoverage() {
        assertNotNull(MediaType.AUDIO)
        assertNotNull(MediaType.VIDEO)
        assertNotNull(ViewMode.GRID)
        assertNotNull(ViewMode.COMPACT)
        assertNotNull(SortOption.DURATION)
        assertNotNull(SearchCategory.VIDEOS)
    }
}
