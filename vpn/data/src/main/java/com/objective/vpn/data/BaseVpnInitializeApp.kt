package com.objective.vpn.data

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.net.VpnService
import android.os.Build
import android.os.StrictMode
import com.objmobile.vpn.domain.VpnInitialization
import de.blinkt.openvpn.BuildConfig
import de.blinkt.openvpn.R
import de.blinkt.openvpn.api.AppRestrictions
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.PRNGFixes
import de.blinkt.openvpn.core.StatusListener
import de.blinkt.openvpn.core.VpnStatus

class BaseVpnInitializeApp(private val context: Context) : VpnInitialization {
    private var mStatus: StatusListener? = null
    override fun initializeApp() {
        PRNGFixes.apply()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannels()
        mStatus = StatusListener()
        mStatus?.init(context)

        if (BuildConfig.BUILD_TYPE == "debug") enableStrictModes()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AppRestrictions.getInstance(context).checkRestrictions(context)
        }
        OpenVPNService()
        VpnStatus.initLogCache(context.cacheDir)
    }

    private fun enableStrictModes() {
        val policy = StrictMode.VmPolicy.Builder().detectAll().penaltyLog().penaltyDeath().build()
        StrictMode.setVmPolicy(policy)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannels() {
        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager // Background message
        var name: CharSequence? = context.getString(R.string.channel_name_background)
        var mChannel = NotificationChannel(
            OpenVPNService.Companion.NOTIFICATION_CHANNEL_BG_ID,
            name,
            NotificationManager.IMPORTANCE_MIN
        )

        mChannel.setDescription(context.getString(R.string.channel_description_background))
        mChannel.enableLights(false)

        mChannel.setLightColor(Color.DKGRAY)
        mNotificationManager.createNotificationChannel(mChannel) // Connection status change messages
        name = context.getString(R.string.channel_name_status)
        mChannel = NotificationChannel(
            OpenVPNService.Companion.NOTIFICATION_CHANNEL_NEWSTATUS_ID,
            name,
            NotificationManager.IMPORTANCE_LOW
        )

        mChannel.setDescription(context.getString(R.string.channel_description_status))
        mChannel.enableLights(true)

        mChannel.setLightColor(Color.BLUE)
        mNotificationManager.createNotificationChannel(mChannel) // Urgent requests, e.g. two factor auth
        name = context.getString(R.string.channel_name_userreq)
        mChannel = NotificationChannel(
            OpenVPNService.Companion.NOTIFICATION_CHANNEL_USERREQ_ID,
            name,
            NotificationManager.IMPORTANCE_HIGH
        )
        mChannel.setDescription(context.getString(R.string.channel_description_userreq))
        mChannel.enableVibration(true)
        mChannel.setLightColor(Color.CYAN)
        mNotificationManager.createNotificationChannel(mChannel)
    }
}