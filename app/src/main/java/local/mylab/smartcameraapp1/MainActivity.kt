package local.mylab.smartcameraapp1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import local.mylab.smartcameraapp1.presentation.camera.CameraScreen
import local.mylab.smartcameraapp1.ui.theme.SmartCameraApp1Theme

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