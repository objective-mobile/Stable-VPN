package com.objmobile.data

import android.content.Context
import android.net.VpnService
import com.objmobile.domain.StableVpnStatus
import com.objmobile.domain.VpnRepository
import com.objmobile.vpn.OpenVpnConnection
import com.objmobile.vpn.domain.VpnConnection
import com.objmobile.vpn.domain.VpnProfile
import com.objmobile.vpn.domain.VpnStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BaseVpnRepository(
    private val appName: String, private val vpnConnection: VpnConnection
) : VpnRepository {
    constructor(context: Context) : this(
        context.getString(R.string.app_name),
        OpenVpnConnection(context)
    )

    override fun connectProfile(country: String, opvConfig: String) {
        vpnConnection.connectVpn(
            VpnProfile(
                appName, country, opvConfig
            )
        )
    }

    override val stableVpnStatus: Flow<StableVpnStatus>
        get() = vpnConnection.vpnStatus().map {
            when (it) {
                is VpnStatus.Connected -> StableVpnStatus.Connected(it.country, it.ip)
                VpnStatus.Connecting -> StableVpnStatus.Connecting
                VpnStatus.Disconnected -> StableVpnStatus.Disconnected
                is VpnStatus.Error -> StableVpnStatus.Error(it.message)
                VpnStatus.Pause -> StableVpnStatus.Pause
            }
        }
}