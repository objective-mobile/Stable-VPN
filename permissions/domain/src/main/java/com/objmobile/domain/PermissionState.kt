package com.objmobile.domain

sealed class PermissionState {
    object Checking : PermissionState()
    object NotRequested : PermissionState()
    object Requesting : PermissionState()
    object Granted : PermissionState()
    object Denied : PermissionState()
}