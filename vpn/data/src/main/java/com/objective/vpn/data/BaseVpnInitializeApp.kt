package com.objective.vpn.data

import android.annotation.TargetApi
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.OnThreadViolationListener
import android.os.StrictMode.OnVmViolationListener
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.os.strictmode.Violation
import androidx.annotation.RequiresApi
import com.objmobile.vpn.domain.VpnInitialization
import de.blinkt.openvpn.BuildConfig
import de.blinkt.openvpn.R
import de.blinkt.openvpn.api.AppRestrictions
import de.blinkt.openvpn.core.LocaleHelper
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.Preferences
import de.blinkt.openvpn.core.StatusListener
import de.blinkt.openvpn.core.VpnStatus
import java.util.concurrent.Executors

class BaseVpnInitializeApp(private val context: Context) : VpnInitialization {
    private var mStatus: StatusListener? = null

    override fun initializeApp() {
        if (BuildConfig.BUILD_TYPE == "debug") enableStrictModes()

        if ("robolectric" == Build.FINGERPRINT) return

        LocaleHelper.setDesiredLocale(context)
        LocaleHelper.updateResources(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannels()
        mStatus = StatusListener()
        mStatus?.init(context.getApplicationContext())

        createFirstLaunchSetting()

        AppRestrictions.getInstance(context).checkRestrictions(context)
    }

    private fun createFirstLaunchSetting() {
        val prefs = Preferences.getDefaultSharedPreferences(context)
        val firstStart = prefs.getLong("firstStart", 0)
        if (firstStart == 0L) {
            val pedit = prefs.edit()
            pedit.putLong("firstStart", System.currentTimeMillis())
            pedit.apply()
        }
    }

    private fun enableStrictModes() {
        val tpbuilder = ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()


        val vpbuilder = VmPolicy.Builder()
            .detectAll()
            .penaltyLog()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            tpbuilder.penaltyListener(
                Executors.newSingleThreadExecutor(),
                OnThreadViolationListener { v: Violation? ->
                    this.logViolation(
                        v!!
                    )
                })
            vpbuilder.penaltyListener(
                Executors.newSingleThreadExecutor(),
                OnVmViolationListener { v: Violation? ->
                    this.logViolation(
                        v!!
                    )
                })
        }

        //tpbuilder.penaltyDeath();
        //vpbuilder.penaltyDeath();
        val policy = vpbuilder.build()
        StrictMode.setVmPolicy(policy)
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    fun logViolation(v: Violation) {
        val name = Application.getProcessName()
        System.err.println("------------------------- Violation detected in " + name + " ------" + v.cause + "---------------------------")
        VpnStatus.logException(VpnStatus.LogLevel.DEBUG, null, v)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannels() {
        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Background message
        var name: CharSequence? = context.getString(R.string.channel_name_background)
        var mChannel = NotificationChannel(
            OpenVPNService.NOTIFICATION_CHANNEL_BG_ID,
            name, NotificationManager.IMPORTANCE_MIN
        )

        mChannel.setDescription(context.getString(R.string.channel_description_background))
        mChannel.enableLights(false)

        mChannel.setLightColor(Color.DKGRAY)
        mNotificationManager.createNotificationChannel(mChannel)

        // Connection status change messages
        name = context.getString(R.string.channel_name_status)
        mChannel = NotificationChannel(
            OpenVPNService.NOTIFICATION_CHANNEL_NEWSTATUS_ID,
            name, NotificationManager.IMPORTANCE_LOW
        )

        mChannel.setDescription(context.getString(R.string.channel_description_status))
        mChannel.enableLights(true)

        mChannel.setLightColor(Color.BLUE)
        mNotificationManager.createNotificationChannel(mChannel)


        // Urgent requests, e.g. two factor auth
        name = context.getString(R.string.channel_name_userreq)
        mChannel = NotificationChannel(
            OpenVPNService.NOTIFICATION_CHANNEL_USERREQ_ID,
            name, NotificationManager.IMPORTANCE_HIGH
        )
        mChannel.setDescription(context.getString(R.string.channel_description_userreq))
        mChannel.enableVibration(true)
        mChannel.setLightColor(Color.CYAN)
        mNotificationManager.createNotificationChannel(mChannel)
    }
}