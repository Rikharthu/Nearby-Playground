package com.example.nearbyplayground.models

import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo

data class NearbyEndpoint(
        val id: String,
        val info: DiscoveredEndpointInfo
)