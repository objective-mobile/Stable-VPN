package com.objmobile.data

import com.objmobile.domain.Permission
import com.objmobile.domain.PermissionChecker
import com.objmobile.domain.PermissionType
import com.objmobile.domain.Permissions

class AndroidPermissionChecker(
    private val vpnChecker: VpnPermissionChecker,
    private val notificationChecker: NotificationPermissionChecker
) : PermissionChecker {
    override suspend fun vpnPermission(): Permission {
        return Permission(PermissionType.VPN, vpnChecker.isGranted())
    }

    override suspend fun notificationPermission(): Permission {
        return Permission(PermissionType.NOTIFICATION, notificationChecker.isGranted())
    }

    override suspend fun allPermissions(): Permissions {
        return Permissions(
            vpnPermission = vpnPermission(), notificationPermission = notificationPermission()
        )
    }
}