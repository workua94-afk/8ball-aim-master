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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            // start overlay service with the projection
            val svc = Intent(this, OverlayService::class.java).apply {
                putExtra(OverlayService.EXTRA_START_PROJECTION_INTENT, intent)
                putExtra(OverlayService.EXTRA_RESULT_CODE, result.resultCode)
            }
            ContextCompat.startForegroundService(this, svc)
            finish() // optional: close activity to let user play
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var hasOverlay by remember { mutableStateOf(checkOverlayPermission()) }
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Button(onClick = { ensureOverlayPermission() }) {
                    Text(if (hasOverlay) "Overlay permission OK" else "Grant overlay permission")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = {
                    // request MediaProjection
                    val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
                    screenCaptureLauncher.launch(captureIntent)
                }) {
                    Text("Start Overlay (MediaProjection)")
                }
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
                .setTitle("Overlay Permission required")
                .setMessage("Shot Master needs permission to draw over other apps to show aiming guides.")
                .setPositiveButton("Open settings") { _, _ ->
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
