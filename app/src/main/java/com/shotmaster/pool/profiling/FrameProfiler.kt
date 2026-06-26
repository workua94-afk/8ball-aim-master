package com.shotmaster.pool.profiling

import android.os.SystemClock
import android.util.Log

class FrameProfiler {
    private val frameTimes = mutableListOf<Long>()
    private var lastReportTime = SystemClock.elapsedRealtimeNanos()

    fun recordFrameTime(timeMs: Long) {
        frameTimes.add(timeMs)
        if (frameTimes.size >= 30) {
            reportStats()
            frameTimes.clear()
        }
    }

    private fun reportStats() {
        if (frameTimes.isEmpty()) return
        val avg = frameTimes.average()
        val min = frameTimes.minOrNull() ?: 0
        val max = frameTimes.maxOrNull() ?: 0
        val p95 = frameTimes.sorted()[((frameTimes.size * 0.95).toInt()).coerceAtMost(frameTimes.size - 1)]

        Log.i(
            "FrameProfiler",
            "30 frames: avg=${String.format("%.1f", avg)}ms, min=$min, max=$max, p95=$p95"
        )
    }
}
