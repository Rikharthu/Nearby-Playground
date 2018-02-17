package com.example.nearbyplayground

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.nearbyplayground.models.NearbyConnection
import com.example.nearbyplayground.models.NearbyConnectionState
import com.example.nearbyplayground.models.NearbyEndpoint
import com.google.android.gms.nearby.Nearby
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    companion object {
        const val USER_NICKNAME = "uber_user"
        const val SERVICE_ID = "com.example.nearbyplayground"
    }

    private var endpoints = listOf<NearbyEndpoint>()
    private lateinit var endpointsAdapter: ArrayAdapter<String>
    private lateinit var myDiscoverer: MyDiscoverer
    private lateinit var myAdvertiser: MyAdvertiser

    private val connectionListener: (NearbyConnection?) -> Unit = {
        Timber.d("Received new connection: $it")
        if (it != null) {
            connection = it
            connection!!.stateListener = connectionStateListener
            connectionStatusTv.text = "Received connection to ${it?.endpointId}"
        } else {
            Timber.d("Connection destroyed")
        }
    }

    private val connectionStateListener: (NearbyConnectionState) -> Unit = {
        Timber.d("Connection state for ${connection?.endpointId} changed to $it")
        connectionStatusTv.text = "${connection!!.endpointId} : $it"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val client = Nearby.getConnectionsClient(this)
        myDiscoverer = MyDiscoverer(client)
        myDiscoverer.endpointsListener = {
            onEndpointsUpdated(it)
        }
        myDiscoverer.connectionListener = connectionListener

        myAdvertiser = MyAdvertiser(client, Build.MODEL)

        advertiseBtn.setOnClickListener {
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
        endpointsList.setOnItemClickListener { _, _, position, _ ->
            val endpoint = endpoints[position]
            onEndpointSelected(endpoint)
        }
    }

    private fun onEndpointsUpdated(endpoints: List<NearbyEndpoint>) {
        val stringBuilder = StringBuilder()
        stringBuilder.append("Endpoints updated:\n")
        for (endpoint in endpoints) {
            stringBuilder.append("\t${endpoint.info.endpointName} with id ${endpoint.id}\n")
        }
        Timber.d(stringBuilder.toString())

        this.endpoints = endpoints
        endpointsAdapter.clear()
        endpointsAdapter.addAll(endpoints.map { it.info.endpointName })
    }

    private var connection: NearbyConnection? = null

    private fun onEndpointSelected(endpoint: NearbyEndpoint) {
        Toast.makeText(this, endpoint.id, Toast.LENGTH_SHORT).show()
        // Start connection
        myDiscoverer.connectToEndpoint(endpoint.id)
    }

    private fun startAdvertising() {
        Timber.d("Starting advertising")
        myAdvertiser.startAdvertising()
    }

    private fun stopAdvertising() {
        Timber.d("Stopping advertising")
        myAdvertiser.stopAdvertising()
    }

    private fun startDiscovering() {
        Timber.d("Starting discovering")
        myDiscoverer.startDiscovery()
    }

    private fun stopDiscovering() {
        Timber.d("Stopping discovering")
        myDiscoverer.stopDiscovering()

    }
}
