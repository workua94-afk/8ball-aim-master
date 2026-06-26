# ProGuard rules for Shot Master

# Keep OpenCV
-keep class org.opencv.** { *; }
-keepclasseswithmembernames class org.opencv.** { *; }

# Keep Hilt
-keep class com.shotmaster.pool.di.** { *; }
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep core vision classes
-keep class com.shotmaster.pool.vision.** { *; }
-keep class com.shotmaster.pool.billing.** { *; }
-keep class com.shotmaster.pool.calibration.** { *; }

# Keep services
-keep class com.shotmaster.pool.OverlayService { *; }
-keep class com.shotmaster.pool.ShotMasterApp { *; }

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
