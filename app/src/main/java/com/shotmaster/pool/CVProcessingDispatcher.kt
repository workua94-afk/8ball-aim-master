package com.shotmaster.pool

import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
kotlin.coroutines.asCoroutineDispatcher

class CVProcessingDispatcher {
    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "CVProcessor").apply { priority = Thread.MAX_PRIORITY - 1 }
    }
    private val dispatcher = executor.asCoroutineDispatcher()

    fun processAsync(
        bitmap: Bitmap,
        onResult: (CVResult) -> Unit,
        processor: (Bitmap) -> CVResult
    ) {
        CoroutineScope(dispatcher).launch {
            try {
                val result = processor(bitmap)
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(result)
                }
            } catch (t: Throwable) {
                Log.e("CVProcessing", "process error", t)
            }
        }
    }

    fun shutdown() {
        executor.shutdown()
    }
}

data class CVResult(
    val tableBounds: android.graphics.Rect?,
    val guideline: Any?,
    val balls: Any?,
    val processingTimeMs: Long
)
