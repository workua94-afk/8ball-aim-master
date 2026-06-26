package com.shotmaster.pool.vision

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class TableDetector {
    data class TableBounds(val rect: Rect, val pocketLocations: List<Point>)

    // Cache for last detection
    private var lastBounds: TableBounds? = null
    private var lastDetectFrame = 0

    // Call this with a Bitmap frame and the frame index to allow caching
    fun detect(bitmap: Bitmap, frameIndex: Int = 0): TableBounds? {
        // Reuse cached bounds for up to 30 frames
        if (lastBounds != null && (frameIndex - lastDetectFrame) in 1..30) {
            return lastBounds
        }

        val mat = Mat()
        val hsv = Mat()
        val mask = Mat()
        try {
            Utils.bitmapToMat(bitmap, mat)
            // Convert to HSV
            Imgproc.cvtColor(mat, hsv, Imgproc.COLOR_RGB2HSV)

            // HSV range for green felt (H:35-85, S:50-255, V:50-200)
            val lower = Scalar(35.0, 50.0, 50.0)
            val upper = Scalar(85.0, 255.0, 200.0)
            Core.inRange(hsv, lower, upper, mask)

            // Morphology to close gaps
            val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(15.0, 15.0))
            Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_CLOSE, kernel)

            // Find contours
            val contours = ArrayList<MatOfPoint>()
            val hierarchy = Mat()
            Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

            var maxArea = 0.0
            var maxRect: Rect? = null
            val frameArea = (mat.rows() * mat.cols()).toDouble()
            for (c in contours) {
                val area = Imgproc.contourArea(c)
                if (area > maxArea) {
                    val br = Imgproc.boundingRect(c)
                    maxArea = area
                    maxRect = br
                }
                c.release()
            }
            hierarchy.release()

            if (maxRect != null && maxArea > frameArea * 0.30) {
                // Convert OpenCV Rect to Android Rect
                val androidRect = Rect(maxRect.x, maxRect.y, maxRect.x + maxRect.width, maxRect.y + maxRect.height)

                // Compute pocket locations (6 standard pockets)
                val margin = (15).coerceAtLeast((maxRect.width * 0.02).toInt())
                val pockets = listOf(
                    Point((androidRect.left + margin).toDouble(), (androidRect.top + margin).toDouble()),
                    Point(((androidRect.left + androidRect.right) / 2.0), (androidRect.top + margin).toDouble()),
                    Point((androidRect.right - margin).toDouble(), (androidRect.top + margin).toDouble()),
                    Point((androidRect.left + margin).toDouble(), (androidRect.bottom - margin).toDouble()),
                    Point(((androidRect.left + androidRect.right) / 2.0), (androidRect.bottom - margin).toDouble()),
                    Point((androidRect.right - margin).toDouble(), (androidRect.bottom - margin).toDouble())
                )

                val bounds = TableBounds(androidRect, pockets)
                lastBounds = bounds
                lastDetectFrame = frameIndex
                return bounds
            }

            return null
        } finally {
            mat.release()
            hsv.release()
            mask.release()
        }
    }
}
