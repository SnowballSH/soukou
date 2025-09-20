package snowballsh.soukou

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import snowballsh.soukou.core.music.AudioProps
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
    var audioProps by remember { mutableStateOf<AudioProps?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    MaterialTheme {
        // Ask user to input a music file
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
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
                            audioProps = loadAudioProps(f)
                        }
                    } catch (_: UnsupportedAudioFileException) {
                        error = "Unsupported audio file type."
                    } catch (e: Exception) {
                        error = "Failed to read audio: ${e.message}"
                    }
                }) {
                    Text("Open Audio…")
                }

                if (audioProps != null) {
                    Button(onClick = { audioProps = null }) { Text("Clear") }
                }
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            audioProps?.let { ap ->
                Divider()
                Text("File: ${ap.path}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                row("Format type", ap.formatType)
                row("Encoding", ap.encoding)
                row("Sample rate (Hz)", ap.sampleRateHz?.toString())
                row("Sample size (bits)", ap.sampleSizeBits?.toString())
                row("Channels", ap.channels?.toString())
                row("Frame size (bytes)", ap.frameSizeBytes?.toString())
            }
        }
    }
}