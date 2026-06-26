package com.shotmaster.pool

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private var mediaProjection: MediaProjection? = null

    private val mediaProjectionManager: MediaProjectionManager by lazy {
        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data ?: return@registerForActivityResult
            mediaProjection = mediaProjectionManager.getMediaProjection(result.resultCode, intent)
            // Start overlay service with the projection
            val svc = Intent(this, OverlayService::class.java).apply {
                putExtra(OverlayService.EXTRA_START_PROJECTION_INTENT, intent)
                putExtra(OverlayService.EXTRA_RESULT_CODE, result.resultCode)
            }
            ContextCompat.startForegroundService(this, svc)
            // Keep activity open so user can manage
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                HomeScreenCompose(
                    onGrantOverlay = { ensureOverlayPermission() },
                    onStartOverlay = {
                        if (checkOverlayPermission()) {
                            val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
                            screenCaptureLauncher.launch(captureIntent)
                        } else {
                            ensureOverlayPermission()
                        }
                    }
                )
            }
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else true
    }

    private fun ensureOverlayPermission() {
        if (!checkOverlayPermission()) {
            AlertDialog.Builder(this)
                .setTitle("Overlay Permission Required")
                .setMessage("Shot Master needs permission to draw over other apps to show aiming guides.")
                .setPositiveButton("Open Settings") { _, _ ->
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}

@Composable
fun HomeScreenCompose(
    onGrantOverlay: () -> Unit,
    onStartOverlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            "Shot Master",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00FF88)
        )

        Text(
            "8 Ball Pool Aiming Guide",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onGrantOverlay,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF88))
        ) {
            Text("Grant Overlay Permission", fontSize = 16.sp, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onStartOverlay,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0099FF))
        ) {
            Text("Start Overlay", fontSize = 16.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* TODO: Settings screen */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00FF88))
        ) {
            Text("Settings")
        }

        Button(
            onClick = { /* TODO: Paywall screen */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6600))
        ) {
            Text("Go Pro")
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            "Training Aid Only — Not for Competitive Play",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
