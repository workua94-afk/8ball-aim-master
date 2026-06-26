# Shot Master — Updated with OpenCV integration

I added OpenCV (org.opencv:opencv:4.9.0) to the app module and implemented a real TableDetector using OpenCV image processing.

What's new:
- settings.gradle.kts now configures mavenCentral() and plugin repositories
- app/build.gradle.kts includes the OpenCV Maven artifact and repositories
- ShotMasterApp initializes OpenCV on application start
- TableDetector (vision) implemented using HSV masking + morphology + contour selection

Next steps (I'll continue implementing these in sequence):
1. GuidelineDetector (Hough lines) and BallDetector (Hough circles)
2. ShotCalculator math and clipping
3. GuidelinePainter drawing in OverlayView
4. Floating control widget behavior and mode switching
5. Compose UI, DataStore, Hilt wiring, BillingManager

If you want me to continue, I'll implement GuidelineDetector and BallDetector next and wire them into the OverlayService processing pipeline (CV dispatcher).