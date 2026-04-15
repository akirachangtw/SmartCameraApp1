package com.akirachang.ai.photographer.presentation.camera.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * HaloIndicator — Cueshot v3.3 Ambient UI placeholder (retained for future Eye-AF integration).
 *
 * Circular Halo for: manual focus, PTF mode, minimalist whitespace confirmation.
 * Unified circle shape per SSOT — no square variants.
 * Update rate: 10fps (ML detection cadence), decoupled from AF hardware calls.
 *
 * Note: Composition guidance Halo (GazeRule + EyeLineRule) is implemented in
 * [com.akirachang.ai.photographer.presentation.camera.components.HaloGuidanceLayer] (D6-B2-T2).
 */
@Composable
fun HaloIndicator(
    modifier: Modifier = Modifier,
) {
    // Retained shell — Eye-AF Halo implementation deferred to post-D6 roadmap.
}
