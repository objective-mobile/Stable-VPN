package com.objmobile.data

interface NotificationPermissionChecker {
    fun isGranted(): Boolean
}