package com.manette.network

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.UUID

class BluetoothClient(
    private val port: Int
) : NetworkClient {
    
    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private val gson = Gson()
    private val clientId = "android_${System.currentTimeMillis()}"
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    
    // Standard SPP UUID
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    
    override suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (bluetoothAdapter == null) {
                Log.e("BluetoothClient", "Bluetooth not supported")
                return@withContext false
            }
            
            // Note: In a real implementation, you would:
            // 1. Scan for devices
            // 2. Let user select device
            // 3. Connect to selected device
            // For now, this is a placeholder
            
            Log.d("BluetoothClient", "Bluetooth connection requires device selection")
            false
        } catch (e: Exception) {
            Log.e("BluetoothClient", "Connection failed: ${e.message}")
            false
        }
    }
    
    suspend fun connectToDevice(device: BluetoothDevice): Boolean = withContext(Dispatchers.IO) {
        try {
            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
            outputStream = socket?.outputStream
            
            // Send connection message
            val connectMessage = mapOf(
                "type" to "connect",
                "client_id" to clientId
            )
            sendMessage(connectMessage)
            
            Log.d("BluetoothClient", "Connected to ${device.name}")
            true
        } catch (e: Exception) {
            Log.e("BluetoothClient", "Connection failed: ${e.message}")
            false
        }
    }
    
    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            try {
                outputStream?.close()
                socket?.close()
                outputStream = null
                socket = null
                Log.d("BluetoothClient", "Disconnected")
            } catch (e: Exception) {
                Log.e("BluetoothClient", "Disconnect error: ${e.message}")
            }
        }
    }
    
    override suspend fun sendInput(inputData: Map<String, Any>): Boolean = withContext(Dispatchers.IO) {
        try {
            val message = mapOf(
                "type" to "input",
                "client_id" to clientId,
                "data" to inputData
            )
            sendMessage(message)
            true
        } catch (e: Exception) {
            Log.e("BluetoothClient", "Send input error: ${e.message}")
            false
        }
    }
    
    override suspend fun sendHeartbeat(): Boolean = withContext(Dispatchers.IO) {
        try {
            val message = mapOf(
                "type" to "heartbeat",
                "client_id" to clientId
            )
            sendMessage(message)
            true
        } catch (e: Exception) {
            Log.e("BluetoothClient", "Send heartbeat error: ${e.message}")
            false
        }
    }
    
    private fun sendMessage(message: Map<String, Any>) {
        val json = gson.toJson(message)
        outputStream?.write((json + "\n").toByteArray())
        outputStream?.flush()
    }
}
