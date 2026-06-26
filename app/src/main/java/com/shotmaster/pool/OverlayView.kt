package com.shotmaster.pool

import android.content.Context
import android.graphics.*
import android.view.View
import androidx.core.graphics.withSave
import com.shotmaster.pool.vision.BallDetector
import com.shotmaster.pool.vision.GuidelineDetector
import com.shotmaster.pool.vision.GuidelinePainter
import com.shotmaster.pool.vision.ShotCalculator

class OverlayView(context: Context) : View(context) {

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = Color.GREEN
        strokeWidth = 4f
    }

    private var debugBitmap: Bitmap? = null
    private var tableBounds: Rect? = null
    private var guideline: GuidelineDetector.GuidelineResult? = null
    private var balls: BallDetector.BallDetectionResult? = null
    private var currentMode: GuidelinePainter.Mode? = null
    private var lastPainter: GuidelinePainter? = null
    private var lastCalculator: ShotCalculator? = null

    fun setDebugBitmap(b: Bitmap) {
        debugBitmap?.recycle()
        debugBitmap = Bitmap.createScaledBitmap(
            b,
            (width.coerceAtLeast(1) / 4).coerceAtLeast(1),
            (height.coerceAtLeast(1) / 4).coerceAtLeast(1),
            true
        )
        b.recycle()
    }

    fun updateWithDetection(
        table: Rect?,
        guideline: Any?,
        balls: Any?,
        mode: GuidelinePainter.Mode,
        painter: GuidelinePainter,
        calculator: ShotCalculator
    ) {
        tableBounds = table
        this.guideline = guideline as? GuidelineDetector.GuidelineResult
        this.balls = balls as? BallDetector.BallDetectionResult
        currentMode = mode
        lastPainter = painter
        lastCalculator = calculator
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.TRANSPARENT)

        // Draw debug bitmap in top-left
        debugBitmap?.let {
            canvas.withSave {
                val left = 20f
                val top = 20f
                val dst = RectF(left, top, left + it.width, top + it.height)
                canvas.drawBitmap(it, null, dst, null)
                paint.style = Paint.Style.STROKE
                paint.color = Color.WHITE
                paint.strokeWidth = 2f
                canvas.drawRect(dst, paint)
            }
        }

        // Draw guidelines if detection is successful
        val tb = tableBounds
        val gl = guideline
        val b = balls
        val painter = lastPainter
        val calc = lastCalculator
        val mode = currentMode

        if (tb != null && painter != null && calc != null && mode != null) {
            try {
                when (mode) {
                    GuidelinePainter.Mode.STANDARD -> {
                        if (gl != null) {
                            val extended = calc.extendGuideline(
                                ShotCalculator.Line(gl.startPoint, gl.endPoint),
                                tb
                            )
                            painter.drawExtendedGuideline(canvas, extended)
                        }
                    }
                    GuidelinePainter.Mode.BANK_SHOT -> {
                        if (gl != null) {
                            val extended = calc.extendGuideline(
                                ShotCalculator.Line(gl.startPoint, gl.endPoint),
                                tb
                            )
                            // Simple bank: reflect off right wall
                            val reflected = calc.computeBankShot(
                                extended.end,
                                org.opencv.core.Point(1.0, 0.0),
                                ShotCalculator.Wall.RIGHT,
                                tb
                            )
                            painter.drawBankShot(canvas, extended, reflected)
                        }
                    }
                    GuidelinePainter.Mode.THREE_LINES -> {
                        if (gl != null && b != null && b.cueBall != null && b.objectBalls.isNotEmpty()) {
                            val pocket = tb.let { r ->
                                org.opencv.core.Point(
                                    (r.right - 20).toDouble(),
                                    (r.bottom - 20).toDouble()
                                )
                            }
                            val result = calc.computeThreeLines(
                                b.cueBall!!,
                                b.objectBalls[0],
                                pocket,
                                tb
                            )
                            painter.drawThreeLines(canvas, result)
                        }
                    }
                    GuidelinePainter.Mode.SUPER_LINE -> {
                        if (gl != null) {
                            val extended = calc.extendGuideline(
                                ShotCalculator.Line(gl.startPoint, gl.endPoint),
                                tb
                            )
                            painter.drawSuperLine(canvas, extended)
                        }
                    }
                }
            } catch (t: Throwable) {
                android.util.Log.e("OverlayView", "draw error", t)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        debugBitmap?.recycle()
        debugBitmap = null
    }
}
