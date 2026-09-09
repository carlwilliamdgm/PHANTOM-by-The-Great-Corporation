package com.manette.hid

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.manette.ManetteApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors

data class HidState(
    val enabled: Boolean = false,
    val connected: Boolean = false,
    val deviceName: String? = null
)

class BluetoothHidService : Service(), BluetoothProfile.ServiceListener {
    
    internal val _hidState = MutableStateFlow(HidState())
    val hidState: StateFlow<HidState> = _hidState.asStateFlow()
    
    var connectedHostDevice: BluetoothDevice? = null
        internal set
        
    private var bluetoothHidDevice: BluetoothHidDevice? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var callback: HidDeviceCallback? = null
    private val executor = Executors.newSingleThreadExecutor()
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "BluetoothHidService"
        
        var instance: BluetoothHidService? = null
            private set
        
        // Standard Gamepad HID Descriptor (8 bytes report)
        // 16 buttons (2 octets) + Stick Gauche X/Y (2 octets) + Stick Droit Z/Rz (2 octets) + Gâchettes Rx/Ry (2 octets)
        private val HID_DESCRIPTOR = byteArrayOf(
            0x05.toByte(), 0x01.toByte(),  // Usage Page (Generic Desktop)
            0x09.toByte(), 0x05.toByte(),  // Usage (Game Pad)
            0xA1.toByte(), 0x01.toByte(),  // Collection (Application)
            0x85.toByte(), 0x01.toByte(),  // Report ID (1)
            // 16 Boutons
            0x05.toByte(), 0x09.toByte(),  // Usage Page (Button)
            0x19.toByte(), 0x01.toByte(),  // Usage Minimum (Button 1)
            0x29.toByte(), 0x10.toByte(),  // Usage Maximum (Button 16)
            0x15.toByte(), 0x00.toByte(),  // Logical Minimum (0)
            0x25.toByte(), 0x01.toByte(),  // Logical Maximum (1)
            0x75.toByte(), 0x01.toByte(),  // Report Size (1 bit)
            0x95.toByte(), 0x10.toByte(),  // Report Count (16 bits = 2 bytes)
            0x81.toByte(), 0x02.toByte(),  // Input (Data, Var, Abs)
            // Left Stick: X, Y (-127..127)
            0x05.toByte(), 0x01.toByte(),  // Usage Page (Generic Desktop)
            0x09.toByte(), 0x30.toByte(),  // Usage (X)
            0x09.toByte(), 0x31.toByte(),  // Usage (Y)
            0x15.toByte(), 0x81.toByte(),  // Logical Minimum (-127)
            0x25.toByte(), 0x7F.toByte(),  // Logical Maximum (127)
            0x75.toByte(), 0x08.toByte(),  // Report Size (8 bits)
            0x95.toByte(), 0x02.toByte(),  // Report Count (2)
            0x81.toByte(), 0x02.toByte(),  // Input (Data, Var, Abs)
            // Right Stick: Z, Rz (-127..127)
            0x09.toByte(), 0x32.toByte(),  // Usage (Z)
            0x09.toByte(), 0x35.toByte(),  // Usage (Rz)
            0x15.toByte(), 0x81.toByte(),  // Logical Minimum (-127)
            0x25.toByte(), 0x7F.toByte(),  // Logical Maximum (127)
            0x75.toByte(), 0x08.toByte(),  // Report Size (8 bits)
            0x95.toByte(), 0x02.toByte(),  // Report Count (2)
            0x81.toByte(), 0x02.toByte(),  // Input (Data, Var, Abs)
            // Triggers: Rx, Ry (0..255)
            0x09.toByte(), 0x33.toByte(),  // Usage (Rx)
            0x09.toByte(), 0x34.toByte(),  // Usage (Ry)
            0x15.toByte(), 0x00.toByte(),  // Logical Minimum (0)
            0x25.toByte(), 0xFF.toByte(),  // Logical Maximum (255)
            0x75.toByte(), 0x08.toByte(),  // Report Size (8 bits)
            0x95.toByte(), 0x02.toByte(),  // Report Count (2)
            0x81.toByte(), 0x02.toByte(),  // Input (Data, Var, Abs)
            0xC0.toByte()  // End Collection
        )
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            callback = HidDeviceCallback(this)
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startHidDevice()
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): android.os.IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            stopHidDevice()
        }
        if (instance == this) {
            instance = null
        }
    }
    
    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && profile == BluetoothProfile.HID_DEVICE) {
            bluetoothHidDevice = proxy as BluetoothHidDevice
            registerHidApp()
        }
    }
    
    override fun onServiceDisconnected(profile: Int) {
        if (profile == BluetoothProfile.HID_DEVICE) {
            bluetoothHidDevice = null
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.P)
    private fun registerHidApp() {
        val hidDevice = bluetoothHidDevice ?: return
        
        val sdp = BluetoothHidDeviceAppSdpSettings(
            "Phantom",
            "Phantom Wireless Controller",
            "The Great Corporation",
            BluetoothHidDevice.SUBCLASS1_COMBO,
            HID_DESCRIPTOR
        )
        
        try {
            hidDevice.registerApp(sdp, null, null, executor, callback)
            Log.i(TAG, "Phantom Bluetooth HID registered successfully — provider: The Great Corporation")
        } catch (e: SecurityException) {
            Log.e(TAG, "Bluetooth permission denied: ${e.message}")
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ManetteApplication.NOTIFICATION_CHANNEL_ID,
                ManetteApplication.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, ManetteApplication.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Phantom Wireless Controller")
            .setContentText("The Great Corporation • Mode Plug & Play actif")
            .setSmallIcon(android.R.drawable.ic_input_add)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    fun startHidDevice(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            Log.e(TAG, "Bluetooth HID requires Android 9+")
            return false
        }
        
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not available")
            return false
        }
        
        try {
            val result = bluetoothAdapter?.getProfileProxy(
                this,
                this,
                BluetoothProfile.HID_DEVICE
            ) ?: false
            
            _hidState.value = HidState(enabled = true)
            Log.d(TAG, "HID device proxy requested: $result")
            return result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start HID device: ${e.message}")
            return false
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.P)
    fun stopHidDevice() {
        bluetoothHidDevice?.let {
            it.unregisterApp()
            bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, it)
        }
        bluetoothHidDevice = null
        _hidState.value = HidState()
        Log.d(TAG, "HID device stopped")
    }
    
    @RequiresApi(Build.VERSION_CODES.P)
    fun connectToDevice(device: BluetoothDevice): Boolean {
        val hidDevice = bluetoothHidDevice ?: return false
        
        try {
            val result = hidDevice.connect(device)
            if (result) {
                _hidState.value = _hidState.value.copy(
                    connected = true,
                    deviceName = device.name
                )
                Log.d(TAG, "Connected to ${device.name}")
            }
            return result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect: ${e.message}")
            return false
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.P)
    fun sendReport(device: BluetoothDevice, report: ByteArray): Boolean {
        val hidDevice = bluetoothHidDevice ?: return false
        
        try {
            val result = hidDevice.sendReport(device, 1, report)
            return result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send report: ${e.message}")
            return false
        }
    }

    fun sendInputReport(report: ByteArray): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        val device = connectedHostDevice ?: return false
        val hidDevice = bluetoothHidDevice ?: return false
        return try {
            hidDevice.sendReport(device, 1, report)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send HID report: ${e.message}")
            false
        }
    }
    
    fun getSetupInstructions(): String {
        return """
Bluetooth HID Plug & Play Mode Setup:

Requirements:
- Android 9.0 (Pie) or higher
- Bluetooth 4.0 or higher
- Host device with Bluetooth support

Setup Steps:
1. Enable Bluetooth on your Android device
2. Enable Bluetooth on your host device (PC, TV, etc.)
3. In the app, select "Plug & Play" mode
4. The app will advertise as a Bluetooth HID device
5. On your host device, scan for Bluetooth devices
6. Select "Phantom" from the list
7. Pair the devices (no PIN required for HID)
8. The host device will recognize it as a gamepad
        """.trimIndent()
    }
}

