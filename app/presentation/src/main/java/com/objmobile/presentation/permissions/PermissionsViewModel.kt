package com.objmobile.presentation.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PermissionsViewModel(
    private val permissionChecker: (() -> Boolean)? = null
) : ViewModel() {
    private val _permissionState = MutableStateFlow<PermissionState>(PermissionState.Checking)
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    init {
        checkInitialPermissionState()
    }

    private fun checkInitialPermissionState() {
        viewModelScope.launch {
            _permissionState.value =
                PermissionState.Checking // Add a small delay to show the checking state
            delay(500)
            val isGranted = permissionChecker?.invoke() ?: false
            _permissionState.value = if (isGranted) {
                PermissionState.Granted
            } else {
                PermissionState.NotRequested
            }
        }
    }

    fun setPermissionRequesting() {
        _permissionState.value = PermissionState.Requesting
    }

    fun setPermissionGranted() {
        _permissionState.value = PermissionState.Granted
    }

    fun setPermissionDenied() {
        _permissionState.value = PermissionState.Denied
    }

    fun resetPermissionState() {
        _permissionState.value = PermissionState.NotRequested
    }

    fun recheckPermissions() {
        checkInitialPermissionState()
    }
}

sealed class PermissionState {
    object Checking : PermissionState()
    object NotRequested : PermissionState()
    object Requesting : PermissionState()
    object Granted : PermissionState()
    object Denied : PermissionState()
}