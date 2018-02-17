package com.example.nearbyplayground

import com.example.nearbyplayground.models.NearbyConnection
import com.example.nearbyplayground.models.NearbyConnectionState
import com.example.nearbyplayground.models.NearbyEndpoint
import com.google.android.gms.nearby.connection.*
import timber.log.Timber

class MyDiscoverer(client: ConnectionsClient, val nickname: String = DEFAULT_NICKNAME)
    : NearbyConnectionManager(client) {

    companion object {
        private const val DEFAULT_NICKNAME = "nearby_playground_discoverer"
    }

    private val discoveredEndpoints = mutableListOf<NearbyEndpoint>()

    /**
     * Listener for discovery process events
     */
    var discoveryListener: ((Boolean) -> Unit)? = null
    var endpointsListener: ((List<NearbyEndpoint>) -> Unit)? = null
    var connectionListener: ((NearbyConnection?) -> Unit)? = null

    init {

    }

    var isDiscovering: Boolean = false
        private set

    fun startDiscovery() {
        if (isDiscovering) {
            Timber.d("Already discovering")
            return
        }

        isDiscovering = true
        client.startDiscovery(
                SERVICE_ID,
                endpointDiscoveryCallback,
                DiscoveryOptions(Strategy.P2P_STAR))
                .addOnSuccessListener {
                    Timber.d("Discovery successfully started")
                    isDiscovering = true
                }.addOnFailureListener {
                    Timber.e(it, "Could not start discovery")
                    isDiscovering = false
                }
    }

    fun stopDiscovering() {
        if (isDiscovering) {
            isDiscovering = false
            client.stopDiscovery()
        }
    }

    private var connection: NearbyConnection? = null

    fun connectToEndpoint(endpointId: String) {
        // Refactor to use listeners instead of returning object
        if (connection != null) {
            Timber.d("Already connection to ${connection!!.endpointId}")
            return
        }

        Timber.d("Trying to connect to $endpointId")
        connection = NearbyConnection(endpointId, this)
        connection!!.state = NearbyConnectionState.REQUESTING
        connectionListener?.invoke(connection!!)

        client.requestConnection(
                nickname,
                endpointId,
                lifecycleCallback)
                .addOnSuccessListener {
                    Timber.d("Successfully requested connection")
                }
                .addOnFailureListener {
                    // TODO clear connection
                    Timber.e(it, "Could not request connection")
                    clearConnection()
                }
    }

    private fun clearConnection() {
        // Notify listener and clear reference
        connection?.state = NearbyConnectionState.NOT_CONNECTED
        connectionListener?.invoke(null)
        connection = null
    }

    private fun onNearbyEndpointFound(endpointId: String, endpointInfo: DiscoveredEndpointInfo) {
        discoveredEndpoints.add(NearbyEndpoint(endpointId, endpointInfo))
        onEndpointsChanged()
    }

    private fun onNearbyEndpointLost(endpointId: String) {
        discoveredEndpoints.removeAll { it.id == endpointId }
        onEndpointsChanged()
    }

    private fun onEndpointsChanged() {
        endpointsListener?.invoke(discoveredEndpoints)
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, p1: DiscoveredEndpointInfo) {
            Timber.d("Endpoint $endpointId found!")
            onNearbyEndpointFound(endpointId, p1)
        }

        override fun onEndpointLost(endpointId: String) {
            Timber.d("Endpoint $endpointId lost!")
            onNearbyEndpointLost(endpointId)
        }

    }

    override fun onNearbyConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
        super.onNearbyConnectionInitiated(endpointId, connectionInfo)
        Timber.d("Accepting connection to $endpointId")
        connection!!.state = NearbyConnectionState.AUTHENTICATING

        // TODO add authentication
        connection!!.state = NearbyConnectionState.AUTH_ACCEPTED

        client.acceptConnection(endpointId, internalPayloadListener)
                .addOnSuccessListener { Timber.d("Accepted connection") }
                .addOnFailureListener {
                    Timber.e(it, "Could not accept connection")
                    connection!!.state = NearbyConnectionState.AUTHENTICATING
                }
    }

    override fun onNearbyConnected(endpointId: String, result: ConnectionResolution) {
        Timber.d("Connected to $endpointId? ${result.status.isSuccess}")
        if (connection != null) {
            stopDiscovering()
            connection!!.state = NearbyConnectionState.CONNECTED
        } else {

        }
    }

    override fun onNearbyDisconnected(endpointId: String) {
        if (connection != null && connection!!.endpointId == endpointId) {
            clearConnection()
            startDiscovery() // TODO add listener for discovery state?
        }
    }

    override fun onNearbyConnectionError(endpointId: String, result: ConnectionResolution) {
        Timber.e("onNearbyConnectionError")
    }

    override fun onNearbyConnectionRejected(endpointId: String) {
        Timber.d("Nearby connection rejected for endpoint $endpointId")
        if (connection != null && connection!!.endpointId == endpointId) {
            clearConnection()
        }
    }
}