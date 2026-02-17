/**
 * ClassName: CameraScreen
 * Description: Main camera screen with ML Kit detection and composition guidance
 * Author: Claude Code
 * Date: 2025-11-17
 * Version: 2.11.1
 *
 * Modifications:
 * - 2025-11-17: Added ML Kit detection result text overlay
 * - 2025-11-18: Replaced text overlay with bounding box overlay
 * - 2025-11-23: Simplified to use MlKitAnalyzer with VIEW_REFERENCED coordinates (v1.4.0)
 * - 2025-11-25: Added smart composition guide with Rule of Thirds grid (v1.5.0)
 * - 2025-11-30: Added directional indicator arrows for composition guidance (v2.0 Phase 4)
 * - 2025-12-01: Added LevelIndicator overlay with sensor integration (v2.1.0)
 * - 2025-12-08: Added CompositionGuideOverlay for visual composition guidance (v2.4.0)
 * - 2026-02-16: Fixed mechanical clamp animation timing: 500ms → 150ms (v2.11.1 D-008)
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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.PathEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.akirachang.ai.photographer.presentation.camera.components.CameraPreview
import com.akirachang.ai.photographer.presentation.camera.components.PermissionRequestScreen
import com.akirachang.ai.photographer.presentation.camera.components.PreviewViewportContainer
import com.akirachang.ai.photographer.presentation.camera.components.overlay.CompositionGuideOverlay
import com.akirachang.ai.photographer.presentation.camera.components.overlay.CompositionHint
// v2.2.2: Removed DetectionOverlay and DirectionalIndicator - visual clutter
import com.akirachang.ai.photographer.presentation.camera.components.overlay.FPSOverlay
import com.akirachang.ai.photographer.presentation.camera.components.overlay.GridOverlay
import com.akirachang.ai.photographer.presentation.camera.components.overlay.GuidelineOverlay
import com.akirachang.ai.photographer.presentation.camera.components.overlay.HDRWarningOverlay
import com.akirachang.ai.photographer.presentation.camera.components.overlay.LevelIndicator
import com.akirachang.ai.photographer.presentation.camera.model.CompositionState
import com.akirachang.ai.photographer.presentation.camera.model.AspectRatioMode
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
            // v2.x.x Phase 5: Also used for Optical Flow coordinate mapping
            var previewSize by remember { mutableStateOf(IntSize.Zero) }

            // v2.11.1 Phase 1: contentBounds calculation moved to PreviewViewportContainer below
            // (Replaced LaunchedEffect manual calculation with PreviewViewportContainer)

            // v2.11.1 Phase 1+2: PreviewViewportContainer calculates accurate contentBounds,
            // replaces manual LaunchedEffect calculation; pointerInput has letterbox filtering
            PreviewViewportContainer(
                targetAspectRatio = state.aspectRatioMode,
                onContentBoundsChanged = { bounds ->
                    viewModel.updateContentBounds(bounds)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { size ->
                        // Get full screen size BEFORE padding (used for optical flow SetPreviewSize)
                        previewSize = size
                        // v2.x.x Phase 5: Notify ViewModel of preview size for optical flow
                        if (size.width > 0 && size.height > 0) {
                            viewModel.onEvent(CameraUiEvent.SetPreviewSize(size.width, size.height))
                        }
                    }
                    .padding(paddingValues)
                    .pointerInput(Unit, state.contentBounds) {
                        detectTapGestures(
                            onTap = { offset ->
                                // v2.11.1 Phase 2: Filter taps in letterbox area
                                val bounds = state.contentBounds
                                if (!bounds.isEmpty && !bounds.contains(offset.x, offset.y)) {
                                    return@detectTapGestures  // Ignore letterbox taps
                                }
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
                                // v2.11.1 Phase 2: Filter long presses in letterbox area
                                val bounds = state.contentBounds
                                if (!bounds.isEmpty && !bounds.contains(offset.x, offset.y)) {
                                    return@detectTapGestures  // Ignore letterbox long presses
                                }
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
            ) { contentBounds ->
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
                            // v2.8.6 Phase 2: Connect PreviewView to CameraX Preview UseCase
                            viewModel.setPreviewSurfaceProvider(previewView.surfaceProvider)
                            // v2.11.1 P0-1: Pass PreviewView reference for accurate coordinate transformation
                            viewModel.setPreviewView(previewView)
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
                    // v2.11.1 Phase 1: Use contentBounds from PreviewViewportContainer (local)
                    // Phase 3: contentBounds.isEmpty = true initially → Grid hidden until calculated
                    GridOverlay(
                        modifier = Modifier.fillMaxSize(),
                        visible = isGridEnabled && !contentBounds.isEmpty,
                        contentBounds = contentBounds.takeIf { !it.isEmpty }
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
                // v2.x.x Phase 1: Supports both ML Kit tracking and Optical Flow tracking
                state.focusPoint?.let { (x, y) ->
                    FocusIndicator(
                        x = x,
                        y = y,
                        isFocusing = state.isFocusing,
                        focusSuccess = state.focusSuccess,
                        isTracking = state.trackedObjectId != null || state.trackedPointActive,  // v2.9.10/v2.x.x: ML Kit + Optical Flow tracking
                        isLocked = state.isAEAFLocked,  // v2.9.9 Day 1-2: Lock mode flag
                        isFading = state.focusIndicatorFading,  // v2.9.9 Day 1-2 Optimize: Fade-out animation
                        fadeStartTime = state.focusIndicatorFadeStartTime,  // v2.9.9 Day 1-2 Optimize: Fade start time
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // v2.9.9 Day 1-2 Optimize Round 3: Lock indicator removed (use FocusIndicator yellow color instead)

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

            // v2.10.0 Phase 3: Photo EXIF information bottom sheet
            val exifInfo = state.currentPhotoExifInfo
            if (state.showPhotoInfo && exifInfo != null) {
                com.akirachang.ai.photographer.presentation.camera.components.PhotoInfoBottomSheet(
                    exifInfo = exifInfo,
                    onDismiss = { viewModel.onEvent(CameraUiEvent.DismissPhotoInfo) }
                )
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
 * Rectangle Focus Indicator with Corner Brackets (v2.11.1)
 * - Searching: Rectangle only, Cyan neon glow
 * - Success: Rectangle + Corner brackets, Cyan, mechanical clamp animation (350ms: 150ms shrink + 200ms bounce)
 * - Dwell: Rectangle + Corner brackets, Cyan Alpha 0.4, stroke 1.5dp
 * - Locked: Rectangle + Corner brackets, Cyber Yellow neon glow
 *
 * Visual Languages (4 items only - D-007, D-008):
 * 1. Mechanical clamp animation (350ms)
 * 2. Fade-out Alpha + Stroke thinning (500ms)
 * 3. Color change (Cyan ↔ Cyber Yellow)
 * 4. Static Glow effect (dual-layer rendering, alpha=0.3)
 */
