package com.manette.hid

import android.bluetooth.BluetoothDevice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HidInputReport(
    val buttons: Int = 0,  // 16 bits for buttons
    val xAxis: Byte = 0,   // -127 to 127
    val yAxis: Byte = 0,   // -127 to 127
    val zAxis: Byte = 0,   // 0 to 255 (left trigger)
    val rzAxis: Byte = 0   // 0 to 255 (right trigger)
)

class HidDeviceProfile {
    
    private val _inputReport = MutableStateFlow(HidInputReport())
    val inputReport: StateFlow<HidInputReport> = _inputReport.asStateFlow()
    
    companion object {
        private const val TAG = "HidDeviceProfile"
        
        // Button bit masks (Xbox 360 layout)
        const val BUTTON_A = 1 shl 0
        const val BUTTON_B = 1 shl 1
        const val BUTTON_X = 1 shl 2
        const val BUTTON_Y = 1 shl 3
        const val BUTTON_LB = 1 shl 4
        const val BUTTON_RB = 1 shl 5
        const val BUTTON_BACK = 1 shl 6
        const val BUTTON_START = 1 shl 7
        const val BUTTON_LS = 1 shl 8
        const val BUTTON_RS = 1 shl 9
        const val DPAD_UP = 1 shl 10
        const val DPAD_DOWN = 1 shl 11
        const val DPAD_LEFT = 1 shl 12
        const val DPAD_RIGHT = 1 shl 13
    }
    
    fun setButton(button: Int, pressed: Boolean) {
        val currentReport = _inputReport.value
        val newButtons = if (pressed) {
            currentReport.buttons or button
        } else {
            currentReport.buttons and button.inv()
        }
        _inputReport.value = currentReport.copy(buttons = newButtons)
    }
    
    fun setJoystick(axis: String, value: Float) {
        val currentReport = _inputReport.value
        val byteValue = (value * 127).toInt().toByte()
        
        _inputReport.value = when (axis) {
            "x" -> currentReport.copy(xAxis = byteValue)
            "y" -> currentReport.copy(yAxis = byteValue)
            else -> currentReport
        }
    }
    
    fun setTrigger(trigger: String, value: Float) {
        val currentReport = _inputReport.value
        val byteValue = (value * 255).toInt().toByte()
        
        _inputReport.value = when (trigger) {
            "left" -> currentReport.copy(zAxis = byteValue)
            "right" -> currentReport.copy(rzAxis = byteValue)
            else -> currentReport
        }
    }
    
    fun generateReport(): ByteArray {
        val report = _inputReport.value
        return byteArrayOf(
            // Buttons (2 bytes, 16 bits)
            (report.buttons and 0xFF).toByte(),
            ((report.buttons shr 8) and 0xFF).toByte(),
            // X Axis
            report.xAxis,
            // Y Axis
            report.yAxis,
            // Z Axis (Left Trigger)
            report.zAxis,
            // RZ Axis (Right Trigger)
            report.rzAxis
        )
    }
    
    fun resetReport() {
        _inputReport.value = HidInputReport()
    }
    
    fun isSupported(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P
    }
    
    fun getUnsupportedMessage(): String {
        return """
Bluetooth HID mode requires Android 9.0 (Pie) or higher.
Your device is running Android ${android.os.Build.VERSION.RELEASE}.

Some manufacturers may restrict HID profile access even on supported Android versions.
If you cannot use Plug & Play mode, please use "The Great" mode instead.
        """.trimIndent()
    }
}
