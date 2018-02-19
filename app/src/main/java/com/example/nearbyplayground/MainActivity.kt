package com.example.nearbyplayground

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
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
        /**
         * Request ACCESS_COARSE_LOCATION permission while trying to advertise
         */
        const val REQUEST_PERMISSION_LOCATION_ADVERTISE = 1
        /**
         * Request ACCESS_COARSE_LOCATION permission while trying to discover
         */
        const val REQUEST_PERMISSION_LOCATION_DISCCOVER = 2
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
        stopAdvertisingBtn.setOnClickListener {
            stopAdvertising()
        }
        discoverBtn.setOnClickListener {
            startDiscovering()
        }
        stopDiscoveryBtn.setOnClickListener {
            stopDiscovering()
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
        val hasLocationPermission = hasPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (!hasLocationPermission) {
            Timber.d("No Location permission")
            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    REQUEST_PERMISSION_LOCATION_ADVERTISE)
            return
        }

        Timber.d("Starting advertising")
        myAdvertiser.startAdvertising()
    }

    private fun stopAdvertising() {
        Timber.d("Stopping advertising")
        myAdvertiser.stopAdvertising()
    }

    private fun startDiscovering() {
        val hasLocationPermission = hasPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (!hasLocationPermission) {
            Timber.d("No Location permission")
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                val builder = AlertDialog.Builder(this)
                builder.setPositiveButton("OK", { _, _ ->
                    requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION,
                            REQUEST_PERMISSION_LOCATION_DISCCOVER)
                })
            } else {
                requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION,
                        REQUEST_PERMISSION_LOCATION_DISCCOVER)
            }
            return
        }

        Timber.d("Starting discovering")
        myDiscoverer.startDiscovery()
    }

    private fun requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf(permission),
                requestCode)
    }

    private fun stopDiscovering() {
        Timber.d("Stopping discovering")
        myDiscoverer.stopDiscovering()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_LOCATION_ADVERTISE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startAdvertising()
                } else {
                    Toast.makeText(this, R.string.location_permission_failure, Toast.LENGTH_LONG).show()
                }
            }
            REQUEST_PERMISSION_LOCATION_DISCCOVER -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startDiscovering()
                } else {
                    Toast.makeText(this, R.string.location_permission_failure, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
