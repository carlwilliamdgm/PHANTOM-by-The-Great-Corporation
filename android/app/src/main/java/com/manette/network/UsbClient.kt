package com.manette.network

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class UsbClient(
    private val port: Int
) : NetworkClient {
    
    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private val gson = Gson()
    private val clientId = "android_${System.currentTimeMillis()}"
    
    override suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Connect to localhost via ADB forwarding
            socket = Socket("127.0.0.1", port)
            outputStream = socket?.getOutputStream()
            inputStream = socket?.getInputStream()
            
            // Send connection message
            val connectMessage = mapOf(
                "type" to "connect",
                "client_id" to clientId
            )
            sendMessage(connectMessage)
            
            Log.d("UsbClient", "Connected via USB (ADB)")
            true
        } catch (e: Exception) {
            Log.e("UsbClient", "Connection failed: ${e.message}")
            Log.e("UsbClient", "Make sure ADB forwarding is set up: adb forward tcp:$port tcp:$port")
            false
        }
    }
    
    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            try {
                outputStream?.close()
                inputStream?.close()
                socket?.close()
                outputStream = null
                inputStream = null
                socket = null
                Log.d("UsbClient", "Disconnected")
            } catch (e: Exception) {
                Log.e("UsbClient", "Disconnect error: ${e.message}")
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
            Log.e("UsbClient", "Send input error: ${e.message}")
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
            Log.e("UsbClient", "Send heartbeat error: ${e.message}")
            false
        }
    }
    
    private fun sendMessage(message: Map<String, Any>) {
        val json = gson.toJson(message)
        outputStream?.write((json + "\n").toByteArray())
        outputStream?.flush()
    }
}
