package com.example.nearbyplayground

import com.google.android.gms.nearby.connection.*
import timber.log.Timber

class MyDiscoverer(client: ConnectionsClient,
                   var discoveryListener: ((Boolean) -> Unit)? = null,
                   var endpointsListener: ((List<DiscoveredEndpointInfo>) -> Unit)? = null)
    : NearbyConnectionManager(client) {

    private val endpointsMap = mutableMapOf<String, DiscoveredEndpointInfo>()

    /**
     * Listener for discovery process events
     */

    init {

    }

    var isDiscovering: Boolean = false
        private set (value) {
            field = value
            discoveryListener?.invoke(field)
        }

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
                    isDiscovering = true
                }.addOnFailureListener {
            isDiscovering = false
        }
    }

    fun stopDiscovering() {
        if (isDiscovering) {
            isDiscovering = false
            client.stopDiscovery()
        }
    }

    fun connectToEndpoint(endpointId: String) {

    }

    private fun onNearbyEndpointFound(endpointId: String, endpointInfo: DiscoveredEndpointInfo) {
        endpointsMap[endpointId] = endpointInfo
        onEndpointsChanged()
    }

    private fun onNearbyEndpointLost(endpointId: String) {
        endpointsMap.remove(endpointId)
        onEndpointsChanged()
    }

    private fun onEndpointsChanged() {
        endpointsListener?.invoke(endpointsMap.values.toList())
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
}