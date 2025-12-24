package com.objmobile.data

import android.content.Context
import android.net.VpnService

class AndroidVpnPermissionChecker(
    private val context: Context
) : VpnPermissionChecker {
    override fun isGranted(): Boolean {
        return VpnService.prepare(context) == null
    }
}