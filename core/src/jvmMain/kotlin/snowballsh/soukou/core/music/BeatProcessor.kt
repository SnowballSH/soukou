package snowballsh.soukou.core.music

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.onsets.PercussionOnsetDetector

class BeatProcessor(
    sampleRateHz: Float,
    bufferSize: Int,
    overlap: Int
) : AudioProcessor {

    private val detector: PercussionOnsetDetector

    @Volatile
    private var _lastBeatTime: Double = 0.0
    val lastBeatTime: Double
        get() = _lastBeatTime

    init {
        detector = PercussionOnsetDetector(
            sampleRateHz,
            bufferSize,
            overlap
        ) { time, _ ->
            _lastBeatTime = time
        }
    }

    override fun process(audioEvent: AudioEvent): Boolean = detector.process(audioEvent)

    override fun processingFinished() {
        detector.processingFinished()
    }
}
