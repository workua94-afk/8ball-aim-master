package com.shotmaster.pool.vision

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import org.opencv.core.Point

class GuidelinePainter {
    enum class Mode {
        STANDARD,
        BANK_SHOT,
        THREE_LINES,
        SUPER_LINE
    }

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    var mode: Mode = Mode.STANDARD
    var lineColor: Int = Color.parseColor("#00FF88") // Neon green
    var lineWidth: Float = 3f
    var opacity: Float = 1f

    fun drawExtendedGuideline(canvas: Canvas, line: ShotCalculator.Line) {
        paint.apply {
            color = lineColor
            strokeWidth = lineWidth
            alpha = (255 * opacity).toInt()
            pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)
        }
        canvas.drawLine(
            line.start.x.toFloat(),
            line.start.y.toFloat(),
            line.end.x.toFloat(),
            line.end.y.toFloat(),
            paint
        )
    }

    fun drawBankShot(canvas: Canvas, incomingLine: ShotCalculator.Line, reflectedLine: ShotCalculator.Line) {
        // Incoming line (orange)
        paint.apply {
            color = Color.parseColor("#FF6600")
            strokeWidth = lineWidth
            alpha = (255 * opacity).toInt()
            pathEffect = DashPathEffect(floatArrayOf(15f, 8f), 0f)
        }
        canvas.drawLine(
            incomingLine.start.x.toFloat(),
            incomingLine.start.y.toFloat(),
            incomingLine.end.x.toFloat(),
            incomingLine.end.y.toFloat(),
            paint
        )

        // Reflected line (cyan)
        paint.apply {
            color = Color.parseColor("#00FFFF")
            strokeWidth = lineWidth
            pathEffect = DashPathEffect(floatArrayOf(15f, 8f), 0f)
        }
        canvas.drawLine(
            reflectedLine.start.x.toFloat(),
            reflectedLine.start.y.toFloat(),
            reflectedLine.end.x.toFloat(),
            reflectedLine.end.y.toFloat(),
            paint
        )

        // Reflection point indicator
        paint.apply {
            pathEffect = null
            style = Paint.Style.FILL
            color = Color.parseColor("#FF6600")
        }
        canvas.drawCircle(
            incomingLine.end.x.toFloat(),
            incomingLine.end.y.toFloat(),
            6f,
            paint
        )
    }

    fun drawThreeLines(canvas: Canvas, result: ShotCalculator.ThreeLinesResult) {
        val shotPaint = Paint().apply {
            color = Color.WHITE
            strokeWidth = lineWidth + 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
            alpha = (255 * opacity).toInt()
        }
        val objectPaint = Paint().apply {
            color = Color.YELLOW
            strokeWidth = lineWidth
            style = Paint.Style.STROKE
            isAntiAlias = true
            alpha = (255 * opacity).toInt()
        }
        val deflectPaint = Paint().apply {
            color = Color.parseColor("#00FFFF")
            strokeWidth = lineWidth
            style = Paint.Style.STROKE
            isAntiAlias = true
            alpha = (255 * opacity).toInt()
            pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        }

        // Shot line (white)
        canvas.drawLine(
            result.shotLine.start.x.toFloat(),
            result.shotLine.start.y.toFloat(),
            result.shotLine.end.x.toFloat(),
            result.shotLine.end.y.toFloat(),
            shotPaint
        )

        // Object ball path (yellow)
        canvas.drawLine(
            result.objectBallLine.start.x.toFloat(),
            result.objectBallLine.start.y.toFloat(),
            result.objectBallLine.end.x.toFloat(),
            result.objectBallLine.end.y.toFloat(),
            objectPaint
        )

        // Cue ball deflection (cyan dashed)
        canvas.drawLine(
            result.cueBallDeflection.start.x.toFloat(),
            result.cueBallDeflection.start.y.toFloat(),
            result.cueBallDeflection.end.x.toFloat(),
            result.cueBallDeflection.end.y.toFloat(),
            deflectPaint
        )

        // Ghost ball circle at contact point
        val ghostPaint = Paint().apply {
            color = Color.WHITE
            alpha = (100 * opacity).toInt()
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawCircle(
            result.shotLine.end.x.toFloat(),
            result.shotLine.end.y.toFloat(),
            20f,
            ghostPaint
        )
    }

    fun drawSuperLine(canvas: Canvas, line: ShotCalculator.Line) {
        paint.apply {
            color = Color.parseColor("#FF00FF")
            strokeWidth = lineWidth + 2f
            alpha = (255 * opacity).toInt()
            pathEffect = null
        }
        canvas.drawLine(
            line.start.x.toFloat(),
            line.start.y.toFloat(),
            line.end.x.toFloat(),
            line.end.y.toFloat(),
            paint
        )
    }
}
