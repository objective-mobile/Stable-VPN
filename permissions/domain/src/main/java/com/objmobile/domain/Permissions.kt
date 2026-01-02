package com.objmobile.domain

class Permissions(
    private val vpnPermission: Permission, private val notificationPermission: Permission
) {
    fun vpnGranted(): Boolean = vpnPermission.isGranted()
    fun notificationGranted(): Boolean = notificationPermission.isGranted()
    fun allGranted(): Boolean = vpnGranted() && notificationGranted()
    fun missing(): List<PermissionType> {
        val missing = mutableListOf<PermissionType>()
        if (vpnPermission.isDenied()) {
            missing.add(PermissionType.VPN)
        }
        if (notificationPermission.isDenied()) {
            missing.add(PermissionType.NOTIFICATION)
        }
        return missing
    }

    fun withVpnGranted(): Permissions = Permissions(
        vpnPermission = Permission(PermissionType.VPN, true),
        notificationPermission = notificationPermission
    )

    fun withNotificationGranted(): Permissions = Permissions(
        vpnPermission = vpnPermission,
        notificationPermission = Permission(PermissionType.NOTIFICATION, true)
    )
}