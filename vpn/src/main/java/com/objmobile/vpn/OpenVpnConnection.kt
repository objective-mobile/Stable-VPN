package com.objmobile.vpn

import android.content.Context
import com.google.gson.Gson
import com.objective.vpn.data.VpnConnectionImpl
import com.objective.vpn.data.VpnConnectionStorage
import com.objmobile.vpn.domain.VpnConnection

class OpenVpnConnection(context: Context) : VpnConnection by VpnConnectionImpl(
    context, VpnConnectionStorage.newInstance(context)
)