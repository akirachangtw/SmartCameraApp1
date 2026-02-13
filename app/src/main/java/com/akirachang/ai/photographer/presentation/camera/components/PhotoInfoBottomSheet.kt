/**
 * ClassName: PhotoInfoBottomSheet
 * Description: Material3 BottomSheet for displaying photo EXIF information
 * Author: Claude Code
 * Date: 2026-02-08
 * Version: 2.11.0
 *
 * Modifications:
 * - 2026-02-08: Initial creation for EXIF display feature (v2.11.0 Phase 3)
 */
package com.akirachang.ai.photographer.presentation.camera.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akirachang.ai.photographer.domain.model.ExifInfo

/**
 * Material3 ModalBottomSheet for displaying photo EXIF metadata
 *
 * Displays comprehensive photo information in a categorized format:
 * - Basic Info: filename, size, resolution, timestamp
 * - GPS Location: latitude, longitude, altitude (if available)
 * - Camera Info: make, model, software, orientation
 *
 * Includes a "Copy" button to copy all information to clipboard.
 *
 * @param exifInfo EXIF metadata to display
 * @param onDismiss Callback when bottom sheet is dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoInfoBottomSheet(
    exifInfo: ExifInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.8f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "照片資訊",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(
                        onClick = {
                            copyToClipboard(context, exifInfo)
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, "複製")
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "關閉")
                    }
                }
            }

            // Basic Information
            InfoSection(title = "基本資訊") {
                InfoRow("檔名", exifInfo.fileName)
                InfoRow("檔案大小", exifInfo.fileSizeFormatted)
                InfoRow("解析度", exifInfo.resolution)
                exifInfo.dateTime?.let {
                    InfoRow("拍攝時間", it)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // GPS Information (only if available)
            if (exifInfo.hasGpsInfo) {
                InfoSection(title = "GPS 位置") {
                    exifInfo.formattedGpsInfo?.let { gpsInfo ->
                        Text(
                            text = gpsInfo,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Camera Information
            InfoSection(title = "相機資訊") {
                InfoRow("製造商", exifInfo.make)
                InfoRow("機型", exifInfo.model)
                InfoRow("軟體版本", exifInfo.software)
                InfoRow("方向", exifInfo.orientationDescription)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Section container with title and content
 */
@Composable
private fun InfoSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        content()
    }
}

/**
 * Label-value row for displaying information
 */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Copy EXIF information to system clipboard
 */
private fun copyToClipboard(context: Context, exifInfo: ExifInfo) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val text = buildString {
        appendLine("檔名: ${exifInfo.fileName}")
        appendLine("檔案大小: ${exifInfo.fileSizeFormatted}")
        appendLine("解析度: ${exifInfo.resolution}")
        exifInfo.dateTime?.let { appendLine("拍攝時間: $it") }

        if (exifInfo.hasGpsInfo) {
            appendLine("\nGPS 位置:")
            exifInfo.formattedGpsInfo?.let { appendLine(it) }
        }

        appendLine("\n相機資訊:")
        appendLine("製造商: ${exifInfo.make}")
        appendLine("機型: ${exifInfo.model}")
        appendLine("軟體版本: ${exifInfo.software}")
        appendLine("方向: ${exifInfo.orientationDescription}")
    }

    val clip = ClipData.newPlainText("Photo EXIF Info", text)
    clipboard.setPrimaryClip(clip)
}
