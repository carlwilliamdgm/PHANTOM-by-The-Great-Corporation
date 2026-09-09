package com.manette.network

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class WebSocketClient(
    private val serverIp: String,
    private val port: Int
) : NetworkClient {
    
    private var webSocketClient: WebSocketClient? = null
    private val gson = Gson()
    private val clientId = "android_${System.currentTimeMillis()}"
    private var connected = false
    
    override suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = URI("ws://$serverIp:$port")
            
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshake: ServerHandshake?) {
                    connected = true
                    Log.d("WebSocketClient", "Connected to server")
                    
                    // Send connection message
                    val connectMessage = mapOf(
                        "type" to "connect",
                        "client_id" to clientId
                    )
                    send(gson.toJson(connectMessage))
                }
                
                override fun onMessage(message: String?) {
                    Log.d("WebSocketClient", "Received: $message")
                    // Handle server messages (haptic feedback, etc.)
                }
                
                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    connected = false
                    Log.d("WebSocketClient", "Connection closed: $reason")
                }
                
                override fun onError(ex: Exception?) {
                    connected = false
                    Log.e("WebSocketClient", "Error: ${ex?.message}")
                }
            }
            
            webSocketClient?.connect()
            
            // Wait for connection
            var attempts = 0
            while (!connected && attempts < 10) {
                kotlinx.coroutines.delay(100)
                attempts++
            }
            
            connected
        } catch (e: Exception) {
            Log.e("WebSocketClient", "Connection failed: ${e.message}")
            false
        }
    }
    
    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            try {
                webSocketClient?.close()
                webSocketClient = null
                connected = false
                Log.d("WebSocketClient", "Disconnected")
            } catch (e: Exception) {
                Log.e("WebSocketClient", "Disconnect error: ${e.message}")
            }
        }
    }
    
    override suspend fun sendInput(inputData: Map<String, Any>): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!connected) return@withContext false
            
            val message = mapOf(
                "type" to "input",
                "client_id" to clientId,
                "data" to inputData
            )
            webSocketClient?.send(gson.toJson(message))
            true
        } catch (e: Exception) {
            Log.e("WebSocketClient", "Send input error: ${e.message}")
            false
        }
    }
    
    override suspend fun sendHeartbeat(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!connected) return@withContext false
            
            val message = mapOf(
                "type" to "heartbeat",
                "client_id" to clientId
            )
            webSocketClient?.send(gson.toJson(message))
            true
        } catch (e: Exception) {
            Log.e("WebSocketClient", "Send heartbeat error: ${e.message}")
            false
        }
    }
}
