package com.objmobile.data

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class AndroidNotificationPermissionChecker(
    private val context: Context
) : NotificationPermissionChecker {
    override fun isGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else { // For API levels below 33, notification permission is granted by default
            true
        }
    }
}