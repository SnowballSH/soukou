package snowballsh.soukou.core.music

import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory
import be.tarsos.dsp.io.jvm.AudioPlayer


const val bufferSize = 2048
const val overlap = bufferSize / 2

class AudioManager(
    val audioProps: AudioProps
) {
    val audioDispatcher: AudioDispatcher = AudioDispatcherFactory.fromFile(
        audioProps.file,
        bufferSize,
        overlap
    )
    val fftProcessor = FFTProcessor()
    val ampProcessor = AmplitudeProcessor()
    val beatProcessor = BeatProcessor(audioProps.sampleRateHz, bufferSize, overlap)

    init {
        audioDispatcher.addAudioProcessor(
            AudioPlayer(audioProps.format)
        )
        audioDispatcher.addAudioProcessor(fftProcessor)
        audioDispatcher.addAudioProcessor(ampProcessor)
        audioDispatcher.addAudioProcessor(beatProcessor)
    }
}