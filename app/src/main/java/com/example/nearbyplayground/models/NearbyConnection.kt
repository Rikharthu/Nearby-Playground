package com.example.nearbyplayground.models

import android.arch.lifecycle.MutableLiveData
import com.example.nearbyplayground.NearbyConnectionManager

class NearbyConnection(
        val endpointId: String,
        private val connectionManager: NearbyConnectionManager
) {

    val stateLiveData = MutableLiveData<NearbyConnectionState>()
    var state: NearbyConnectionState = NearbyConnectionState.UNKNOWN
        set(value) {
            if (field != value) {
                field = value
                stateLiveData.value = field
            }
        }

    init {
        state = NearbyConnectionState.NOT_CONNECTED
    }

    fun isConnected() = state == NearbyConnectionState.CONNECTED
}