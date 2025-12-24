package com.objmobile.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PermissionManager(
    private val repository: PermissionRepository
) {
    suspend fun current(): Permissions = repository.current()
    fun state(): Flow<PermissionState> = repository.observe().map { permissions ->
        when {
            permissions.allGranted() -> PermissionState.Granted
            else -> PermissionState.NotRequested
        }
    }

    fun permissions(): Flow<Permissions> = repository.observe()
    suspend fun grantVpn(): Permissions = repository.grantVpn()
    suspend fun grantNotification(): Permissions = repository.grantNotification()
    suspend fun deny(): Permissions = repository.deny()
    suspend fun reset(): Permissions = repository.reset()
    suspend fun missing(): List<PermissionType> = current().missing()
}