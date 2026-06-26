package com.shotmaster.pool.calibration

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.shotmaster.pool.vision.BallDetector
import com.shotmaster.pool.vision.TableDetector
import org.opencv.android.Utils
import org.opencv.core.Mat

class CalibrationRoutine {
    data class CalibrationResult(
        val ballRadiusMin: Int,
        val ballRadiusMax: Int,
        val success: Boolean
    )

    fun runCalibration(
        calibrationBitmap: Bitmap,
        tableDetector: TableDetector,
        ballDetector: BallDetector
    ): CalibrationResult {
        return try {
            val mat = Mat()
            Utils.bitmapToMat(calibrationBitmap, mat)
            try {
                // Detect table
                val tableBounds = tableDetector.detect(calibrationBitmap, 0)
                if (tableBounds == null) {
                    Log.w("Calibration", "Could not detect table")
                    return CalibrationResult(12, 22, false)
                }

                // Detect balls
                val ballResult = ballDetector.detect(mat, tableBounds.rect)
                if (ballResult.cueBall == null && ballResult.objectBalls.isEmpty()) {
                    Log.w("Calibration", "No balls detected")
                    return CalibrationResult(12, 22, false)
                }

                // Calculate average ball radius
                val allBalls = mutableListOf<BallDetector.Circle>()
                ballResult.cueBall?.let { allBalls.add(it) }
                allBalls.addAll(ballResult.objectBalls)

                if (allBalls.isEmpty()) {
                    return CalibrationResult(12, 22, false)
                }

                val avgRadius = allBalls.map { it.radius }.average().toInt()
                val minRadius = (avgRadius * 0.8).toInt().coerceAtLeast(8)
                val maxRadius = (avgRadius * 1.2).toInt().coerceAtMost(40)

                Log.i(
                    "Calibration",
                    "Detected ${allBalls.size} balls with avg radius $avgRadius -> range [$minRadius, $maxRadius]"
                )

                return CalibrationResult(minRadius, maxRadius, true)
            } finally {
                mat.release()
            }
        } catch (t: Throwable) {
            Log.e("Calibration", "Calibration failed", t)
            CalibrationResult(12, 22, false)
        }
    }
}
