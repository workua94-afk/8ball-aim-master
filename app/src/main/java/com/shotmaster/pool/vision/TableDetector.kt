package com.shotmaster.pool.vision

import android.graphics.Rect
import org.opencv.core.Point

class TableDetector {
    data class TableBounds(val rect: Rect, val pocketLocations: List<Point>)

    fun detect(frameAnyFormat: Any): TableBounds? {
        val screenRect = Rect(0, 0, 1080, 1920)
        val pockets = listOf(
            Point(screenRect.left + 20.0, screenRect.top + 20.0),
            Point(screenRect.exactCenterX().toDouble(), screenRect.top + 20.0),
            Point(screenRect.right - 20.0, screenRect.top + 20.0),
            Point(screenRect.left + 20.0, screenRect.bottom - 20.0),
            Point(screenRect.exactCenterX().toDouble(), screenRect.bottom - 20.0),
            Point(screenRect.right - 20.0, screenRect.bottom - 20.0)
        )
        return TableBounds(screenRect, pockets)
    }
}
