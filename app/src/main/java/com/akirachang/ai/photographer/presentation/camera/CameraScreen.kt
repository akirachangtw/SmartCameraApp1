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
 * - 2026-03-15: Removed FocusIndicator (legacy), replaced with FocusOverlay (V2 pipeline) (v2.12.0-16)
 * - 2026-03-18: Replaced FocusOverlay with FocusIndicatorOverlay (SPEC impl, local recomposition defense) (v2.12.0-19)
 */
package com.akirachang.ai.photographer.presentation.camera

import android.Manifest
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.ui.res.stringResource
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import com.akirachang.ai.photographer.presentation.camera.components.BottomControlBar
import com.akirachang.ai.photographer.presentation.camera.components.PermissionRequestScreen
import com.akirachang.ai.photographer.presentation.camera.components.PreviewViewportContainer
import com.akirachang.ai.photographer.presentation.camera.components.overlay.CompositionGuideOverlay
import com.akirachang.ai.photographer.presentation.camera.components.overlay.GridOverlay
import com.akirachang.ai.photographer.presentation.camera.components.overlay.HDRWarningOverlay
import com.akirachang.ai.photographer.presentation.camera.components.overlay.LevelIndicator
import com.akirachang.ai.photographer.presentation.camera.components.HaloGuidanceLayer
import com.akirachang.ai.photographer.presentation.camera.model.CompositionState
import com.akirachang.ai.photographer.presentation.camera.model.AspectRatioMode
import com.akirachang.ai.photographer.R
import com.akirachang.ai.photographer.domain.camera.CameraMode
import com.akirachang.ai.photographer.domain.config.AmbientConstants
import com.akirachang.ai.photographer.BuildConfig
import com.akirachang.ai.photographer.domain.model.CameraLens
import com.akirachang.ai.photographer.presentation.camera.components.TapFocusRing
import com.akirachang.ai.photographer.presentation.camera.components.TapFocusState
import com.akirachang.ai.photographer.presentation.camera.focus.TapFocusController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

