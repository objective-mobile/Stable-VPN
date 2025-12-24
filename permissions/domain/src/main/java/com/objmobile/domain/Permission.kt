package com.objmobile.domain

data class Permission(
    val type: PermissionType, val granted: Boolean
) {
    fun isGranted(): Boolean = granted
    fun isDenied(): Boolean = !granted
}