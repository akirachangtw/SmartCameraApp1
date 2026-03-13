package com.akirachang.ai.photographer.presentation.camera.focus

import com.akirachang.ai.photographer.domain.model.focus.FocusStateInput
import com.akirachang.ai.photographer.domain.model.focus.FocusUiState

/**
 * Domain interface for focus state machine.
 * v2.12.0-15 / §17.11.9
 *
 * Rules:
 * - reduce() must be pure: same input → same output.
 * - Must NOT call any hardware API.
 * - Must NOT hold mutable state (stateless reducer).
 * - SystemEvent.LENS_SWITCHED and LIFECYCLE_STOPPED must always reset to FocusUiState.EMPTY.
 */
interface FocusStateMachine {
    /**
     * Given the current [input], compute the new [FocusUiState].
     * Callers are responsible for maintaining previous state if needed.
     */
    fun reduce(input: FocusStateInput): FocusUiState
}