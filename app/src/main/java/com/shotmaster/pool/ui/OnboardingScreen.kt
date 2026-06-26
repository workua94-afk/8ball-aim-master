package com.shotmaster.pool.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(onContinue: () -> Unit) {
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
            "Welcome to Shot Master",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00FF88)
        )

        Text(
            "8 Ball Pool Aiming Guide",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Disclaimer
        Text(
            "Important Disclaimer",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF6600)
        )

        Text(
            "Shot Master is an independent training tool that uses on-device screen analysis to help players learn shot angles. It does not modify, inject, or interfere with any game's code or memory. It is intended for practice and educational use only. Using aiming assistance tools may violate the Terms of Service of certain competitive gaming platforms. Please review the Terms of Service of any game before use. The developer is not responsible for any account actions taken by game publishers.",
            fontSize = 12.sp,
            color = Color.White,
            modifier = Modifier.background(Color(0xFF1A2835)).padding(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy info
        Text(
            "Privacy & Data",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00FF88)
        )

        Text(
            "✓ No screenshots are saved or transmitted\n✓ All analysis happens on your device\n✓ No personal data is collected\n✓ Billing handled by Google Play",
            fontSize = 12.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF88))
        ) {
            Text("I Understand & Continue", fontSize = 16.sp, color = Color.Black)
        }

        Button(
            onClick = { /* Exit app */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00FF88))
        ) {
            Text("Cancel")
        }
    }
}