@Composable
private fun FocusIndicator(
    x: Float,
    y: Float,
    isFocusing: Boolean,
    focusSuccess: Boolean?,
    isTracking: Boolean,
    isLocked: Boolean = false,
    isFading: Boolean = false,
    fadeStartTime: Long = 0L,
    modifier: Modifier = Modifier
) {
    // Position tracking with smooth animation
    val animatedX by animateFloatAsState(
        targetValue = x,
        animationSpec = if (isTracking) {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
        } else {
            tween(0)
        },
        label = "focus_x"
    )

    val animatedY by animateFloatAsState(
        targetValue = y,
        animationSpec = if (isTracking) {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
        } else {
            tween(0)
        },
        label = "focus_y"
    )

    // Overshoot animation (shrink 10% → bounce back)
    val scale = remember { Animatable(1.0f) }

    LaunchedEffect(focusSuccess, isFocusing, isTracking, isLocked) {
        when {
            focusSuccess == true && !isFocusing && !isTracking && !isLocked -> {
                // Focus success: Mechanical clamp animation (v2.11.1: 350ms total = 150ms + 200ms)
                // Phase 2: Shrink to 0.9 (150ms)
                scale.animateTo(0.9f, tween(150, easing = FastOutLinearInEasing))
                // Phase 3: Bounce back to 1.0 (Spring ~200ms)
                scale.animateTo(1.0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh))
            }
            isTracking || isLocked -> scale.snapTo(1.0f)
            isFocusing -> scale.animateTo(1.2f, tween(300))
            else -> scale.snapTo(1.0f)
        }
    }

    // Stroke width (Dwell: 2.5dp → 1.5dp)
    val animatedStrokeWidth by animateFloatAsState(
        targetValue = if (isFading) 1.5f else 2.5f,
        animationSpec = tween(500),
        label = "focus_stroke"
    )

    // Alpha
    val alpha by animateFloatAsState(
        targetValue = when {
            isTracking || isLocked -> 1.0f
            isFading -> 0.4f
            focusSuccess == false -> 0.6f
            else -> 1.0f
        },
        animationSpec = tween(500),
        label = "focus_alpha"
    )

    // Color: Cyan neon glow or Cyber Yellow (locked)
    val color = when {
        isLocked -> Color(0xFFFFD700)  // Cyber Yellow (#FFD700)
        else -> Color(0xFF00E5FF)      // Cyan neon glow (#00E5FF)
    }

    // Corner brackets appear only on success/locked
    val showCornerBrackets = focusSuccess == true || isLocked

    Canvas(modifier = modifier) {
        val size = 48.dp.toPx() * scale.value
        val halfSize = size / 2
        val strokeWidth = animatedStrokeWidth.dp.toPx()

        // Glow Layer (Bottom) - Neon effect
        drawRect(
            color = color,
            topLeft = Offset(animatedX - halfSize, animatedY - halfSize),
            size = Size(size, size),
            alpha = alpha * 0.3f,
            style = Stroke(width = strokeWidth * 2.4f)
        )

        // Core Layer (Top)
        drawRect(
            color = color,
            topLeft = Offset(animatedX - halfSize, animatedY - halfSize),
            size = Size(size, size),
            alpha = alpha,
            style = Stroke(width = strokeWidth)
        )

        // Corner Brackets (Conditional)
        if (showCornerBrackets) {
            val bracketLength = 12.dp.toPx()
            // Glow brackets
            drawCornerBrackets(
                centerX = animatedX,
                centerY = animatedY,
                halfSize = halfSize,
                bracketLength = bracketLength,
                strokeWidth = strokeWidth * 2.4f,
                color = color,
                alpha = alpha * 0.3f
            )
            // Core brackets
            drawCornerBrackets(
                centerX = animatedX,
                centerY = animatedY,
                halfSize = halfSize,
                bracketLength = bracketLength,
                strokeWidth = strokeWidth,
                color = color,
                alpha = alpha
            )
        }
    }
}

