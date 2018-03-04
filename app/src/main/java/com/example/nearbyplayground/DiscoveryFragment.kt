package com.example.nearbyplayground

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Switch
import android.widget.Toast
import com.example.nearbyplayground.models.NearbyConnection
import com.example.nearbyplayground.models.NearbyEndpoint
import kotlinx.android.synthetic.main.fragment_discovery.*
import timber.log.Timber


class DiscoveryFragment : Fragment() {

    private lateinit var discoverySwitch: Switch
    private var endpoints = listOf<NearbyEndpoint>()
    private lateinit var endpointsAdapter: ArrayAdapter<String>

    private lateinit var viewModel: DiscoveryViewModel
    private var connection: NearbyConnection? = null
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(activity).get(DiscoveryViewModel::class.java)

        viewModel.connection.observe(this, Observer<NearbyConnection> {
            connection = it

        })
        viewModel.endpoints.observe(this, Observer<List<NearbyEndpoint>> {
            onEndpointsUpdated(it)
        })
        viewModel.discoveryState.observe(this, Observer<Boolean> {
            Timber.d("Discovery state updated: $it")
            if (it != null) {
                Timber.d(if (it) "Discovering" else "Not discovering", Toast.LENGTH_SHORT)
                discoverySwitch.isChecked = it
                discoveryInfoContainer.visibility = if (it) View.VISIBLE else View.GONE
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private lateinit var menu: Menu


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        inflater.inflate(R.menu.discovery_screen_menu, menu)
        discoverySwitch = menu.findItem(R.id.menu_discovery_switch).actionView.findViewById(R.id.action_switch)
        discoverySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) onStartDiscovery()
            else onStopDiscovery()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Timber.d("options item selected ${item.title}")
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity.title = "Discovery"
        return inflater!!.inflate(R.layout.fragment_discovery, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        endpointsAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1)
        endpointsList.adapter = endpointsAdapter
        endpointsList.setOnItemClickListener { _, _, position, _ ->
            val endpoint = endpoints[position]
            onEndpointSelected(endpoint)
        }
    }

    private fun onEndpointSelected(endpoint: NearbyEndpoint) {
        Toast.makeText(context, "Selected $endpoint", Toast.LENGTH_SHORT).show()
//                discoverer.connectToEndpoint(endpoint.id)
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        init()
    }

    private fun init() {

    }

    private fun onEndpointsUpdated(endpoints: List<NearbyEndpoint>?) {
        if (endpoints != null && !endpoints.isEmpty()) {
            discoveryInfoContainer.visibility = View.GONE
            val stringBuilder = StringBuilder()
            stringBuilder.append("Endpoints updated:\n")
            for (endpoint in endpoints) {
                stringBuilder.append("\t${endpoint.info.endpointName} with id ${endpoint.id}\n")
            }
            Timber.d(stringBuilder.toString())

            this.endpoints = endpoints
            endpointsAdapter.clear()
            endpointsAdapter.addAll(endpoints.map { it.info.endpointName })
        } else {
            endpointsAdapter.clear()
        }
    }

    private fun onStopDiscovery() {
//        discoverer.stopDiscovering()
        viewModel.stopDiscovery()
    }

    private fun onStartDiscovery() {
//        discoverer.startDiscovery()
        viewModel.startDiscovery()
        endpointsAdapter.clear()
    }

    private fun onStartAdvertising() {
//        advertiser.startAdvertising()
    }

    private fun onStopAdvertising() {
//        advertiser.stopAdvertising()
    }

    companion object {

        fun newInstance(): DiscoveryFragment {
            val fragment = DiscoveryFragment()
            return fragment
        }
    }
}
