package com.objmobile.data

interface VpnPermissionChecker {
    fun isGranted(): Boolean
}