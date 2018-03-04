package com.example.nearbyplayground

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    companion object {
        /**
         * Request ACCESS_COARSE_LOCATION permission while trying to advertise
         */
        const val REQUEST_PERMISSION_LOCATION_ADVERTISE = 1
        /**
         * Request ACCESS_COARSE_LOCATION permission while trying to discover
         */
        const val REQUEST_PERMISSION_LOCATION_DISCCOVER = 2

        const val FRAGMENT_TAG_DISCOVERY = "discovery_fragment"
    }

    private lateinit var discoveryViewModel: DiscoveryViewModel

    private var currentFragment: Fragment? = null
    private var currentFragmentTag: String? = null

    private var discoveryFragment: DiscoveryFragment? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        discoveryViewModel = ViewModelProviders.of(this).get(DiscoveryViewModel::class.java)
        discoveryViewModel.navigationState.observe(this, Observer<Int> {
            if (it != null) {
                when (it) {
                    DiscoveryViewModel.CONTROLLER_UI -> {
                        Timber.d("Navigating to controller UI")

                    }
                    DiscoveryViewModel.DISCOVERY_UI -> {
                        Timber.d("Navigating to discovery UI")
                        showDiscoveryUI()
                    }
                }
            }
        })
    }

    private fun showDiscoveryUI() {
        if (discoveryFragment == null) {
            discoveryFragment = DiscoveryFragment.newInstance()
        }
        swapFragment(discoveryFragment!!, FRAGMENT_TAG_DISCOVERY)
    }

    private fun swapFragment(fragment: Fragment, tag: String) {
        if (currentFragment != fragment) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment, tag)
                    .commit()
            currentFragment = fragment
            currentFragmentTag = tag
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.discovery_screen_menu, menu)
//        return true
//    }

    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Timber.d("Received payload from $endpointId")
            val data = String(payload.asBytes()!!)
            Timber.d("Payload: $data")
        }

        override fun onPayloadTransferUpdate(endpointId: String, payloadUpdate: PayloadTransferUpdate) {

        }
    }

    override fun onStart() {
        super.onStart()
//        myDiscoverer.payloadListener = payloadCallback
//        myAdvertiser.payloadListener = payloadCallback
    }

    override fun onStop() {
        super.onStop()
//        myDiscoverer.payloadListener = null
//        myAdvertiser.payloadListener = null
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
        //myAdvertiser.startAdvertising()
    }

    private fun startDiscovering() {
        val hasLocationPermission = hasPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (!hasLocationPermission) {
            Timber.d("No Location permission")
            requestPermissions()
            return
        }

        Timber.d("Starting discovering")
        //myDiscoverer.startDiscovery()
    }

    private fun requestPermissions() {
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
    }

    private fun requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf(permission),
                requestCode)
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
