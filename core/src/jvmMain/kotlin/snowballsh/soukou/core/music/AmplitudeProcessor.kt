package snowballsh.soukou.core.music

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import kotlin.math.sqrt

class AmplitudeProcessor : AudioProcessor {
    var currentVolume = 0.0

    override fun process(event: AudioEvent): Boolean {
        val buf = event.floatBuffer
        val sumSquares = buf.fold(0.0) { acc, sample -> acc + (sample * sample) }
        val rms = sqrt(sumSquares / buf.size)
        currentVolume = rms
        return true
    }

    override fun processingFinished() {}
}