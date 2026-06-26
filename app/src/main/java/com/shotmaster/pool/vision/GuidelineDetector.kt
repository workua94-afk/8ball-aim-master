package com.shotmaster.pool.vision

import android.graphics.Rect
import android.util.Log
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class GuidelineDetector {
    data class GuidelineResult(val startPoint: Point, val endPoint: Point, val confidence: Float = 1f)

    private var lastGuideline: GuidelineResult? = null
    private var lastDetectFrame = 0

    fun detect(mat: Mat, tableBounds: Rect, frameIndex: Int = 0): GuidelineResult? {
        // Reuse cached result for up to 15 frames
        if (lastGuideline != null && (frameIndex - lastDetectFrame) in 1..15) {
            return lastGuideline
        }

        val crop = Mat()
        val blurred = Mat()
        val edges = Mat()
        val lines = Mat()

        try {
            // Crop to table bounds
            val roi = org.opencv.core.Rect(
                tableBounds.left.coerceAtLeast(0),
                tableBounds.top.coerceAtLeast(0),
                (tableBounds.right - tableBounds.left).coerceAtMost(mat.cols() - tableBounds.left),
                (tableBounds.bottom - tableBounds.top).coerceAtMost(mat.rows() - tableBounds.top)
            )
            mat.submat(roi).copyTo(crop)

            // Gaussian blur
            Imgproc.GaussianBlur(crop, blurred, org.opencv.core.Size(5.0, 5.0), 0.0)

            // Canny edge detection
            Imgproc.Canny(blurred, edges, 50.0, 150.0)

            // Probabilistic Hough Line Transform
            Imgproc.HoughLinesP(
                edges,
                lines,
                1.0,
                PI / 180.0,
                30,
                15.0,
                8.0
            )

            if (lines.rows() == 0) {
                return null
            }

            // Cluster lines by angle and position
            val clusteredLines = clusterLines(lines, crop.width(), crop.height())
            if (clusteredLines.isEmpty()) {
                return null
            }

            // Get dominant line (first cluster is largest)
            val (startPt, endPt) = clusteredLines[0]

            // Convert back to full frame coordinates
            val result = GuidelineResult(
                Point(startPt.x + tableBounds.left, startPt.y + tableBounds.top),
                Point(endPt.x + tableBounds.left, endPt.y + tableBounds.top),
                confidence = 0.8f
            )

            lastGuideline = result
            lastDetectFrame = frameIndex
            return result
        } catch (t: Throwable) {
            Log.e("GuidelineDetector", "detect error", t)
            return null
        } finally {
            crop.release()
            blurred.release()
            edges.release()
            lines.release()
        }
    }

    private fun clusterLines(
        linesMatrix: Mat,
        width: Int,
        height: Int
    ): List<Pair<Point, Point>> {
        val lines = mutableListOf<Pair<Point, Point>>()
        for (i in 0 until linesMatrix.rows()) {
            val data = FloatArray(4)
            linesMatrix.get(i, 0, data)
            val x1 = data[0].toDouble()
            val y1 = data[1].toDouble()
            val x2 = data[2].toDouble()
            val y2 = data[3].toDouble()
            lines.add(Pair(Point(x1, y1), Point(x2, y2)))
        }

        if (lines.isEmpty()) return emptyList()

        // Calculate angles for all lines
        val angles = lines.map { (p1, p2) ->
            atan2(p2.y - p1.y, p2.x - p1.x) * 180.0 / PI
        }

        // Group by similar angle (within ±5 degrees)
        val clusters = mutableMapOf<Int, MutableList<Int>>()
        for ((idx, angle) in angles.withIndex()) {
            var clusterKey: Int? = null
            for ((key, cluster) in clusters) {
                if (abs(angle - key) < 5.0) {
                    clusterKey = key
                    break
                }
            }
            if (clusterKey == null) {
                clusterKey = angle.toInt()
            }
            clusters.getOrPut(clusterKey) { mutableListOf() }.add(idx)
        }

        // Sort clusters by size (largest first)
        val sortedClusters = clusters.values.sortedByDescending { it.size }

        // Average lines in top cluster
        return if (sortedClusters.isNotEmpty()) {
            val topCluster = sortedClusters[0]
            val avgLine = averageLines(topCluster.map { lines[it] })
            listOf(avgLine)
        } else {
            lines.take(1)
        }
    }

    private fun averageLines(linesList: List<Pair<Point, Point>>): Pair<Point, Point> {
        var sumX1 = 0.0
        var sumY1 = 0.0
        var sumX2 = 0.0
        var sumY2 = 0.0
        for ((p1, p2) in linesList) {
            sumX1 += p1.x
            sumY1 += p1.y
            sumX2 += p2.x
            sumY2 += p2.y
        }
        val count = linesList.size
        return Pair(
            Point(sumX1 / count, sumY1 / count),
            Point(sumX2 / count, sumY2 / count)
        )
    }
}
