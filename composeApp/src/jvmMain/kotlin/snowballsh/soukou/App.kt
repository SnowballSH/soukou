package snowballsh.soukou

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import snowballsh.soukou.core.music.AudioCoroutineController
import snowballsh.soukou.core.music.AudioFrame
import snowballsh.soukou.core.music.AudioManager
import snowballsh.soukou.core.music.AudioProps
import snowballsh.soukou.core.music.loadAudioProps
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.sound.sampled.UnsupportedAudioFileException
import kotlin.math.min
import kotlin.math.sqrt


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

    var audioProps by remember { mutableStateOf<AudioProps?>(null) }
    var controller by remember { mutableStateOf<AudioCoroutineController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    // Live frame coming from audio thread; update UI state on UI scope
    var lastFrame by remember { mutableStateOf<AudioFrame?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun stopPlayback(clearFrame: Boolean = false) {
        val active = controller
        isPlaying = false
        if (active != null) {
            active.stop()
            controller = null
        }
        if (clearFrame) {
            lastFrame = null
        }
    }

    suspend fun startPlayback(props: AudioProps) {
        stopPlayback(clearFrame = false)

        val manager = AudioManager(props)
        val newController = AudioCoroutineController(
            manager = manager,
            scope = scope,
            onFrame = { frame ->
                scope.launch { lastFrame = frame }
            },
            onFinished = {
                scope.launch {
                    isPlaying = false
                    controller = null
                }
            }
        )

        controller = newController
        lastFrame = null
        isPlaying = true
        newController.start()
    }

    // Stop controller when composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            scope.launch { stopPlayback(clearFrame = true) }
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
            Text("Real-time audio visualizer", style = MaterialTheme.typography.headlineMedium)

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
                            val props = loadAudioProps(f)
                            audioProps = props
                            lastFrame = null
                            scope.launch { startPlayback(props) }
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
                    Button(onClick = {
                        scope.launch {
                            stopPlayback(clearFrame = true)
                            audioProps = null
                        }
                    }) { Text("Clear") }
                }
            }

            if (audioProps != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        enabled = audioProps != null,
                        onClick = {
                            val props = audioProps ?: return@Button
                            scope.launch { startPlayback(props) }
                        }
                    ) {
                        Text(if (isPlaying) "Restart" else "Play")
                    }

                    Button(
                        enabled = controller != null,
                        onClick = { scope.launch { stopPlayback(clearFrame = false) } }
                    ) {
                        Text("Stop")
                    }

                    Text(
                        if (isPlaying) "Playing" else "Stopped",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // Show audio props if loaded
            audioProps?.let { ap ->
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
                Column(
                    Modifier
                        .widthIn(max = 640.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SpectrumVisualizer(
                        spectrum = f.spectrum,
                        rms = f.rmsSmoothed,
                        transient = f.transient,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
}

@Composable
private fun SpectrumVisualizer(
    spectrum: FloatArray,
    rms: Double,
    transient: Boolean,
    modifier: Modifier = Modifier,
    bars: Int = 48
) {
    val density = LocalDensity.current
    val horizontalPaddingPx = with(density) { 16.dp.toPx() }
    val verticalPaddingPx = with(density) { 16.dp.toPx() }

    val bucketValues = remember(spectrum, bars) {
        val safeSpectrum = if (spectrum.isNotEmpty()) spectrum else floatArrayOf()
        val safeBars = min(bars, safeSpectrum.size.coerceAtLeast(1))
        val bucketSize = (safeSpectrum.size / safeBars).coerceAtLeast(1)

        FloatArray(safeBars) { index ->
            val start = index * bucketSize
            val end = min(start + bucketSize, safeSpectrum.size)
            var maxValue = 0f
            for (i in start until end) {
                val value = safeSpectrum[i]
                if (value > maxValue) {
                    maxValue = value
                }
            }
            maxValue
        }
    }

    val maxBucket = bucketValues.maxOrNull()?.takeIf { it > 0f } ?: 1f
    val intensity = sqrt(rms.toFloat().coerceAtLeast(0f)).coerceIn(0f, 1f)
    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor = colorScheme.surfaceVariant.copy(alpha = 0.55f)
    val gradient = remember(intensity, transient, colorScheme.primary, colorScheme.secondary) {
        val base = colorScheme.primary
        val highlight = colorScheme.secondary
        val topColor = if (transient) highlight.copy(alpha = 0.95f) else highlight.copy(alpha = 0.75f)
        val bottomColor = base.copy(alpha = 0.6f + 0.35f * intensity)
        Brush.verticalGradient(listOf(topColor, bottomColor))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .padding(vertical = 8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barCount = bucketValues.size
            if (barCount == 0 || size.width <= 0f || size.height <= 0f) {
                return@Canvas
            }

            val availableWidth = size.width - horizontalPaddingPx * 2
            val availableHeight = size.height - verticalPaddingPx * 2
            if (availableWidth <= 0f || availableHeight <= 0f) return@Canvas

            val space = availableWidth / (barCount * 5f)
            val totalSpacing = space * (barCount - 1)
            val barWidth = ((availableWidth - totalSpacing) / barCount).coerceAtLeast(1f)

            var x = horizontalPaddingPx
            for (index in 0 until barCount) {
                val raw = bucketValues[index] / maxBucket
                val eased = sqrt(raw.coerceIn(0f, 1f))
                val barHeight = eased * availableHeight
                val top = size.height - verticalPaddingPx - barHeight

                drawRoundRect(
                    brush = gradient,
                    topLeft = Offset(x, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )

                x += barWidth + space
            }
        }
    }
}
