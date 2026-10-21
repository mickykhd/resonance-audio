package com.resonance.audio.app

import com.resonance.audio.core.LibrarySource
import com.resonance.audio.core.PlayerController
import com.resonance.audio.core.SettingsStore
import com.resonance.audio.core.model.FolderEntry
import com.resonance.audio.core.model.PlaybackUiState
import com.resonance.audio.core.model.Playlist
import com.resonance.audio.core.model.RepeatMode
import com.resonance.audio.core.model.Route
import com.resonance.audio.core.model.ScanState
import com.resonance.audio.core.model.Station
import com.resonance.audio.core.model.StorageInfo
import com.resonance.audio.core.model.StreamTrack
import com.resonance.audio.core.model.PodcastEpisode
import com.resonance.audio.core.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class SortMode(val label: String) { DATE_ADDED("Date Added"), TITLE("Title"), ARTIST("Artist"), DURATION("Duration") }

class AppStore(
    private val library: LibrarySource,
    private val settings: SettingsStore,
    private val player: PlayerController,
    private val scope: CoroutineScope,
) {
    val playerState: StateFlow<PlaybackUiState> = player.state
    val eqState = player.eq

    val route = MutableStateFlow(Route.LIBRARY)
    val nowPlayingOpen = MutableStateFlow(false)

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks

    private val _folders = MutableStateFlow<List<FolderEntry>>(emptyList())
    val folders: StateFlow<List<FolderEntry>> = _folders

    private val _storage = MutableStateFlow(
        StorageInfo(
            usedBytes = 0,
            totalBytes = 0,
            musicBytes = 0,
            podcastsBytes = 0,
            trackCount = 0,
            scanState = ScanState.IDLE,
            lastScanAtMs = 0,
        )
    )
    val storage: StateFlow<StorageInfo> = _storage

    val scanning = MutableStateFlow(false)
    val permissionDenied = MutableStateFlow(false)

    val playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val favorites = MutableStateFlow<Set<String>>(emptySet())
    val recentIds = MutableStateFlow<List<String>>(emptyList())

    val libraryFilter = MutableStateFlow("All Songs")
    val sortMode = MutableStateFlow(SortMode.DATE_ADDED)
    val searchOpen = MutableStateFlow(false)
    val searchQuery = MutableStateFlow("")

    val wifiOnly = MutableStateFlow(true)
    val quality = MutableStateFlow(2)
    val ignoreShort = MutableStateFlow(true)
    val autoScan = MutableStateFlow(true)

    val messages = MutableSharedFlow<String>(extraBufferCapacity = 4)

    private fun notify(msg: String) {
        messages.tryEmit(msg)
    }

    fun init() {
        scope.launch {
            loadSettings()
            loadLibrary(interactive = false)
        }
    }

    private suspend fun loadSettings() {
        wifiOnly.value = settings.wifiOnly()
        quality.value = settings.streamQuality()
        ignoreShort.value = settings.ignoreShortAudio()
        autoScan.value = settings.autoScanNewMedia()
        favorites.value = settings.favorites()
        recentIds.value = settings.recentTrackIds()
        playlists.value = settings.playlists()
    }

    suspend fun loadLibrary(interactive: Boolean) {
        if (_storage.value.scanState != ScanState.SCANNING) {
            _storage.value = _storage.value.copy(scanState = ScanState.SCANNING)
        }
        val granted = if (interactive) library.requestPermission() else library.hasAudioPermission()
        if (!granted) {
            permissionDenied.value = true
            _storage.value = _storage.value.copy(scanState = ScanState.IDLE)
            return
        }
        permissionDenied.value = false
        val minDuration = if (ignoreShort.value) 30_000 else 0
        val all = library.scanTracks().filter { it.durationMs >= minDuration }
        _tracks.value = all
        _folders.value = reconcileFolders(library.scanFolders())
        _storage.value = library.storageInfo(_tracks.value.size).let {
            it.copy(lastScanAtMs = settings.lastScanMs())
        }
        _storage.value = _storage.value.copy(scanState = ScanState.INDEXED)
    }

    private suspend fun reconcileFolders(raw: List<FolderEntry>): List<FolderEntry> {
        return raw.map { entry ->
            entry.copy(enabled = settings.folderEnabled(entry.path))
        }
    }

    fun quickScan() {
        scope.launch {
            if (scanning.value) return@launch
            scanAndIndex()
        }
    }

    val scanTick = MutableStateFlow(0)

    private suspend fun scanAndIndex() {
        scanning.value = true
        try {
            loadLibrary(interactive = false)
            settings.setLastScanMs(com.resonance.audio.core.epochNowMs())
            _storage.value = _storage.value.copy(lastScanAtMs = settings.lastScanMs())
            scanTick.value += 1
            notify("Library rescanned — ${_tracks.value.size} tracks indexed")
        } finally {
            scanning.value = false
        }
    }

    fun play(tracks: List<Track>, index: Int, sourceLabel: String = "Playing from Library") {
        val t = tracks
        if (t.isEmpty()) return
        val i = index.coerceIn(0, t.lastIndex)
        currentOrLastQueue.value = t
        player.playQueue(t, i, sourceLabel)
        scope.launch { settings.pushRecentTrackId(t[i].id) }
        refreshRecents()
    }

    fun shuffleAll() {
        val t = _tracks.value
        if (t.isEmpty()) {
            notify("No indexed tracks yet — run a Quick Scan")
            return
        }
        player.setShuffle(true)
        val start = (0 until t.size).random()
        play(t, start, "Playing from Library")
        openNowPlaying()
    }

    fun playStation(st: Station) {
        scope.launch {
            if (!networkAllowsStreaming()) return@launch
            val track = Track(
                id = st.id,
                title = st.name,
                artist = st.provider,
                album = "Live Radio",
                durationMs = 0,
                path = st.streamUrl,
                formatLabel = st.bitrateLabel,
                isRemote = true,
            )
            player.playLive(listOf(track), 0, "Now Streaming")
        }
    }

    fun playDownload(t: StreamTrack) {
        scope.launch {
            if (!networkAllowsStreaming()) return@launch
            val track = Track(
                id = t.id,
                title = t.title,
                artist = t.author,
                album = t.source,
                durationMs = 0,
                path = t.url,
                formatLabel = t.formatLine,
                isRemote = true,
            )
            player.playLive(listOf(track), 0, "Creative Commons")
        }
    }

    fun playPodcast(p: PodcastEpisode) {
        scope.launch {
            if (!networkAllowsStreaming()) return@launch
            val track = Track(
                id = p.id,
                title = "${p.show} — ${p.title}",
                artist = p.show,
                album = "Podcast",
                durationMs = 0,
                path = p.url,
                formatLabel = "RSS Episode",
                isRemote = true,
            )
            player.playLive(listOf(track), 0, "RSS Feeds")
        }
    }

    private suspend fun networkAllowsStreaming(): Boolean {
        if (wifiOnly.value && library.isOnMeteredNetwork()) {
            notify("Wi-Fi only mode is on — turn it off to use mobile data")
            return false
        }
        return true
    }

    fun downloadStreamTrack(t: StreamTrack) {
        scope.launch {
            if (!networkAllowsStreaming()) return@launch
            library.downloadToLibrary(t.title, t.author, t.url)
            loadLibrary(interactive = false)
            notify("Saved to your library: ${t.title}")
        }
    }

    fun toggleFolder(path: String, on: Boolean) {
        scope.launch {
            settings.setFolderEnabled(path, on)
            _folders.value = _folders.value.map {
                if (it.path == path) it.copy(enabled = on) else it
            }
            loadLibrary(interactive = false)
        }
    }

    fun addCustomFolder(path: String) {
        scope.launch {
            settings.addCustomFolder(path)
            loadLibrary(interactive = false)
        }
    }

    fun setIgnoreShort(on: Boolean) {
        scope.launch {
            ignoreShort.value = on
            settings.setIgnoreShortAudio(on)
            loadLibrary(interactive = false)
        }
    }

    fun setAutoScan(on: Boolean) {
        scope.launch {
            autoScan.value = on
            settings.setAutoScanNewMedia(on)
        }
    }

    fun setWifiOnly(on: Boolean) {
        scope.launch {
            wifiOnly.value = on
            settings.setWifiOnly(on)
        }
    }

    fun setQuality(q: Int) {
        scope.launch {
            quality.value = q
            settings.setStreamQuality(q)
        }
    }

    fun toggleFavorite(track: Track) {
        scope.launch {
            val on = track.id !in favorites.value
            settings.setFavorite(track.id, on)
            favorites.value = settings.favorites()
        }
    }

    fun isFavorite(id: String): Boolean = id in favorites.value

    private fun refreshRecents() {
        scope.launch {
            recentIds.value = settings.recentTrackIds()
        }
    }

    val currentOrLastQueue = MutableStateFlow<List<Track>>(emptyList())

    fun recentForCarousel(limit: Int = 8): List<Track> {
        val byId = _tracks.value.associateBy { it.id }
        val recentTracks = recentIds.value.mapNotNull { byId[it] }
        val recentIdSet = recentIds.value.toSet()
        val queue = currentOrLastQueue.value.filter { it.id !in recentIdSet }
        return (recentTracks + queue.take(4)).distinct().take(limit)
    }

    fun createPlaylist(name: String, seedTrack: Track? = null) {
        val n = name.trim()
        if (n.isEmpty()) return
        scope.launch {
            val id = "pl-${com.resonance.audio.core.model.stableHash("name:$n")}"
            if (playlists.value.any { it.id == id }) {
                notify("A playlist with that name already exists")
                return@launch
            }
            val p = Playlist(id, n, seedTrack?.let { listOf(it.id) } ?: emptyList())
            settings.savePlaylist(p)
            playlists.value = settings.playlists()
        }
    }

    fun deletePlaylist(id: String) {
        scope.launch {
            settings.deletePlaylist(id)
            playlists.value = settings.playlists()
        }
    }

    fun addToPlaylist(playlistId: String, track: Track) {
        scope.launch {
            val p = playlists.value.firstOrNull { it.id == playlistId } ?: return@launch
            if (track.id in p.trackIds) {
                notify("Already in playlist")
                return@launch
            }
            settings.savePlaylist(p.copy(trackIds = p.trackIds + track.id))
            playlists.value = settings.playlists()
            notify("Added to ${p.name}")
        }
    }

    fun playlistTracks(p: Playlist): List<Track> {
        val byId = _tracks.value.associateBy { it.id }
        return p.trackIds.mapNotNull { byId[it] }
    }

    fun playPlaylist(p: Playlist) {
        val ts = playlistTracks(p)
        if (ts.isEmpty()) {
            notify("Playlist is empty")
            return
        }
        play(ts, 0, "Playing from ${p.name}")
        openNowPlaying()
    }

    fun setRoute(r: Route) {
        route.value = r
    }

    fun openNowPlaying() {
        if (playerState.value.hasCurrent) nowPlayingOpen.value = true
    }

    fun closeNowPlaying() {
        nowPlayingOpen.value = false
    }

    fun toggleSearch(): Boolean {
        val next = !searchOpen.value
        searchOpen.value = next
        if (!next) searchQuery.value = ""
        return next
    }

    fun playerActionNext() = player.next()
    fun playerActionPrev() = player.previous()
    fun playerActionPlayPause() = player.playPause()
    fun playerSeek(ms: Long) = player.seekTo(ms)
    fun playerShuffle(on: Boolean) = player.setShuffle(on)
    fun playerRepeat(): RepeatMode = player.cycleRepeatMode()

    fun setEqBand(index: Int, levelDb: Int) {
        scope.launch { player.setEqBandLevel(index, levelDb) }
    }

    fun applyEqPreset(name: String) {
        scope.launch { player.applyEqPreset(name) }
    }

    fun setBassBoost(fraction: Float) {
        scope.launch { player.setBassBoost(fraction) }
    }

    fun setSpatialFrac(fraction: Float) {
        scope.launch { player.setSpatial(fraction) }
    }

    fun isCurrentFavorite(): Boolean {
        val id = playerState.value.currentId
        return id.isNotEmpty() && id in favorites.value
    }

    fun toggleFavoriteCurrent() {
        val id = playerState.value.currentId
        val track = _tracks.value.firstOrNull { it.id == id }
            ?: currentOrLastQueue.value.firstOrNull { it.id == id }
            ?: Track(
                id = id,
                title = playerState.value.title,
                artist = playerState.value.artist,
                album = "",
                durationMs = playerState.value.durationMs,
                path = "",
                formatLabel = playerState.value.formatLabel,
            )
        toggleFavorite(track)
    }
}
