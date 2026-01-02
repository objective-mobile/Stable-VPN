package com.objmobile.data

import android.content.Context
import com.objmobile.domain.PermissionManager

object PermissionFactory {
    fun createPermissionManager(context: Context): PermissionManager {
        val vpnChecker = AndroidVpnPermissionChecker(context)
        val notificationChecker = AndroidNotificationPermissionChecker(context)
        val permissionChecker = AndroidPermissionChecker(vpnChecker, notificationChecker)
        val repository = PermissionBaseRepository(permissionChecker)

        return PermissionManager(repository)
    }
}