package com.resonance.audio

import com.resonance.audio.core.GroupKind
import com.resonance.audio.core.groupTracks
import com.resonance.audio.core.model.Track
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class GroupingTest {

    private fun track(
        id: String,
        title: String,
        artist: String = "Art",
        album: String = "Album",
        genre: String = "Rock",
        folder: String = "Music/Resonance",
        duration: Long = 180_000,
    ) = Track(
        id = id,
        title = title,
        artist = artist,
        album = album,
        genre = genre,
        durationMs = duration,
        path = "content://media/$id",
        formatLabel = "MP3 320",
        folderPath = folder,
    )

    @Test
    fun artistsAreGroupedAlphabeticallyAndTracksSortByTitle() {
        val tracks = listOf(
            track("1", "Zebra", artist = "Beta"),
            track("2", "Apple", artist = "Alpha"),
            track("3", "Mango", artist = "Alpha"),
        )
        val groups = groupTracks(tracks, GroupKind.ARTIST)
        assertEquals(listOf("Alpha", "Beta"), groups.map { it.title })
        assertEquals(listOf("Apple", "Mango"), groups[0].tracks.map { it.title })
        assertEquals(2, groups[0].count)
        assertEquals(360_000, groups[0].durationMs)
        assertTrue(groups[0].subtitle.startsWith("1 album • 2 tracks • 6 min"))
    }

    @Test
    fun blankMetadataFallsBackToUnknownBuckets() {
        val tracks = listOf(
            track("1", "A", artist = "", album = "", genre = "", folder = ""),
        )
        assertEquals("Unknown Artist", groupTracks(tracks, GroupKind.ARTIST).single().title)
        assertEquals("Unknown Album", groupTracks(tracks, GroupKind.ALBUM).single().title)
        assertEquals("Unknown Genre", groupTracks(tracks, GroupKind.GENRE).single().title)
        assertEquals("Internal Storage", groupTracks(tracks, GroupKind.FOLDER).single().title)
    }

    @Test
    fun foldersUseLastPathSegmentAsTitleAndKeepFullPathAsKey() {
        val tracks = listOf(
            track("1", "A", folder = "Music/Resonance"),
            track("2", "B", folder = "Music/Resonance/"),
            track("3", "C", folder = "Music/Jazz"),
        )
        val groups = groupTracks(tracks, GroupKind.FOLDER)
        assertEquals(listOf("Jazz", "Resonance"), groups.map { it.title })
        assertEquals("Music/Resonance", groups[1].key)
        assertEquals(2, groups[1].count)
    }

    @Test
    fun genresSeparateTracksAndReportLength() {
        val groups = groupTracks(
            listOf(
                track("1", "A", genre = "Ambient", duration = 2_400_000),
                track("2", "B", genre = "Ambient", duration = 1_200_000),
                track("3", "C", genre = "Jazz"),
                track("4", "D", genre = "Jazz"),
                track("5", "E", genre = "Jazz"),
                track("6", "F", genre = "Jazz"),
                track("7", "G", genre = "Jazz"),
                track("8", "H", genre = "Jazz"),
                track("9", "I", genre = "Jazz"),
                track("10", "J", genre = "Jazz"),
                track("11", "K", genre = "Jazz"),
                track("12", "L", genre = "Jazz"),
                track("13", "M", genre = "Jazz"),
            ),
            GroupKind.GENRE,
        )
        val ambient = groups.first { it.title == "Ambient" }
        assertEquals("2 tracks • 1 hr 0 min", ambient.subtitle)
        assertEquals(3_600_000, ambient.durationMs)
    }

    @Test
    fun seedDependsOnKindAndKey() {
        val tracks = listOf(track("1", "A", artist = "Alpha", album = "Alpha", genre = "Alpha", folder = "Alpha"))
        val artistSeed = groupTracks(tracks, GroupKind.ARTIST).single().seed
        val albumSeed = groupTracks(tracks, GroupKind.ALBUM).single().seed
        assertNotEquals(artistSeed, albumSeed)
        assertEquals(artistSeed, groupTracks(tracks, GroupKind.ARTIST).single().seed)
    }

    @Test
    fun emptyInputProducesNoGroups() {
        assertTrue(groupTracks(emptyList(), GroupKind.ALBUM).isEmpty())
    }
}
