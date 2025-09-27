package snowballsh.soukou.core.music

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.util.fft.FFT

const val fftSize = bufferSize / 2

class FFTProcessor : AudioProcessor {
    val fft = FFT(bufferSize)
    val amplitudes = FloatArray(fftSize)  // will hold magnitudes for frequencies

    override fun process(event: AudioEvent): Boolean {
        val audioBuffer = event.floatBuffer  // float[] of length bufferSize
        // Perform FFT. Need a complex buffer: use size 2*bufferSize, pad with 0s.
        val complexBuffer = FloatArray(bufferSize * 2)
        audioBuffer.copyInto(complexBuffer, startIndex = 0, endIndex = audioBuffer.size)
        fft.forwardTransform(complexBuffer)  // in-place FFT
        fft.modulus(
            complexBuffer, amplitudes
        )

        return true
    }

    override fun processingFinished() { /* no-op */
    }
}
