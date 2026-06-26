package com.shotmaster.pool

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import android.view.Gravity

class OverlayService : Service() {

    companion object {
        const val CHANNEL_ID = "shot_master_channel"
        const val NOTIFICATION_ID = 41234

        const val EXTRA_START_PROJECTION_INTENT = "extra_start_projection_intent"
        const val EXTRA_RESULT_CODE = "extra_result_code"
    }

    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        fun getService(): OverlayService = this@OverlayService
    }

    private lateinit var windowManager: WindowManager
    private var overlayView: OverlayView? = null
    private var screenCaptureManager: ScreenCaptureManager? = null
    private var mediaProjection: MediaProjection? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())

        // Retrieve projection intent and start capture
        val projIntent = intent?.getParcelableExtra<Intent>(EXTRA_START_PROJECTION_INTENT)
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, -1) ?: -1
        if (projIntent != null && resultCode != -1) {
            val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = mpm.getMediaProjection(resultCode, projIntent)
            startOverlay()
        } else {
            // If missing, stop service
            stopSelf()
        }

        return START_STICKY
    }

    private fun startOverlay() {
        if (overlayView != null) return

        overlayView = OverlayView(this)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        windowManager.addView(overlayView, params)

        // start capture at device resolution
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        mediaProjection?.let { mp ->
            screenCaptureManager = ScreenCaptureManager(
                mediaProjection = mp,
                screenWidth = width,
                screenHeight = height,
                onFrame = { bitmap ->
                    // Frame received on background thread from ScreenCaptureManager
                    // TODO: offload to CV dispatcher and run OpenCV detectors
                    overlayView?.post {
                        // For now, just display a simple indicator
                        overlayView?.setDebugBitmap(bitmap)
                        overlayView?.invalidate()
                    }
                }
            )
            screenCaptureManager?.start()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onDestroy() {
        screenCaptureManager?.stop()
        overlayView?.let { windowManager.removeView(it) }
        overlayView = null
        mediaProjection?.stop()
        super.onDestroy()
    }

    private fun buildNotification() =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Shot Master Active")
            .setContentText("Overlay running — tap to manage")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Shot Master",
                NotificationManager.IMPORTANCE_LOW
            )
            val mgr = getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(channel)
        }
    }
}
