/**
 * ClassName: CameraScreen
 * Description: Main camera screen with ML Kit detection and composition guidance
 * Author: Claude Code
 * Date: 2025-11-17
 * Version: 2.4.0
 *
 * Modifications:
 * - 2025-11-17: Added ML Kit detection result text overlay
 * - 2025-11-18: Replaced text overlay with bounding box overlay
 * - 2025-11-23: Simplified to use MlKitAnalyzer with VIEW_REFERENCED coordinates (v1.4.0)
 * - 2025-11-25: Added smart composition guide with Rule of Thirds grid (v1.5.0)
 * - 2025-11-30: Added directional indicator arrows for composition guidance (v2.0 Phase 4)
 * - 2025-12-01: Added LevelIndicator overlay with sensor integration (v2.1.0)
 * - 2025-12-08: Added CompositionGuideOverlay for visual composition guidance (v2.4.0)
 */
package com.akirachang.ai.photographer.presentation.camera

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Settings
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
import com.akirachang.ai.photographer.presentation.camera.components.CameraPreview
import com.akirachang.ai.photographer.presentation.camera.components.PermissionRequestScreen
import com.akirachang.ai.photographer.presentation.camera.components.overlay.CompositionGuideOverlay
import com.akirachang.ai.photographer.presentation.camera.components.overlay.CompositionHint
// v2.2.2: Removed DetectionOverlay and DirectionalIndicator - visual clutter
import com.akirachang.ai.photographer.presentation.camera.components.overlay.GridOverlay
import com.akirachang.ai.photographer.presentation.camera.components.overlay.GuidelineOverlay
import com.akirachang.ai.photographer.presentation.camera.components.overlay.HDRWarningOverlay
import com.akirachang.ai.photographer.presentation.camera.components.overlay.LevelIndicator
import com.akirachang.ai.photographer.presentation.camera.model.CompositionState
import com.akirachang.ai.photographer.domain.config.FeatureFlags

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
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val compositionState by viewModel.compositionState.collectAsState() // v1.5.0
    val lightingState by viewModel.lightingState.collectAsState() // v2.2.0
    val orientationState by viewModel.orientationState.collectAsState() // v2.1.0

    // Overlay visibility preferences (v2.1.0)
    val isGridEnabled by viewModel.isGridEnabled.collectAsState(initial = true)
    val isCompositionHintEnabled by viewModel.isCompositionHintEnabled.collectAsState(initial = true)
    val isDetectionOverlayEnabled by viewModel.isDetectionOverlayEnabled.collectAsState(initial = true)
    val isLevelIndicatorEnabled by viewModel.isLevelIndicatorEnabled.collectAsState(initial = true)
    val isDirectionalIndicatorEnabled by viewModel.isDirectionalIndicatorEnabled.collectAsState(initial = true)

    val snackbarHostState = remember { SnackbarHostState() }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // v2.6.1: Bind camera to lifecycle for LiveCameraSource
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        viewModel.bindCameraToLifecycle(lifecycleOwner)
    }

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
                // v2.5.1: Refactored to use Bitmap preview from CameraInputSource
                // v2.0 Phase 2: Pass size callback for accurate composition analysis
                val previewBitmap by viewModel.previewBitmap.collectAsState()
                CameraPreview(
                    previewBitmap = previewBitmap,
                    modifier = Modifier.fillMaxSize(),
                    onSizeChanged = { width, height ->
                        viewModel.updatePreviewSize(width, height)
                    }
                )

                // v2.2.2: DetectionOverlay removed - visual clutter, not professional
                // User feedback: Green face boxes and blue object labels are distracting

                // v1.5.0: Rule of Thirds grid overlay
                // v2.1.0: Respect overlay preferences
                GridOverlay(
                    modifier = Modifier.fillMaxSize(),
                    visible = isGridEnabled
                )

                // v2.4.0: Visual composition guide frame
                // Shows target position where face should move to
                val guideData = when (val currentState = compositionState) {
                    is CompositionState.Good -> currentState.guideData
                    is CompositionState.NeedsAdjustment -> currentState.guideData
                    else -> null
                }
                CompositionGuideOverlay(
                    guideData = guideData,
                    modifier = Modifier.fillMaxSize()
                )

                // v2.7.0 Phase 2: Guideline Overlay (Rule of Thirds)
                GuidelineOverlay(
                    enabled = FeatureFlags.enableGuidelineOverlay,
                    modifier = Modifier.fillMaxSize()
                )

                // v2.2.0: HDR/Lighting warning overlay (highest priority - shows on top)
                // v2.6.1: Only show in AUTO mode
                HDRWarningOverlay(
                    lightingState = lightingState,
                    hdrMode = state.hdrMode,  // v2.6.1: Pass HDR mode to control visibility
                    modifier = Modifier.fillMaxSize()
                )

                // v1.5.0: Composition hint overlay
                // v2.1.0: Respect overlay preferences
                if (isCompositionHintEnabled) {
                    CompositionHint(
                        compositionState = compositionState,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 56.dp) // Below HDR warning
                    )
                }

                // v2.2.2: DirectionalIndicator removed - arrows point to wrong locations
                // User feedback: Cannot find correct composition points by following arrows
                // Solution: Text-based CompositionHint is clearer and more accurate

                // v2.1.0: Level indicator overlay
                if (isLevelIndicatorEnabled) {
                    orientationState?.let { orientation ->
                        LevelIndicator(
                            roll = orientation.roll,
                            visible = true,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 120.dp) // Above camera controls
                        )
                    }
                }

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

                // 頂部控制欄（閃光燈、設定）
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 設定按鈕 (v2.1.0)
                    IconButton(
                        onClick = onNavigateToSettings
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "設定",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // 閃光燈按鈕
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

                    // HDR 按鈕 (v2.6.1)
                    IconButton(
                        onClick = { viewModel.onEvent(CameraUiEvent.ToggleHDR) }
                    ) {
                        Icon(
                            imageVector = when (state.hdrMode) {
                                com.akirachang.ai.photographer.presentation.camera.model.HDRMode.ON -> Icons.Default.WbSunny  // HDR ON - 太陽圖示
                                com.akirachang.ai.photographer.presentation.camera.model.HDRMode.OFF -> Icons.Default.BrightnessHigh  // HDR OFF - 亮度圖示（劃線）
                                com.akirachang.ai.photographer.presentation.camera.model.HDRMode.AUTO -> Icons.Default.AutoAwesome  // AUTO - 星星圖示
                            },
                            contentDescription = "HDR 模式: ${state.hdrMode.displayName}",
                            tint = when (state.hdrMode) {
                                com.akirachang.ai.photographer.presentation.camera.model.HDRMode.ON -> Color(0xFFFFA726)  // 橙色 - 強制開啟
                                com.akirachang.ai.photographer.presentation.camera.model.HDRMode.OFF -> Color(0xFF757575)  // 灰色 - 停用
                                com.akirachang.ai.photographer.presentation.camera.model.HDRMode.AUTO -> Color.White  // 白色 - 自動模式
                            },
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
