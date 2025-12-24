package com.objmobile.domain

interface PermissionChecker {
    suspend fun vpnPermission(): Permission
    suspend fun notificationPermission(): Permission
    suspend fun allPermissions(): Permissions
}