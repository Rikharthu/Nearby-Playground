package com.example.nearbyplayground

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    companion object {
        const val USER_NICKNAME = "uber_user"
        const val SERVICE_ID = "com.example.nearbyplayground"
    }

    private val endpoints = mutableListOf<Pair<String, DiscoveredEndpointInfo>>()
    private lateinit var endpointsAdapter: ArrayAdapter<String>
    private lateinit var myDiscoverer: MyDiscoverer
    private lateinit var myAdvertiser: MyAdvertiser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val client = Nearby.getConnectionsClient(this)
        myDiscoverer = MyDiscoverer(client,
                {
                    if (it) {
                        // Started discovering
                        Timber.d("Started discovering")
                        discoverRadio.isChecked = true
                        endpointsAdapter.clear()
                    } else {
                        Timber.d("Stopped discovering")
                        discoverRadio.isChecked = false
                    }
                },
                {
                    Timber.d("Endpoints updated:")
                    val strBuilder = StringBuilder()
                    it.forEach {
                        strBuilder.append("\t${it.endpointName} with service id ${it.serviceId}")
                    }
                    endpointsAdapter.clear()
                    val namesArray = it.map { it.endpointName }
                    endpointsAdapter.addAll(namesArray)
                    Timber.d(strBuilder.toString())
                })
        myAdvertiser = MyAdvertiser(client, {
            if (it) {
                // Started discovering
                Timber.d("Started advertising")
                advertiseRadio.isChecked = true
            } else {
                Timber.d("Stopped advertising")
                advertiseRadio.isChecked = false
            }
        })

        advertiseBtn.setOnClickListener {
            myAdvertiser.startAdvertising()
        }
        stopAdvertisingBtn.setOnClickListener {
            myAdvertiser.stopAdvertising()
        }
        discoverBtn.setOnClickListener {
            myDiscoverer.startDiscovery()
        }
        stopDiscoveryBtn.setOnClickListener {
            myDiscoverer.stopDiscovering()
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
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val data = String(payload.asBytes()!!)
            Timber.d("Received \"$data\" from $endpointId")
        }

        override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {

        }
    }
}
