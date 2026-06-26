package com.shotmaster.pool.di

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesManager(private val context: Context) {
    companion object {
        private val Context.dataStore by preferencesDataStore("shot_master_prefs")
        val GUIDELINE_MODE = stringPreferencesKey("guideline_mode")
        val LINE_COLOR = intPreferencesKey("line_color")
        val LINE_WIDTH = floatPreferencesKey("line_width")
        val OVERLAY_OPACITY = floatPreferencesKey("overlay_opacity")
        val FRAME_RATE = intPreferencesKey("frame_rate")
        val AUTO_DETECT_SENSITIVITY = floatPreferencesKey("auto_detect_sensitivity")
        val SHOW_GHOST_BALL = booleanPreferencesKey("show_ghost_ball")
        val IS_PRO = booleanPreferencesKey("is_pro")
        val BALL_RADIUS_MIN = intPreferencesKey("ball_radius_min")
        val BALL_RADIUS_MAX = intPreferencesKey("ball_radius_max")
    }

    val guidelineMode: Flow<String> = context.dataStore.data.map {
        it[GUIDELINE_MODE] ?: "STANDARD"
    }

    val lineColor: Flow<Int> = context.dataStore.data.map {
        it[LINE_COLOR] ?: 0xFF00FF88.toInt()
    }

    val lineWidth: Flow<Float> = context.dataStore.data.map {
        it[LINE_WIDTH] ?: 3f
    }

    val overlayOpacity: Flow<Float> = context.dataStore.data.map {
        it[OVERLAY_OPACITY] ?: 1f
    }

    val frameRate: Flow<Int> = context.dataStore.data.map {
        it[FRAME_RATE] ?: 15
    }

    val autoDetectSensitivity: Flow<Float> = context.dataStore.data.map {
        it[AUTO_DETECT_SENSITIVITY] ?: 25f
    }

    val showGhostBall: Flow<Boolean> = context.dataStore.data.map {
        it[SHOW_GHOST_BALL] ?: true
    }

    val isPro: Flow<Boolean> = context.dataStore.data.map {
        it[IS_PRO] ?: false
    }

    val ballRadiusMin: Flow<Int> = context.dataStore.data.map {
        it[BALL_RADIUS_MIN] ?: 12
    }

    val ballRadiusMax: Flow<Int> = context.dataStore.data.map {
        it[BALL_RADIUS_MAX] ?: 22
    }

    suspend fun setGuidelineMode(mode: String) {
        context.dataStore.updateData { it.toMutablePreferences().apply { set(GUIDELINE_MODE, mode) } }
    }

    suspend fun setLineColor(color: Int) {
        context.dataStore.updateData { it.toMutablePreferences().apply { set(LINE_COLOR, color) } }
    }

    suspend fun setLineWidth(width: Float) {
        context.dataStore.updateData { it.toMutablePreferences().apply { set(LINE_WIDTH, width) } }
    }

    suspend fun setOverlayOpacity(opacity: Float) {
        context.dataStore.updateData { it.toMutablePreferences().apply { set(OVERLAY_OPACITY, opacity) } }
    }

    suspend fun setFrameRate(rate: Int) {
        context.dataStore.updateData { it.toMutablePreferences().apply { set(FRAME_RATE, rate) } }
    }

    suspend fun setAutoDetectSensitivity(sensitivity: Float) {
        context.dataStore.updateData { it.toMutablePreferences().apply { set(AUTO_DETECT_SENSITIVITY, sensitivity) } }
    }

    suspend fun setShowGhostBall(show: Boolean) {
        context.dataStore.updateData { it.toMutablePreferences().apply { set(SHOW_GHOST_BALL, show) } }
    }

    suspend fun setIsPro(pro: Boolean) {
        context.dataStore.updateData { it.toMutablePreferences().apply { set(IS_PRO, pro) } }
    }

    suspend fun setBallRadiusRange(min: Int, max: Int) {
        context.dataStore.updateData {
            it.toMutablePreferences().apply {
                set(BALL_RADIUS_MIN, min)
                set(BALL_RADIUS_MAX, max)
            }
        }
    }
}
