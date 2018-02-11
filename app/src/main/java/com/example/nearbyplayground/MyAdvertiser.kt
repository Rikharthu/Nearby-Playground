package com.example.nearbyplayground

import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionsClient
import timber.log.Timber

class MyAdvertiser(client: ConnectionsClient) : NearbyConnectionManager(client) {

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
                "my_advertiser",
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


}