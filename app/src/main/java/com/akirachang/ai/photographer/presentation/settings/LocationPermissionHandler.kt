/**
 * ClassName: LocationPermissionHandler
 * Description: GPS permission request handler using Accompanist Permissions
 * Author: Claude Code
 * Date: 2026-02-08
 * Version: 2.11.0
 *
 * Modifications:
 * - 2026-02-08: Initial creation for GPS EXIF feature (v2.11.0 Phase 1)
 */
package com.akirachang.ai.photographer.presentation.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*

/**
 * Handles GPS location permission requests using Accompanist Permissions library.
 *
 * Displays an AlertDialog when permission is needed, and manages both grant/deny flows.
 * Provides guidance for permanently denied permissions (redirects to app settings).
 *
 * @param onPermissionGranted Callback invoked when both FINE and COARSE location permissions are granted
 * @param onPermissionDenied Callback invoked when user denies permission or dismisses dialog
 * @param content Composable content to display when permission is already granted
 *
 * Usage:
 * ```
 * LocationPermissionHandler(
 *     onPermissionGranted = { viewModel.setGpsEnabled(true) },
 *     onPermissionDenied = { /* keep toggle OFF */ }
 * ) {
 *     // Content shown when permission already granted
 * }
 * ```
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionHandler(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val locationPermissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val locationPermissionState = rememberMultiplePermissionsState(locationPermissions)

    val hasPermission = locationPermissionState.allPermissionsGranted

    // Trigger callback when permission is granted
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            onPermissionGranted()
        }
    }

    if (hasPermission) {
        // Permission already granted, show content
        content()
    } else {
        // Permission not granted, show request dialog
        val isPermanentlyDenied = locationPermissionState.permissions.any {
            !it.status.isGranted && !it.status.shouldShowRationale
        }

        AlertDialog(
            onDismissRequest = {
                onPermissionDenied()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location Permission",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = if (isPermanentlyDenied) "權限被拒絕" else "需要位置權限",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = if (isPermanentlyDenied) {
                        "位置權限已被永久拒絕。\n\n" +
                        "若要啟用 GPS 位置標記功能，請前往應用程式設定手動授予位置權限。\n\n" +
                        "路徑：設定 → 應用程式 → SmartCameraApp → 權限 → 位置"
                    } else {
                        "SmartCameraApp 需要位置權限來將 GPS 座標寫入照片 EXIF 資訊。\n\n" +
                        "這有助於記錄拍攝地點（經緯度、海拔）。\n\n" +
                        "您可以隨時在設定中關閉 GPS 標記功能。"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                if (isPermanentlyDenied) {
                    // Permanently denied - show "Go to Settings" button
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                            onPermissionDenied()
                        }
                    ) {
                        Text("前往設定")
                    }
                } else {
                    // First request - show "Grant Permission" button
                    Button(
                        onClick = {
                            locationPermissionState.launchMultiplePermissionRequest()
                        }
                    ) {
                        Text("授予權限")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onPermissionDenied) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * Extension property to check if all permissions in a MultiplePermissionsState are granted.
 */
@OptIn(ExperimentalPermissionsApi::class)
val MultiplePermissionsState.allPermissionsGranted: Boolean
    get() = permissions.all { it.status.isGranted }
