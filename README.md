# GlassFold Launcher

An experimental Android launcher that presents a clean grid plus a translucent dock, tuned for foldable devices.

## Requirements
- Android SDK 34+ (ensure `ANDROID_HOME` or `ANDROID_SDK_ROOT` is set, or place `sdk.dir` in `local.properties`)
- Java 17 (toolchains configured in Gradle)
- Kotlin 1.9

## Build
```bash
./gradlew :app:assembleDebug
```

If `./gradlew` is missing, the CI workflow and `settings.gradle` will create a wrapper and attempt to detect the SDK path automatically.

## Features
- Home screen grid populated from installed launchable apps
- Customizable dock transparency, blur, corner radius, and app count
- Settings screen backed by `res/xml/prefs_launcher.xml`

## Troubleshooting
- JVM target mismatch: The project enforces Java/Kotlin 17 via Gradle toolchains. If you see target errors, ensure Java 17 is installed and rerun the build.
- Missing SDK: Provide `local.properties` with `sdk.dir=/path/to/android-sdk` or set `ANDROID_SDK_ROOT`.
