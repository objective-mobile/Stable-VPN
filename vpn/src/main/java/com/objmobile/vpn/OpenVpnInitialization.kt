package com.objmobile.vpn

import android.content.Context
import com.objective.vpn.data.BaseVpnInitializeApp
import com.objmobile.vpn.domain.VpnInitialization

class OpenVpnInitialization(context: Context) :
    VpnInitialization by BaseVpnInitializeApp(context) {}