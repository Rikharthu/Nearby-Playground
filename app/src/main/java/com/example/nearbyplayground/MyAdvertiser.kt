package com.example.nearbyplayground

import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import timber.log.Timber

class MyAdvertiser(client: ConnectionsClient, val nickname: String = DEFAULT_NICKNAME)
    : NearbyConnectionManager(client) {

    companion object {
        private const val DEFAULT_NICKNAME = "nearby_playground_advertiser"
    }

    var isAdvertising: Boolean = false
        private set

    init {

    }

    fun startAdvertising() {
        if (isAdvertising) {
            Timber.d("Already advertising")
            return
        }

        isAdvertising = true
        client.startAdvertising(
                nickname,
                SERVICE_ID,
                lifecycleCallback,
                AdvertisingOptions(STRATEGY))
                .addOnSuccessListener {
                    Timber.d("Advertising started")
                    isAdvertising = true
                }.addOnFailureListener {
                    Timber.d(it, "Failed to start advertising")
                    isAdvertising = false
                }
    }

    fun stopAdvertising() {
        if (isAdvertising) {
            isAdvertising = false
            client.stopAdvertising()
        }
    }

    override fun onNearbyConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
        Timber.d("Initiating connection with $endpointId")
        client.acceptConnection(endpointId, internalPayloadListener)
    }

    override fun onNearbyConnected(endpointId: String, result: ConnectionResolution) {
        Timber.d("Connected to $endpointId? ${result.status.isSuccess}")
    }

    override fun onNearbyDisconnected(endpointId: String) {
        // Do nothing
    }

    override fun onNearbyConnectionError(endpointId: String, result: ConnectionResolution) {
        // Do nothing
    }

    override fun onNearbyConnectionRejected(endpointId: String) {
        // Do nothing
    }
}