package com.objmobile.stablevpn

import android.app.Application
import com.objmobile.data.StableVpnInitialization

class StableVpnApplication : Application() {
    val vpnInitialization by lazy {
        StableVpnInitialization(this)
    }
}