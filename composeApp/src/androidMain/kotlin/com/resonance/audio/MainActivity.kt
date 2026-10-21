package com.resonance.audio

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.resonance.audio.app.AppStore
import com.resonance.audio.data.MediaStoreLibrary
import com.resonance.audio.data.PrefsSettingsStore
import com.resonance.audio.playback.ExoPlayerEngine
import com.resonance.audio.ui.AppRoot
import com.resonance.audio.ui.theme.ObsidianTheme
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainActivity : ComponentActivity() {

    private val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var storeRef: AppStore? = null

    private lateinit var permissionLauncher: androidx.activity.result.ActivityResultLauncher<String>
    private lateinit var folderLauncher: androidx.activity.result.ActivityResultLauncher<android.net.Uri?>
    private var permissionGate: CompletableDeferred<Boolean>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settings = PrefsSettingsStore(applicationContext, "resonance_prefs")

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                permissionGate?.complete(granted)
            }
        folderLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
                if (uri != null) {
                    try {
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION,
                        )
                    } catch (t: Throwable) {
                        // tree permission persistence unavailable; session use still fine
                    }
                    storeRef?.addCustomFolder(uri.toString())
                }
            }

        val library = MediaStoreLibrary(applicationContext, settings) {
            val gate = CompletableDeferred<Boolean>()
            permissionGate = gate
            val permission =
                if (Build.VERSION.SDK_INT >= 33) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
            permissionLauncher.launch(permission)
            gate.await()
        }
        val engine = ExoPlayerEngine(applicationContext)
        com.resonance.audio.playback.PlaybackRegistry.engine = engine
        val store = AppStore(library, settings, engine, storeScope)
        storeRef = store
        store.init()

        setContent {
            ObsidianTheme {
                AppRoot(store, onAddFolder = { folderLauncher.launch(null) })
            }
        }
    }
}
