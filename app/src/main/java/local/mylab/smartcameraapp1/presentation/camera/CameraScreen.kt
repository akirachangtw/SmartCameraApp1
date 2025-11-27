/**
 * ClassName: CameraScreen
 * Description: Main camera screen with ML Kit detection and composition guidance
 * Author: Claude Code
 * Date: 2025-11-17
 * Version: 1.5.0
 *
 * Modifications:
 * - 2025-11-17: Added ML Kit detection result text overlay
 * - 2025-11-18: Replaced text overlay with bounding box overlay
 * - 2025-11-23: Simplified to use MlKitAnalyzer with VIEW_REFERENCED coordinates (v1.4.0)
 * - 2025-11-25: Added smart composition guide with Rule of Thirds grid (v1.5.0)
 */
package local.mylab.smartcameraapp1.presentation.camera

import android.Manifest
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import local.mylab.smartcameraapp1.presentation.camera.components.CameraPreview
import local.mylab.smartcameraapp1.presentation.camera.components.PermissionRequestScreen
import local.mylab.smartcameraapp1.presentation.camera.components.overlay.CompositionHint
import local.mylab.smartcameraapp1.presentation.camera.components.overlay.DetectionOverlay
import local.mylab.smartcameraapp1.presentation.camera.components.overlay.GridOverlay

/**
 * 相機畫面 - 拍照功能
 *
 * 權限說明：
 * - 只需要 CAMERA 權限即可拍照和儲存照片
 * - 照片使用 MediaStore API 儲存，不需要 READ_MEDIA_* 權限
 * - 如果未來需要顯示圖庫，請使用 MediaPermissionHandler 處理 READ_MEDIA_* 權限
 *   參考：MEDIA_PERMISSIONS_GUIDE.md
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val compositionState by viewModel.compositionState.collectAsState() // v1.5.0
    val snackbarHostState = remember { SnackbarHostState() }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // 顯示錯誤訊息
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // 顯示拍照成功訊息
    LaunchedEffect(state.lastCapturedImageUri) {
        state.lastCapturedImageUri?.let {
            snackbarHostState.showSnackbar("照片已保存")
        }
    }

    if (cameraPermissionState.status.isGranted) {
        Scaffold(
            modifier = modifier,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 相機預覽
                // v2.0 Phase 2: Pass size callback for accurate composition analysis
                CameraPreview(
                    controller = viewModel.getCameraController(),
                    modifier = Modifier.fillMaxSize(),
                    onSizeChanged = { width, height ->
                        viewModel.updatePreviewSize(width, height)
                    }
                )

                // ML Kit 偵測邊界框覆蓋層
                // v1.4.0: 座標已經在 View 空間，無需傳遞尺寸資訊
                state.detectionResult?.let { result ->
                    DetectionOverlay(
                        detectionResult = result,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // v1.5.0: Rule of Thirds grid overlay
                GridOverlay(
                    modifier = Modifier.fillMaxSize(),
                    visible = true
                )

                // v1.5.0: Composition hint overlay
                CompositionHint(
                    compositionState = compositionState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 56.dp) // Below flash button
                )

                // 簡易計數顯示（可選，保留用於調試）
                state.detectionResult?.let { result ->
                    Text(
                        text = "${result.faces.size} 人臉, ${result.objects.size} 物件",
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 頂部控制欄（閃光燈）
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { viewModel.onEvent(CameraUiEvent.ToggleFlash) }
                    ) {
                        Icon(
                            imageVector = when (state.flashMode) {
                                ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                                ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                                else -> Icons.Default.FlashOff
                            },
                            contentDescription = "閃光燈",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // 底部控制欄
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 切換鏡頭按鈕
                    IconButton(
                        onClick = { viewModel.onEvent(CameraUiEvent.SwitchCamera) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "切換鏡頭",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // 拍照按鈕
                    FloatingActionButton(
                        onClick = { viewModel.onEvent(CameraUiEvent.CapturePhoto) },
                        modifier = Modifier
                            .size(72.dp)
                            .border(4.dp, Color.White, CircleShape),
                        shape = CircleShape,
                        containerColor = if (state.isCapturing) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "拍照",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // 佔位符（對稱布局）
                    Box(modifier = Modifier.size(48.dp))
                }
            }
        }
    } else {
        PermissionRequestScreen(
            permissionState = cameraPermissionState,
            rationaleMessage = "此應用需要相機權限才能拍照",
            deniedMessage = if (cameraPermissionState.status.shouldShowRationale) {
                "相機權限被拒絕。請在設定中啟用相機權限。"
            } else {
                "請授予相機權限以使用此功能"
            }
        )
    }
}
