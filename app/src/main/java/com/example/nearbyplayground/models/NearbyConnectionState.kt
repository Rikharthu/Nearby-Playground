package com.example.nearbyplayground.models

enum class NearbyConnectionState {
    UNKNOWN,
    NOT_CONNECTED,
    REQUESTING,
    AUTHENTICATING,
    AUTH_ACCEPTED,
    AUTH_REJECTED,
    CONNECTED
}