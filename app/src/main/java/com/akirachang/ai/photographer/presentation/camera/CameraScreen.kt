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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
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
import com.akirachang.ai.photographer.presentation.camera.components.overlay.FPSOverlay
import com.akirachang.ai.photographer.presentation.camera.components.overlay.GridOverlay
import com.akirachang.ai.photographer.presentation.camera.components.overlay.GuidelineOverlay
import com.akirachang.ai.photographer.presentation.camera.components.overlay.HDRWarningOverlay
import com.akirachang.ai.photographer.presentation.camera.components.overlay.LevelIndicator
import com.akirachang.ai.photographer.presentation.camera.model.CompositionState
import com.akirachang.ai.photographer.domain.config.FeatureFlags
import com.akirachang.ai.photographer.BuildConfig

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
    val guidelineResults by viewModel.guidelineResults.collectAsState() // v2.7.0 Phase 2 Sprint 4
    val currentFps by viewModel.currentFps.collectAsState() // v2.8.4 Quick Win

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

    // v2.9.5: Detect camera capabilities after permission granted
    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (cameraPermissionState.status.isGranted) {
            viewModel.detectCameraCapabilitiesIfNeeded()
        }
    }

    // 顯示錯誤訊息
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // v2.9.8: Removed text message for photo saved - rely on UI visual feedback only
    // (Button disable + CircularProgressIndicator)

    if (cameraPermissionState.status.isGranted) {
        Scaffold(
            modifier = modifier,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            // v2.9.9: Track preview size for Tap-to-Focus
            var previewSize by remember { mutableStateOf(IntSize.Zero) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .onSizeChanged { size ->
                        previewSize = size
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                // v2.9.9: Tap-to-Focus - single tap triggers focus
                                if (FeatureFlags.enableManualFocus &&
                                    previewSize.width > 0 &&
                                    previewSize.height > 0) {
                                    viewModel.onEvent(
                                        CameraUiEvent.FocusOnPoint(
                                            x = offset.x,
                                            y = offset.y,
                                            viewWidth = previewSize.width,
                                            viewHeight = previewSize.height
                                        )
                                    )
                                }
                            },
                            onLongPress = { offset ->
                                // v2.9.9 Day 1-2: AE/AF Lock - long press triggers lock
                                if (FeatureFlags.enableManualFocus &&
                                    previewSize.width > 0 &&
                                    previewSize.height > 0) {
                                    viewModel.onEvent(
                                        CameraUiEvent.LockAEAF(
                                            x = offset.x,
                                            y = offset.y,
                                            viewWidth = previewSize.width,
                                            viewHeight = previewSize.height
                                        )
                                    )
                                }
                            }
                        )
                    }
            ) {
                // 相機預覽
                // v2.8.6 Phase 2: PreviewView migration with dual-path rendering
                // v2.5.1: Refactored to use Bitmap preview from CameraInputSource
                // v2.0 Phase 2: Pass size callback for accurate composition analysis
                // v2.8.6 Phase 0: A/B Test - Conditionally disable preview
                val previewBitmap by viewModel.previewBitmap.collectAsState()
                if (!CameraViewModel.DEBUG_DISABLE_BITMAP_PREVIEW) {
                    CameraPreview(
                        previewBitmap = previewBitmap,
                        supportsNativePreview = viewModel.supportsNativePreview(),
                        onPreviewViewCreated = { previewView ->
                            // Connect PreviewView to CameraX Preview UseCase
                            viewModel.setPreviewSurfaceProvider(previewView.surfaceProvider)
                        },
                        modifier = Modifier.fillMaxSize(),
                        onSizeChanged = { width, height ->
                            viewModel.updatePreviewSize(width, height)
                        }
                    )
                } else {
                    // Test A: Show black screen instead of bitmap preview
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "A/B Test: Preview Disabled\n(Investigating Graphics Memory)",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // v2.2.2: DetectionOverlay removed - visual clutter, not professional
                // User feedback: Green face boxes and blue object labels are distracting

                // v2.8.6 Phase 0: A/B Test - Conditionally disable overlays (Test B)
                if (!CameraViewModel.DEBUG_DISABLE_OVERLAYS) {
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

                    // v2.7.0 Phase 2 Sprint 3+4: Guideline Overlay (OpenCV-driven)
                    // Shows detected composition guideline lines from Hough Line Transform
                    // Falls back to rule of thirds grid when no lines detected
                    GuidelineOverlay(
                        enabled = FeatureFlags.enableGuidelineOverlay,
                        analysisResult = guidelineResults,
                        modifier = Modifier.fillMaxSize()
                    )
                }

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

                // v2.8.4 Quick Win: FPS performance monitor
                // Only visible in debug builds for performance validation
                FPSOverlay(
                    fps = currentFps,
                    visible = BuildConfig.DEBUG,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                )

                // v2.9.9: Focus indicator overlay
                // v2.9.10: Shows white circle in tracking mode, color change in static mode
                // v2.9.9 Day 1-2: Shows dashed 3-segment arc, yellow square when locked
                state.focusPoint?.let { (x, y) ->
                    FocusIndicator(
                        x = x,
                        y = y,
                        isFocusing = state.isFocusing,
                        focusSuccess = state.focusSuccess,
                        isTracking = state.trackedObjectId != null,  // v2.9.10: Tracking mode flag
                        isLocked = state.isAEAFLocked,  // v2.9.9 Day 1-2: Lock mode flag
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // v2.9.9 Day 1-2: Lock indicator (yellow square frame)
                state.lockPoint?.let { (x, y) ->
                    if (state.isAEAFLocked) {
                        LockIndicator(
                            x = x,
                            y = y,
                            modifier = Modifier.fillMaxSize()
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

                    // 拍照按鈕 (v2.9.8: 添加視覺反饋 - 禁用 + 進度指示器)
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingActionButton(
                            onClick = {
                                // v2.9.8: 只有未拍照時才可點擊（實現禁用效果）
                                if (!state.isCapturing) {
                                    viewModel.onEvent(CameraUiEvent.CapturePhoto)
                                }
                            },
                            modifier = Modifier
                                .size(72.dp)
                                .border(4.dp, Color.White, CircleShape)
                                .alpha(if (state.isCapturing) 0.5f else 1.0f),  // v2.9.8: 拍照中降低透明度
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

                        // v2.9.8: 外圈進度指示器（拍照中顯示）
                        if (state.isCapturing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(84.dp),  // 比按鈕大一圈 (72 + 12)
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        }
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

/**
 * Focus Indicator - Shows animated dashed 3-segment arc at focus point
 * v2.9.9: Tap-to-Focus visual feedback
 * v2.9.10: Traycer dual-mode rendering (tracking vs static)
 * v2.9.9 Day 1-2: Traycer v2 professional dashed arc design (3 × 120°)
 *
 * @param x X coordinate of focus point
 * @param y Y coordinate of focus point
 * @param isFocusing True while focus is in progress
 * @param focusSuccess True if focus succeeded, false if failed, null if still focusing
 * @param isTracking True if in tracking mode (Traycer), false for static focus
 * @param isLocked True if AE/AF is locked (long press)
 * @param modifier Modifier for the canvas
 */
@Composable
private fun FocusIndicator(
    x: Float,
    y: Float,
    isFocusing: Boolean,
    focusSuccess: Boolean?,
    isTracking: Boolean,  // v2.9.10: Tracking mode flag
    isLocked: Boolean = false,  // v2.9.9 Day 1-2: Lock mode flag
    modifier: Modifier = Modifier
) {
    // v2.9.10: Tracking mode - smooth position transition
    val animatedX by animateFloatAsState(
        targetValue = x,
        animationSpec = if (isTracking) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        } else {
            tween(durationMillis = 0)  // Static mode: no animation
        },
        label = "focus_x"
    )

    val animatedY by animateFloatAsState(
        targetValue = y,
        animationSpec = if (isTracking) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        } else {
            tween(durationMillis = 0)
        },
        label = "focus_y"
    )

    // v2.9.10: Tracking mode - no scale animation
    // v2.9.9 Day 1-2: Lock mode - no scale animation
    val scale by animateFloatAsState(
        targetValue = if (isTracking || isLocked) 1.0f else if (isFocusing) 1.2f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "focus_scale"
    )

    // v2.9.10: Tracking mode - fixed alpha 1.0
    // v2.9.9 Day 1-2: Lock mode - fixed alpha 1.0
    val alpha by animateFloatAsState(
        targetValue = if (isTracking || isLocked) 1.0f else if (focusSuccess != null) 0.6f else 1.0f,
        animationSpec = tween(durationMillis = 500),
        label = "focus_alpha"
    )

    // v2.9.9 Day 1-2: Plan B color scheme (white/cyan/yellow)
    val color = when {
        isLocked -> Color(0xFFFFEB3B)  // Yellow - locked (AE/AF lock)
        isTracking -> Color.White  // White - tracking mode
        focusSuccess == true -> Color.White  // White - static success
        focusSuccess == false -> Color(0xFFFF5252)  // Red - failed
        else -> Color(0xFF00E5FF)  // Cyan - focusing (極光藍)
    }

    Canvas(modifier = modifier) {
        val radius = 40.dp.toPx() * scale
        val strokeWidth = 3.dp.toPx()

        // v2.9.9 Day 1-2: Dashed path effect (dash: 8dp, gap: 6dp)
        val dashEffect = PathEffect.dashPathEffect(
            floatArrayOf(8.dp.toPx(), 6.dp.toPx()),
            phase = 0f
        )

        // v2.9.9 Day 1-2: Draw 3 equal arc segments (120° each)
        // Segment 1: 0° to 120°
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(animatedX - radius, animatedY - radius),
            size = Size(radius * 2, radius * 2),
            alpha = alpha,
            style = Stroke(width = strokeWidth, pathEffect = dashEffect)
        )

        // Segment 2: 120° to 240°
        drawArc(
            color = color,
            startAngle = 120f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(animatedX - radius, animatedY - radius),
            size = Size(radius * 2, radius * 2),
            alpha = alpha,
            style = Stroke(width = strokeWidth, pathEffect = dashEffect)
        )

        // Segment 3: 240° to 360°
        drawArc(
            color = color,
            startAngle = 240f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(animatedX - radius, animatedY - radius),
            size = Size(radius * 2, radius * 2),
            alpha = alpha,
            style = Stroke(width = strokeWidth, pathEffect = dashEffect)
        )

        // Draw center dot
        drawCircle(
            color = color,
            radius = 3.dp.toPx(),
            center = Offset(animatedX, animatedY),
            alpha = alpha
        )
    }
}

/**
 * Lock Indicator - Shows yellow dashed square when AE/AF is locked
 * v2.9.9 Day 1-2: Traycer v2 AE/AF Lock visual feedback
 *
 * @param x X coordinate of lock point
 * @param y Y coordinate of lock point
 * @param modifier Modifier for the canvas
 */
@Composable
private fun LockIndicator(
    x: Float,
    y: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val size = 80.dp.toPx()  // Square size
        val strokeWidth = 3.dp.toPx()
        val color = Color(0xFFFFEB3B)  // Yellow

        // v2.9.9 Day 1-2: Dashed path effect (dash: 8dp, gap: 6dp)
        val dashEffect = PathEffect.dashPathEffect(
            floatArrayOf(8.dp.toPx(), 6.dp.toPx()),
            phase = 0f
        )

        // Draw dashed square frame
        drawRect(
            color = color,
            topLeft = Offset(x - size / 2, y - size / 2),
            size = Size(size, size),
            style = Stroke(width = strokeWidth, pathEffect = dashEffect)
        )
    }
}
