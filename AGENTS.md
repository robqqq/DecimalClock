# Repository Guidelines

## Project Structure & Module Organization
- Android/Compose app lives in `app/` with Kotlin sources under `app/src/main/java/com/example/decimaltime`.
- UI assets such as Compose themes and widget code are next to feature files; XML drawables and layouts live in `app/src/main/res`.
- Instrumented tests belong in `app/src/androidTest`, JVM unit tests in `app/src/test`, and Gradle/IDE settings stay at the repo root.

## Build, Test, and Development Commands
- `./gradlew assembleDebug` builds the debug APK; add `-x test` only when you intentionally skip unit tests.
- `./gradlew test` runs JVM unit tests in `app/src/test`.
- `./gradlew connectedAndroidTest` executes instrumentation tests on an attached emulator/device.
- `./gradlew lint ktlintFormat` (if ktlint is added) should be run before commits; lint fixes common Compose issues early.

## Coding Style & Naming Conventions
- Kotlin follows Jetpack Compose defaults: 4-space indentation, PascalCase for classes/composables (`DecimalClockScreen`), camelCase for functions and fields.
- Keep composables small and previewable; prefer passing `Modifier` as the first optional parameter after required ones.
- Resource names use lowercase underscore (`ic_launcher_foreground.xml`), and Gradle modules stick to lowerCamel `applicationId`.

## Testing Guidelines
- Unit tests use JUnit4; name files `<Feature>Test.kt` and functions `fun method_underTest_expectedBehavior()`.
- Compose UI tests go under `androidTest` using `createAndroidComposeRule`.
- Target at least smoke coverage for new time formatting or widget logic, especially anything touching `DecimalTimeFormatter`.

## Commit & Pull Request Guidelines
- Follow the existing history's imperative style (e.g., `create project`); keep summaries under ~60 chars.
- Each PR should explain the change, note any UI impact (include screenshots for visual tweaks), and link related issues.
- Reference test evidence in the PR body, e.g., "`./gradlew test connectedAndroidTest`".

## Security & Configuration Tips
- Local secrets (API keys, keystores) stay out of the repo; add paths to `local.properties` or `.gradle/gradle.properties`.
- When testing widgets, ensure the emulator has the correct locale/timezone to match decimal clock expectations.
