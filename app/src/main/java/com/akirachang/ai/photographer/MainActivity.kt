package com.akirachang.ai.photographer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import com.akirachang.ai.photographer.presentation.camera.CameraScreen
import com.akirachang.ai.photographer.ui.theme.SmartCameraApp1Theme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartCameraApp1Theme {
                CameraScreen(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}