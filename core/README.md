# core module

A Kotlin Multiplatform library with a JVM target, intended for reusable, testable logic shared with the app.

What it contains:
- `snowballsh.soukou.core.Greeter` in `src/commonMain`
- Music processing utilities in `snowballsh.soukou.core.music` backed by the [TarsosDSP](https://github.com/JorenSix/TarsosDSP) library (JVM target)
- Unit tests in `src/commonTest` plus JVM-specific tests in `src/jvmTest`

Build & test this module independently:

```bash
./gradlew :core:compileKotlinJvm
./gradlew :core:jvmTest
```

Use from the app:
- The app (`composeApp`) depends on this module and can import `snowballsh.soukou.core.Greeter`.
- `composeApp` also showcases `snowballsh.soukou.core.music.analyzeReferenceTone()` to display a detected pitch on screen.

Notes:
- Tests use Kotlin `kotlin.test` API mapped to JUnit on JVM.
- The JVM implementation pulls in the `com.github.st-h:TarsosDSP` dependency for DSP utilities.
- You can add more targets later by expanding the `kotlin { }` block in `core/build.gradle.kts`.

