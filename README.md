# Shot Master — Starter scaffold

This repository contains a starter Android app scaffold for "Shot Master" (8 Ball Pool aiming overlay).

Quick start:
1. Open the project in Android Studio (Arctic Fox or newer).
2. Add an OpenCV Android module (or jitpack/maven dependency) and enable `implementation(project(":opencv"))` in app/build.gradle.kts.
3. Add Hilt plugin & annotate Application if using DI.
4. Run on a physical device (MediaProjection is not available on emulator).
5. Grant overlay permission (Settings → Draw over other apps) and then press "Start Overlay" to allow screen capture. The overlay service will start and show a small preview in the corner.

Important TODOs:
- Implement OpenCV detectors inside `vision/` (TableDetector, GuidelineDetector, BallDetector).
- Implement ShotCalculator and GuidelinePainter for actual drawing.
- Add Compose UI screens, settings persistence, billing flow per the build prompt.
- Add privacy policy & disclaimers on onboarding.

Privacy note: the current skeleton does not save or transmit screenshots. The real implementation must keep all processing local and declare this in your privacy policy.
