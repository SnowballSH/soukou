package snowballsh.soukou.core.music

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.pow

class AudioCoroutineController(
    private val manager: AudioManager,
    private val scope: CoroutineScope,                 // pass rememberCoroutineScope() from Compose
    private val onFrame: (AudioFrame) -> Unit = {},
    private val onFinished: () -> Unit = {}
) {
    private var job: Job? = null

    private val eps = 1e-12
    private var smoothedRms = 0.0
    private val alpha = 0.25f // smoothing

    // Track beats reported by BeatProcessor to emit a one-shot transient flag
    private var lastEmittedBeatTime: Double = -1.0

    private val uiTap = object : AudioProcessor {
        override fun process(event: AudioEvent): Boolean {
            val timeSec = event.timeStamp

            // Pull values calculated by processors wired in AudioManager
            val rms = manager.ampProcessor.currentVolume

            // Smooth RMS for UI
            smoothedRms = if (smoothedRms == 0.0) rms else alpha * rms + (1 - alpha) * smoothedRms
            val dbFS = 20.0 * kotlin.math.log10(max(rms, eps))

            // Spectrum as computed by FFTProcessor; copy for UI safety and apply gentle companding
            val sourceAmps = manager.fftProcessor.amplitudes
            val spectrum = FloatArray(sourceAmps.size)
            for (i in sourceAmps.indices) {
                val v = sourceAmps[i]
                spectrum[i] = if (v < 1e-6f) 0f else v.pow(0.6f)
            }

            // Transient based on BeatProcessor (one-shot when a new beat time appears)
            val beatTime = manager.beatProcessor.lastBeatTime
            val transient = if (beatTime > lastEmittedBeatTime) {
                lastEmittedBeatTime = beatTime
                true
            } else {
                false
            }

            onFrame(
                AudioFrame(
                    spectrum = spectrum,
                    rms = rms,
                    rmsSmoothed = smoothedRms,
                    dbFS = dbFS,
                    timeSec = timeSec,
                    transient = transient
                )
            )
            return true
        }

        override fun processingFinished() {
            onFinished()
        }
    }

    fun start() {
        if (job?.isActive == true) return
        manager.audioDispatcher.addAudioProcessor(uiTap)
        job = scope.launch(Dispatchers.IO) {
            // AudioDispatcher implements Runnable, so just call run()
            manager.audioDispatcher.run()
        }
    }

    suspend fun stop() {
        val j = job ?: return
        manager.audioDispatcher.stop()
        // Cooperatively wait for completion (processingFinished will be called)
        j.join()
        // Remove our UI tap to avoid duplicates on a future start
        manager.audioDispatcher.removeAudioProcessor(uiTap)
        job = null
    }
}