// D4-T4 / SV-D4-T4-FIX-01: Testability probes for shutter button section.
internal const val TAG_SHUTTER_BREATHING_RING = "ShutterBreathingRing"
internal const val TAG_SHUTTER_PROGRESS_CIRCLE = "ShutterProgressCircle"

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
    val isLevelIndicatorEnabled by viewModel.isLevelIndicatorEnabled.collectAsState(initial = true)
    // D7-B2-T3: PTF onboarding flag — false on first launch; true after user taps "Got it".
    // Rule 14: collectAsState at composable top before any conditional logic.
    val isPtfOnboarded by viewModel.isPtfOnboarded.collectAsState(initial = true)

    val tapFocusController: TapFocusController = hiltViewModel()
    val tapFocusState by tapFocusController.tapFocusState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // v2.6.1: Bind camera to lifecycle for LiveCameraSource
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        viewModel.bindCameraToLifecycle(lifecycleOwner)
        viewModel.onEvent(CameraUiEvent.StartAnalysis)
    }

    // v2.11.4-05: Auto-unlock AE/AF when app goes to background (screen lock or home button).
    // Matches Pixel 9a built-in camera behavior: screen lock clears AE/AF lock indicator.
    // ON_STOP fires when Activity is no longer visible (screen off / task switch).
    // ON_PAUSE would be too aggressive (fires on any focus loss, e.g. notification shade).
    // [P08-F4-FIX-REPAIR] AI-6: ON_START → StartAnalysis (resume); ON_STOP → StopAnalysis.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.onEvent(CameraUiEvent.StartAnalysis)
                Lifecycle.Event.ON_STOP -> {
                    viewModel.onEvent(CameraUiEvent.StopAnalysis)
                    viewModel.onEvent(CameraUiEvent.UnlockAEAF)
                    tapFocusController.resetToIdle()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
        // D4-T2: Extract isAligned once — passed down to shutter button (UDF).
        // Extreme-left check guards against null compositionResult before first detection frame.
        val isAligned = state.compositionResult?.isAllAligned == true

        // D7-B2-FIX: PTF active flag — drives UI minimalism (hides guidance overlays).
        val isPtfActive = state.cameraMode is CameraMode.PassToFriend

        Scaffold(
            modifier = modifier,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            // Track preview size for Tap-to-Focus and Optical Flow coordinate mapping
            var previewSize by remember { mutableStateOf(IntSize.Zero) }
            val scrimHeightDp = with(LocalDensity.current) { AmbientConstants.SCRIM_HEIGHT_PX.toDp() }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // ── Viewfinder block (664dp / 78.7%) ──────────────────────────
                Box(modifier = Modifier.fillMaxWidth().height(664.dp)) {
                    PreviewViewportContainer(
                        targetAspectRatio = state.aspectRatioMode,
                        onContentBoundsChanged = { bounds ->
                            viewModel.updateContentBounds(bounds)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .onSizeChanged { size -> previewSize = size }
                            .pointerInput(Unit, state.contentBounds) {
                                detectTapGestures(
                                    onTap = { offset ->
                                        val bounds = state.contentBounds
                                        if (!bounds.isEmpty && !bounds.contains(offset.x, offset.y)) {
                                            return@detectTapGestures
                                        }
                                        if (previewSize.width > 0 && previewSize.height > 0) {
                                            tapFocusController.onTap(
                                                x = offset.x,
                                                y = offset.y,
                                                viewWidth = previewSize.width,
                                                viewHeight = previewSize.height,
                                                isFrontCamera = state.currentCamera == CameraLens.FRONT,
                                            )
                                        }
                                    },
                                    onLongPress = { offset ->
                                        val bounds = state.contentBounds
                                        if (!bounds.isEmpty && !bounds.contains(offset.x, offset.y)) {
                                            return@detectTapGestures
                                        }
                                        if (previewSize.width > 0 && previewSize.height > 0) {
                                            tapFocusController.onLongPress(
                                                x = offset.x,
                                                y = offset.y,
                                                viewWidth = previewSize.width,
                                                viewHeight = previewSize.height,
                                                isFrontCamera = state.currentCamera == CameraLens.FRONT,
                                            )
                                        }
                                    }
                                )
                            }
                    ) { _ ->
                        val previewBitmap by viewModel.previewBitmap.collectAsState()
                        CameraPreview(
                            previewBitmap = previewBitmap,
                            supportsNativePreview = viewModel.supportsNativePreview(),
                            onPreviewViewCreated = { previewView ->
                                viewModel.setPreviewSurfaceProvider(previewView.surfaceProvider)
                                viewModel.setPreviewView(previewView)
                            },
                            modifier = Modifier.fillMaxSize(),
                            onSizeChanged = { width, height ->
                                viewModel.updatePreviewSize(width, height)
                            }
                        )

                        HaloGuidanceLayer(
                            faces = state.detectedFaces,
                            compositionResult = state.compositionResult,
                            isPtfMode = isPtfActive,
                            ptfAlignmentResult = state.ptfAlignmentResult,
                            modifier = Modifier.fillMaxSize(),
                        )

                        if (!isPtfActive) {
                            val guideData = when (val currentState = compositionState) {
                                is CompositionState.Good -> currentState.guideData
                                is CompositionState.NeedsAdjustment -> currentState.guideData
                                else -> null
                            }
                            CompositionGuideOverlay(
                                guideData = guideData,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        HDRWarningOverlay(
                            lightingState = lightingState,
                            hdrMode = state.hdrMode,
                            modifier = Modifier.fillMaxSize()
                        )

                        GridOverlay(
                            modifier = Modifier.fillMaxSize(),
                            visible = isGridEnabled && !isPtfActive,
                            // P08-F2 (CSO 裁決): null → grid 覆蓋全畫面，FILL_CENTER preview 填滿全螢幕
                            contentBounds = null
                        )

                        // Top scrim — below FocusIndicatorOverlay so focus ring stays visible
                        // Z-order: above camera overlays, below FocusIndicator + controls
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(scrimHeightDp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.22f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .align(Alignment.TopStart)
                        )

                        if (isLevelIndicatorEnabled) {
                            orientationState?.let { orientation ->
                                LevelIndicator(
                                    roll = orientation.roll,
                                    visible = true,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 16.dp)
                                )
                            }
                        }


                        // P05-T3: TapFocusRing wired — replaces FocusIndicatorOverlay.
                        // Idle state renders nothing (early return in TapFocusRing).
                        val isTapFocusReady = true
                        if (isTapFocusReady) {
                            TapFocusRing(
                                state = tapFocusState,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        // Floating Top Bar — Layout Spec v2.2 §3.2 (Layer 6, above scrim)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row {
                                IconButton(
                                    onClick = { viewModel.onEvent(CameraUiEvent.ToggleFlash) },
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Icon(
                                        imageVector = when (state.flashMode) {
                                            ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                                            ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                                            else -> Icons.Default.FlashOff
                                        },
                                        contentDescription = stringResource(R.string.flash_cd),
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.onEvent(CameraUiEvent.ToggleHDR) },
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    val hdrCd = stringResource(R.string.hdr_cd)
                                    Text(
                                        text = stringResource(R.string.hdr_label),
                                        color = when (state.hdrMode) {
                                            com.akirachang.ai.photographer.presentation.camera.model.HDRMode.ON -> Color.White.copy(alpha = 0.85f)
                                            com.akirachang.ai.photographer.presentation.camera.model.HDRMode.OFF -> Color.White.copy(alpha = 0.40f)
                                            com.akirachang.ai.photographer.presentation.camera.model.HDRMode.AUTO -> Color.White.copy(alpha = 0.65f)
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.semantics { contentDescription = hdrCd },
                                    )
                                }
                                // Reserved slot — future Lens Smudge indicator (48dp touch target)
                                Spacer(modifier = Modifier.size(48.dp))
                            }
                            Row {
                                IconButton(
                                    onClick = onNavigateToSettings,
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = stringResource(R.string.settings_cd),
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                                IconButton(
                                    onClick = { },
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.Help,
                                        contentDescription = stringResource(R.string.help_cd),
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = isPtfActive && !isPtfOnboarded,
                            enter = androidx.compose.animation.fadeIn() +
                                    androidx.compose.animation.slideInVertically(initialOffsetY = { it }),
                            exit = androidx.compose.animation.fadeOut() +
                                   androidx.compose.animation.slideOutVertically(targetOffsetY = { it }),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .background(
                                        color = Color.Black.copy(alpha = 0.72f),
                                        shape = MaterialTheme.shapes.medium,
                                    )
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.ptf_onboarding_message),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                )
                                androidx.compose.foundation.layout.Spacer(
                                    modifier = Modifier.size(10.dp)
                                )
                                androidx.compose.material3.TextButton(
                                    onClick = {
                                        viewModel.onEvent(CameraUiEvent.DismissPtfOnboarding)
                                    }
                                ) {
                                    Text(
                                        text = stringResource(R.string.ptf_onboarding_got_it),
                                        color = Color(0xFF50C864),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                    )
                                }
                            }
                        }
                    }
                }

                // Separator — viewfinder / control band boundary α0.05 (1 physical pixel)
                Canvas(modifier = Modifier.fillMaxWidth().height((1f / LocalDensity.current.density).dp)) {
                    drawRect(color = Color.White.copy(alpha = 0.05f))
                }

                // ── Control Band (remaining height) ───────────────────────────
                BottomControlBar(
                    cameraMode = state.cameraMode,
                    isAligned = isAligned,
                    isCapturing = state.isCapturing,
                    onTogglePill = {
                        val nextMode = if (state.cameraMode is CameraMode.PassToFriend) {
                            CameraMode.Normal
                        } else {
                            CameraMode.PassToFriend
                        }
                        viewModel.onEvent(CameraUiEvent.SetCameraMode(nextMode))
                    },
                    onCapture = { viewModel.onEvent(CameraUiEvent.CapturePhoto) },
                    onFlipCamera = { viewModel.onEvent(CameraUiEvent.SwitchCamera) },
                    onExitPtf = { viewModel.onEvent(CameraUiEvent.SetCameraMode(CameraMode.Normal)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
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
 * Stateless shutter button section — extracted for UI testability (SV-D4-T4-FIX-01).
 *
 * Contains:
 * - Green breathing outer ring ([TAG_SHUTTER_BREATHING_RING]) — visible only when
 *   [isAligned] AND NOT [isCapturing] (FIX-01 mutex, FIX-02 lifecycle scope).
 * - FAB shutter button — animates from 72dp (Normal) to 80dp (PassToFriend) on mode switch.
 * - Circular progress indicator ([TAG_SHUTTER_PROGRESS_CIRCLE]) — visible only when [isCapturing].
 *
 * @param isAligned    True when compositionResult.isAllAligned == true.
 * @param isCapturing  True while photo capture is in-flight.
 * @param cameraMode   Current [CameraMode] — drives shutter size animation.
 * @param onCapture    Invoked on FAB click when not capturing.
 * @param modifier     Modifier passed to the root [Box].
 */
@Composable
internal fun ShutterButtonSection(
    isAligned: Boolean,
    isCapturing: Boolean,
    cameraMode: CameraMode,
    onCapture: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Rule 14: all animateXxx / remember calls must be at composable top — before any if/when.
    val isPtfActive = cameraMode is CameraMode.PassToFriend
    val shutterSize by animateDpAsState(
        targetValue = if (isPtfActive) 80.dp else 72.dp,
        animationSpec = tween(durationMillis = AmbientConstants.TRANSITION_FAST_MS.toInt()),
        label = "shutter_size",
    )
    val ringBaseSize by animateDpAsState(
        targetValue = if (isPtfActive) 94.dp else 84.dp,
        animationSpec = tween(durationMillis = AmbientConstants.TRANSITION_FAST_MS.toInt()),
        label = "ring_base_size",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        // D4-T2: Green breathing outer ring — only when aligned AND not capturing.
        // SV-D4-T2-FIX-01: isCapturing takes absolute priority over isAligned.
        // SV-D4-T2-FIX-02: InfiniteTransition scoped inside condition — clock only ticks when rendered.
        if (isAligned && !isCapturing) {
            val shutterInfiniteTransition = rememberInfiniteTransition(label = "ShutterRing")
            val shutterRingAlpha by shutterInfiniteTransition.animateFloat(
                initialValue = 0.10f,
                targetValue = 0.18f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = AmbientConstants.BREATHE_CYCLE_MS.toInt(), easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "shutter_ring_alpha",
            )
            val shutterRingScale by shutterInfiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = AmbientConstants.BREATHE_CYCLE_MS.toInt(), easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "shutter_ring_scale",
            )
            Canvas(
                modifier = Modifier
                    .requiredSize((72 * shutterRingScale).dp)
                    .testTag(TAG_SHUTTER_BREATHING_RING),
            ) {
                drawCircle(
                    color = Color(0xFF50C864).copy(alpha = shutterRingAlpha),
                    radius = size.minDimension / 2f,
                    style = Stroke(width = 6.dp.toPx()),
                )
            }
        }

        FloatingActionButton(
            onClick = {
                // v2.9.8: 只有未拍照時才可點擊（實現禁用效果）
                if (!isCapturing) {
                    onCapture()
                }
            },
            modifier = Modifier
                .size(shutterSize)
                .border(4.dp, Color.White.copy(alpha = 0.30f), CircleShape)
                .alpha(if (isCapturing) 0.5f else 1.0f),
            shape = CircleShape,
            containerColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "拍照",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // v2.9.8: 外圈進度指示器（拍照中顯示）
        // requiredSize: ignore parent 80dp height constraint so ring stays circular
        // (BottomControlBar shutter zone = 80dp; ringBaseSize = 84/94dp overflows without this)
        if (isCapturing) {
            CircularProgressIndicator(
                modifier = Modifier
                    .requiredSize(ringBaseSize)
                    .testTag(TAG_SHUTTER_PROGRESS_CIRCLE),
                color = Color(0xFF50C864).copy(alpha = 0.80f),
                strokeWidth = 3.dp
            )
        }
    }
}
