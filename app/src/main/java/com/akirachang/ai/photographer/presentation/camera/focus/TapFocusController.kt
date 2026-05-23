package com.akirachang.ai.photographer.presentation.camera.focus

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akirachang.ai.photographer.domain.config.AmbientConstants
import com.akirachang.ai.photographer.domain.repository.CameraRepository
import com.akirachang.ai.photographer.domain.sensor.OrientationSensorManager
import com.akirachang.ai.photographer.presentation.camera.components.TapFocusState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.sqrt
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * TapFocusController — P05-T3.
 *
 * Owns the tap-focus state machine: Tap event → state transition → CameraX AF → result write-back.
 * State is exposed via [tapFocusState] for TapFocusRing rendering in CameraScreen.
 *
 * Front-camera guard: [onLongPress] rejects Locked state when isFrontCamera == true
 * (Rule §3.2: fixed-focus lens, AE/AF lock has no physical meaning).
 *
 * All CameraX AF calls route through [CameraRepository] (no direct CameraControl access).
 */
@HiltViewModel
class TapFocusController @Inject constructor(
    private val cameraRepository: CameraRepository,
    private val orientationSensorManager: OrientationSensorManager,
) : ViewModel() {

    companion object {
        private const val TAG = "TapFocusController"
    }

    private val _tapFocusState = MutableStateFlow<TapFocusState>(TapFocusState.Idle)
    val tapFocusState: StateFlow<TapFocusState> = _tapFocusState.asStateFlow()

    // Incremented on every new tap intent — stale coroutines check this before writing state
    private var sessionId = 0

    private var flashJob: Job? = null
    private var driftJob: Job? = null
    private var failedResetJob: Job? = null

    fun onTap(x: Float, y: Float, viewWidth: Int, viewHeight: Int) {
        val newCenter = Offset(x, y)
        val current = _tapFocusState.value

        cancelActiveJobs()

        when (current) {
            is TapFocusState.Locked -> {
                // Unlock AE/AF before switching to new focus point
                viewModelScope.launch {
                    cameraRepository.cancelFocusAndMetering()
                    Log.d(TAG, "[Re-tap] AE/AF unlocked")
                }
                flashAndFocus(current.center, newCenter, x, y, viewWidth, viewHeight)
            }
            // SSOT A.4: re-tap from Focusing/Focused → Failed flash at old position → new Focusing
            is TapFocusState.Focusing -> {
                flashAndFocus(current.center, newCenter, x, y, viewWidth, viewHeight)
            }
            is TapFocusState.Focused -> {
                flashAndFocus(current.center, newCenter, x, y, viewWidth, viewHeight)
            }
            else -> {
                val mySession = ++sessionId
                doFocus(newCenter, x, y, viewWidth, viewHeight, mySession)
            }
        }
    }

    fun onLongPress(x: Float, y: Float, viewWidth: Int, viewHeight: Int, isFrontCamera: Boolean) {
        if (isFrontCamera) {
            Log.v(TAG, "[LongPress] Locked rejected — front camera (Rule §3.2: fixed-focus)")
            return
        }
        val center = Offset(x, y)
        cancelActiveJobs()
        val mySession = ++sessionId
        _tapFocusState.value = TapFocusState.Locked(center)
        viewModelScope.launch {
            val result = cameraRepository.lockAEAF(x, y, viewWidth, viewHeight)
            Log.i(TAG, "[LongPress] lockAEAF result=${result.isSuccess} session=$mySession")
        }
    }

    private fun flashAndFocus(
        oldCenter: Offset,
        newCenter: Offset,
        x: Float,
        y: Float,
        viewWidth: Int,
        viewHeight: Int,
    ) {
        val mySession = ++sessionId
        _tapFocusState.value = TapFocusState.Failed(oldCenter)
        flashJob = viewModelScope.launch {
            delay(AmbientConstants.FOCUS_RETAP_FLASH_MS)
            if (sessionId == mySession) {
                doFocus(newCenter, x, y, viewWidth, viewHeight, mySession)
            }
        }
    }

    private fun doFocus(center: Offset, x: Float, y: Float, viewWidth: Int, viewHeight: Int, mySession: Int) {
        _tapFocusState.value = TapFocusState.Focusing(center)
        viewModelScope.launch {
            val result = withTimeoutOrNull(AmbientConstants.FOCUS_AF_TIMEOUT_MS) {
                try {
                    cameraRepository.focusOnPoint(x, y, viewWidth, viewHeight)
                } catch (e: Exception) {
                    Log.w(TAG, "[AF] focusOnPoint exception: ${e.message}")
                    Result.failure(e)
                }
            }
            if (sessionId != mySession) return@launch

            if (result != null && result.isSuccess) {
                _tapFocusState.value = TapFocusState.Focused(center)
                Log.i(TAG, "[AF] Focused at $center session=$mySession")
                startDriftMonitoring(center, mySession)
            } else {
                val reason = if (result == null) "timeout(${AmbientConstants.FOCUS_AF_TIMEOUT_MS}ms)" else "hal_failed"
                Log.w(TAG, "[AF] Failed ($reason) at $center session=$mySession")
                transitionToFailed(center, mySession)
            }
        }
    }

    private fun startDriftMonitoring(center: Offset, mySession: Int) {
        driftJob = viewModelScope.launch {
            try {
                var baseRoll: Float? = null
                var basePitch: Float? = null
                orientationSensorManager.startOrientationTracking().first { orientation ->
                    if (sessionId != mySession || _tapFocusState.value !is TapFocusState.Focused) {
                        return@first true // session changed or state lost — stop monitoring
                    }
                    if (baseRoll == null) {
                        baseRoll = orientation.roll
                        basePitch = orientation.pitch
                        return@first false
                    }
                    val dRoll = orientation.roll - baseRoll!!
                    val dPitch = orientation.pitch - basePitch!!
                    sqrt(dRoll * dRoll + dPitch * dPitch) >= AmbientConstants.FOCUS_DRIFT_THRESHOLD_DEG
                }
                // Drift detected: run fadeout then reset to Idle
                if (sessionId == mySession && _tapFocusState.value is TapFocusState.Focused) {
                    Log.i(TAG, "[Drift] ≥${AmbientConstants.FOCUS_DRIFT_THRESHOLD_DEG}° — fadeout ${AmbientConstants.FOCUS_FADEOUT_MS}ms")
                    delay(AmbientConstants.FOCUS_FADEOUT_MS)
                    if (sessionId == mySession) {
                        _tapFocusState.value = TapFocusState.Idle
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "[Drift] monitoring ended: ${e.message}")
            }
        }
    }

    private fun transitionToFailed(center: Offset, mySession: Int) {
        _tapFocusState.value = TapFocusState.Failed(center)
        failedResetJob = viewModelScope.launch {
            delay(AmbientConstants.FOCUS_FAILED_MS + AmbientConstants.FOCUS_FADEOUT_MS)
            if (sessionId == mySession && _tapFocusState.value is TapFocusState.Failed) {
                _tapFocusState.value = TapFocusState.Idle
            }
        }
    }

    private fun cancelActiveJobs() {
        flashJob?.cancel()
        driftJob?.cancel()
        failedResetJob?.cancel()
    }
}
