package com.shotmaster.pool

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.shotmaster.pool.vision.GuidelineDetector
import com.shotmaster.pool.vision.TableDetector
import com.shotmaster.pool.vision.BallDetector
import com.shotmaster.pool.vision.ShotCalculator
import com.shotmaster.pool.vision.GuidelinePainter

class OverlayService : Service() {

    companion object {
        const val CHANNEL_ID = "shot_master_channel"
        const val NOTIFICATION_ID = 41234

        const val EXTRA_START_PROJECTION_INTENT = "extra_start_projection_intent"
        const val EXTRA_RESULT_CODE = "extra_result_code"

        const val ACTION_SET_MODE = "com.shotmaster.pool.ACTION_SET_MODE"
        const val EXTRA_MODE = "extra_mode"
    }

    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        fun getService(): OverlayService = this@OverlayService
    }

    private lateinit var windowManager: WindowManager
    private var overlayView: OverlayView? = null
    private var screenCaptureManager: ScreenCaptureManager? = null
    private var mediaProjection: MediaProjection? = null

    private val tableDetector = TableDetector()
    private val guidelineDetector = GuidelineDetector()
    private val ballDetector = BallDetector()
    private val shotCalculator = ShotCalculator()
    private val guidelinePainter = GuidelinePainter()
    private val cvDispatcher = CVProcessingDispatcher()

    private var currentMode = GuidelinePainter.Mode.STANDARD
    private var frameIndex = 0

    private val modeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_SET_MODE) {
                val modeStr = intent.getStringExtra(EXTRA_MODE) ?: "STANDARD"
                try {
                    currentMode = GuidelinePainter.Mode.valueOf(modeStr)
                    guidelinePainter.mode = currentMode
                    Log.i("OverlayService", "Mode changed to $currentMode")
                } catch (e: Exception) {
                    Log.e("OverlayService", "Invalid mode: $modeStr", e)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        guidelinePainter.mode = currentMode
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())

        // Register mode receiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(modeReceiver, IntentFilter(ACTION_SET_MODE), Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(modeReceiver, IntentFilter(ACTION_SET_MODE))
        }

        // Retrieve projection intent and start capture
        val projIntent = intent?.getParcelableExtra<Intent>(EXTRA_START_PROJECTION_INTENT)
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, -1) ?: -1
        if (projIntent != null && resultCode != -1) {
            val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = mpm.getMediaProjection(resultCode, projIntent)
            startOverlay()
        } else {
            Log.e("OverlayService", "Missing projection intent or result code")
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

        // Start capture at device resolution
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        mediaProjection?.let { mp ->
            screenCaptureManager = ScreenCaptureManager(
                mediaProjection = mp,
                screenWidth = width,
                screenHeight = height,
                onFrame = { bitmap ->
                    frameIndex++
                    cvDispatcher.processAsync(bitmap, { result ->
                        // Update OverlayView on main thread
                        overlayView?.updateWithDetection(
                            result.tableBounds,
                            result.guideline,
                            result.balls,
                            currentMode,
                            guidelinePainter,
                            shotCalculator
                        )
                        overlayView?.invalidate()
                    }, { bmp ->
                        processFrame(bmp, frameIndex)
                    })
                }
            )
            screenCaptureManager?.start()
            Log.i("OverlayService", "Screen capture started: ${width}x${height}")
        }
    }

    private fun processFrame(bitmap: android.graphics.Bitmap, index: Int): CVResult {
        val mat = org.opencv.core.Mat()
        try {
            org.opencv.android.Utils.bitmapToMat(bitmap, mat)
            val startTime = System.currentTimeMillis()

            // Detect table
            val tableBounds = tableDetector.detect(bitmap, index)
            if (tableBounds == null) {
                return CVResult(null, null, null, System.currentTimeMillis() - startTime)
            }

            // Detect guideline
            val guideline = guidelineDetector.detect(mat, tableBounds.rect, index)

            // Detect balls
            val balls = ballDetector.detect(mat, tableBounds.rect)

            val elapsed = System.currentTimeMillis() - startTime
            Log.d("CVPipeline", "Frame $index: ${elapsed}ms (table=${tableBounds != null}, guideline=${guideline != null}, balls=${balls.cueBall != null})")

            return CVResult(
                android.graphics.Rect(tableBounds.rect),
                guideline,
                balls,
                elapsed
            )
        } catch (t: Throwable) {
            Log.e("CVPipeline", "process frame error", t)
            return CVResult(null, null, null, 0)
        } finally {
            mat.release()
            bitmap.recycle()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(modeReceiver)
        } catch (e: Exception) {}
        screenCaptureManager?.stop()
        overlayView?.let { windowManager.removeView(it) }
        overlayView = null
        mediaProjection?.stop()
        cvDispatcher.shutdown()
        super.onDestroy()
    }

    private fun buildNotification() =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Shot Master Active")
            .setContentText("Overlay running — tap to manage")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
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

data class CVResult(
    val tableBounds: android.graphics.Rect?,
    val guideline: Any?,
    val balls: Any?,
    val processingTimeMs: Long
)