/**
 * Draw L-shaped corner brackets (v2.11.0)
 */
private fun DrawScope.drawCornerBrackets(
    centerX: Float,
    centerY: Float,
    halfSize: Float,
    bracketLength: Float,
    strokeWidth: Float,
    color: Color,
    alpha: Float
) {
    // Top-left corner
    drawLine(
        color = color,
        start = Offset(centerX - halfSize, centerY - halfSize),
        end = Offset(centerX - halfSize + bracketLength, centerY - halfSize),
        strokeWidth = strokeWidth,
        alpha = alpha
    )
    drawLine(
        color = color,
        start = Offset(centerX - halfSize, centerY - halfSize),
        end = Offset(centerX - halfSize, centerY - halfSize + bracketLength),
        strokeWidth = strokeWidth,
        alpha = alpha
    )

    // Top-right corner
    drawLine(
        color = color,
        start = Offset(centerX + halfSize, centerY - halfSize),
        end = Offset(centerX + halfSize - bracketLength, centerY - halfSize),
        strokeWidth = strokeWidth,
        alpha = alpha
    )
    drawLine(
        color = color,
        start = Offset(centerX + halfSize, centerY - halfSize),
        end = Offset(centerX + halfSize, centerY - halfSize + bracketLength),
        strokeWidth = strokeWidth,
        alpha = alpha
    )

    // Bottom-left corner
    drawLine(
        color = color,
        start = Offset(centerX - halfSize, centerY + halfSize),
        end = Offset(centerX - halfSize + bracketLength, centerY + halfSize),
        strokeWidth = strokeWidth,
        alpha = alpha
    )
    drawLine(
        color = color,
        start = Offset(centerX - halfSize, centerY + halfSize),
        end = Offset(centerX - halfSize, centerY + halfSize - bracketLength),
        strokeWidth = strokeWidth,
        alpha = alpha
    )

    // Bottom-right corner
    drawLine(
        color = color,
        start = Offset(centerX + halfSize, centerY + halfSize),
        end = Offset(centerX + halfSize - bracketLength, centerY + halfSize),
        strokeWidth = strokeWidth,
        alpha = alpha
    )
    drawLine(
        color = color,
        start = Offset(centerX + halfSize, centerY + halfSize),
        end = Offset(centerX + halfSize, centerY + halfSize - bracketLength),
        strokeWidth = strokeWidth,
        alpha = alpha
    )
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
        val size = 62.dp.toPx()  // v2.9.9 Day 1-2 Optimize Round 2: 70dp → 62dp (1.1x new circle diameter 28dp)
        val strokeWidth = 2.5.dp.toPx()  // v2.9.9 Day 1-2 Optimize: Match FocusIndicator strokeWidth
        val color = Color(0xFFFFC107)  // Yellow (#FFC107) - Professional standard

        // v2.9.9 Day 1-2: Dashed path effect (dash: 16dp, gap: 12dp) - Round 2: Increased gap for clarity
        val dashEffect = PathEffect.dashPathEffect(
            floatArrayOf(16.dp.toPx(), 12.dp.toPx()),  // v2.9.9 Day 1-2 Optimize Round 2: 12dp/10dp → 16dp/12dp
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

/**
 * Calculate content bounds for FIT_CENTER layout
 *
 * v2.11.1 P0-3: Helper function to calculate preview content bounds
 * excluding FIT_CENTER letterbox (black bars).
 *
 * @param screenWidth Screen width in pixels
 * @param screenHeight Screen height in pixels
 * @param targetAspectRatio Target aspect ratio mode
 * @return RectF representing content bounds
 */
private fun calculateContentBoundsForAspectRatio(
    screenWidth: Int,
    screenHeight: Int,
    targetAspectRatio: AspectRatioMode
): android.graphics.RectF {
    val screenAspect = screenWidth.toFloat() / screenHeight.toFloat()
    val rational = targetAspectRatio.toRational()

    // v2.11.1 P0-3 FIX: Handle portrait mode (height > width)
    // AspectRatioMode defines landscape ratios (e.g., 16:9 = width:height)
    // For portrait screens, we need to invert the ratio
    val isPortrait = screenHeight > screenWidth
    val targetAspect = if (isPortrait) {
        // Invert ratio for portrait: 16:9 becomes 9:16
        rational.denominator.toFloat() / rational.numerator.toFloat()
    } else {
        rational.numerator.toFloat() / rational.denominator.toFloat()
    }

    val (contentWidth, contentHeight) = if (screenAspect > targetAspect) {
        // Screen is wider than target → letterbox on left/right
        val width = screenHeight * targetAspect
        val height = screenHeight.toFloat()
        Pair(width, height)
    } else {
        // Screen is taller than target → letterbox on top/bottom
        val width = screenWidth.toFloat()
        val height = screenWidth / targetAspect
        Pair(width, height)
    }

    // Center content within screen
    val left = (screenWidth - contentWidth) / 2f
    val top = (screenHeight - contentHeight) / 2f
    val right = left + contentWidth
    val bottom = top + contentHeight

    return android.graphics.RectF(left, top, right, bottom)
}
