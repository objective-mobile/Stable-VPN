# VPN Permissions Screen

This presentation module provides a Compose screen for requesting VPN permissions in your Android
VPN application.

## Components

### PermissionsScreen

The main Composable screen that displays the permission request UI with:

- App branding (using VpnTopAppBar)
- Clear explanation of why permissions are needed
- Next button that triggers permission request
- Loading state during permission request
- Error handling for denied permissions

### PermissionsViewModel

Manages the permission request state:

- Tracks permission state (NotRequested, Requesting, Granted, Denied)
- Provides methods to update permission state
- Exposes state as StateFlow for Compose

### VpnPermissionConstants

Contains constants used for VPN permission handling

## Usage

### Basic Integration

```kotlin
@Composable
fun MyApp() {
    val permissionsViewModel: PermissionsViewModel = viewModel()

    PermissionsScreen(
        viewModel = permissionsViewModel,
        onPermissionGranted = { // Navigate to main VPN screen
        },
        onRequestPermission = { // Handle permission request in your app module
            // This is where you'd use VpnService.prepare() and startActivityForResult
            permissionsViewModel.setPermissionRequesting()
        })
}
```

### With Navigation Compose

```kotlin
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "permissions") {
        composable("permissions") {
            val viewModel: PermissionsViewModel = viewModel()
            PermissionsScreen(viewModel = viewModel, onPermissionGranted = {
                navController.navigate("main") {
                    popUpTo("permissions") { inclusive = true }
                }
            }, onRequestPermission = { // Handle VPN permission request
                viewModel.setPermissionRequesting() // Your app module should handle the actual VpnService.prepare() call
            })
        }
        composable("main") {
            VpnScreen(/* your VPN screen */)
        }
    }
}
```

### Handling Permission Results

In your app module (where you have Activity context), handle the permission results:

```kotlin
// In your Activity or wherever you handle VPN permissions
private fun handleVpnPermissionResult(resultCode: Int) {
    if (resultCode == Activity.RESULT_OK) {
        permissionsViewModel.setPermissionGranted()
    } else {
        permissionsViewModel.setPermissionDenied()
    }
}
```

## Required Permissions

Make sure your AndroidManifest.xml includes the necessary VPN permissions:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE" />

<service
    android:name="your.vpn.service.VpnService"
    android:permission="android.permission.BIND_VPN_SERVICE">
    <intent-filter>
        <action android:name="android.net.VpnService" />
    </intent-filter>
</service>
```

## Design

The screen follows the same design patterns as VpnScreen:

- Uses VpnTopAppBar for consistent branding
- Same background gradient and color scheme
- Material 3 design components
- Responsive layout with proper spacing
- Loading and error states

## States

The screen handles four permission states:

- **NotRequested**: Initial state, shows "Next" button
- **Requesting**: Shows loading spinner and "Requesting..." text
- **Granted**: Automatically navigates to next screen
- **Denied**: Shows error message and "Try Again" button