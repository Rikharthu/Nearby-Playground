package com.example.nearbyplayground.models

import com.example.nearbyplayground.NearbyConnectionManager

class NearbyConnection(
        val endpointId: String,
        private val connectionManager: NearbyConnectionManager,
        stateListener: ((NearbyConnectionState) -> Unit)? = null
) {
    var stateListener: ((NearbyConnectionState) -> Unit)? = null
        set(value) {
            field = value
            // When new listener is set immediately notify of current state value
            field?.invoke(state)
        }

    var state: NearbyConnectionState = NearbyConnectionState.UNKNOWN
        set(value) {
            if (field != value) {
                field = value
                stateListener?.invoke(state)
            }
        }

    init {
        state = NearbyConnectionState.NOT_CONNECTED
        this.stateListener = stateListener
    }

    fun isConnected() = state == NearbyConnectionState.CONNECTED
}