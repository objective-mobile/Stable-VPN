package com.objmobile.stablevpn

import android.app.Application
import com.google.firebase.FirebaseApp
import com.objmobile.data.StableVpnInitialization

class StableVpnApplication : Application() {
    val vpnInitialization by lazy {
        StableVpnInitialization(this)
    }
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}