package snowballsh.soukou.core.music

data class AudioFrame(
    val spectrum: FloatArray,
    val rms: Double,
    val rmsSmoothed: Double,
    val dbFS: Double,
    val timeSec: Double,
    val transient: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioFrame

        if (rms != other.rms) return false
        if (rmsSmoothed != other.rmsSmoothed) return false
        if (dbFS != other.dbFS) return false
        if (timeSec != other.timeSec) return false
        if (transient != other.transient) return false
        if (!spectrum.contentEquals(other.spectrum)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rms.hashCode()
        result = 31 * result + rmsSmoothed.hashCode()
        result = 31 * result + dbFS.hashCode()
        result = 31 * result + timeSec.hashCode()
        result = 31 * result + transient.hashCode()
        result = 31 * result + spectrum.contentHashCode()
        return result
    }
}

