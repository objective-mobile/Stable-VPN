@file:OptIn(ExperimentalMaterial3Api::class)

package com.objmobile.presentation.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.objmobile.presentation.components.VpnTopAppBar
import com.objmobile.presentation.ui.theme.StableVPNTheme

@Composable
fun PermissionsScreen(
    viewModel: PermissionsViewModel,
    onPermissionGranted: () -> Unit,
    onRequestPermission: () -> Unit
) {
    val permissionState by viewModel.permissionState.collectAsState()

    PermissionsScreenContent(
        permissionState = permissionState,
        onNextClick = onRequestPermission,
        onPermissionGranted = onPermissionGranted
    )
}

@Composable
private fun PermissionsScreenContent(
    permissionState: PermissionState, onNextClick: () -> Unit, onPermissionGranted: () -> Unit = {}
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
                            text = "To provide you with secure VPN protection, StableVPN needs permission to create and manage VPN connections on your device.\n\n" + "This permission allows the app to:\n" + "• Create secure VPN tunnels\n" + "• Route your internet traffic through encrypted connections\n" + "• Protect your privacy and data\n\n" + "Your data remains private and secure at all times.",
                            fontSize = 16.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
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
                                        text = "Permission denied. Please grant VPN permission to continue.",
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
            permissionState = PermissionState.NotRequested,
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