@RequiresApi(Build.VERSION_CODES.P)
class HidDeviceCallback(private val service: BluetoothHidService) : 
    BluetoothHidDevice.Callback() {
    
    override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
        android.util.Log.d("HidCallback", "App status changed: $registered")
        if (registered && pluggedDevice != null) {
            service.connectedHostDevice = pluggedDevice
            service._hidState.value = service._hidState.value.copy(
                connected = true,
                deviceName = pluggedDevice.name
            )
        }
    }
    
    override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
        android.util.Log.d("HidCallback", "Connection state changed: $state")
        when (state) {
            BluetoothProfile.STATE_CONNECTED -> {
                service.connectedHostDevice = device
                service._hidState.value = service._hidState.value.copy(
                    connected = true,
                    deviceName = device?.name
                )
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                service.connectedHostDevice = null
                service._hidState.value = service._hidState.value.copy(
                    connected = false,
                    deviceName = null
                )
            }
        }
    }
    
    override fun onGetReport(device: BluetoothDevice?, type: Byte, reportId: Byte, bufferSize: Int) {
        android.util.Log.d("HidCallback", "Get report requested")
    }
    
    override fun onSetReport(device: BluetoothDevice?, type: Byte, reportId: Byte, buffer: ByteArray?) {
        android.util.Log.d("HidCallback", "Set report received")
    }
    
    override fun onInterruptData(device: BluetoothDevice?, reportId: Byte, buffer: ByteArray?) {
        android.util.Log.d("HidCallback", "Interrupt data received")
    }
    
    override fun onVirtualCableUnplug(device: BluetoothDevice?) {
        android.util.Log.d("HidCallback", "Virtual unplug")
    }
}
