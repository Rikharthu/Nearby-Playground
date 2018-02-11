package com.example.nearbyplayground

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

abstract class MainActivity : AppCompatActivity() {

    companion object {
        const val USER_NICKNAME = "uber_user"
        const val SERVICE_ID = "com.example.nearbyplayground"
    }

    private val endpoints = mutableListOf<Pair<String, DiscoveredEndpointInfo>>()
    private lateinit var endpointsAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        advertiseBtn.setOnClickListener {
            endpoints.clear()
            startAdvertising()
        }
        discoverBtn.setOnClickListener {
            startDiscovering()
        }
        stopDiscoveryBtn.setOnClickListener {
            stopDiscovering()
        }
        stopAdvertisingBtn.setOnClickListener {
            stopAdvertising()
        }

        endpointsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        endpointsList.adapter = endpointsAdapter
        endpointsList.setOnItemClickListener { parent, view, position, id ->
            val endpoint = endpoints[position]
            onEndpointSelected(endpoint.first, endpoint.second)
        }
    }

    private fun onEndpointSelected(endpointId: String, endpointInfo: DiscoveredEndpointInfo) {
        Toast.makeText(this, endpointId, Toast.LENGTH_SHORT).show()
        // Start connection
        Nearby.getConnectionsClient(this)
                .requestConnection(
                        USER_NICKNAME,
                        endpointId,
                        connectionLifecycleCallback)
                .addOnSuccessListener {
                    // We successfully requested a connection. Now both sides
                    // must accept before the connection is established.
                }
                .addOnFailureListener {
                    Timber.e(it, "Nearby Connections failed to request the connection")
                }
    }

    private fun stopAdvertising() {
        Timber.d("Stopping advertising")
        Nearby.getConnectionsClient(this).stopAdvertising()
    }

    private fun stopDiscovering() {
        Timber.d("Stopping discovering")
        Nearby.getConnectionsClient(this).stopDiscovery()
    }

    private fun startDiscovering() {
        Timber.d("Starting discovering")
        Nearby.getConnectionsClient(this)
                .startDiscovery(
                        SERVICE_ID,
                        endpointDiscoveryCallback,
                        DiscoveryOptions(Strategy.P2P_STAR)
                )
                .addOnSuccessListener {
                    this.onSuccessDiscovering()
                }
                .addOnFailureListener {
                    Timber.e(it, "Unable to start discovering")
                }

    }

    private fun onSuccessDiscovering() {
        Timber.d("Discovery started")
    }

    private fun startAdvertising() {
        Timber.d("Starting advertising")
        Nearby.getConnectionsClient(this)
                .startAdvertising(
                        USER_NICKNAME,
                        SERVICE_ID,
                        this.connectionLifecycleCallback,
                        AdvertisingOptions(Strategy.P2P_STAR))
                .addOnSuccessListener {
                    this.onSuccessAdvertising()
                }
                .addOnFailureListener { Timber.e(it, "Unable to start advertising") }
    }

    private fun onSuccessAdvertising() {
        Timber.d("Started advertising")

    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val data = String(payload.asBytes()!!)
            Timber.d("Received \"$data\" from $endpointId")
        }

        override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {

        }

    }

    /**
     * Callback that will be invoked when discoverers request to connect to the advertiser
     */
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    // We're connected! Can now start sending and receiving data
                    Timber.d("Connected to $endpointId")
                    onConnected(endpointId)
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    // The connection was rejected by one or both sides
                    Timber.d("Connection to $endpointId was rejected")
                }
                ConnectionsStatusCodes.ERROR -> {
                    // The connection broke before it was able to be accepted
                    Timber.d("Error occurred while connection to $endpointId")
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            // We've been disconnected from this endpoint. No more data can be
            // sent or received.
            Timber.d("onDisconnected $endpointId")
        }

        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            // Connection is initiated. API is symmetric now
            Timber.d("Incoming connection from $endpointId with token ${connectionInfo.authenticationToken}")
            // Accept connection
            Nearby.getConnectionsClient(this@MainActivity).acceptConnection(endpointId, payloadCallback)
        }
    }

    private fun onConnected(endpointId: String) {
        val payload = Payload.fromBytes("Hello, $endpointId!".toByteArray())
        Nearby.getConnectionsClient(this).sendPayload(endpointId, payload)
    }


    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, discoveredEndpointInfo: DiscoveredEndpointInfo) {
            Timber.d("Endpoint $endpointId found!")
            endpoints.add(Pair(endpointId, discoveredEndpointInfo))
            endpointsAdapter.add(endpointId)
        }

        override fun onEndpointLost(endpointId: String) {
            // A previously discovered endpoint has gone away.
            Timber.d("Endpoint $endpointId lost!")
            val endpointToRemove = endpoints.single { it.first == endpointId }
            endpoints.remove(endpointToRemove)
            endpointsAdapter.remove(endpointId)
        }
    }
}
