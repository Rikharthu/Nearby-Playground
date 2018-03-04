package com.example.nearbyplayground

import android.arch.lifecycle.MutableLiveData
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

    // LiveData
    var discoveryLiveData = MutableLiveData<Boolean>()
    var endpointsLiveData = MutableLiveData<List<NearbyEndpoint>>()
    var connectionsLiveData = MutableLiveData<NearbyConnection>()

    init {

    }

    fun startDiscovery() {
        if (discoveryLiveData.value != null && discoveryLiveData.value!!) {
            Timber.d("Already discovering")
            return
        }
        Timber.d("Starting discovery")
        discoveryLiveData.value = true
        client.startDiscovery(
                SERVICE_ID,
                endpointDiscoveryCallback,
                DiscoveryOptions(Strategy.P2P_STAR))
                .addOnSuccessListener {
                    Timber.d("Discovery successfully started")
                    discoveryLiveData.value = true
                }.addOnFailureListener {
                    Timber.e(it, "Could not start discovery")
                    discoveryLiveData.value = false
                }
    }

    fun stopDiscovering() {
        if (discoveryLiveData.value != null && discoveryLiveData.value!!) {
            Timber.d("Stopping discovering")
            discoveryLiveData.value = false
            client.stopDiscovery()
            clearEndpoints()
        }
    }

    private fun clearEndpoints() {
        discoveredEndpoints.clear()
        onEndpointsChanged()
    }

    fun connectToEndpoint(endpointId: String) {
        if (connectionsLiveData.value != null) {
            Timber.d("Already connecting to ${connectionsLiveData.value!!.endpointId}")
            return
        }
        val endpoint = discoveredEndpoints.find { it.id == endpointId }
        if (endpoint == null) {
            Timber.d("Not a valid endpoint ID")
            return
        }

        Timber.d("Trying to connect to $endpointId")
        val connection = NearbyConnection(endpoint.id, this)
        connection.state = NearbyConnectionState.REQUESTING
        connectionsLiveData.value = connection

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
        val connection = connectionsLiveData.value
        if (connection != null) {
            connection.state = NearbyConnectionState.NOT_CONNECTED
            connectionsLiveData.value = null
        }
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
        endpointsLiveData.value = discoveredEndpoints
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
        Timber.d("Accepting connection to $endpointId")
        val connection = connectionsLiveData.value

        if (connection != null && connection.endpointId == endpointId) {
            connection.state = NearbyConnectionState.AUTHENTICATING
            // TODO add authentication
            acceptConnection()
        } else {
            // We didn't request this connection, reject
            rejectConnection(endpointId)
        }
    }

    private fun acceptConnection() {
        val connection = connectionsLiveData.value ?: return

        connection.state = NearbyConnectionState.AUTH_ACCEPTED
        acceptConnection(connection.endpointId)
                .addOnSuccessListener {
                    Timber.d("Accepted connection")
                }
                .addOnFailureListener {
                    // revert state
                    // TODO why?
                    connection.state = NearbyConnectionState.AUTHENTICATING
                }
    }

    private fun acceptConnection(endpointId: String) =
            client.acceptConnection(endpointId, internalPayloadListener)


    private fun rejectConnection(endpointId: String) {

    }

    override fun onNearbyConnected(endpointId: String, result: ConnectionResolution) {
        Timber.d("Connected to $endpointId? ${result.status.isSuccess}")
        val connection = connectionsLiveData.value
        if (connection != null && connection.endpointId == endpointId) {
            stopDiscovering()
            connection.state = NearbyConnectionState.CONNECTED
        } else {
            disconnectFromEndpoint(endpointId)
        }
    }

    override fun onNearbyDisconnected(endpointId: String) {
        Timber.d("Endpoint $endpointId disconnected")
        val connection = connectionsLiveData.value
        if (connection != null) {
            clearConnection()
        }
    }

    override fun onNearbyConnectionError(endpointId: String, result: ConnectionResolution) {
        Timber.e("onNearbyConnectionError")
    }

    override fun onNearbyConnectionRejected(endpointId: String) {
        Timber.d("Nearby connection rejected for endpoint $endpointId")
        val connection = connectionsLiveData.value
        if (connection != null) {
            clearConnection()
        }
    }
}