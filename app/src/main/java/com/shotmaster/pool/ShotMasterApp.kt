package com.shotmaster.pool

import android.app.Application
import android.util.Log
import org.opencv.android.OpenCVLoader

class ShotMasterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize OpenCV
        try {
            val ok = OpenCVLoader.initDebug()
            Log.i("ShotMasterApp", "OpenCV initDebug result=$ok")
        } catch (t: Throwable) {
            Log.w("ShotMasterApp", "OpenCV init failed", t)
        }
    }
}
