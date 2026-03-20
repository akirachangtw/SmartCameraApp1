/**
 * ClassName: SettingsScreen
 * Description: Settings screen for configuring camera overlay visibility
 * Author: Claude Code
 * Date: 2025-12-01
 * Version: 2.1.0
 *
 * Modifications:
 * - 2025-12-01: Initial creation for V1.0 completion (Settings feature)
 */
package com.akirachang.ai.photographer.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import com.akirachang.ai.photographer.presentation.settings.components.ExpandableSettingsCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

/**
 * Settings screen for camera overlay configuration
 *
 * Allows users to toggle visibility of various camera overlays:
 * - Grid overlay (Rule of Thirds)
 * - Composition hint
 * - Detection overlay (bounding boxes)
 * - Level indicator
 * - Directional indicator
 *
 * @param onNavigateBack Callback when back button is pressed
 * @param modifier Modifier for this composable
 * @param viewModel ViewModel managing settings state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isGridEnabled by viewModel.isGridEnabled.collectAsState(initial = true)
    val isCompositionHintEnabled by viewModel.isCompositionHintEnabled.collectAsState(initial = true)
    val isDetectionOverlayEnabled by viewModel.isDetectionOverlayEnabled.collectAsState(initial = true)
    val isLevelIndicatorEnabled by viewModel.isLevelIndicatorEnabled.collectAsState(initial = true)
    val isDirectionalIndicatorEnabled by viewModel.isDirectionalIndicatorEnabled.collectAsState(initial = true)
    val isAutoHDREnabled by viewModel.isAutoHDREnabled.collectAsState(initial = true)
    val isGpsTaggingEnabled by viewModel.isGpsTaggingEnabled.collectAsState(initial = false)  // v2.9.7 Day 1-2: GPS default OFF
    val aspectRatioMode by viewModel.aspectRatioMode.collectAsState(initial = "RATIO_16_9")  // v2.11.0 Phase 2: Aspect ratio

    val scope = rememberCoroutineScope()

    // v2.11.0 Phase 1: GPS permission dialog state
    var showLocationPermissionDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Camera Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ========================================
            // Camera Settings (Fixed - Always Visible)
            // ========================================
            Text(
                text = "Camera Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Core camera capture and tagging options",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Aspect Ratio — single row with 3 options
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AspectRatio,
                        contentDescription = "Aspect Ratio",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Aspect Ratio",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Single row: 4:3 | 16:9
                val aspectRatioOptions = listOf(
                    "RATIO_4_3" to "4:3",
                    "RATIO_16_9" to "16:9"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    aspectRatioOptions.forEach { (mode, label) ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .selectable(
                                    selected = (aspectRatioMode == mode),
                                    onClick = {
                                        scope.launch {
                                            viewModel.setAspectRatioMode(mode)
                                        }
                                    }
                                )
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            RadioButton(
                                selected = (aspectRatioMode == mode),
                                onClick = {
                                    scope.launch {
                                        viewModel.setAspectRatioMode(mode)
                                    }
                                }
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (aspectRatioMode == mode) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Auto HDR Toggle (v2.3.0) - moved to Camera Settings
            SettingsSwitchItem(
                icon = Icons.Default.Star,
                title = "自動 HDR",
                description = "逆光場景自動調整曝光補償",
                checked = isAutoHDREnabled,
                onCheckedChange = { enabled ->
                    scope.launch {
                        viewModel.setAutoHDREnabled(enabled)
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // GPS Tagging Toggle (v2.9.7 Day 1-2, v2.11.0 Phase 1) - moved to Camera Settings
            SettingsSwitchItem(
                icon = Icons.Default.LocationOn,
                title = "GPS 位置標記",
                description = "將拍攝位置（經緯度、海拔）寫入照片 EXIF（需要位置權限，預設關閉保護隱私）",
                checked = isGpsTaggingEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        // Turning ON - trigger permission check
                        showLocationPermissionDialog = true
                    } else {
                        // Turning OFF - directly disable
                        scope.launch {
                            viewModel.setGpsTaggingEnabled(false)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ========================================
            // Overlay Settings (Expandable - Default Expanded)
            // v2.11.1: UI Modernization with ExpandableSettingsCard
            // ========================================
            ExpandableSettingsCard(
                title = "Overlay Settings",
                icon = Icons.Default.Visibility,
                subtitle = "Configure camera preview overlays",
                initialExpanded = true
            ) {
                // Grid Overlay Toggle
                SettingsSwitchItem(
                    icon = Icons.Default.GridOn,
                    title = "Grid Overlay",
                    description = "Show Rule of Thirds composition grid",
                    checked = isGridEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            viewModel.setGridEnabled(enabled)
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Composition Hint Toggle
                SettingsSwitchItem(
                    icon = Icons.Default.Info,
                    title = "Composition Hint",
                    description = "Show composition quality feedback",
                    checked = isCompositionHintEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            viewModel.setCompositionHintEnabled(enabled)
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Detection Overlay Toggle
                SettingsSwitchItem(
                    icon = Icons.Default.Visibility,
                    title = "Detection Boxes",
                    description = "Show face and object detection bounding boxes",
                    checked = isDetectionOverlayEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            viewModel.setDetectionOverlayEnabled(enabled)
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Level Indicator Toggle
                SettingsSwitchItem(
                    icon = Icons.Default.Recycling,
                    title = "Level Indicator",
                    description = "Show horizon level indicator with device tilt",
                    checked = isLevelIndicatorEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            viewModel.setLevelIndicatorEnabled(enabled)
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Directional Indicator Toggle
                SettingsSwitchItem(
                    icon = Icons.Default.Navigation,
                    title = "Directional Arrows",
                    description = "Show directional arrows for composition guidance",
                    checked = isDirectionalIndicatorEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            viewModel.setDirectionalIndicatorEnabled(enabled)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========================================
            // Camera Diagnostics (Expandable - Default Collapsed)
            // v2.11.1: UI Modernization with ExpandableSettingsCard
            // ========================================
            ExpandableSettingsCard(
                title = "Camera Diagnostics",
                icon = Icons.Default.Info,
                subtitle = "Hardware capability detection",
                initialExpanded = false
            ) {
                var diagnosticInfo by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

                // Load diagnostic info initially
                if (diagnosticInfo.isEmpty()) {
                    diagnosticInfo = viewModel.getCameraDiagnosticInfo()
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Device Information
                    Text(
                        text = "Device Information",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    DiagnosticInfoRow("Manufacturer", diagnosticInfo["Manufacturer"] ?: "N/A")
                    DiagnosticInfoRow("Model", diagnosticInfo["Model"] ?: "N/A")
                    DiagnosticInfoRow("Android SDK", diagnosticInfo["SDK"] ?: "N/A")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Detection Results
                    Text(
                        text = "Detection Results",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    DiagnosticInfoRow("Cache Version", diagnosticInfo["Cache Version"] ?: "N/A")
                    DiagnosticInfoRow("App Version", diagnosticInfo["App Version"] ?: "N/A")
                    DiagnosticInfoRow("Supports 800x600", diagnosticInfo["Supports 800x600"] ?: "N/A")
                    DiagnosticInfoRow("Target Resolution", diagnosticInfo["Target Resolution"] ?: "N/A")
                    DiagnosticInfoRow("Filter Max", diagnosticInfo["Filter Max"] ?: "N/A")

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Re-detect Button
                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.reDetectCameraCapabilities()
                                    // Reload diagnostic info after re-detection
                                    kotlinx.coroutines.delay(500)
                                    diagnosticInfo = viewModel.getCameraDiagnosticInfo()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Re-detect")
                        }

                        // Clear Cache Button
                        OutlinedButton(
                            onClick = {
                                viewModel.clearCameraCapabilityCache()
                                diagnosticInfo = viewModel.getCameraDiagnosticInfo()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear Cache")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reset button
            TextButton(
                onClick = {
                    scope.launch {
                        viewModel.resetToDefaults()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset to Defaults")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // v2.11.0 Phase 1: GPS permission handler
        if (showLocationPermissionDialog) {
            LocationPermissionHandler(
                onPermissionGranted = {
                    // Permission granted - enable GPS tagging
                    scope.launch {
                        viewModel.setGpsTaggingEnabled(true)
                    }
                    showLocationPermissionDialog = false
                },
                onPermissionDenied = {
                    // Permission denied - keep toggle OFF
                    showLocationPermissionDialog = false
                }
            ) {}
        }
    }
}


/**
 * Diagnostic information row
 * v2.9.5: Display key-value pair for diagnostic info
 *
 * @param label Information label
 * @param value Information value
 */
@Composable
private fun DiagnosticInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Reusable switch item for settings
 *
 * @param icon Icon to display
 * @param title Setting title
 * @param description Setting description
 * @param checked Current state
 * @param onCheckedChange Callback when state changes
 */
@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 16.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
