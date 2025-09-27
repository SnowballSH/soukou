package snowballsh.soukou.core.music

import java.io.File
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.UnsupportedAudioFileException

data class AudioProps(
    val file: File,
    val format: AudioFormat,
    val encoding: String,
    val sampleRateHz: Float,
    val sampleSizeBits: Int,
    val channels: Int,
    val frameRateHz: Float,
    val frameSizeBytes: Int
)

fun loadAudioProps(file: File): AudioProps {
    val aff: AudioFileFormat = AudioSystem.getAudioFileFormat(file)
    val format: AudioFormat? = aff.format

    return AudioProps(
        file = file,
        format = format ?: throw UnsupportedAudioFileException("Unsupported audio format"),
        encoding = format.encoding.toString(),
        sampleRateHz = format.sampleRate,
        sampleSizeBits = format.sampleSizeInBits,
        channels = format.channels,
        frameRateHz = format.frameRate,
        frameSizeBytes = format.frameSize
    )
}
