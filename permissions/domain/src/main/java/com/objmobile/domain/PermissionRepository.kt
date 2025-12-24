package com.objmobile.domain

import kotlinx.coroutines.flow.Flow

interface PermissionRepository {
    suspend fun current(): Permissions
    fun observe(): Flow<Permissions>
    suspend fun grantVpn(): Permissions
    suspend fun grantNotification(): Permissions
    suspend fun deny(): Permissions
    suspend fun reset(): Permissions
}