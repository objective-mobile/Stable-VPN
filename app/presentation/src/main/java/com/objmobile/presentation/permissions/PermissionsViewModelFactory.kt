package com.objmobile.presentation.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.objmobile.domain.PermissionManager

class PermissionsViewModelFactory(
    private val permissionManager: PermissionManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PermissionsViewModel::class.java)) {
            return PermissionsViewModel(permissionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}