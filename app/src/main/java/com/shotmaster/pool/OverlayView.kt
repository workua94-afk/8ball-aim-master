package com.shotmaster.pool

import android.content.Context
import android.graphics.*
import android.view.View
import androidx.core.graphics.withSave

class OverlayView(context: Context) : View(context) {

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = Color.GREEN
        strokeWidth = 4f
    }

    // Debug bitmap from ScreenCaptureManager (small preview). For production remove.
    private var debugBitmap: Bitmap? = null

    fun setDebugBitmap(b: Bitmap) {
        // Keep a scaled copy to avoid huge memory usage
        debugBitmap?.recycle()
        debugBitmap = Bitmap.createScaledBitmap(b, (width.coerceAtLeast(1)/4).coerceAtLeast(1), (height.coerceAtLeast(1)/4).coerceAtLeast(1), true)
        b.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.TRANSPARENT)

        // Debug: draw the small bitmap in top-left for visual confirmation
        debugBitmap?.let {
            canvas.withSave {
                val left = 20f
                val top = 20f
                val dst = RectF(left, top, left + it.width, top + it.height)
                canvas.drawBitmap(it, null, dst, null)
                // border
                paint.style = Paint.Style.STROKE
                paint.color = Color.WHITE
                paint.strokeWidth = 2f
                canvas.drawRect(dst, paint)
            }
        }

        // TODO: draw actual guideline lines computed by ShotCalculator
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        debugBitmap?.recycle()
        debugBitmap = null
    }
}
