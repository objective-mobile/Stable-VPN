@file:OptIn(ExperimentalMaterial3Api::class)

package com.objmobile.presentation.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.objmobile.domain.Permission
import com.objmobile.domain.PermissionState
import com.objmobile.domain.PermissionType
import com.objmobile.domain.Permissions
import com.objmobile.presentation.components.VpnTopAppBar
import com.objmobile.presentation.ui.theme.StableVPNTheme

@Composable
fun PermissionsScreen(
    viewModel: PermissionsViewModel,
    onPermissionGranted: () -> Unit,
    onRequestPermission: () -> Unit
) {
    val permissionState by viewModel.permissionState.collectAsState()
    val permissions by viewModel.permissions.collectAsState()

    PermissionsScreenContent(
        permissionState = permissionState, permissions = permissions,
        onNextClick = onRequestPermission,
        onPermissionGranted = onPermissionGranted
    )
}

@Composable
private fun PermissionsScreenContent(
    permissionState: PermissionState, permissions: Permissions = Permissions(
        vpnPermission = Permission(PermissionType.VPN, false),
        notificationPermission = Permission(PermissionType.NOTIFICATION, false)
    ), onNextClick: () -> Unit, onPermissionGranted: () -> Unit = {}
) { // Handle permission granted state
    if (permissionState is PermissionState.Granted) {
        onPermissionGranted()
        return
    }
    val backgroundGradient = Brush.verticalGradient(
        listOf(
            Color(0xFFF8FAFC), Color(0xFFEEF2FF)
        )
    )

    Scaffold(
        topBar = {
            VpnTopAppBar()
        }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (permissionState) {
                is PermissionState.Checking -> { // Show checking state with progress indicator
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Color(0xFF1D4ED8),
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Checking permissions...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> { // Show normal permission screen content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) { // Title
                        Text(
                            text = "Permissions Required",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D4ED8),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp)) // Description
                        Text(
                            text = "StableVPN requires the following permissions to provide secure VPN protection and keep you informed about your connection status:",
                            fontSize = 16.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp)) // Permission items
                        PermissionItem(
                            title = "VPN Connection",
                            description = "Create and manage secure VPN tunnels",
                            isGranted = permissions.vpnGranted()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        PermissionItem(
                            title = "Notifications",
                            description = "Show connection status and important alerts",
                            isGranted = permissions.notificationGranted()
                        )

                        Spacer(modifier = Modifier.height(48.dp)) // Next Button or Loading State
                        when (permissionState) {
                            is PermissionState.Requesting -> {
                                Button(
                                    onClick = { },
                                    enabled = false,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF94A3B8),
                                        disabledContainerColor = Color(0xFF94A3B8)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Requesting...",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }

                            is PermissionState.Denied -> {
                                Column {
                                    Text(
                                        text = "Some permissions were denied. Please grant all required permissions to continue.",
                                        fontSize = 14.sp,
                                        color = Color(0xFFEF4444),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = onNextClick,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF1D4ED8)
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text(
                                            text = "Try Again",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            else -> {
                                Button(
                                    onClick = onNextClick,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1D4ED8)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = "Next",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionItem(
    title: String, description: String, isGranted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) { // Status indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444),
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )
            Text(
                text = description, fontSize = 14.sp, color = Color(0xFF64748B), lineHeight = 20.sp
            )
        } // Status text
        Text(
            text = if (isGranted) "Granted" else "Required",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444)
        )
    }
}

// --- Previews ---
@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun PermissionsScreenPreview_Checking() {
    StableVPNTheme {
        PermissionsScreenContent(
            permissionState = PermissionState.Checking,
            onNextClick = {},
            onPermissionGranted = {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun PermissionsScreenPreview_NotRequested() {
    StableVPNTheme {
        PermissionsScreenContent(
            permissionState = PermissionState.NotRequested, permissions = Permissions(
                vpnPermission = Permission(PermissionType.VPN, false),
                notificationPermission = Permission(PermissionType.NOTIFICATION, false)
            ),
            onNextClick = {}, onPermissionGranted = {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun PermissionsScreenPreview_PartiallyGranted() {
    StableVPNTheme {
        PermissionsScreenContent(
            permissionState = PermissionState.NotRequested,
            permissions = Permissions(
                vpnPermission = Permission(PermissionType.VPN, true),
                notificationPermission = Permission(PermissionType.NOTIFICATION, false)
            ),
            onNextClick = {},
            onPermissionGranted = {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun PermissionsScreenPreview_Requesting() {
    StableVPNTheme {
        PermissionsScreenContent(
            permissionState = PermissionState.Requesting,
            onNextClick = {},
            onPermissionGranted = {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun PermissionsScreenPreview_Denied() {
    StableVPNTheme {
        PermissionsScreenContent(
            permissionState = PermissionState.Denied,
            onNextClick = {},
            onPermissionGranted = {})
    }
}