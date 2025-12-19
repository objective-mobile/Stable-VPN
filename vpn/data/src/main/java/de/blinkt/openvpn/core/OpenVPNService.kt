/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */
package de.blinkt.openvpn.core

import android.Manifest.permission
import android.annotation.TargetApi
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.UiModeManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.content.pm.ShortcutManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.VpnService
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.system.OsConstants
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.objective.vpn.data.VpnConnectionStorage
import com.objective.vpn.data.VpnStatusData
import com.objmobile.vpn.data.mapper.ConnectionStatusMapper
import de.blinkt.openvpn.DisconnectVPNActivity
import de.blinkt.openvpn.LaunchVPN
import de.blinkt.openvpn.R
import de.blinkt.openvpn.VpnProfile
import de.blinkt.openvpn.api.ExternalAppDatabase
import de.blinkt.openvpn.core.NetworkSpace.IpAddress
import de.blinkt.openvpn.core.VpnStatus.ByteCountListener
import de.blinkt.openvpn.core.VpnStatus.StateListener
import de.blinkt.openvpn.utils.TotalTraffic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.net.Inet6Address
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.charset.Charset
import java.util.Calendar
import java.util.Locale
import java.util.Objects
import java.util.Vector
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class OpenVPNService : VpnService(), StateListener, Handler.Callback, ByteCountListener,
    IOpenVPNServiceInternal {
    private var byteIn: String? = null
    private var byteOut: String? = null
    private var duration: String? = null
    private val mDnslist = Vector<String>()
    private val mRoutes = NetworkSpace()
    private val mRoutesv6 = NetworkSpace()
    private val mProcessLock = Any()
    private val vpnConnectionStorage by lazy {
        VpnConnectionStorage.newInstance(this)
    }
    private var lastChannel: String? = null
    private var mProcessThread: Thread? = null
    private var mProfile: VpnProfile? = null
    private var mDomain: String? = null
    private var mLocalIP: CIDRIP? = null
    private var mMtu = 0
    private var mLocalIPv6: String? = null
    private var mDeviceStateReceiver: DeviceStateReceiver? = null
    private var mDisplayBytecount = false
    private var mStarting = false
    private var mConnecttime: Long = 0
    var management: OpenVPNManagement? = null
        private set
    private val scope = CoroutineScope(SupervisorJob())

    /*private final IBinder mBinder = new IOpenVPNServiceInternal.Stub() {

       @Override
       public boolean protect(int fd) throws RemoteException {
           return OpenVPNService.this.protect(fd);
       }

       @Override
       public void userPause(boolean shouldbePaused) throws RemoteException {
           OpenVPNService.this.userPause(shouldbePaused);
       }

       @Override
       public boolean stopVPN(boolean replaceConnection) throws RemoteException {
           return OpenVPNService.this.stopVPN(replaceConnection);
       }

       @Override
       public void addAllowedExternalApp(String packagename) throws RemoteException {
           OpenVPNService.this.addAllowedExternalApp(packagename);
       }

       @Override
       public boolean isAllowedExternalApp(String packagename) throws RemoteException {
           return OpenVPNService.this.isAllowedExternalApp(packagename);

       }

       @Override
       public void challengeResponse(String repsonse) throws RemoteException {
           OpenVPNService.this.challengeResponse(repsonse);
       }


   };*/
    private val mBinder: IBinder = LocalBinder()
    var isConnected: Boolean = false
    private var mLastTunCfg: String? = null
    private var mRemoteGW: String? = null
    private var guiHandler: Handler? = null
    private var mlastToast: Toast? = null
    private var mOpenVPNThread: Runnable? = null
    val contentIntent: PendingIntent?
        get() {
            try {
                if (mNotificationActivityClass != null) { // Let the configure Button show the Log
                    val intent = Intent(
                        baseContext, mNotificationActivityClass
                    )
                    val typeStart = Objects.requireNonNull<Any?>(
                        mNotificationActivityClass!!.getField("TYPE_START").get(null)
                    ).toString()
                    val typeFromNotify = Objects.requireNonNull<Any?>(
                        mNotificationActivityClass!!.getField("TYPE_FROM_NOTIFY").get(null)
                    ).toString().toInt()
                    intent.putExtra(typeStart, typeFromNotify)
                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                    return PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                }
            } catch (e: Exception) {
                Log.e(
                    this.javaClass.canonicalName, "Build detail intent error", e
                )
                e.printStackTrace()
            }
            return null
        }

    @Throws(RemoteException::class)
    override fun addAllowedExternalApp(packagename: String?) {
        val extapps = ExternalAppDatabase(this@OpenVPNService)
        extapps.addApp(packagename)
    }

    @Throws(RemoteException::class)
    override fun isAllowedExternalApp(packagename: String?): Boolean {
        val extapps = ExternalAppDatabase(this@OpenVPNService)
        return extapps.checkRemoteActionPermission(this, packagename)
    }

    @Throws(RemoteException::class)
    override fun challengeResponse(response: String) {
        if (this.management != null) {
            val b64response = Base64.encodeToString(
                response.toByteArray(Charset.forName("UTF-8")), Base64.DEFAULT
            )
            management!!.sendCRResponse(b64response)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        val action = intent.action
        if (action != null && action == START_SERVICE) return mBinder
        else return super.onBind(intent)
    }

    override fun onRevoke() {
        VpnStatus.logError(R.string.permission_revoked)
        management!!.stopVPN(false)
        endVpnService()
    }

    // Similar to revoke but do not try to stop process
    fun openvpnStopped() {
        endVpnService()
    }

    fun endVpnService() {
        synchronized(mProcessLock) {
            mProcessThread = null
        }
        VpnStatus.removeByteCountListener(this)
        unregisterDeviceStateReceiver()
        ProfileManager.setConntectedVpnProfileDisconnected(this)
        mOpenVPNThread = null
        if (!mStarting) {
            stopForeground(!mNotificationAlwaysVisible)

            if (!mNotificationAlwaysVisible) {
                stopSelf()
                VpnStatus.removeStateListener(this)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String?): String? {
        if (lastChannel == null || lastChannel!!.isEmpty()) {
            val chan = NotificationChannel(
                channelId,
                getString(R.string.channel_name_background),
                NotificationManager.IMPORTANCE_NONE
            )
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)

            lastChannel = channelId
        }

        return lastChannel
    }

    private fun showNotification(
        msg: String?,
        tickerText: String?,
        channel: String,
        `when`: Long,
        status: ConnectionStatus?,
        intent: Intent?
    ) {
        var channel = channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = createNotificationChannel(channel)!!
        }
        val mNotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager //The Error: it seems a bug in android greater than version 8, where it needs to Identify the channelId before a starting a foreground : https://stackoverflow.com/questions/47531742/startforeground-fail-after-upgrade-to-android-8-1/51281297#51281297 // It was Already Fixed in Android 12 :https://issuetracker.google.com/issues/192032398#comment6
        val nBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channel)
        } else {
            Notification.Builder(this)
        }
        val priority: Int
        if (channel == NOTIFICATION_CHANNEL_BG_ID) priority = PRIORITY_MIN
        else if (channel == NOTIFICATION_CHANNEL_USERREQ_ID) priority = PRIORITY_MAX
        else priority = PRIORITY_DEFAULT

        if (mProfile != null) nBuilder.setContentTitle(
            getString(
                R.string.notifcation_title
            )
        )
        else nBuilder.setContentTitle(getString(R.string.notifcation_title_notconnect))

        nBuilder.setContentText(msg)
        nBuilder.setOnlyAlertOnce(true)
        nBuilder.setOngoing(true)
        nBuilder.setSmallIcon(R.drawable.ic_notification)
        if (status == ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT) {
            val pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            nBuilder.setContentIntent(pIntent)
        } else {
            val contentPendingIntent = this.contentIntent
            if (contentPendingIntent != null) {
                nBuilder.setContentIntent(contentPendingIntent)
            } else {
                nBuilder.setContentIntent(this.graphPendingIntent)
            }
        }

        if (`when` != 0L) nBuilder.setWhen(`when`) // Try to set the priority available since API 16 (Jellybean)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            jbNotificationExtras(priority, nBuilder)
            addVpnActionsToNotification(nBuilder)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) lpNotificationExtras(
            nBuilder, Notification.CATEGORY_SERVICE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nBuilder.setChannelId(channel)
            if (mProfile != null) nBuilder.setShortcutId(mProfile!!.uuidString)
        }

        if (tickerText != null && tickerText != "") nBuilder.setTicker(tickerText)
        try {
            val notification = nBuilder.build()
            val notificationId = channel.hashCode()

            mNotificationManager.notify(notificationId, notification)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    notificationId,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            } else {
                startForeground(notificationId, notification)
            }

            if (lastChannel != null && channel != lastChannel) { // Cancel old notification
                mNotificationManager.cancel(lastChannel.hashCode())
            }
        } catch (th: Throwable) {
            Log.e(javaClass.canonicalName, "Error when show notification", th)
        } // Check if running on a TV
        if (runningOnAndroidTV() && priority >= 0) guiHandler!!.post(object : Runnable {
            override fun run() {
                if (mlastToast != null) mlastToast!!.cancel()
                val toastText = String.format(Locale.getDefault(), "%s - %s", mProfile!!.mName, msg)
                mlastToast = Toast.makeText(baseContext, toastText, Toast.LENGTH_SHORT)
                mlastToast!!.show()
            }
        })
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun lpNotificationExtras(nbuilder: Notification.Builder, category: String?) {
        nbuilder.setCategory(category)
        nbuilder.setLocalOnly(true)
    }

    private fun getIconByConnectionStatus(level: ConnectionStatus): Int {
        when (level) {
            ConnectionStatus.LEVEL_CONNECTED -> return R.drawable.ic_stat_vpn
            ConnectionStatus.LEVEL_AUTH_FAILED, ConnectionStatus.LEVEL_NONETWORK, ConnectionStatus.LEVEL_NOTCONNECTED -> return R.drawable.ic_stat_vpn_offline
            ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET, ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT -> return R.drawable.ic_stat_vpn_outline
            ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED -> return R.drawable.ic_stat_vpn_empty_halo
            ConnectionStatus.LEVEL_VPNPAUSED -> return android.R.drawable.ic_media_pause
            ConnectionStatus.UNKNOWN_LEVEL -> return R.drawable.ic_stat_vpn
            else -> return R.drawable.ic_stat_vpn
        }
    }

    private fun runningOnAndroidTV(): Boolean {
        val uiModeManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
        return uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun jbNotificationExtras(
        priority: Int, nbuilder: Notification.Builder
    ) {
        try {
            if (priority != 0) {
                val setpriority =
                    nbuilder.javaClass.getMethod("setPriority", Int::class.javaPrimitiveType)
                setpriority.invoke(nbuilder, priority)
                val setUsesChronometer = nbuilder.javaClass.getMethod(
                    "setUsesChronometer", Boolean::class.javaPrimitiveType
                )
                setUsesChronometer.invoke(nbuilder, true)
            } //ignore exception
        } catch (e: NoSuchMethodException) {
            VpnStatus.logException(e)
        } catch (e: IllegalArgumentException) {
            VpnStatus.logException(e)
        } catch (e: InvocationTargetException) {
            VpnStatus.logException(e)
        } catch (e: IllegalAccessException) {
            VpnStatus.logException(e)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private fun addVpnActionsToNotification(nbuilder: Notification.Builder) {
        val disconnectVPN = Intent(this, DisconnectVPNActivity::class.java)
        disconnectVPN.action = DISCONNECT_VPN
        val disconnectPendingIntent =
            PendingIntent.getActivity(this, 0, disconnectVPN, PendingIntent.FLAG_IMMUTABLE)

        nbuilder.addAction(
            R.drawable.ic_menu_close_clear_cancel,
            getString(R.string.cancel_connection),
            disconnectPendingIntent
        )
        val pauseVPN = Intent(this, OpenVPNService::class.java)
        if (mDeviceStateReceiver == null || !mDeviceStateReceiver!!.isUserPaused) {
            pauseVPN.action = PAUSE_VPN
            val pauseVPNPending =
                PendingIntent.getService(this, 0, pauseVPN, PendingIntent.FLAG_IMMUTABLE)
            nbuilder.addAction(
                R.drawable.ic_menu_pause, getString(R.string.pauseVPN), pauseVPNPending
            )
        } else {
            pauseVPN.action = RESUME_VPN
            val resumeVPNPending =
                PendingIntent.getService(this, 0, pauseVPN, PendingIntent.FLAG_IMMUTABLE)
            nbuilder.addAction(
                R.drawable.ic_menu_play, getString(R.string.resumevpn), resumeVPNPending
            )
        }
    }

    fun getUserInputIntent(needed: String?): PendingIntent? {
        val intent = Intent(applicationContext, LaunchVPN::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        intent.putExtra("need", needed)
        val b = Bundle()
        b.putString("need", needed)
        val pIntent = PendingIntent.getActivity(this, 12, intent, PendingIntent.FLAG_IMMUTABLE)
        return pIntent
    }

    val graphPendingIntent: PendingIntent?
        get() { // Let the configure Button show the Log
            val intent = Intent()
            intent.component = ComponentName(this, packageName + ".view.MainActivity")

            intent.putExtra("PAGE", "graph")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            val startLW = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            return startLW
        }

    @Synchronized
    fun registerDeviceStateReceiver(magnagement: OpenVPNManagement?) { // Registers BroadcastReceiver to track network connection changes.
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        mDeviceStateReceiver = DeviceStateReceiver(magnagement) // Fetch initial network state
        mDeviceStateReceiver!!.networkStateChange(this)

        registerReceiver(mDeviceStateReceiver, filter)
        VpnStatus.addByteCountListener(mDeviceStateReceiver)/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            addLollipopCMListener(); */
    }

    @Synchronized
    fun unregisterDeviceStateReceiver() {
        if (mDeviceStateReceiver != null) try {
            VpnStatus.removeByteCountListener(mDeviceStateReceiver)
            this.unregisterReceiver(mDeviceStateReceiver)
        } catch (iae: IllegalArgumentException) { // I don't know why  this happens:
            // java.lang.IllegalArgumentException: Receiver not registered: de.blinkt.openvpn.NetworkSateReceiver@41a61a10
            // Ignore for now ...
            iae.printStackTrace()
        }
        mDeviceStateReceiver = null/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            removeLollipopCMListener();*/
    }

    override fun userPause(shouldBePaused: Boolean) {
        if (mDeviceStateReceiver != null) mDeviceStateReceiver!!.userPause(shouldBePaused)
    }

    @Throws(RemoteException::class)
    override fun stopVPN(replaceConnection: Boolean): Boolean {
        if (this.management != null) return this.management!!.stopVPN(replaceConnection)
        else return false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.getBooleanExtra(
                ALWAYS_SHOW_NOTIFICATION, false
            )
        ) mNotificationAlwaysVisible = true

        VpnStatus.addStateListener(this)
        VpnStatus.addByteCountListener(this)

        guiHandler = Handler(mainLooper)

        if (intent != null && DISCONNECT_VPN == intent.action) {
            try {
                stopVPN(false)
            } catch (e: RemoteException) {
                VpnStatus.logException(e)
            }
            return START_NOT_STICKY
        }

        if (intent != null && PAUSE_VPN == intent.action) {
            if (mDeviceStateReceiver != null) mDeviceStateReceiver!!.userPause(true)
            return START_NOT_STICKY
        }

        if (intent != null && RESUME_VPN == intent.action) {
            if (mDeviceStateReceiver != null) mDeviceStateReceiver!!.userPause(false)
            return START_NOT_STICKY
        }


        if (intent != null && START_SERVICE == intent.action) return START_NOT_STICKY
        if (intent != null && START_SERVICE_STICKY == intent.action) {
            return START_REDELIVER_INTENT
        } // Always show notification here to avoid problem with startForeground timeout
        VpnStatus.logInfo(R.string.building_configration)
        VpnStatus.updateStateString(
            "VPN_GENERATE_CONFIG", "", R.string.building_configration, ConnectionStatus.LEVEL_START
        )
        showNotification(
            VpnStatus.getLastCleanLogMessage(this),
            VpnStatus.getLastCleanLogMessage(this),
            NOTIFICATION_CHANNEL_NEWSTATUS_ID,
            0,
            ConnectionStatus.LEVEL_START,
            null
        )

        if (intent != null && intent.hasExtra(packageName + ".profileUUID")) {
            val profileUUID = intent.getStringExtra(packageName + ".profileUUID")
            val profileVersion = intent.getIntExtra(
                packageName + ".profileVersion", 0
            ) // Try for 10s to get current version of the profile
            mProfile = ProfileManager.get(this, profileUUID, profileVersion, 100)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                updateShortCutUsage(mProfile)
            }
        } else {/* The intent is null when we are set as always-on or the service has been restarted. */
            mProfile = ProfileManager.getLastConnectedProfile(this)
            VpnStatus.logInfo(R.string.service_restarted)/* Got no profile, just stop */
            if (mProfile == null) {
                Log.d(
                    "OpenVPN", "Got no last connected profile on null intent. Assuming always on."
                )
                mProfile = ProfileManager.getAlwaysOnVPN(this)

                if (mProfile == null) {
                    stopSelf(startId)
                    return START_NOT_STICKY
                }
            }/* Do the asynchronous keychain certificate stuff */
            mProfile!!.checkForRestart(this)
        }

        if (mProfile == null) {
            stopSelf(startId)
            return START_NOT_STICKY
        }/* start the OpenVPN process itself in a background thread */
        Thread(object : Runnable {
            override fun run() {
                startOpenVPN()
            }
        }).start()


        ProfileManager.setConnectedVpnProfile(this, mProfile)
        VpnStatus.setConnectedVPNProfile(mProfile!!.uuidString)
        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun updateShortCutUsage(profile: VpnProfile?) {
        if (profile == null) return
        val shortcutManager = getSystemService<ShortcutManager>(ShortcutManager::class.java)
        shortcutManager.reportShortcutUsed(profile.uuidString)
    }

    private fun startOpenVPN() {
        try {
            mProfile!!.writeConfigFile(this)
        } catch (e: IOException) {
            VpnStatus.logException("Error writing config file", e)
            endVpnService()
            return
        }
        val nativeLibraryDirectory = applicationInfo.nativeLibraryDir
        var tmpDir: String?
        try {
            tmpDir = application.cacheDir.canonicalPath
        } catch (e: IOException) {
            e.printStackTrace()
            tmpDir = "/tmp"
        } // Write OpenVPN binary
        val argv =
            VPNLaunchHelper.buildOpenvpnArgv(this) // Set a flag that we are starting a new VPN
        mStarting = true // Stop the previous session by interrupting the thread.
        stopOldOpenVPNProcess() // An old running VPN should now be exited
        mStarting = false // Start a new session by creating a new thread.
        val useOpenVPN3 = VpnProfile.doUseOpenVPN3(this) // Open the Management Interface
        if (!useOpenVPN3) { // start a Thread that handles incoming messages of the managment socket
            val ovpnManagementThread = OpenVpnManagementThread(mProfile, this)
            if (ovpnManagementThread.openManagementInterface(this)) {
                val mSocketManagerThread = Thread(ovpnManagementThread, "OpenVPNManagementThread")
                mSocketManagerThread.start()
                this.management = ovpnManagementThread
                VpnStatus.logInfo("started Socket Thread")
            } else {
                endVpnService()
                return
            }
        }
        val processThread: Runnable?
        if (useOpenVPN3) {
            val mOpenVPN3 = instantiateOpenVPN3Core()
            processThread = mOpenVPN3 as Runnable?
            this.management = mOpenVPN3
        } else {
            processThread = OpenVPNThread(this, argv, nativeLibraryDirectory, tmpDir)
            mOpenVPNThread = processThread
        }

        synchronized(mProcessLock) {
            mProcessThread = Thread(processThread, "OpenVPNProcessThread")
            mProcessThread!!.start()
        }

        Handler(mainLooper).post(object : Runnable {
            override fun run() {
                if (mDeviceStateReceiver != null) unregisterDeviceStateReceiver()

                registerDeviceStateReceiver(this@OpenVPNService.management)
            }
        })
    }

    private fun stopOldOpenVPNProcess() {
        if (this.management != null) {
            if (mOpenVPNThread != null) (mOpenVPNThread as OpenVPNThread).setReplaceConnection()
            if (management!!.stopVPN(true)) { // an old was asked to exit, wait 1s
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) { //ignore
                }
            }
        }

        forceStopOpenVpnProcess()
    }

    fun forceStopOpenVpnProcess() {
        synchronized(mProcessLock) {
            if (mProcessThread != null) {
                mProcessThread!!.interrupt()
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) { //ignore
                }
            }
        }
    }

    private fun instantiateOpenVPN3Core(): OpenVPNManagement? {
        try {
            val cl = Class.forName("de.blinkt.openvpn.core.OpenVPNThreadv3")
            return cl.getConstructor(OpenVPNService::class.java, VpnProfile::class.java)
                .newInstance(this, mProfile) as OpenVPNManagement
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return null
    }

    override fun asBinder(): IBinder {
        return mBinder
    }

    override fun onDestroy() {
        scope.launch {
            vpnConnectionStorage.saveVpnStatus(VpnStatusData(com.objmobile.vpn.domain.VpnStatus.Disconnected))
        }
        synchronized(mProcessLock) {
            if (mProcessThread != null) {
                management!!.stopVPN(true)
            }
        }

        if (mDeviceStateReceiver != null) {
            this.unregisterReceiver(mDeviceStateReceiver)
        } // Just in case unregister for state
        VpnStatus.removeStateListener(this)
        VpnStatus.flushLog()
    }

    private val tunConfigString: String
        get() { // The format of the string is not important, only that
            // two identical configurations produce the same result
            var cfg = "TUNCFG UNQIUE STRING ips:"

            if (mLocalIP != null) cfg += mLocalIP.toString()
            if (mLocalIPv6 != null) cfg += mLocalIPv6


            cfg += "routes: " + TextUtils.join(
                "|", mRoutes.getNetworks(true)
            ) + TextUtils.join("|", mRoutesv6.getNetworks(true))
            cfg += "excl. routes:" + TextUtils.join(
                "|", mRoutes.getNetworks(false)
            ) + TextUtils.join("|", mRoutesv6.getNetworks(false))
            cfg += "dns: " + TextUtils.join("|", mDnslist)
            cfg += "domain: " + mDomain
            cfg += "mtu: " + mMtu
            return cfg
        }

    fun openTun(): ParcelFileDescriptor? { //Debug.startMethodTracing(getExternalFilesDir(null).toString() + "/opentun.trace", 40* 1024 * 1024);
        val builder: Builder = Builder()

        VpnStatus.logInfo(R.string.last_openvpn_tun_config)
        val allowUnsetAF =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !mProfile!!.mBlockUnusedAddressFamilies
        if (allowUnsetAF) {
            allowAllAFFamilies(builder)
        }

        if (mLocalIP == null && mLocalIPv6 == null) {
            VpnStatus.logError(getString(R.string.opentun_no_ipaddr))
            return null
        }

        if (mLocalIP != null) { // OpenVPN3 manages excluded local networks by callback
            if (!VpnProfile.doUseOpenVPN3(this)) addLocalNetworksToRoutes()
            try {
                builder.addAddress(mLocalIP!!.mIp, mLocalIP!!.len)
            } catch (iae: IllegalArgumentException) {
                VpnStatus.logError(R.string.dns_add_error, mLocalIP, iae.localizedMessage)
                return null
            }
        }

        if (mLocalIPv6 != null) {
            val ipv6parts =
                mLocalIPv6!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            try {
                builder.addAddress(ipv6parts[0], ipv6parts[1].toInt())
            } catch (iae: IllegalArgumentException) {
                VpnStatus.logError(R.string.ip_add_error, mLocalIPv6, iae.localizedMessage)
                return null
            }
        }


        for (dns in mDnslist) {
            try {
                builder.addDnsServer(dns)
            } catch (iae: IllegalArgumentException) {
                VpnStatus.logError(R.string.dns_add_error, dns, iae.localizedMessage)
            }
        }
        val release = Build.VERSION.RELEASE
        if ((Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && !release.startsWith("4.4.3") && !release.startsWith(
                "4.4.4"
            ) && !release.startsWith("4.4.5") && !release.startsWith("4.4.6")) && mMtu < 1280
        ) {
            VpnStatus.logInfo(
                String.format(
                    Locale.US,
                    "Forcing MTU to 1280 instead of %d to workaround Android Bug #70916",
                    mMtu
                )
            )
            builder.setMtu(1280)
        } else {
            builder.setMtu(mMtu)
        }
        val positiveIPv4Routes = mRoutes.getPositiveIPList()
        val positiveIPv6Routes = mRoutesv6.getPositiveIPList()

        if ("samsung" == Build.BRAND && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mDnslist.size >= 1) { // Check if the first DNS Server is in the VPN range
            try {
                val dnsServer = IpAddress(CIDRIP(mDnslist.get(0), 32), true)
                var dnsIncluded = false
                for (net in positiveIPv4Routes) {
                    if (net.containsNet(dnsServer)) {
                        dnsIncluded = true
                    }
                }
                if (!dnsIncluded) {
                    val samsungwarning = String.format(
                        "Warning Samsung Android 5.0+ devices ignore DNS servers outside the VPN range. To enable DNS resolution a route to your DNS Server (%s) has been added.",
                        mDnslist.get(0)
                    )
                    VpnStatus.logWarning(samsungwarning)
                    positiveIPv4Routes.add(dnsServer)
                }
            } catch (e: Exception) { // If it looks like IPv6 ignore error
                if (!mDnslist.get(0)
                        .contains(":")
                ) VpnStatus.logError("Error parsing DNS Server IP: " + mDnslist.get(0))
            }
        }
        val multicastRange = IpAddress(CIDRIP("224.0.0.0", 3), true)

        for (route in positiveIPv4Routes) {
            try {
                if (multicastRange.containsNet(route)) VpnStatus.logDebug(
                    R.string.ignore_multicast_route, route.toString()
                )
                else builder.addRoute(route.getIPv4Address(), route.networkMask)
            } catch (ia: IllegalArgumentException) {
                VpnStatus.logError(getString(R.string.route_rejected) + route + " " + ia.localizedMessage)
            }
        }

        for (route6 in positiveIPv6Routes) {
            try {
                builder.addRoute(route6.getIPv6Address(), route6.networkMask)
            } catch (ia: IllegalArgumentException) {
                VpnStatus.logError(getString(R.string.route_rejected) + route6 + " " + ia.localizedMessage)
            }
        }


        if (mDomain != null) builder.addSearchDomain(mDomain!!)
        var ipv4info: String?
        var ipv6info: String?
        if (allowUnsetAF) {
            ipv4info = "(not set, allowed)"
            ipv6info = "(not set, allowed)"
        } else {
            ipv4info = "(not set)"
            ipv6info = "(not set)"
        }
        val ipv4len: Int
        if (mLocalIP != null) {
            ipv4len = mLocalIP!!.len
            ipv4info = mLocalIP!!.mIp
        } else {
            ipv4len = -1
        }

        if (mLocalIPv6 != null) {
            ipv6info = mLocalIPv6
        }

        if ((!mRoutes.getNetworks(false).isEmpty() || !mRoutesv6.getNetworks(false)
                .isEmpty()) && this.isLockdownEnabledCompat
        ) {
            VpnStatus.logInfo("VPN lockdown enabled (do not allow apps to bypass VPN) enabled. Route exclusion will not allow apps to bypass VPN (e.g. bypass VPN for local networks)")
        }
        if (mDomain != null) builder.addSearchDomain(mDomain!!)
        VpnStatus.logInfo(R.string.local_ip_info, ipv4info, ipv4len, ipv6info, mMtu)
        VpnStatus.logInfo(R.string.dns_server_info, TextUtils.join(", ", mDnslist), mDomain)
        VpnStatus.logInfo(
            R.string.routes_info_incl,
            TextUtils.join(", ", mRoutes.getNetworks(true)),
            TextUtils.join(", ", mRoutesv6.getNetworks(true))
        )
        VpnStatus.logInfo(
            R.string.routes_info_excl,
            TextUtils.join(", ", mRoutes.getNetworks(false)),
            TextUtils.join(", ", mRoutesv6.getNetworks(false))
        )
        VpnStatus.logDebug(
            R.string.routes_debug,
            TextUtils.join(", ", positiveIPv4Routes),
            TextUtils.join(", ", positiveIPv6Routes)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setAllowedVpnPackages(builder)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) { // VPN always uses the default network
            builder.setUnderlyingNetworks(null)
        }
        var session = mProfile!!.mName
        if (mLocalIP != null && mLocalIPv6 != null) session =
            getString(R.string.session_ipv6string, session, mLocalIP, mLocalIPv6)
        else if (mLocalIP != null) session =
            getString(R.string.session_ipv4string, session, mLocalIP)
        else session = getString(R.string.session_ipv4string, session, mLocalIPv6)

        builder.setSession(session) // No DNS Server, log a warning
        if (mDnslist.size == 0) VpnStatus.logInfo(R.string.warn_no_dns)

        mLastTunCfg = this.tunConfigString // Reset information
        mDnslist.clear()
        mRoutes.clear()
        mRoutesv6.clear()
        mLocalIP = null
        mLocalIPv6 = null
        mDomain = null

        builder.setConfigureIntent(this.graphPendingIntent!!)

        try { //Debug.stopMethodTracing();
            val tun = builder.establish()
            if (tun == null) throw NullPointerException("Android establish() method returned null (Really broken network configuration?)")
            return tun
        } catch (e: Exception) {
            VpnStatus.logError(R.string.tun_open_error)
            VpnStatus.logError(getString(R.string.error) + e.localizedMessage)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                VpnStatus.logError(R.string.tun_error_helpful)
            }
            return null
        }
    }

    private val isLockdownEnabledCompat: Boolean
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return isLockdownEnabled
            } else {/* We cannot determine this, return false */
                return false
            }
        }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun allowAllAFFamilies(builder: Builder) {
        builder.allowFamily(OsConstants.AF_INET)
        builder.allowFamily(OsConstants.AF_INET6)
    }

    private fun addLocalNetworksToRoutes() {
        for (net in NetworkUtils.getLocalNetworks(this, false)) {
            val netparts = net.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val ipAddr = netparts[0]
            val netMask = netparts[1].toInt()
            if (ipAddr == mLocalIP!!.mIp) continue

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT && !mProfile!!.mAllowLocalLAN) {
                mRoutes.addIPSplit(CIDRIP(ipAddr, netMask), true)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && mProfile!!.mAllowLocalLAN) mRoutes.addIP(
                CIDRIP(ipAddr, netMask), false
            )
        } // IPv6 is Lollipop+ only so we can skip the lower than KITKAT case
        if (mProfile!!.mAllowLocalLAN) {
            for (net in NetworkUtils.getLocalNetworks(this, true)) {
                addRoutev6(net, false)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setAllowedVpnPackages(builder: Builder) {
        var profileUsesOrBot = false

        for (c in mProfile!!.mConnections) {
            if (c.mProxyType == Connection.ProxyType.ORBOT) profileUsesOrBot = true
        }

        if (profileUsesOrBot) VpnStatus.logDebug("VPN Profile uses at least one server entry with Orbot. Setting up VPN so that OrBot is not redirected over VPN.")
        var atLeastOneAllowedApp = false

        if (mProfile!!.mAllowedAppsVpnAreDisallowed && profileUsesOrBot) {
            try {
                builder.addDisallowedApplication(ORBOT_PACKAGE_NAME)
            } catch (e: PackageManager.NameNotFoundException) {
                VpnStatus.logDebug("Orbot not installed?")
            }
        }

        for (pkg in mProfile!!.mAllowedAppsVpn) {
            try {
                if (mProfile!!.mAllowedAppsVpnAreDisallowed) {
                    builder.addDisallowedApplication(pkg)
                } else {
                    if (!(profileUsesOrBot && pkg == ORBOT_PACKAGE_NAME)) {
                        builder.addAllowedApplication(pkg)
                        atLeastOneAllowedApp = true
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                mProfile!!.mAllowedAppsVpn.remove(pkg)
                VpnStatus.logInfo(R.string.app_no_longer_exists, pkg)
            }
        }

        if (!mProfile!!.mAllowedAppsVpnAreDisallowed && !atLeastOneAllowedApp) {
            VpnStatus.logDebug(R.string.no_allowed_app, packageName)
            try {
                builder.addAllowedApplication(packageName)
            } catch (e: PackageManager.NameNotFoundException) {
                VpnStatus.logError("This should not happen: " + e.localizedMessage)
            }
        }

        if (mProfile!!.mAllowedAppsVpnAreDisallowed) {
            VpnStatus.logDebug(
                R.string.disallowed_vpn_apps_info, TextUtils.join(", ", mProfile!!.mAllowedAppsVpn)
            )
        } else {
            VpnStatus.logDebug(
                R.string.allowed_vpn_apps_info, TextUtils.join(", ", mProfile!!.mAllowedAppsVpn)
            )
        }

        if (mProfile!!.mAllowAppVpnBypass) {
            builder.allowBypass()
            VpnStatus.logDebug("Apps may bypass VPN")
        }
    }

    fun addDNS(dns: String?) {
        mDnslist.add(dns)
    }

    fun setDomain(domain: String?) {
        if (mDomain == null) {
            mDomain = domain
        }
    }

    /**
     * Route that is always included, used by the v3 core
     */
    private fun addRoute(route: CIDRIP?, include: Boolean) {
        mRoutes.addIP(route, include)
    }

    fun addRoute(dest: String?, mask: String, gateway: String?, device: String?) {
        val route = CIDRIP(dest, mask)
        var include = isAndroidTunDevice(device)
        val gatewayIP = IpAddress(CIDRIP(gateway, 32), false)

        if (mLocalIP == null) {
            VpnStatus.logError("Local IP address unset and received. Neither pushed server config nor local config specifies an IP addresses. Opening tun device is most likely going to fail.")
            return
        }
        val localNet = IpAddress(mLocalIP, true)
        if (localNet.containsNet(gatewayIP)) include = true

        if (gateway != null && (gateway == "255.255.255.255" || gateway == mRemoteGW)) include =
            true


        if (route.len == 32 && mask != "255.255.255.255") {
            VpnStatus.logWarning(R.string.route_not_cidr, dest, mask)
        }

        if (route.normalise()) VpnStatus.logWarning(
            R.string.route_not_netip, dest, route.len, route.mIp
        )

        mRoutes.addIP(route, include)
    }

    fun addRoutev6(
        network: String, device: String?
    ) { // Tun is opened after ROUTE6, no device name may be present
        val included = isAndroidTunDevice(device)
        addRoutev6(network, included)
    }

    fun addRoutev6(network: String, included: Boolean) {
        val v6parts = network.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        try {
            val ip = InetAddress.getAllByName(v6parts[0])[0] as Inet6Address?
            val mask = v6parts[1].toInt()
            mRoutesv6.addIPv6(ip, mask, included)
        } catch (e: UnknownHostException) {
            VpnStatus.logException(e)
        }
    }

    private fun isAndroidTunDevice(device: String?): Boolean {
        return device != null && (device.startsWith("tun") || "(null)" == device || VPNSERVICE_TUN == device)
    }

    fun setMtu(mtu: Int) {
        mMtu = mtu
    }

    private fun setLocalIP(cdrip: CIDRIP?) {
        mLocalIP = cdrip
    }

    fun setLocalIP(local: String?, netmask: String, mtu: Int, mode: String?) {
        mLocalIP = CIDRIP(local, netmask)
        mMtu = mtu
        mRemoteGW = null
        val netMaskAsInt = CIDRIP.getInt(netmask)

        if (mLocalIP!!.len == 32 && netmask != "255.255.255.255") { // get the netmask as IP
            val masklen: Int
            val mask: Long
            if ("net30" == mode) {
                masklen = 30
                mask = -0x4
            } else {
                masklen = 31
                mask = -0x2
            } // Netmask is Ip address +/-1, assume net30/p2p with small net
            if ((netMaskAsInt and mask) == (mLocalIP!!.int and mask)) {
                mLocalIP!!.len = masklen
            } else {
                mLocalIP!!.len = 32
                if ("p2p" != mode) VpnStatus.logWarning(R.string.ip_not_cidr, local, netmask, mode)
            }
        }
        if (("p2p" == mode && mLocalIP!!.len < 32) || ("net30" == mode && mLocalIP!!.len < 30)) {
            VpnStatus.logWarning(R.string.ip_looks_like_subnet, local, netmask, mode)
        }/* Workaround for Lollipop, it  does not route traffic to the VPNs own network mask */
        if (mLocalIP!!.len <= 31 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val interfaceRoute = CIDRIP(mLocalIP!!.mIp, mLocalIP!!.len)
            interfaceRoute.normalise()
            addRoute(interfaceRoute, true)
        } // Configurations are sometimes really broken...
        mRemoteGW = netmask
    }

    fun setLocalIPv6(ipv6addr: String?) {
        mLocalIPv6 = ipv6addr
    }

    override fun updateState(
        state: String?, logmessage: String?, resid: Int, level: ConnectionStatus, intent: Intent?
    ) { // If the process is not running, ignore any state,
        // Notification should be invisible in this state
        doSendBroadcast(state, level)
        if (mProcessThread == null && !mNotificationAlwaysVisible) return
        var channel: String =
            NOTIFICATION_CHANNEL_NEWSTATUS_ID // Display byte count only after being connected
        run {
            if (level == ConnectionStatus.LEVEL_CONNECTED) {
                mDisplayBytecount = true
                mConnecttime = System.currentTimeMillis()
                if (!runningOnAndroidTV()) channel = NOTIFICATION_CHANNEL_BG_ID
            } else {
                mDisplayBytecount = false
            } // Other notifications are shown,
            // This also mean we are no longer connected, ignore bytecount messages until next
            // CONNECTED
            // Does not work :(
            getString(resid)
            showNotification(
                VpnStatus.getLastCleanLogMessage(this),
                VpnStatus.getLastCleanLogMessage(this),
                channel,
                0,
                level,
                intent
            )
        }
    }

    override fun setConnectedVPN(uuid: String?) {
    }

    private fun doSendBroadcast(state: String?, level: ConnectionStatus) {
        val vpnstatus = Intent()
        vpnstatus.action = "de.blinkt.openvpn.VPN_STATUS"
        vpnstatus.putExtra("status", level.toString())
        vpnstatus.putExtra("detailstatus", state)
        sendBroadcast(vpnstatus, permission.ACCESS_NETWORK_STATE)
        Log.d("OpenVPNService", "doSendBroadcast: state $state $level")
        Log.d("OpenVPNService", "doSendBroadcast: ${mProfile?.name}")
        Log.d("OpenVPNService", "doSendBroadcast: ${mProfile?.mServerName}")
        Log.d("OpenVPNService", "doSendBroadcast: ${mProfile?.country}")

        scope.launch {
            vpnConnectionStorage.saveVpnStatus(
                VpnStatusData(
                    ConnectionStatusMapper.mapToDomain(
                        level,
                        mProfile?.country ?: "",
                        extractIpFromString(mProfile?.mName ?: "") ?: "",
                        ""
                    )
                )
            )
        }
        sendMessage(state)
    }

    fun extractIpFromString(input: String): String? {
        val ipRegex = Regex("""\b(?:\d{1,3}\.){3}\d{1,3}\b""")
        return ipRegex.find(input)?.value
    }

    var c: Long = Calendar.getInstance().timeInMillis
    var time: Long = 0
    var lastPacketReceive: Int = 0
    var seconds: String = "0"
    var minutes: String? = null
    var hours: String? = null
    override fun updateByteCount(`in`: Long, out: Long, diffIn: Long, diffOut: Long) {
        TotalTraffic.calcTraffic(this, `in`, out, diffIn, diffOut)
        if (mDisplayBytecount) {
            val netstat = String.format(
                getString(R.string.statusline_bytecount),
                humanReadableByteCount(`in`, false, resources),
                humanReadableByteCount(
                    diffIn / OpenVPNManagement.mBytecountInterval, true, resources
                ),
                humanReadableByteCount(out, false, resources),
                humanReadableByteCount(
                    diffOut / OpenVPNManagement.mBytecountInterval, true, resources
                )
            )


            showNotification(
                netstat,
                null,
                NOTIFICATION_CHANNEL_BG_ID,
                mConnecttime,
                ConnectionStatus.LEVEL_CONNECTED,
                null
            )
            byteIn = String.format(
                "↓%2\$s",
                getString(R.string.statusline_bytecount),
                humanReadableByteCount(`in`, false, resources)
            ) + " - " + humanReadableByteCount(
                diffIn / OpenVPNManagement.mBytecountInterval, false, resources
            ) + "/s"
            byteOut = String.format(
                "↑%2\$s",
                getString(R.string.statusline_bytecount),
                humanReadableByteCount(out, false, resources)
            ) + " - " + humanReadableByteCount(
                diffOut / OpenVPNManagement.mBytecountInterval, false, resources
            ) + "/s"
            time = Calendar.getInstance().timeInMillis - c
            lastPacketReceive =
                convertTwoDigit((time / 1000).toInt() % 60).toInt() - seconds.toInt()
            seconds = convertTwoDigit((time / 1000).toInt() % 60)
            minutes = convertTwoDigit(((time / (1000 * 60)) % 60).toInt())
            hours = convertTwoDigit(((time / (1000 * 60 * 60)) % 24).toInt())
            duration = hours + ":" + minutes + ":" + seconds
            lastPacketReceive = checkPacketReceive(lastPacketReceive)
            sendMessage(duration, lastPacketReceive.toString(), byteIn, byteOut)
        }
    }

    fun checkPacketReceive(value: Int): Int {
        var value = value
        value -= 2
        if (value < 0) return 0
        else return value
    }

    fun convertTwoDigit(value: Int): String {
        if (value < 10) return "0" + value
        else return value.toString() + ""
    }

    override fun handleMessage(msg: Message): Boolean {
        val r = msg.callback
        if (r != null) {
            r.run()
            return true
        } else {
            return false
        }
    }

    val tunReopenStatus: String
        get() {
            val currentConfiguration = this.tunConfigString
            if (currentConfiguration == mLastTunCfg) {
                return "NOACTION"
            } else {
                val release = Build.VERSION.RELEASE
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && !release.startsWith(
                        "4.4.3"
                    ) && !release.startsWith("4.4.4") && !release.startsWith("4.4.5") && !release.startsWith(
                        "4.4.6"
                    )
                )  // There will be probably no 4.4.4 or 4.4.5 version, so don't waste effort to do parsing here
                    return "OPEN_AFTER_CLOSE"
                else return "OPEN_BEFORE_CLOSE"
            }
        }

    fun requestInputFromUser(resid: Int, needed: String?) {
        VpnStatus.updateStateString(
            "NEED", "need " + needed, resid, ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT
        )
        showNotification(
            getString(resid),
            getString(resid),
            NOTIFICATION_CHANNEL_NEWSTATUS_ID,
            0,
            ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT,
            null
        )
    }

    fun trigger_sso(info: String) {
        val channel: String = NOTIFICATION_CHANNEL_USERREQ_ID
        val method = info.split(":".toRegex(), limit = 2).toTypedArray()[0]
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val nbuilder = Notification.Builder(this)
        nbuilder.setAutoCancel(true)
        val icon = android.R.drawable.ic_dialog_info
        nbuilder.setSmallIcon(icon)
        val intent: Intent?
        val reason: Int
        if (method == "CR_TEXT") {
            val challenge: String? = info.split(":".toRegex(), limit = 2).toTypedArray()[1]
            reason = R.string.crtext_requested
            nbuilder.setContentTitle(getString(reason))
            nbuilder.setContentText(challenge)

            intent = Intent()
            intent.component = ComponentName(
                this, packageName + ".activities.CredentialsPopup"
            )

            intent.putExtra(EXTRA_CHALLENGE_TXT, challenge)
        } else {
            VpnStatus.logError("Unknown SSO method found: " + method)
            return
        } // updateStateString trigger the notification of the VPN to be refreshed, save this intent
        // to have that notification also this intent to be set
        val pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        VpnStatus.updateStateString(
            "USER_INPUT",
            "waiting for user input",
            reason,
            ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT,
            intent
        )
        nbuilder.setContentIntent(pIntent) // Try to set the priority available since API 16 (Jellybean)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) jbNotificationExtras(
            PRIORITY_MAX, nbuilder
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) lpNotificationExtras(
            nbuilder, Notification.CATEGORY_STATUS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nbuilder.setChannelId(channel)
        }
        @Suppress("deprecation") val notification = nbuilder.notification
        val notificationId = channel.hashCode()

        mNotificationManager.notify(notificationId, notification)
    }

    //sending message to main activity
    private fun sendMessage(state: String?) {
        val intent = Intent("connectionState")
        intent.putExtra("state", state)
        status = state
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    //sending message to main activity
    private fun sendMessage(
        duration: String?, lastPacketReceive: String?, byteIn: String?, byteOut: String?
    ) {
        val intent = Intent("connectionState")
        intent.putExtra("duration", duration)
        intent.putExtra("lastPacketReceive", lastPacketReceive)
        intent.putExtra("byteIn", byteIn)
        intent.putExtra("byteOut", byteOut)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    inner class LocalBinder : Binder() {
        val service: OpenVPNService
            get() = // Return this instance of LocalService so clients can call public methods
                this@OpenVPNService
    }

    companion object {
        const val START_SERVICE: String = "de.blinkt.openvpn.START_SERVICE"
        const val START_SERVICE_STICKY: String = "de.blinkt.openvpn.START_SERVICE_STICKY"
        const val ALWAYS_SHOW_NOTIFICATION: String = "de.blinkt.openvpn.NOTIFICATION_ALWAYS_VISIBLE"
        const val DISCONNECT_VPN: String = "de.blinkt.openvpn.DISCONNECT_VPN"
        const val NOTIFICATION_CHANNEL_BG_ID: String = "openvpn_bg"
        const val NOTIFICATION_CHANNEL_NEWSTATUS_ID: String = "openvpn_newstat"
        const val NOTIFICATION_CHANNEL_USERREQ_ID: String = "openvpn_userreq"
        const val VPNSERVICE_TUN: String = "vpnservice-tun"
        const val ORBOT_PACKAGE_NAME: String = "org.torproject.android"
        private const val PAUSE_VPN = "de.blinkt.openvpn.PAUSE_VPN"
        private const val RESUME_VPN = "de.blinkt.openvpn.RESUME_VPN"
        const val EXTRA_CHALLENGE_TXT: String = "de.blinkt.openvpn.core.CR_TEXT_CHALLENGE"
        const val EXTRA_CHALLENGE_OPENURL: String = "de.blinkt.openvpn.core.OPENURL_CHALLENGE"
        private val PRIORITY_MIN = -2
        private const val PRIORITY_DEFAULT = 0
        private const val PRIORITY_MAX = 2
        private var mNotificationAlwaysVisible = false
        private var mNotificationActivityClass: Class<out Activity?>? =
            null //it will be call from mainactivity for get current status
        var status: String? = ""
            private set

        // From: http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
        @JvmStatic
        fun humanReadableByteCount(bytes: Long, speed: Boolean, res: Resources): String {
            var bytes = bytes
            if (speed) bytes = bytes * 8
            val unit = if (speed) 1000 else 1024
            val exp = max(0, min((ln(bytes.toDouble()) / ln(unit.toDouble())).toInt(), 3))
            val bytesUnit = (bytes / unit.toDouble().pow(exp.toDouble())).toFloat()

            if (speed) when (exp) {
                0 -> return res.getString(R.string.bits_per_second, bytesUnit)
                1 -> return res.getString(R.string.kbits_per_second, bytesUnit)
                2 -> return res.getString(R.string.mbits_per_second, bytesUnit)
                else -> return res.getString(R.string.gbits_per_second, bytesUnit)
            }
            else when (exp) {
                0 -> return res.getString(R.string.volume_byte, bytesUnit)
                1 -> return res.getString(R.string.volume_kbyte, bytesUnit)
                2 -> return res.getString(R.string.volume_mbyte, bytesUnit)
                else -> return res.getString(R.string.volume_gbyte, bytesUnit)
            }
        }

        /**
         * Sets the activity which should be opened when tapped on the permanent notification tile.
         *
         * @param activityClass The activity class to open
         */
        fun setNotificationActivityClass(activityClass: Class<out Activity?>?) {
            mNotificationActivityClass = activityClass
        }

        fun setDefaultStatus() {
            status = ""
        }
    }
}