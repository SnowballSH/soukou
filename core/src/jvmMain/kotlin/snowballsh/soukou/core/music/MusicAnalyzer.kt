package snowballsh.soukou.core.music

import be.tarsos.dsp.io.jvm.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchDetectionResult
import be.tarsos.dsp.pitch.PitchProcessor
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Simple wrapper around TarsosDSP that demonstrates pitch detection from in-memory audio samples.
 */
class MusicAnalyzer(
    private val sampleRate: Float,
    private val bufferSize: Int = 1024,
    private val overlap: Int = 0,
    private val algorithm: PitchProcessor.PitchEstimationAlgorithm = PitchProcessor.PitchEstimationAlgorithm.YIN,
) {
    init {
        require(sampleRate > 0) { "Sample rate must be positive." }
        require(bufferSize > 0) { "Buffer size must be positive." }
        require(overlap >= 0) { "Overlap must not be negative." }
        require(overlap < bufferSize) { "Overlap must be smaller than buffer size." }
    }

    /**
     * Runs the pitch detector over the provided audio samples.
     */
    fun analyzePitch(samples: FloatArray): PitchAnalysis {
        require(samples.isNotEmpty()) { "Audio sample array must not be empty." }

        val dispatcher = AudioDispatcherFactory.fromFloatArray(
            samples,
            sampleRate.roundToInt(),
            bufferSize,
            overlap,
        )
        var pitchSum = 0f
        var probabilitySum = 0f
        var detectionCount = 0

        val handler = PitchDetectionHandler { result: PitchDetectionResult, _ ->
            val pitch = result.pitch
            if (pitch > 0f && result.isPitched) {
                detectionCount += 1
                pitchSum += pitch
                probabilitySum += result.probability
            }
        }

        dispatcher.addAudioProcessor(
            PitchProcessor(algorithm, sampleRate, bufferSize, handler),
        )
        dispatcher.run()

        val averagePitch = if (detectionCount > 0) pitchSum / detectionCount else null
        val averageProbability = if (detectionCount > 0) probabilitySum / detectionCount else 0f

        return PitchAnalysis(
            averagePitchHz = averagePitch,
            averageProbability = averageProbability,
            detectionCount = detectionCount,
        )
    }

    /**
     * Generates a sine wave for the requested frequency and duration and analyses it.
     */
    fun analyzeSineWave(
        frequencyHz: Float,
        durationSeconds: Float,
    ): PitchAnalysis {
        require(frequencyHz > 0f) { "Frequency must be positive." }
        require(durationSeconds > 0f) { "Duration must be positive." }

        val sampleCount = max(
            bufferSize,
            (durationSeconds * sampleRate).roundToInt().coerceAtLeast(1),
        )

        val sampleRateDouble = sampleRate.toDouble()
        val angularVelocity = 2.0 * PI * frequencyHz / sampleRateDouble
        val samples = FloatArray(sampleCount) { index ->
            sin(angularVelocity * index).toFloat()
        }

        return analyzePitch(samples)
    }
}

/**
 * Convenience helper used by the desktop app to showcase the analyser with a reference A4 tone.
 */
fun analyzeReferenceTone(): PitchAnalysis {
    val analyzer = MusicAnalyzer(sampleRate = 44_100f)
    return analyzer.analyzeSineWave(frequencyHz = 440f, durationSeconds = 1f)
}
