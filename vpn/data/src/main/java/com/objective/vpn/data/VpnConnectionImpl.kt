package com.objective.vpn.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.objmobile.vpn.domain.VpnConnection
import com.objmobile.vpn.domain.VpnProfile
import com.objmobile.vpn.domain.VpnStatus
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.OpenVPNService.LocalBinder
import de.blinkt.openvpn.core.ProfileManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class VpnConnectionImpl(
    private val context: Context,
    private val vpnConnectionStorage: VpnConnectionStorage
) : VpnConnection {


    override fun connectVpn(vpnProfile: VpnProfile) {
        OpenVpnApi.startVpn(context, vpnProfile.ovpnConfig, vpnProfile.country, null, null)
    }

    override suspend fun disconnectVpn() {
        Log.d("VpnConnectionImpl", "disconnectVpn")
        val intent = Intent(context, OpenVPNService::class.java)
        intent.setAction(OpenVPNService.START_SERVICE)
        try {
            val mService: OpenVPNService = withTimeout(2000) {
                suspendCoroutine { continuation ->
                    context.bindService(intent, object : ServiceConnection {
                        override fun onServiceConnected(
                            className: ComponentName?,
                            service: IBinder?
                        ) {
                            val binder = service as LocalBinder
                            continuation.resume(binder.service)
                        }

                        override fun onServiceDisconnected(arg0: ComponentName?) {
                            continuation.resumeWithException(IllegalStateException("Empty service"))
                        }
                    }, Context.BIND_AUTO_CREATE)
                }
            }
            ProfileManager.setConntectedVpnProfileDisconnected(context)
            Log.d("VpnConnectionImpl", "disconnectVpn ${mService.time}")
            mService.management?.stopVPN(false)
        } catch (e: Exception) {
            Log.e("VpnConnectionImpl", "disconnectVpn ${e.message}")
        }
    }

    override fun vpnStatus(): Flow<VpnStatus> =
        vpnConnectionStorage.getVpnStatus().map { it.toVpnStatus() }
}