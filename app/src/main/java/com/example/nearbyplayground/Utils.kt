package com.example.nearbyplayground

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

fun hasPermissions(context: Context, permission: String) = ContextCompat.checkSelfPermission(context,
        permission) == PackageManager.PERMISSION_GRANTED