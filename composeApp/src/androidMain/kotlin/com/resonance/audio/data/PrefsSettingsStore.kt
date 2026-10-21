package com.resonance.audio.data

import android.content.Context
import android.content.SharedPreferences
import com.resonance.audio.core.SettingsStore
import com.resonance.audio.core.model.Playlist

/** SharedPreferences backed implementation of SettingsStore. */
class PrefsSettingsStore(context: Context, name: String) : SettingsStore {

    private val prefs: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    private fun readBool(key: String, def: Boolean): Boolean = prefs.getBoolean(key, def)

    private fun writeBool(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    override suspend fun wifiOnly(): Boolean = readBool(K_WIFI, true)

    override suspend fun setWifiOnly(on: Boolean) = writeBool(K_WIFI, on)

    override suspend fun streamQuality(): Int = prefs.getInt(K_QUALITY, 2)

    override suspend fun setStreamQuality(q: Int) {
        prefs.edit().putInt(K_QUALITY, q).apply()
    }

    override suspend fun ignoreShortAudio(): Boolean = readBool(K_IGNORE_SHORT, true)

    override suspend fun setIgnoreShortAudio(on: Boolean) = writeBool(K_IGNORE_SHORT, on)

    override suspend fun autoScanNewMedia(): Boolean = readBool(K_AUTO_SCAN, true)

    override suspend fun setAutoScanNewMedia(on: Boolean) = writeBool(K_AUTO_SCAN, on)

    override suspend fun folderEnabled(path: String): Boolean = prefs.getBoolean(folderKey(path), true)

    override suspend fun setFolderEnabled(path: String, on: Boolean) {
        prefs.edit().putBoolean(folderKey(path), on).apply()
    }

    private fun folderKey(path: String) = "fol:" + path.hashCode().toString(16)

    override suspend fun customFolders(): List<String> =
        prefs.getString(K_CUSTOM_FOLDERS, "")?.split(UNIT_SEP)?.filter { it.isNotBlank() } ?: emptyList()

    override suspend fun addCustomFolder(path: String) {
        val cur = customFolders().toMutableList()
        if (path !in cur) cur += path
        prefs.edit().putString(K_CUSTOM_FOLDERS, cur.joinToString(UNIT_SEP)).apply()
    }

    override suspend fun favorites(): Set<String> =
        prefs.getString(K_FAVORITES, "")?.split(UNIT_SEP)?.filter { it.isNotBlank() }?.toSet() ?: emptySet()

    override suspend fun setFavorite(id: String, on: Boolean) {
        val cur = favorites().toMutableSet()
        if (on) cur += id else cur -= id
        prefs.edit().putString(K_FAVORITES, cur.joinToString(UNIT_SEP)).apply()
    }

    override suspend fun recentTrackIds(): List<String> =
        prefs.getString(K_RECENT, "")?.split(UNIT_SEP)?.filter { it.isNotBlank() } ?: emptyList()

    override suspend fun pushRecentTrackId(id: String) {
        val cur = recentTrackIds().toMutableList()
        cur -= id
        cur.add(0, id)
        if (cur.size > 20) prefs.edit().putString(K_RECENT, cur.take(20).joinToString(UNIT_SEP)).apply()
        else prefs.edit().putString(K_RECENT, cur.joinToString(UNIT_SEP)).apply()
    }

    override suspend fun playlists(): List<Playlist> {
        val raw = prefs.getString(K_PLAYLISTS, "") ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return raw.split(REC_SEP).mapNotNull { rec ->
            val parts = rec.split(FIELD_SEP)
            if (parts.size < 3) return@mapNotNull null
            Playlist(
                id = parts[0],
                name = parts[1],
                trackIds = if (parts[2].isBlank()) emptyList() else parts[2].split(UNIT_SEP),
            )
        }
    }

    override suspend fun savePlaylist(playlist: Playlist) {
        val cur = playlists().toMutableList()
        cur.removeAll { it.id == playlist.id }
        cur.add(playlist)
        storePlaylists(cur)
    }

    override suspend fun deletePlaylist(id: String) {
        storePlaylists(playlists().filter { it.id != id })
    }

    private fun storePlaylists(list: List<Playlist>) {
        // "id<US>name<RS>trackIds...</US>..." — control-char separators, sanitized names
        val encoded = list.joinToString(REC_SEP) { p ->
            listOf(p.id, sanitize(p.name), p.trackIds.joinToString(UNIT_SEP)).joinToString(FIELD_SEP)
        }
        prefs.edit().putString(K_PLAYLISTS, encoded).apply()
    }

    override suspend fun lastScanMs(): Long = prefs.getLong(K_LAST_SCAN, 0L)

    override suspend fun setLastScanMs(ms: Long) {
        prefs.edit().putLong(K_LAST_SCAN, ms).apply()
    }

    private companion object {
        const val K_WIFI = "wifiOnly"
        const val K_QUALITY = "streamQuality"
        const val K_IGNORE_SHORT = "ignoreShort"
        const val K_AUTO_SCAN = "autoScan"
        const val K_CUSTOM_FOLDERS = "customFolders"
        const val K_FAVORITES = "favorites"
        const val K_RECENT = "recentIds"
        const val K_PLAYLISTS = "playlists"
        const val K_LAST_SCAN = "lastScanMs"
        const val UNIT_SEP = "\u0001"
        const val FIELD_SEP = "\u001F"
        const val REC_SEP = "\u001E"

        private fun sanitize(s: String) = s.replace(REC_SEP, "_").replace(FIELD_SEP, "_").replace(UNIT_SEP, "_")
    }
}
