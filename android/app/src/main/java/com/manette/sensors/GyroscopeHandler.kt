package com.manette.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GyroscopeData(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val timestamp: Long = 0L
)

class GyroscopeHandler(private val context: Context) : SensorEventListener {
    
    private val _gyroData = MutableStateFlow(GyroscopeData())
    val gyroData: StateFlow<GyroscopeData> = _gyroData.asStateFlow()
    
    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()
    
    private var sensorManager: SensorManager? = null
    private var gyroscopeSensor: Sensor? = null
    
    // Calibration offsets
    private var offsetX = 0f
    private var offsetY = 0f
    private var offsetZ = 0f
    
    // Sensitivity multiplier
    private var sensitivity = 1.0f
    
    // Smoothing factor (0-1, higher = more smoothing)
    private var smoothingFactor = 0.1f
    
    // Previous values for smoothing
    private var prevX = 0f
    private var prevY = 0f
    private var prevZ = 0f
    
    companion object {
        private const val TAG = "GyroscopeHandler"
    }
    
    fun initialize(): Boolean {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscopeSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        
        if (gyroscopeSensor == null) {
            Log.e(TAG, "Gyroscope sensor not available")
            return false
        }
        
        Log.d(TAG, "Gyroscope sensor initialized")
        return true
    }
    
    fun start() {
        if (gyroscopeSensor == null) {
            Log.e(TAG, "Cannot start: sensor not initialized")
            return
        }
        
        sensorManager?.registerListener(
            this,
            gyroscopeSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        
        _enabled.value = true
        Log.d(TAG, "Gyroscope started")
    }
    
    fun stop() {
        sensorManager?.unregisterListener(this)
        _enabled.value = false
        Log.d(TAG, "Gyroscope stopped")
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_GYROSCOPE) {
                // Apply calibration
                val calibratedX = it.values[0] - offsetX
                val calibratedY = it.values[1] - offsetY
                val calibratedZ = it.values[2] - offsetZ
                
                // Apply smoothing
                val smoothedX = prevX + smoothingFactor * (calibratedX - prevX)
                val smoothedY = prevY + smoothingFactor * (calibratedY - prevY)
                val smoothedZ = prevZ + smoothingFactor * (calibratedZ - prevZ)
                
                prevX = smoothedX
                prevY = smoothedY
                prevZ = smoothedZ
                
                // Apply sensitivity
                val finalX = smoothedX * sensitivity
                val finalY = smoothedY * sensitivity
                val finalZ = smoothedZ * sensitivity
                
                _gyroData.value = GyroscopeData(
                    x = finalX,
                    y = finalY,
                    z = finalZ,
                    timestamp = it.timestamp
                )
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Gyroscope accuracy changed: $accuracy")
    }
    
    fun calibrate() {
        // Reset offsets to current values (assuming device is stationary)
        offsetX = _gyroData.value.x
        offsetY = _gyroData.value.y
        offsetZ = _gyroData.value.z
        Log.d(TAG, "Gyroscope calibrated: offsets($offsetX, $offsetY, $offsetZ)")
    }
    
    fun resetCalibration() {
        offsetX = 0f
        offsetY = 0f
        offsetZ = 0f
        Log.d(TAG, "Gyroscope calibration reset")
    }
    
    fun setSensitivity(value: Float) {
        sensitivity = value.coerceIn(0.1f, 5.0f)
        Log.d(TAG, "Sensitivity set to $sensitivity")
    }
    
    fun setSmoothing(value: Float) {
        smoothingFactor = value.coerceIn(0.01f, 1.0f)
        Log.d(TAG, "Smoothing set to $smoothingFactor")
    }
    
    fun isAvailable(): Boolean {
        return gyroscopeSensor != null
    }
    
    fun getPitch(): Float {
        // Calculate pitch from gyro data (simplified)
        return _gyroData.value.x
    }
    
    fun getRoll(): Float {
        // Calculate roll from gyro data (simplified)
        return _gyroData.value.y
    }
    
    fun getYaw(): Float {
        // Calculate yaw from gyro data (simplified)
        return _gyroData.value.z
    }
}
