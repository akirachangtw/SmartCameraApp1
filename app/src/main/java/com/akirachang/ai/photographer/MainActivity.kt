package com.akirachang.ai.photographer

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint
import com.akirachang.ai.photographer.presentation.camera.CameraScreen
import com.akirachang.ai.photographer.presentation.settings.SettingsScreen
import com.akirachang.ai.photographer.ui.theme.SmartCameraApp1Theme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartCameraApp1Theme {
                // v2.10.2: Request all permissions at startup (Camera + Location + Storage)
                // User preference: "初次執行時就會顯示提示是否允許存取"
                val permissions = buildList {
                    add(Manifest.permission.CAMERA)
                    add(Manifest.permission.ACCESS_FINE_LOCATION)

                    // Storage permissions (API level dependent)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // Android 13+ (API 33+): Granular media permissions
                        add(Manifest.permission.READ_MEDIA_IMAGES)
                        add(Manifest.permission.READ_MEDIA_VIDEO)
                    } else {
                        // Android 12 and below: Legacy storage permissions
                        add(Manifest.permission.READ_EXTERNAL_STORAGE)
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                            // Android 9 and below: Also need WRITE permission
                            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    }
                }

                val permissionsState = rememberMultiplePermissionsState(
                    permissions = permissions
                )

                // Request permissions on first launch
                LaunchedEffect(Unit) {
                    if (!permissionsState.allPermissionsGranted) {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                }

                // v2.9.5: Simple state-based navigation for Settings
                var showSettings by remember { mutableStateOf(false) }

                if (showSettings) {
                    SettingsScreen(
                        onNavigateBack = { showSettings = false },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CameraScreen(
                        modifier = Modifier.fillMaxSize(),
                        onNavigateToSettings = { showSettings = true }
                    )
                }
            }
        }
    }
}