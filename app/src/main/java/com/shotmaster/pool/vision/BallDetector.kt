package com.shotmaster.pool.vision

import android.graphics.Rect
import android.util.Log
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class BallDetector {
    data class Circle(val center: Point, val radius: Float)
    data class BallDetectionResult(val cueBall: Circle?, val objectBalls: List<Circle>)

    // Tunable parameters (adjust per device resolution)
    private var ballMinRadius = 12
    private var ballMaxRadius = 22

    fun setRadiusRange(min: Int, max: Int) {
        ballMinRadius = min
        ballMaxRadius = max
    }

    fun detect(mat: Mat, tableBounds: Rect): BallDetectionResult {
        val gray = Mat()
        val blurred = Mat()
        val circles = Mat()

        try {
            // Crop to table bounds
            val roi = org.opencv.core.Rect(
                tableBounds.left.coerceAtLeast(0),
                tableBounds.top.coerceAtLeast(0),
                (tableBounds.right - tableBounds.left).coerceAtMost(mat.cols() - tableBounds.left),
                (tableBounds.bottom - tableBounds.top).coerceAtMost(mat.rows() - tableBounds.top)
            )
            val cropMat = mat.submat(roi)

            // Convert to grayscale
            Imgproc.cvtColor(cropMat, gray, Imgproc.COLOR_RGB2GRAY)

            // Gaussian blur
            Imgproc.GaussianBlur(gray, blurred, org.opencv.core.Size(5.0, 5.0), 0.0)

            // Hough Circle Detection
            Imgproc.HoughCircles(
                blurred,
                circles,
                Imgproc.HOUGH_GRADIENT,
                1.5,  // dp
                30.0, // minDist
                100.0, // param1 (Canny high threshold)
                25.0, // param2 (accumulator threshold)
                ballMinRadius, // minRadius
                ballMaxRadius  // maxRadius
            )

            var cueBall: Circle? = null
            val objectBalls = mutableListOf<Circle>()

            if (circles.cols() > 0) {
                for (i in 0 until circles.cols()) {
                    val vCircle = circles.get(0, i)
                    val x = vCircle[0].toFloat() + tableBounds.left
                    val y = vCircle[1].toFloat() + tableBounds.top
                    val radius = vCircle[2].toFloat()

                    // Sample color at center
                    val isWhite = isWhiteBall(mat, x.toInt(), y.toInt())

                    if (isWhite) {
                        if (cueBall == null || (x - tableBounds.exactCenterX()).toInt().let { abs(it) } <
                            (cueBall.center.x - tableBounds.exactCenterX()).toInt().let { abs(it) }) {
                            // Keep cue ball (usually closer to center or leftmost)
                            cueBall = Circle(Point(x.toDouble(), y.toDouble()), radius)
                        }
                    } else {
                        objectBalls.add(Circle(Point(x.toDouble(), y.toDouble()), radius))
                    }
                }
            }

            return BallDetectionResult(cueBall, objectBalls)
        } catch (t: Throwable) {
            Log.e("BallDetector", "detect error", t)
            return BallDetectionResult(null, emptyList())
        } finally {
            gray.release()
            blurred.release()
            circles.release()
        }
    }

    private fun isWhiteBall(mat: Mat, x: Int, y: Int): Boolean {
        // Sample a few pixels around the center
        val xClamped = x.coerceIn(1, mat.cols() - 2)
        val yClamped = y.coerceIn(1, mat.rows() - 2)
        val samples = listOf(
            Pair(xClamped, yClamped),
            Pair(xClamped - 2, yClamped),
            Pair(xClamped + 2, yClamped),
            Pair(xClamped, yClamped - 2),
            Pair(xClamped, yClamped + 2)
        )

        var whiteCount = 0
        for ((sx, sy) in samples) {
            val pixel = mat.get(sy, sx)
            if (pixel.size >= 3) {
                val r = pixel[0].toInt()
                val g = pixel[1].toInt()
                val b = pixel[2].toInt()
                // White if R,G,B all > 200
                if (r > 200 && g > 200 && b > 200) {
                    whiteCount++
                }
            }
        }
        return whiteCount >= 3
    }
}
