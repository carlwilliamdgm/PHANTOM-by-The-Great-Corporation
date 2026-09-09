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
import kotlin.math.sqrt

data class AccelerometerData(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val timestamp: Long = 0L
)

class AccelerometerHandler(private val context: Context) : SensorEventListener {
    
    private val _accelData = MutableStateFlow(AccelerometerData())
    val accelData: StateFlow<AccelerometerData> = _accelData.asStateFlow()
    
    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()
    
    private var sensorManager: SensorManager? = null
    private var accelerometerSensor: Sensor? = null
    
    // Calibration offsets
    private var offsetX = 0f
    private var offsetY = 0f
    private var offsetZ = 0f
    
    // Sensitivity multiplier
    private var sensitivity = 1.0f
    
    // Smoothing factor (0-1, higher = more smoothing)
    private var smoothingFactor = 0.2f
    
    // Previous values for smoothing
    private var prevX = 0f
    private var prevY = 0f
    private var prevZ = 0f
    
    // Gravity compensation
    private var gravityX = 0f
    private var gravityY = 0f
    private var gravityZ = 0f
    
    companion object {
        private const val TAG = "AccelerometerHandler"
        private const val ALPHA = 0.8f // Gravity filter coefficient
    }
    
    fun initialize(): Boolean {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        if (accelerometerSensor == null) {
            Log.e(TAG, "Accelerometer sensor not available")
            return false
        }
        
        Log.d(TAG, "Accelerometer sensor initialized")
        return true
    }
    
    fun start() {
        if (accelerometerSensor == null) {
            Log.e(TAG, "Cannot start: sensor not initialized")
            return
        }
        
        sensorManager?.registerListener(
            this,
            accelerometerSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        
        _enabled.value = true
        Log.d(TAG, "Accelerometer started")
    }
    
    fun stop() {
        sensorManager?.unregisterListener(this)
        _enabled.value = false
        Log.d(TAG, "Accelerometer stopped")
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                // Apply gravity filter (high-pass filter to remove gravity)
                gravityX = ALPHA * gravityX + (1 - ALPHA) * it.values[0]
                gravityY = ALPHA * gravityY + (1 - ALPHA) * it.values[1]
                gravityZ = ALPHA * gravityZ + (1 - ALPHA) * it.values[2]
                
                val linearX = it.values[0] - gravityX
                val linearY = it.values[1] - gravityY
                val linearZ = it.values[2] - gravityZ
                
                // Apply calibration
                val calibratedX = linearX - offsetX
                val calibratedY = linearY - offsetY
                val calibratedZ = linearZ - offsetZ
                
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
                
                _accelData.value = AccelerometerData(
                    x = finalX,
                    y = finalY,
                    z = finalZ,
                    timestamp = it.timestamp
                )
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Accelerometer accuracy changed: $accuracy")
    }
    
    fun calibrate() {
        // Calibrate to current orientation (assuming device is flat)
        offsetX = _accelData.value.x
        offsetY = _accelData.value.y
        offsetZ = _accelData.value.z - 9.81f // Account for gravity
        Log.d(TAG, "Accelerometer calibrated: offsets($offsetX, $offsetY, $offsetZ)")
    }
    
    fun resetCalibration() {
        offsetX = 0f
        offsetY = 0f
        offsetZ = 0f
        gravityX = 0f
        gravityY = 0f
        gravityZ = 0f
        Log.d(TAG, "Accelerometer calibration reset")
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
        return accelerometerSensor != null
    }
    
    fun getMagnitude(): Float {
        val data = _accelData.value
        return sqrt(data.x * data.x + data.y * data.y + data.z * data.z)
    }
    
    fun getTiltX(): Float {
        // Calculate tilt around X axis (pitch)
        val data = _accelData.value
        return Math.toDegrees(kotlin.math.atan2(data.y, data.z).toDouble()).toFloat()
    }
    
    fun getTiltY(): Float {
        // Calculate tilt around Y axis (roll)
        val data = _accelData.value
        return Math.toDegrees(kotlin.math.atan2(-data.x, sqrt(data.y * data.y + data.z * data.z)).toDouble()).toFloat()
    }
    
    fun isShaking(threshold: Float = 2.0f): Boolean {
        return getMagnitude() > threshold
    }
}
