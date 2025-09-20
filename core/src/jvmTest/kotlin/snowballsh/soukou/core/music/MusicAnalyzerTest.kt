package snowballsh.soukou.core.music

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class MusicAnalyzerTest {
    private val sampleRate = 44_100f

    @Test
    fun detectsPitchOfSineWave() {
        val analyzer = MusicAnalyzer(sampleRate = sampleRate)
        val result = analyzer.analyzeSineWave(frequencyHz = 440f, durationSeconds = 1f)

        val pitch = result.averagePitchHz
        assertNotNull(pitch, "Expected a detected pitch for a clean sine wave")
        assertTrue(abs(pitch - 440f) < 5f, "Detected pitch should be close to 440 Hz but was $pitch")
        assertTrue(result.detectionCount > 0, "Should collect at least one voiced frame")
        assertTrue(result.averageProbability > 0.5f, "Sine wave should have reasonable confidence")
    }

    @Test
    fun analyzePitchRejectsEmptySamples() {
        val analyzer = MusicAnalyzer(sampleRate = sampleRate)
        assertFailsWith<IllegalArgumentException> {
            analyzer.analyzePitch(FloatArray(0))
        }
    }

    @Test
    fun silenceYieldsNoPitch() {
        val analyzer = MusicAnalyzer(sampleRate = sampleRate)
        val silentAudio = FloatArray((sampleRate).toInt())

        val result = analyzer.analyzePitch(silentAudio)

        assertNull(result.averagePitchHz, "Silence should not produce a pitch")
        assertEquals(0, result.detectionCount)
        assertEquals(0f, result.averageProbability)
        assertTrue(!result.hasReliablePitch)
    }
}
