package com.manette.network

interface NetworkClient {
    suspend fun connect(): Boolean
    suspend fun disconnect()
    suspend fun sendInput(inputData: Map<String, Any>): Boolean
    suspend fun sendHeartbeat(): Boolean
}
