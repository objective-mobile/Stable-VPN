package com.objmobile.data

import com.objmobile.domain.Permission
import com.objmobile.domain.PermissionChecker
import com.objmobile.domain.PermissionRepository
import com.objmobile.domain.PermissionType
import com.objmobile.domain.Permissions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PermissionBaseRepository(
    private val checker: PermissionChecker
) : PermissionRepository {
    private val _permissions = MutableStateFlow(
        Permissions(
            vpnPermission = Permission(PermissionType.VPN, false),
            notificationPermission = Permission(PermissionType.NOTIFICATION, false)
        )
    )

    override suspend fun current(): Permissions {
        val current = checker.allPermissions()
        _permissions.value = current
        return current
    }

    override fun observe(): Flow<Permissions> = _permissions.asStateFlow()
    override suspend fun grantVpn(): Permissions {
        val updated = _permissions.value.withVpnGranted()
        _permissions.value = updated
        return updated
    }

    override suspend fun grantNotification(): Permissions {
        val updated = _permissions.value.withNotificationGranted()
        _permissions.value = updated
        return updated
    }

    override suspend fun deny(): Permissions {
        val denied = Permissions(
            vpnPermission = Permission(PermissionType.VPN, false),
            notificationPermission = Permission(PermissionType.NOTIFICATION, false)
        )
        _permissions.value = denied
        return denied
    }

    override suspend fun reset(): Permissions {
        return deny()
    }
}