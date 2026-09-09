package com.manette.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manette.ui.theme.GamepadSurface

@Composable
fun StatusIndicator(
    connected: Boolean,
    latency: Int,
    connectionType: String,
    battery: Int
) {
    Row(
        modifier = Modifier
            .background(GamepadSurface, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Connection status
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    if (connected) Color.Green else Color.Red,
                    CircleShape
                )
        )
        
        // Latency
        if (connected) {
            Text(
                text = "${latency}ms",
                fontSize = 12.sp,
                color = Color.White
            )
        }
        
        // Connection type
        Text(
            text = connectionType.uppercase(),
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // Battery
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BatteryFull,
                contentDescription = "Battery",
                tint = if (battery > 20) Color.Green else Color.Red,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "$battery%",
                fontSize = 10.sp,
                color = Color.White
            )
        }
    }
}
