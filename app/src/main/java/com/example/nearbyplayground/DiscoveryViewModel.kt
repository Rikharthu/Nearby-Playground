package com.example.nearbyplayground

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.os.Build
import android.support.annotation.IntDef
import com.example.nearbyplayground.models.NearbyConnection
import com.example.nearbyplayground.models.NearbyEndpoint
import com.google.android.gms.nearby.Nearby
import timber.log.Timber

class DiscoveryViewModel(application: Application) : AndroidViewModel(application) {

    @IntDef(DISCOVERY_UI.toLong(), CONTROLLER_UI.toLong())
    annotation class NavigationState

    companion object {
        const val DISCOVERY_UI = 1
        const val CONTROLLER_UI = 2
    }

    private val discoverer: MyDiscoverer
    private val advertiser: MyAdvertiser
    val connection: LiveData<NearbyConnection>
    val discoveryState: LiveData<Boolean>
    val endpoints: LiveData<List<NearbyEndpoint>>
    val navigationState: MutableLiveData<Int>

    init {
        val client = Nearby.getConnectionsClient(application)
        discoverer = MyDiscoverer(client)
        advertiser = MyAdvertiser(client, Build.MODEL)

        connection = discoverer.connectionsLiveData
        discoveryState = discoverer.discoveryLiveData
        endpoints = discoverer.endpointsLiveData
        navigationState = MutableLiveData()
        navigationState.value = DISCOVERY_UI
    }

    fun startDiscovery() {
        discoverer.startDiscovery()
    }

    fun stopDiscovery() {
        discoverer.stopDiscovering()
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("Clearing ViewModel")
        discoverer.stopDiscovering()
        advertiser.stopAdvertising()
    }
}