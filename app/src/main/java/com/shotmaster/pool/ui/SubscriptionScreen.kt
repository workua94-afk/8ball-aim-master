package com.shotmaster.pool.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shotmaster.pool.billing.BillingManager

@Composable
fun SubscriptionScreen(billingManager: BillingManager, onDismiss: () -> Unit) {
    var selectedSku by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Go Pro",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00FF88)
        )

        Text(
            "Unlock premium features",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Feature list
        listOf(
            "✓ Unlimited Bank Shot mode",
            "✓ 3-Lines Guideline",
            "✓ Super Line mode",
            "✓ Custom line colors",
            "✓ Ad-free experience"
        ).forEach { feature ->
            Text(
                feature,
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Subscription options
        listOf(
            Triple(BillingManager.SKU_WEEKLY, "$2.99/week", "Cancel anytime"),
            Triple(BillingManager.SKU_MONTHLY, "$6.99/month", "Best value"),
            Triple(BillingManager.SKU_YEARLY, "$39.99/year", "Save 40%")
        ).forEach { (sku, price, subtitle) ->
            Button(
                onClick = { selectedSku = sku },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedSku == sku) Color(0xFF00FF88) else Color(0xFF333333)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(price, fontWeight = FontWeight.Bold)
                    Text(subtitle, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00FF88))
        ) {
            Text("Maybe Later")
        }

        Text(
            "Subscriptions auto-renew. You can cancel anytime in Settings.",
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
