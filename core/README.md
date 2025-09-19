# core module

A Kotlin Multiplatform library with a JVM target, intended for reusable, testable logic shared with the app.

What it contains:
- `snowballsh.soukou.core.Greeter` in `src/commonMain`
- Unit tests in `src/commonTest`

Build & test this module independently:

```bash
./gradlew :core:compileKotlinJvm
./gradlew :core:jvmTest
```

Use from the app:
- The app (`composeApp`) depends on this module and can import `snowballsh.soukou.core.Greeter`.

Notes:
- Tests use Kotlin `kotlin.test` API mapped to JUnit on JVM.
- You can add more targets later by expanding the `kotlin { }` block in `core/build.gradle.kts`.

