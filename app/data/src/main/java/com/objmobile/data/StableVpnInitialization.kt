package com.objmobile.data

import android.content.Context
import com.objmobile.vpn.OpenVpnInitialization
import com.objmobile.vpn.domain.VpnInitialization

class StableVpnInitialization(private val vpnInitialization: VpnInitialization) {
    constructor(context: Context) : this(OpenVpnInitialization(context))

    fun initialize() {
        vpnInitialization.initializeApp()
    }
}