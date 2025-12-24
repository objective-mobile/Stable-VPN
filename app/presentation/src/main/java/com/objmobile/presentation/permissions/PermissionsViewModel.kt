package com.objmobile.presentation.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.objmobile.domain.PermissionManager
import com.objmobile.domain.PermissionState
import com.objmobile.domain.PermissionType
import com.objmobile.domain.Permissions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PermissionsViewModel(
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _permissionState = MutableStateFlow<PermissionState>(PermissionState.Checking)
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()
    val permissions = permissionManager.permissions().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = Permissions(
            vpnPermission = com.objmobile.domain.Permission(PermissionType.VPN, false),
            notificationPermission = com.objmobile.domain.Permission(
                PermissionType.NOTIFICATION,
                false
            )
        )
    )

    init {
        checkInitialPermissionState()
    }

    private fun checkInitialPermissionState() {
        viewModelScope.launch {
            _permissionState.value = PermissionState.Checking
            val currentPermissions = permissionManager.current()
            _permissionState.value = when {
                currentPermissions.allGranted() -> PermissionState.Granted
                else -> PermissionState.NotRequested
            }
        }
    }

    fun setPermissionRequesting() {
        _permissionState.value = PermissionState.Requesting
    }
    fun setVpnPermissionGranted() {
        viewModelScope.launch {
            permissionManager.grantVpn()
            updateOverallPermissionState()
        }
    }

    fun setNotificationPermissionGranted() {
        viewModelScope.launch {
            permissionManager.grantNotification()
            updateOverallPermissionState()
        }
    }

    fun setPermissionDenied() {
        viewModelScope.launch {
            permissionManager.deny()
            _permissionState.value = PermissionState.Denied
        }
    }

    fun resetPermissionState() {
        viewModelScope.launch {
            permissionManager.reset()
            _permissionState.value = PermissionState.NotRequested
        }
    }

    fun recheckPermissions() {
        checkInitialPermissionState()
    }
    private suspend fun updateOverallPermissionState() {
        val currentPermissions = permissionManager.current()
        _permissionState.value = if (currentPermissions.allGranted()) {
            PermissionState.Granted
        } else {
            PermissionState.NotRequested
        }
    }

    fun getMissingPermissions(): List<PermissionType> {
        return permissions.value.missing()
    }
}