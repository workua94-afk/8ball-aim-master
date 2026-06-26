# Shot Master — Build & Deployment Guide

## End-to-End Setup in Android Studio

### **Step 1: Open the Project**

1. Open **Android Studio** (Arctic Fox 2020.3.1 or newer recommended)
2. Click **File** → **Open** and select the `8ball-aim-master` directory
3. Wait for Gradle sync to complete (~2–5 minutes on first run)
4. Check the **Build** tab at the bottom for any errors

### **Step 2: Add Hilt Plugin (if not auto-detected)**

1. Open `build.gradle.kts` (project root)
2. Add to `pluginManagement` block:
   ```kotlin
   id("com.google.dagger.hilt.android") version "2.51" apply false
   ```
3. Sync Gradle again

### **Step 3: Update Play Billing Credentials**

1. Open `app/src/main/java/com/shotmaster/pool/billing/BillingManager.kt`
2. Replace the placeholder SKU IDs with your actual **Play Console** product IDs:
   ```kotlin
   const val SKU_WEEKLY = "your_weekly_sku"      // e.g., shot_master_weekly
   const val SKU_MONTHLY = "your_monthly_sku"    // e.g., shot_master_monthly
   const val SKU_YEARLY = "your_yearly_sku"      // e.g., shot_master_yearly
   ```
3. (Optional for testing) Add your test account email to Play Console → App → Settings → License Testing

### **Step 4: Update Privacy Policy URL (Onboarding)**

1. If you have a hosted privacy policy, update `app/src/main/java/com/shotmaster/pool/ui/OnboardingScreen.kt`
2. Add a link or text in the UI pointing to your privacy policy
3. Privacy policy must state:
   - No screenshots are saved or transmitted
   - All processing is local/on-device
   - No personal data collected
   - Billing by Google Play

### **Step 5: Connect a Physical Device**

**Why:** MediaProjection API is not available on emulators; you **must** test on a real Android device (API 26+).

**Setup:**
1. Connect an Android phone via USB (Android 8.0 / API 26 or higher)
2. Enable **Developer Options**: Settings → About Phone → tap Build Number 7 times
3. Enable **USB Debugging**: Settings → Developer Options → USB Debugging → **ON**
4. In Android Studio, select your device in the device dropdown (top toolbar)
5. Check logcat to confirm connection: `adb devices` should list your device

### **Step 6: Grant Runtime Permissions**

1. On your device, go to **Settings** → **Apps** → **Permissions**
2. Grant (or pre-grant) the following:
   - **Draw over other apps** (SYSTEM_ALERT_WINDOW)
   - **Camera** (for future camera-based enhancements, optional)
3. The app will request **POST_NOTIFICATIONS** (Android 13+) and **MediaProjection** at runtime

### **Step 7: Build & Run**

1. In Android Studio, click **Build** → **Make Project**
   - Wait for `BUILD SUCCESSFUL`
   - Check the **Build** output tab for any errors
2. Click **Run** → **Run 'app'** (or press `Shift + F10`)
   - Select your connected device
   - The app will install and launch (~1–2 min)
3. Logcat will show:
   ```
   I/ShotMasterApp: OpenCV initDebug result=true
   I/OverlayService: Screen capture started: 1080x2400
   ```

### **Step 8: Test the Overlay**

1. On the app's main screen, tap **Grant Overlay Permission** → open Settings and enable
2. Tap **Start Overlay** → accept the "Screen Recording" permission prompt
3. The app will show a semi-transparent debug preview in the top-left corner of your screen
4. Open **8 Ball Pool** in the background → you should see the small debug bitmap updating
5. Tap the device back button to return to the app and stop the overlay service

### **Step 9: Tuning Parameters (if needed)**

Depending on your device's screen resolution, adjust ball detection parameters:

**For 1080×2400 phones:**
- `TableDetector`: HSV range is pre-tuned; adjust S/V if table looks wrong
- `BallDetector`: min radius = 12, max radius = 22 (adjust in `BallDetector.kt` or via calibration)

**For 1440×3120 phones (QHD):**
- Increase ball min/max radius by ~30%: min = 16, max = 28

