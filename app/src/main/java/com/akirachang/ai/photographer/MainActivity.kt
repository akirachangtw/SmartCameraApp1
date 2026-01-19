package com.akirachang.ai.photographer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import com.akirachang.ai.photographer.presentation.camera.CameraScreen
import com.akirachang.ai.photographer.presentation.settings.SettingsScreen
import com.akirachang.ai.photographer.ui.theme.SmartCameraApp1Theme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartCameraApp1Theme {
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