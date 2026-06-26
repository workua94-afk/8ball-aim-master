package com.shotmaster.pool.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shotmaster.pool.di.PreferencesManager

@Composable
fun SettingsScreen(prefsManager: PreferencesManager) {
    val guidelineMode by prefsManager.guidelineMode.collectAsState(initial = "STANDARD")
    val lineColor by prefsManager.lineColor.collectAsState(initial = 0xFF00FF88.toInt())
    val lineWidth by prefsManager.lineWidth.collectAsState(initial = 3f)
    val overlayOpacity by prefsManager.overlayOpacity.collectAsState(initial = 1f)
    val frameRate by prefsManager.frameRate.collectAsState(initial = 15)
    val autoDetectSensitivity by prefsManager.autoDetectSensitivity.collectAsState(initial = 25f)
    val showGhostBall by prefsManager.showGhostBall.collectAsState(initial = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00FF88)
        )

        SettingSection("Guideline Mode") {
            val modes = listOf("STANDARD", "BANK_SHOT", "THREE_LINES", "SUPER_LINE")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                modes.forEach { mode ->
                    FilterChip(
                        selected = guidelineMode == mode,
                        onClick = { /* TODO: persist */ },
                        label = { Text(mode) }
                    )
                }
            }
        }

        SettingSection("Line Color") {
            val colors = listOf(
                0xFF00FF88 to "Green",
                0xFFFFFF00 to "Yellow",
                0xFFFFFFFF to "White",
                0xFF00FFFF to "Cyan",
                0xFFFF00FF to "Magenta",
                0xFFFF6600 to "Orange"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colors.forEach { (color, name) ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(color))
                    )
                }
            }
        }

        SettingSection("Line Thickness") {
            Slider(
                value = lineWidth,
                onValueChange = { /* TODO: persist */ },
                valueRange = 1f..6f,
                modifier = Modifier.fillMaxWidth(),
                steps = 4
            )
            Text("${lineWidth.toInt()} px", color = Color.White)
        }

        SettingSection("Frame Rate") {
            Slider(
                value = frameRate.toFloat(),
                onValueChange = { /* TODO: persist */ },
                valueRange = 10f..30f,
                modifier = Modifier.fillMaxWidth(),
                steps = 19
            )
            Text("$frameRate FPS", color = Color.White)
        }

        SettingSection("Auto-Detect Sensitivity") {
            Slider(
                value = autoDetectSensitivity,
                onValueChange = { /* TODO: persist */ },
                valueRange = 10f..50f,
                modifier = Modifier.fillMaxWidth(),
                steps = 39
            )
            Text("${autoDetectSensitivity.toInt()}", color = Color.White)
        }

        SettingSection("Overlay Opacity") {
            Slider(
                value = overlayOpacity,
                onValueChange = { /* TODO: persist */ },
                valueRange = 0.4f..1f,
                modifier = Modifier.fillMaxWidth(),
                steps = 5
            )
            Text("${(overlayOpacity * 100).toInt()}%", color = Color.White)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Show Ghost Ball", color = Color.White)
            Switch(
                checked = showGhostBall,
                onCheckedChange = { /* TODO: persist */ }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SettingSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF00FF88)
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}
