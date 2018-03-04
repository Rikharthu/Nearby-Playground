package com.example.nearbyplayground

import android.app.Application
import android.content.Context
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    companion object {
        fun get(context: Context) = context.applicationContext as App
    }
}