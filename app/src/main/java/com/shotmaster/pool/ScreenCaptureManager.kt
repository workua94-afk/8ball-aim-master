package com.shotmaster.pool

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import java.nio.ByteBuffer

class ScreenCaptureManager(
    private val mediaProjection: MediaProjection,
    private val screenWidth: Int,
    private val screenHeight: Int,
    private val onFrame: (Bitmap) -> Unit
) {
    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null
    private val handlerThread = HandlerThread("ScreenCapture").also { it.start() }
    private val handler = Handler(handlerThread.looper)
    @Volatile private var frameCounter = 0
    private val throttleEvery = 3  // process every 3rd frame (approx 15 FPS at 45 FPS source)

    fun start() {
        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2)
        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
            try {
                frameCounter++
                if (frameCounter % throttleEvery != 0) {
                    return@setOnImageAvailableListener
                }
                val bitmap = imageToBitmap(image)
                onFrame(bitmap)
            } catch (t: Throwable) {
                Log.e("ScreenCaptureManager", "frame error", t)
            } finally {
                image.close()
            }
        }, handler)

        virtualDisplay = mediaProjection.createVirtualDisplay(
            "ShotMasterCapture",
            screenWidth,
            screenHeight,
            (ResourcesGetter.densityDpi),
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            handler
        )
    }

    fun stop() {
        try {
            virtualDisplay?.release()
        } catch (t: Throwable) {}
        try {
            imageReader?.close()
        } catch (t: Throwable) {}
        try {
            mediaProjection.stop()
        } catch (_: Throwable) {}
        handlerThread.quitSafely()
    }

    private fun imageToBitmap(image: Image): Bitmap {
        val plane = image.planes[0]
        val buffer: ByteBuffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * screenWidth
        val bitmap = Bitmap.createBitmap(screenWidth + rowPadding / pixelStride, screenHeight, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        return Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight)
    }
}
