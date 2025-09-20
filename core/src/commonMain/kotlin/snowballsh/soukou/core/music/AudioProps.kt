package snowballsh.soukou.core.music

import java.io.File
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

data class AudioProps(
    val path: String,
    val formatType: String?,
    val encoding: String?,
    val sampleRateHz: Float?,
    val sampleSizeBits: Int?,
    val channels: Int?,
    val frameRateHz: Float?,
    val frameSizeBytes: Int?
)

fun loadAudioProps(file: File): AudioProps {
    val aff: AudioFileFormat = AudioSystem.getAudioFileFormat(file)
    val format: AudioFormat? = aff.format

    return AudioProps(
        path = file.path,
        formatType = aff.type.toString(),
        encoding = format?.encoding?.toString(),
        sampleRateHz = format?.sampleRate,
        sampleSizeBits = format?.sampleSizeInBits,
        channels = format?.channels,
        frameRateHz = format?.frameRate,
        frameSizeBytes = format?.frameSize
    )
}