**For tablets (2K+):**
- Increase ball radius further: min = 24, max = 40

Or use the **auto-calibration** routine:
- Capture a frame while 8 Ball Pool's waiting screen is visible (shows table + balls)
- The app auto-detects average ball size and adjusts parameters

### **Step 10: Performance Tuning**

**Frame processing time should be < 80ms per frame.**

Monitor in Logcat:
```
D/CVPipeline: Frame 123: 45ms (table=true, guideline=true, balls=true)
```

If > 80ms:
1. Reduce `TableDetector` recompute frequency (already cached every 30 frames)
2. Downscale the input bitmap before OpenCV processing
3. Use fewer Hough algorithm iterations
4. Profile with Android Studio's **CPU Profiler** (Run → Profile 'app')

### **Step 11: Prepare for Play Store Release**

#### **App Icon**
1. In Android Studio, right-click `app/src/main/res` → **New** → **Image Asset**
2. Upload your 512×512 icon PNG
3. Leave as default or customize (will generate all required sizes)
4. Click **Next** → **Finish**

#### **App Signing**
1. Go to **Build** → **Generate Signed Bundle / APK**
2. Select **APK**
3. Click **Create new** → create a keystore file (save it securely!)
4. Fill in: **Key store path**, **Password**, **Key alias**, **Key password**
5. Choose **Release** build variant
6. Finish signing (~30 seconds)
7. The signed APK will be in `app/release/`

#### **Play Console Setup**
1. Go to **Google Play Console** → **Create App** → fill in app name, category (Games), content rating
2. **Store Listing** → add title, short description, full description (mention "training aid only")
3. **Graphics** → upload screenshots (5–8 images showing the overlay in action)
4. **Content Rating** → complete the questionnaire (rate as appropriate)
5. **Pricing & Distribution** → set to **Free** or choose subscription-only
6. **Billing** → create the three subscription SKUs (weekly, monthly, yearly) with exact IDs matching your code
7. **Release** → upload signed APK and promote to **Production** (or **Closed Testing** first)
8. **Data Safety** → declare no personal data collection, no crash logs, billing only
9. Wait **1–2 hours** for Google Play to review and publish

#### **Privacy Policy**
1. Host a privacy policy at a public URL (e.g., GitHub Pages, your website)
2. Include:
   ```
   - No screenshots are saved to device or uploaded
   - All image processing happens on-device in memory
   - No personal data is collected or transmitted
   - Billing handled exclusively by Google Play
   - No third-party analytics or tracking
   ```
3. Update the onboarding screen with a link to this URL

### **Step 12: Testing & Debugging**

**Common Issues:**

| Issue | Solution |
|---|---|
| "OpenCV initDebug result=false" | Ensure OpenCV AAR is properly downloaded. Try: **Build** → **Clean Project** → **Rebuild Project** |
| Screen Recording permission denied | This is expected; user must grant it when prompted by the OS. Test again after granting. |
| "No table detected" | Adjust HSV range in `TableDetector`. Try adjusting H: 30–90 or S: 40–255 if table is misdetected. |
| "Guideline not detected" | Ensure 8 Ball Pool's guideline is visible in-game. Adjust Canny thresholds in `GuidelineDetector` (50, 150). |
| "Balls not detected" | Adjust ball radius range in `BallDetector` or run auto-calibration. Increase Hough param2 for sensitivity. |
| High frame time (>150ms) | Profile with Android Profiler; likely GPU/CPU bottleneck. Downscale input bitmap before OpenCV. |

### **Step 13: Continuous Improvement**

1. **Collect device feedback** from beta testers on different phone models
2. **Adjust HSV/Hough parameters** per device or resolution
3. **Monitor Play Store reviews** for crashes, false positives, or accuracy issues
4. **Push updates** with tuned parameter presets per resolution
5. **Add A/B testing** in Settings for new guideline modes

---

## Folder Structure Reference

