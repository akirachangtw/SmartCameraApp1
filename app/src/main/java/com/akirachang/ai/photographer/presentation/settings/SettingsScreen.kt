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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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

    val scope = rememberCoroutineScope()

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
        ) {
            // Section: Overlay Settings
            Text(
                text = "Overlay Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Configure which overlays to display on the camera preview",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

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

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Auto HDR Toggle (v2.3.0)
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
        }
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
