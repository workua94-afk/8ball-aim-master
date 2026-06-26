package com.shotmaster.pool.vision

import android.graphics.Rect
import org.opencv.core.Point
import kotlin.math.*

class ShotCalculator {
    data class Line(val start: Point, val end: Point)
    data class ThreeLinesResult(
        val shotLine: Line,
        val objectBallLine: Line,
        val cueBallDeflection: Line
    )

    /**
     * Extend a guideline across the table, clipping to table bounds
     */
    fun extendGuideline(detectedLine: Line, tableBounds: Rect): Line {
        val dx = detectedLine.end.x - detectedLine.start.x
        val dy = detectedLine.end.y - detectedLine.start.y
        val len = sqrt(dx * dx + dy * dy)
        if (len < 0.1) return detectedLine

        // Scale factor to extend far beyond table
        val scale = max(tableBounds.width(), tableBounds.height()).toDouble() * 2.0
        val extendedEnd = Point(
            detectedLine.start.x + (dx / len) * scale,
            detectedLine.start.y + (dy / len) * scale
        )

        return clipLineToRect(detectedLine.start, extendedEnd, tableBounds)
    }

    /**
     * Compute a bank shot reflection off a table wall
     */
    fun computeBankShot(hitPoint: Point, incomingDirection: Point, wall: Wall, tableBounds: Rect): Line {
        val reflectedDir = when (wall) {
            Wall.TOP, Wall.BOTTOM -> Point(incomingDirection.x, -incomingDirection.y)
            Wall.LEFT, Wall.RIGHT -> Point(-incomingDirection.x, incomingDirection.y)
        }
        val len = sqrt(reflectedDir.x * reflectedDir.x + reflectedDir.y * reflectedDir.y)
        if (len < 0.1) return Line(hitPoint, hitPoint)

        val scale = max(tableBounds.width(), tableBounds.height()).toDouble()
        val reflectedEnd = Point(
            hitPoint.x + (reflectedDir.x / len) * scale,
            hitPoint.y + (reflectedDir.y / len) * scale
        )
        return clipLineToRect(hitPoint, reflectedEnd, tableBounds)
    }

    /**
     * Compute 3-lines guideline (shot line, object ball line, cue ball deflection)
     */
    fun computeThreeLines(
        cueBall: BallDetector.Circle,
        targetBall: BallDetector.Circle,
        pocket: Point,
        tableBounds: Rect
    ): ThreeLinesResult {
        // Ghost ball center (point on the line from cue to target, at contact distance)
        val dx = targetBall.center.x - cueBall.center.x
        val dy = targetBall.center.y - cueBall.center.y
        val dist = sqrt(dx * dx + dy * dy)
        if (dist < 0.1) {
            return ThreeLinesResult(
                Line(cueBall.center, cueBall.center),
                Line(targetBall.center, pocket),
                Line(cueBall.center, cueBall.center)
            )
        }

        // Ghost ball is 2*ballRadius away from target center
        val contactDist = 2.0 * targetBall.radius
        val ghostBallCenter = Point(
            targetBall.center.x - (dx / dist) * contactDist,
            targetBall.center.y - (dy / dist) * contactDist
        )

        // Shot line: cue ball → ghost ball
        val shotLine = clipLineToRect(cueBall.center, ghostBallCenter, tableBounds)

        // Object ball line: target ball → pocket
        val objectBallLine = clipLineToRect(targetBall.center, pocket, tableBounds)

        // Cue ball deflection: perpendicular to object ball line, from ghost ball
        val perpDx = -(pocket.y - targetBall.center.y)
        val perpDy = (pocket.x - targetBall.center.x)
        val perpLen = sqrt(perpDx * perpDx + perpDy * perpDy)
        if (perpLen < 0.1) {
            val deflectionEnd = Point(ghostBallCenter.x + 100, ghostBallCenter.y)
            return ThreeLinesResult(
                shotLine,
                objectBallLine,
                clipLineToRect(ghostBallCenter, deflectionEnd, tableBounds)
            )
        }

        val deflectionEnd = Point(
            ghostBallCenter.x + (perpDx / perpLen) * 200,
            ghostBallCenter.y + (perpDy / perpLen) * 200
        )
        val cueBallDeflection = clipLineToRect(ghostBallCenter, deflectionEnd, tableBounds)

        return ThreeLinesResult(shotLine, objectBallLine, cueBallDeflection)
    }

    /**
     * Clip a line to table bounds using parametric clipping
     */
    private fun clipLineToRect(p1: Point, p2: Point, rect: Rect): Line {
        var x1 = p1.x
        var y1 = p1.y
        var x2 = p2.x
        var y2 = p2.y

        val xMin = rect.left.toDouble()
        val xMax = rect.right.toDouble()
        val yMin = rect.top.toDouble()
        val yMax = rect.bottom.toDouble()

        val dx = x2 - x1
        val dy = y2 - y1

        if (abs(dx) < 0.1 && abs(dy) < 0.1) {
            return Line(p1, p2)
        }

        var t0 = 0.0
        var t1 = 1.0

        // Cohen-Sutherland line clipping
        fun clipAxis(p0: Double, p1: Double, axisMin: Double, axisMax: Double) {
            if (abs(p1 - p0) > 0.01) {
                val t = (axisMin - p0) / (p1 - p0)
                if (p1 > p0) {
                    if (t > t0) t0 = t
                    if (t < t1) t1 = t
                } else {
                    if (t < t1) t1 = t
                    if (t > t0) t0 = t
                }
            }
        }

        clipAxis(x1, x2, xMin, xMax)
        clipAxis(y1, y2, yMin, yMax)

        if (t0 <= t1) {
            return Line(
                Point(x1 + t0 * dx, y1 + t0 * dy),
                Point(x1 + t1 * dx, y1 + t1 * dy)
            )
        }
        return Line(p1, p2)
    }

    enum class Wall {
        TOP, BOTTOM, LEFT, RIGHT
    }
}
