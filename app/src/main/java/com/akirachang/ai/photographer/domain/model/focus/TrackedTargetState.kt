package com.akirachang.ai.photographer.domain.model.focus

/**
 * Output of TargetTracker: the current set of tracked targets with primary selection.
 * v2.12.0-15 / §17.11.5
 */
data class TrackedTargetState(
    val allTargets: List<FocusTarget>,
    val primaryTarget: FocusTarget?,
    val recentlyLostTarget: FocusTarget?,
)
