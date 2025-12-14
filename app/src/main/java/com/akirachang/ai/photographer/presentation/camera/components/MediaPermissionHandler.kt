package com.akirachang.ai.photographer.presentation.camera.components

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.akirachang.ai.photographer.util.PermissionUtils

// Extension properties for PermissionStatus
@OptIn(ExperimentalPermissionsApi::class)
private val PermissionStatus.isGranted: Boolean
    get() = this is PermissionStatus.Granted

@OptIn(ExperimentalPermissionsApi::class)
private val PermissionStatus.shouldShowRationale: Boolean
    get() = (this as? PermissionStatus.Denied)?.shouldShowRationale == true

/**
 * 處理媒體權限請求的 Composable
 * 支援 Android 14+ 的部分照片存取 (Selected Photos Access)
 *
 * 使用範例：
 * ```
 * MediaPermissionHandler(
 *     onPermissionsGranted = { hasFullAccess, hasPartialAccess ->
 *         if (hasFullAccess) {
 *             // 顯示所有照片
 *         } else if (hasPartialAccess) {
 *             // 只顯示用戶選擇的照片
 *         }
 *     }
 * ) { hasFullAccess, hasPartialAccess, permissionState ->
 *     // 您的 UI 內容
 *     if (hasFullAccess || hasPartialAccess) {
 *         // 顯示照片庫
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MediaPermissionHandler(
    onPermissionsGranted: ((hasFullAccess: Boolean, hasPartialAccess: Boolean) -> Unit)? = null,
    content: @Composable (
        hasFullAccess: Boolean,
        hasPartialAccess: Boolean,
        permissionState: MultiplePermissionsState
    ) -> Unit
) {
    val mediaPermissions = PermissionUtils.getRequiredMediaPermissions()
    val mediaPermissionState = rememberMultiplePermissionsState(mediaPermissions)

    // 計算權限狀態
    val permissionMap = mediaPermissionState.permissions.associate { permissionState ->
        permissionState.permission to permissionState.status.isGranted
    }

    val hasFullAccess = PermissionUtils.hasFullMediaAccess(permissionMap)
    val hasPartialAccess = PermissionUtils.hasPartialMediaAccess(permissionMap)

    // 當權限狀態改變時通知
    LaunchedEffect(hasFullAccess, hasPartialAccess) {
        if (hasFullAccess || hasPartialAccess) {
            onPermissionsGranted?.invoke(hasFullAccess, hasPartialAccess)
        }
    }

    when {
        hasFullAccess || hasPartialAccess -> {
            content(hasFullAccess, hasPartialAccess, mediaPermissionState)
        }
        mediaPermissionState.permissions.any { it.status.shouldShowRationale } -> {
            MediaPermissionRationale(
                onRequestPermission = { mediaPermissionState.launchMultiplePermissionRequest() }
            )
        }
        else -> {
            MediaPermissionRequest(
                onRequestPermission = { mediaPermissionState.launchMultiplePermissionRequest() }
            )
        }
    }
}

@Composable
private fun MediaPermissionRequest(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "需要媒體庫存取權限",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                "此功能需要存取您的照片和影片。\n您可以選擇允許存取所有照片，或僅選擇部分照片。"
            } else {
                "此功能需要存取您的照片和影片。"
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("授予權限")
        }
    }
}

@Composable
private fun MediaPermissionRationale(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "需要媒體庫存取權限",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "此應用需要存取您的照片和影片才能顯示圖庫。\n請在設定中授予媒體庫存取權限。",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("再次請求權限")
        }
    }
}

/**
 * 顯示目前的媒體存取狀態（用於除錯或通知用戶）
 */
@Composable
fun MediaAccessStatusText(
    hasFullAccess: Boolean,
    hasPartialAccess: Boolean
) {
    val statusText = when {
        hasFullAccess -> "完整媒體存取權限"
        hasPartialAccess && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
            "部分照片存取權限（僅限選擇的照片）"
        else -> "無媒體存取權限"
    }

    Text(
        text = statusText,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
