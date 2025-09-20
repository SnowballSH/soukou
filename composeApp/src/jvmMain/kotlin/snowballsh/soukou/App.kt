package snowballsh.soukou

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import soukou.composeapp.generated.resources.Res
import soukou.composeapp.generated.resources.compose_multiplatform
import snowballsh.soukou.core.Greeter
import snowballsh.soukou.core.music.PitchAnalysis
import snowballsh.soukou.core.music.analyzeReferenceTone
import kotlin.math.roundToInt

@Composable
@Preview
fun App() {
    MaterialTheme {
        var analysis by remember { mutableStateOf<PitchAnalysis?>(null) }
        val greeting = remember { Greeter().greet("Compose") }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { analysis = analyzeReferenceTone() }) {
                Text("Analyze reference tone")
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(painterResource(Res.drawable.compose_multiplatform), null)
                Text("Core: $greeting")
                if (analysis == null) {
                    Text("Press the button to analyse a generated A4 tone.")
                } else {
                    val result = analysis!!
                    val pitchText = result.averagePitchHz?.let { "${it.roundToInt()} Hz" } ?: "No stable pitch detected"
                    val probabilityPercent = (result.averageProbability * 100).roundToInt().coerceIn(0, 100)
                    Text("Estimated pitch: $pitchText")
                    Text("Detections: ${result.detectionCount}, confidence ≈ $probabilityPercent%")
                    Text(
                        if (result.hasReliablePitch) "Pitch detection is confident." else "Pitch detection was inconclusive."
                    )
                }
            }
        }
    }
}