package com.manette.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manette.ui.theme.GamepadAccent
import com.manette.ui.theme.GamepadBackground
import com.manette.ui.theme.GamepadPrimary

@Composable
fun ConfigScreen(
    onBack: () -> Unit,
    onStartGame: () -> Unit,
    onOpenLayoutEditor: () -> Unit
) {
    var serverIp by remember { mutableStateOf("192.168.1.100") }
    var serverPort by remember { mutableStateOf("8888") }
    var connectionType by remember { mutableStateOf("udp") }
    var sensitivity by remember { mutableFloatStateOf(1.0f) }
    var deadzone by remember { mutableFloatStateOf(0.1f) }
    var hapticEnabled by remember { mutableStateOf(true) }
    var gyroEnabled by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GamepadBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("← Back", color = GamepadPrimary)
                }
                Text(
                    text = "Configuration",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = GamepadPrimary
                )
                Spacer(modifier = Modifier.width(80.dp))
            }
            
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Connection Settings
                ConfigSection(title = "Connection Settings") {
                    OutlinedTextField(
                        value = serverIp,
                        onValueChange = { serverIp = it },
                        label = { Text("Server IP") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GamepadPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = serverPort,
                        onValueChange = { serverPort = it },
                        label = { Text("Server Port") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GamepadPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Connection Type", color = MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ConnectionTypeButton(
                            text = "UDP",
                            selected = connectionType == "udp",
                            onClick = { connectionType = "udp" }
                        )
                        ConnectionTypeButton(
                            text = "WebSocket",
                            selected = connectionType == "websocket",
                            onClick = { connectionType = "websocket" }
                        )
                        ConnectionTypeButton(
                            text = "Bluetooth",
                            selected = connectionType == "bluetooth",
                            onClick = { connectionType = "bluetooth" }
                        )
                        ConnectionTypeButton(
                            text = "USB",
                            selected = connectionType == "usb",
                            onClick = { connectionType = "usb" }
                        )
                    }
                }
                
                // Sensitivity Settings
                ConfigSection(title = "Sensitivity Settings") {
                    Text("Joystick Sensitivity: ${sensitivity}x", color = MaterialTheme.colorScheme.onSurface)
                    Slider(
                        value = sensitivity,
                        onValueChange = { sensitivity = it },
                        valueRange = 0.1f..3.0f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            activeTrackColor = GamepadPrimary
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Deadzone: ${(deadzone * 100).toInt()}%", color = MaterialTheme.colorScheme.onSurface)
                    Slider(
                        value = deadzone,
                        onValueChange = { deadzone = it },
                        valueRange = 0.0f..0.5f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            activeTrackColor = GamepadPrimary
                        )
                    )
                }
                
                // Feature Toggles
                ConfigSection(title = "Features") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Haptic Feedback", color = MaterialTheme.colorScheme.onSurface)
                        Switch(
                            checked = hapticEnabled,
                            onCheckedChange = { hapticEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GamepadPrimary,
                                checkedTrackColor = GamepadPrimary.copy(alpha = 0.5f)
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Gyroscope Controls", color = MaterialTheme.colorScheme.onSurface)
                        Switch(
                            checked = gyroEnabled,
                            onCheckedChange = { gyroEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GamepadPrimary,
                                checkedTrackColor = GamepadPrimary.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
                
                // Layout Editor Button
                Button(
                    onClick = onOpenLayoutEditor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GamepadAccent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Open Layout Editor", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            // Start Game Button
            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GamepadPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Start Game", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ConfigSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GamepadPrimary
            )
            content()
        }
    }
}

@Composable
fun RowScope.ConnectionTypeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) GamepadPrimary else MaterialTheme.colorScheme.surface,
            contentColor = if (selected) GamepadBackground else MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, fontSize = 12.sp)
    }
}
