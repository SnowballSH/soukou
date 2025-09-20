package snowballsh.soukou.core.music

/**
 * Represents the summary of a pitch analysis using TarsosDSP.
 *
 * @property averagePitchHz The average pitch detected across all voiced frames, or `null` when no stable pitch was found.
 * @property averageProbability The average confidence reported by TarsosDSP for voiced frames (0.0 – 1.0).
 * @property detectionCount Number of voiced frames considered in the average.
 */
data class PitchAnalysis(
    val averagePitchHz: Float?,
    val averageProbability: Float,
    val detectionCount: Int,
) {
    /** Indicates whether the analysis detected a confident pitch. */
    val hasReliablePitch: Boolean
        get() = averagePitchHz != null && averageProbability >= 0.8f
}
