package com.resonance.audio.data

import android.Manifest
import android.app.DownloadManager
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.resonance.audio.core.LibrarySource
import com.resonance.audio.core.model.FolderEntry
import com.resonance.audio.core.model.StorageInfo
import com.resonance.audio.core.model.Track
import com.resonance.audio.core.qualityLabelFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/** MediaStore + SAF implementation of the on-device library. */
class MediaStoreLibrary(
    private val context: Context,
    private val prefs: PrefsSettingsStore,
    private val requestPermissionAction: suspend () -> Boolean,
) : LibrarySource {

    override suspend fun hasAudioPermission(): Boolean {
        val ok33 = Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        val legacy = Build.VERSION.SDK_INT < 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
        return ok33 || legacy
    }

    override suspend fun requestPermission(): Boolean {
        if (hasAudioPermission()) return true
        return requestPermissionAction() && hasAudioPermission()
    }

    override suspend fun scanTracks(): List<Track> = withContext(Dispatchers.IO) {
        val out = mutableListOf<Track>()
        val hasGenre = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        val projection = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.RELATIVE_PATH,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.BITRATE,
        )
        if (hasGenre) projection += MediaStore.Audio.Media.GENRE
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection.toTypedArray(),
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC",
        )?.use { c ->
            val iId = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val iTitle = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val iArtist = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val iAlbum = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val iGenre = if (hasGenre) c.getColumnIndex(MediaStore.Audio.Media.GENRE) else -1
            val iDur = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val iData = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val iRel = c.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)
            val iSize = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val iBit = c.getColumnIndexOrThrow(MediaStore.Audio.Media.BITRATE)
            while (c.moveToNext()) {
                val dataPath = c.getString(iData) ?: continue
                val duration = if (iDur >= 0 && !c.isNull(iDur)) c.getLong(iDur) else 0L
                val size = if (iSize >= 0 && !c.isNull(iSize)) c.getLong(iSize) else 0L
                val mediaBitrate = if (iBit >= 0 && !c.isNull(iBit)) (c.getLong(iBit) / 1000L).toInt() else 0
                val bitrate = if (mediaBitrate > 0) mediaBitrate
                else if (duration > 0) ((size * 8.0) / (duration / 1000.0) / 1000.0).toInt()
                else 0
                val ext = dataPath.substringAfterLast('.', "").lowercase(Locale.US)
                val folder = if (iRel >= 0 && !c.isNull(iRel)) c.getString(iRel)?.trimEnd('/') else null
                val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, c.getLong(iId))
                val genre = if (iGenre >= 0 && !c.isNull(iGenre)) c.getString(iGenre)?.trim().orEmpty() else ""
                out += Track(
                    id = "ms-${c.getLong(iId)}",
                    title = (c.getString(iTitle) ?: "Unknown Title").trim(),
                    artist = (c.getString(iArtist) ?: "Unknown Artist").trim(),
                    album = (c.getString(iAlbum) ?: "").trim(),
                    genre = genre,
                    durationMs = duration,
                    path = uri.toString(),
                    formatLabel = qualityLabelFor(ext, bitrate),
                    folderPath = folder ?: "",
                    sizeBytes = size,
                )
            }
        }
        out
    }

    override suspend fun scanFolders(): List<FolderEntry> = withContext(Dispatchers.IO) {
        val tracks = scanTracks()
        val grouped = tracks.groupBy { it.folderPath.ifBlank { "Internal Storage" } }
        val entries = grouped.map { (folder, ts) ->
            FolderEntry(
                path = folder,
                displayName = folder.substringAfterLast('/').replaceFirstChar { it.titlecase(Locale.US) },
                trackCount = ts.size,
                enabled = true,
                labelBytes = ts.sumOf { it.sizeBytes },
            )
        }.sortedWith(compareByDescending<FolderEntry> { it.trackCount }.thenBy { it.path })

        val custom = prefs.customFolders().mapNotNull { treeUri ->
            val rootName = treeDisplayName(Uri.parse(treeUri)) ?: return@mapNotNull null
            val docs = safAudioDocs(Uri.parse(treeUri))
            FolderEntry(
                path = treeUri,
                displayName = "SAF · $rootName",
                trackCount = docs.size,
                enabled = true,
                labelBytes = 0,
            )
        }
        entries + custom
    }

    override suspend fun storageInfo(trackCount: Int): StorageInfo = withContext(Dispatchers.IO) {
        val stat = StatFs(Environment.getDataDirectory().absolutePath)
        val total = stat.totalBytes
        val free = stat.availableBytes
        val tracks = scanTracks()
        val music = tracks.sumOf { it.sizeBytes }
        val podcastDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS)
        val podcasts = dirSize(podcastDir).takeIf { it > 0 } ?: music / 16
        StorageInfo(
            usedBytes = total - free,
            totalBytes = total,
            musicBytes = music,
            podcastsBytes = podcasts,
            trackCount = trackCount,
            scanState = com.resonance.audio.core.model.ScanState.INDEXED,
            lastScanAtMs = 0,
        )
    }

    private fun dirSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    override suspend fun downloadToLibrary(title: String, artist: String, url: String) {
        withContext(Dispatchers.IO) {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val fileName = sanitizeFile("${artist}_${title}.mp3")
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(title)
                .setDescription("Resonance Audio download")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
            val id = try {
                dm.enqueue(request)
            } catch (t: Throwable) {
                -1L
            }
            if (id > 0) {
                awaitDownload(dm, id)
                val target = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                    fileName,
                )
                if (target.exists()) {
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(target.absolutePath),
                        arrayOf("audio/mpeg"),
                        null,
                    )
                }
            }
        }
    }

    private suspend fun awaitDownload(dm: DownloadManager, id: Long) {
        val endStates = setOf(DownloadManager.STATUS_SUCCESSFUL, DownloadManager.STATUS_FAILED)
        var tries = 0
        while (tries < 360) {
            val status = queryStatus(dm, id)
            if (status in endStates) return
            delay(500)
            tries++
        }
    }

    private fun queryStatus(dm: DownloadManager, id: Long): Int {
        val q = DownloadManager.Query().setFilterById(id)
        return try {
            dm.query(q)?.use { c ->
                if (c.moveToFirst()) c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                else DownloadManager.STATUS_FAILED
            } ?: DownloadManager.STATUS_FAILED
        } catch (t: Throwable) {
            DownloadManager.STATUS_FAILED
        }
    }

    override suspend fun isOnMeteredNetwork(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.isActiveNetworkMetered
    }

    // ---- SAF helpers (custom folders via tree URIs) ----

    private fun treeDisplayName(tree: Uri): String? = try {
        val docId = DocumentsContract.getTreeDocumentId(tree)
        val docUri = DocumentsContract.buildDocumentUriUsingTree(tree, docId)
        context.contentResolver.query(
            docUri,
            arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
    } catch (t: Throwable) {
        null
    }

    private fun childrenOf(tree: Uri, parentDocId: String): List<Pair<String, String>> {
        val uri = DocumentsContract.buildChildDocumentsUriUsingTree(tree, parentDocId)
        val out = mutableListOf<Pair<String, String>>()
        context.contentResolver.query(
            uri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            ),
            null,
            null,
            null,
        )?.use { c ->
            while (c.moveToNext()) {
                out += (c.getString(0) ?: continue) to (c.getString(1) ?: "")
            }
        }
        return out
    }

    private fun safAudioDocs(tree: Uri): List<Uri> = try {
        val rootId = DocumentsContract.getTreeDocumentId(tree)
        val first = childrenOf(tree, rootId)
        val second = first.filter { it.second == DocumentsContract.Document.MIME_TYPE_DIR }
            .flatMap { childrenOf(tree, it.first) }
        (first + second).asSequence()
            .filter { it.second != DocumentsContract.Document.MIME_TYPE_DIR }
            .map { it.first }
            .distinct()
            .map { DocumentsContract.buildDocumentUriUsingTree(tree, it) }
            .toList()
    } catch (t: Throwable) {
        emptyList()
    }
}

private fun sanitizeFile(name: String): String =
    name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
