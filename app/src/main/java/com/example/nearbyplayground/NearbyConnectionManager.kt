package com.example.nearbyplayground

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import timber.log.Timber


abstract class NearbyConnectionManager(
        protected val client: ConnectionsClient
) {

    protected val lifecycleCallback: ConnectionLifecycleCallback

    var payloadListener: PayloadCallback? = null
    protected val internalPayloadListener: PayloadCallback

    companion object {
        const val SERVICE_ID = "com.example.nearbyplayground"
        val STRATEGY = Strategy.P2P_STAR

        fun createNearbyConnectionsClient(context: Context): ConnectionsClient {
            return Nearby.getConnectionsClient(context.applicationContext)
        }
    }

    init {
        lifecycleCallback = object : ConnectionLifecycleCallback() {
            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                when (result.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_OK -> {
                        // We're connected! Can now start sending and receiving data
                        Timber.d("Connected to $endpointId")
                        onNearbyConnected(endpointId, result)
                    }
                    ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                        // The connection was rejected by one or both sides
                        Timber.d("Connection to $endpointId was rejected")
                        onNearbyConnectionRejected(endpointId)
                    }
                    ConnectionsStatusCodes.ERROR -> {
                        // The connection broke before it was able to be accepted
                        Timber.d("Error occurred while connection to $endpointId")
                        onNearbyConnectionError(endpointId, result)
                    }
                }
            }

            override fun onDisconnected(endpointId: String) {
                Timber.d("Nearby disconnected: $endpointId")
                onNearbyDisconnected(endpointId)
            }

            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                Timber.d("Connection initiated for endpointId $endpointId")
                onNearbyConnectionInitiated(endpointId, connectionInfo)
            }

        }

        internalPayloadListener = object : PayloadCallback() {
            override fun onPayloadReceived(endpointId: String, payload: Payload) {
                Timber.d("Payload received from $endpointId")
                payloadListener?.onPayloadReceived(endpointId, payload)
            }

            override fun onPayloadTransferUpdate(endpointId: String, payloadTransferUpdate: PayloadTransferUpdate?) {
                Timber.d("Payload transfer update from $endpointId")
                payloadListener?.onPayloadTransferUpdate(endpointId, payloadTransferUpdate)
            }

        }
    }

    /**
     * A callback indicating that Nearby connection has been established
     */
    protected abstract fun onNearbyConnected(endpointId: String, result: ConnectionResolution)

    /**
     * A callback indicating that Nearby connection has been lost
     */
    protected abstract fun onNearbyDisconnected(endpointId: String)

    /**
     * A callback indicating that there is an incoming request to establish a connection
     */
    protected abstract fun onNearbyConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo)

    /**
     * A callback indicating that error occured during Nearby connection
     */
    protected abstract fun onNearbyConnectionError(endpointId: String, result: ConnectionResolution)

    /**
     * A callback indicating that outgoing connection has been refused
     */
    protected abstract fun onNearbyConnectionRejected(endpointId: String)

    fun disconnectFromEndpoint(endpointId: String) {
        client.disconnectFromEndpoint(endpointId)
    }

    fun sendData(endpointId: String, payload: Payload) {
        client.sendPayload(endpointId, payload)
    }
}