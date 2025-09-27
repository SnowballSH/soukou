package snowballsh.soukou

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import snowballsh.soukou.core.music.AudioCoroutineController
import snowballsh.soukou.core.music.AudioFrame
import snowballsh.soukou.core.music.AudioManager
import snowballsh.soukou.core.music.loadAudioProps
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.sound.sampled.UnsupportedAudioFileException


@Composable
fun row(label: String, value: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label)
        Text(value ?: "—")
    }
}

@Composable
@Preview
fun App() {
    val scope = rememberCoroutineScope()

    var audioManager by remember { mutableStateOf<AudioManager?>(null) }
    var controller by remember { mutableStateOf<AudioCoroutineController?>(null) }

    // Live frame coming from audio thread; update UI state on UI scope
    var lastFrame by remember { mutableStateOf<AudioFrame?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // When audioManager changes, stop previous controller and start a new one
    LaunchedEffect(audioManager) {
        // Stop previous
        controller?.stop()
        controller = null

        audioManager?.let { mgr ->
            controller = AudioCoroutineController(
                manager = mgr,
                scope = scope,
                onFrame = { frame ->
                    // Ensure state change on UI thread
                    scope.launch { lastFrame = frame }
                },
                onFinished = {
                    scope.launch { /* could set a flag if needed */ }
                }
            ).also { it.start() }
        }
    }

    // Stop controller when composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            val c = controller
            if (c != null) {
                // Best-effort stop
                scope.launch { c.stop() }
            }
        }
    }

    MaterialTheme {
        // Ask user to input a music file
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Load an audio file and show properties", style = MaterialTheme.typography.headlineMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    try {
                        error = null
                        val fd = FileDialog(null as Frame?, "Choose audio", FileDialog.LOAD)
                        fd.isVisible = true
                        val dir = fd.directory
                        val name = fd.file
                        if (dir != null && name != null) {
                            val f = File(dir, name)
                            val ap = loadAudioProps(f)
                            audioManager = AudioManager(ap)
                        }
                    } catch (_: UnsupportedAudioFileException) {
                        error = "Unsupported audio file type."
                    } catch (e: Exception) {
                        error = "Failed to read audio: ${e.message}"
                    }
                }) {
                    Text("Open Audio…")
                }

                if (audioManager != null) {
                    Button(onClick = {
                        val c = controller
                        if (c != null) scope.launch { c.stop() }
                        controller = null
                        audioManager = null
                        lastFrame = null
                    }) { Text("Clear") }
                }
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // Show audio props if loaded
            audioManager?.audioProps?.let { ap ->
                Column(Modifier.widthIn(max = 600.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    row("File", ap.file.name)
                    row("Encoding", ap.encoding)
                    row("Sample rate (Hz)", ap.sampleRateHz.toString())
                    row("Sample size (bits)", ap.sampleSizeBits.toString())
                    row("Channels", ap.channels.toString())
                    row("Frame rate (Hz)", ap.frameRateHz.toString())
                    row("Frame size (bytes)", ap.frameSizeBytes.toString())
                }
            }

            // Live metrics
            lastFrame?.let { f ->
                Column(Modifier.widthIn(max = 600.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    row("Time (s)", "%.2f".format(f.timeSec))
                    row("RMS", "%.4f".format(f.rms))
                    row("RMS (smoothed)", "%.4f".format(f.rmsSmoothed))
                    row("dBFS", "%.2f".format(f.dbFS))
                    row("Beat/transient", if (f.transient) "Yes" else "No")
                }
            }
        }
    }
}