```
8ball-aim-master/
├── app/
│   ├── src/main/
│   │   ├── java/com/shotmaster/pool/
│   │   │   ├── MainActivity.kt               ← Home UI
│   │   │   ├── OverlayService.kt             ← Main service (orchestrates pipeline)
│   │   │   ├── OverlayView.kt                ← Overlay canvas drawing
│   │   │   ├── ScreenCaptureManager.kt       ← MediaProjection + ImageReader
│   │   │   ├── CVProcessingDispatcher.kt     ← CV executor thread
│   │   │   ├── ShotMasterApp.kt              ← App init + OpenCV loader
│   │   │   ├── di/
│   │   │   │   ├── AppModule.kt              ← Hilt DI module
│   │   │   │   └── PreferencesManager.kt     ← DataStore persistence
│   │   │   ├── vision/
│   │   │   │   ├── TableDetector.kt          ← HSV table detection
│   │   │   │   ├── GuidelineDetector.kt      ← HoughLinesP guideline
│   │   │   │   ├── BallDetector.kt           ← HoughCircles ball detection
│   │   │   │   ├── ShotCalculator.kt         ← Line math + clipping
│   │   │   │   └── GuidelinePainter.kt       ← Canvas drawing (4 modes)
│   │   │   ├── billing/
│   │   │   │   └── BillingManager.kt         ← Play Billing integration
│   │   │   ├── calibration/
│   │   │   │   └── CalibrationRoutine.kt     ← Auto-calibration routine
│   │   │   ├── profiling/
│   │   │   │   └── FrameProfiler.kt          ← Performance monitoring
│   │   │   └── ui/
│   │   │       ├── SettingsScreen.kt         ← Settings UI (Compose)
│   │   │       ├── SubscriptionScreen.kt     ← Paywall UI
│   │   │       └── OnboardingScreen.kt       ← Disclaimer + privacy
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── overlay_control_panel.xml ← Floating widget layout
│   │   │   └── mipmap/
│   │   │       └── ic_launcher.xml           ← App icon
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts                      ← App-level Gradle (dependencies, Hilt plugin)
│   └── proguard-rules.pro                    ← R8 obfuscation rules
├── build.gradle.kts                         ← Project-level Gradle
├── settings.gradle.kts                      ← Project settings (repos, includes)
└── README.md                                ← This file
```

---

## Licensing & Legal

**Disclaimer (must appear in app and Play Store):**
> "Shot Master is an independent training tool that uses on-device screen analysis to help players learn shot angles. It does not modify, inject, or interfere with any game's code or memory. It is intended for practice and educational use only. Using aiming assistance tools may violate the Terms of Service of certain competitive gaming platforms. Please review the Terms of Service of any game before use. The developer is not responsible for any account actions taken by game publishers."

**Privacy Policy (host externally):**
> "Shot Master does not save, transmit, or log any captured images. All screen analysis is performed locally on your device. No personal data is collected. Billing is handled exclusively by Google Play."

---

## Next Steps

1. ✅ Clone/pull the repo and open in Android Studio
2. ✅ Complete Gradle sync (Step 1–2)
3. ✅ Update billing SKUs and privacy policy URL (Step 3–4)
4. ✅ Connect physical device and grant permissions (Step 5–6)
5. ✅ Build and run (Step 7–8)
6. ✅ Test overlay with 8 Ball Pool open (Step 8)
7. ⭐ (Optional) Auto-calibrate or manually tune parameters (Step 9)
8. ⭐ (Optional) Profile and optimize frame processing (Step 10)
9. 📱 Prepare Play Console and release (Step 11–13)

---

## Support & Troubleshooting

- **OpenCV not loading?** → Check logcat: `I/ShotMasterApp`. If "false", rebuild the project.
- **Hilt compilation errors?** → Ensure `com.google.dagger.hilt.android` plugin is in app-level Gradle.
- **Permissions denied?** → Grant manually in Settings or allow via Android OS prompt on first overlay start.
- **Frame processing slow?** → Use Android Profiler (Run → Profile) to identify bottleneck (likely Hough or contour detection).
- **Table/balls not detected?** → Adjust HSV range or Hough parameters; see Step 9.

For detailed OpenCV tuning, see the original build prompt at the repo root.
