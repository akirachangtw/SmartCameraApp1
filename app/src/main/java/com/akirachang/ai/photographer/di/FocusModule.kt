package com.akirachang.ai.photographer.di

import com.akirachang.ai.photographer.data.focus.control.CameraXFocusController
import com.akirachang.ai.photographer.data.focus.geometry.PreviewViewGeometryMapper
import com.akirachang.ai.photographer.data.focus.policy.DefaultEyeAfPolicyResolver
import com.akirachang.ai.photographer.data.focus.tracking.DefaultTargetTracker
import com.akirachang.ai.photographer.domain.focus.control.FocusController
import com.akirachang.ai.photographer.domain.focus.geometry.GeometryMapper
import com.akirachang.ai.photographer.domain.focus.policy.EyeAfPolicyResolver
import com.akirachang.ai.photographer.domain.focus.tracking.TargetTracker
import com.akirachang.ai.photographer.domain.repository.CameraRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

/**
 * DI module for v2.12.0-15 new focus pipeline components.
 * v2.12.0-15 SV-S5-Final: Establishes interface→implementation bindings for ViewModelComponent.
 *
 * Scope: ViewModelComponent — each ViewModel instance gets its own pipeline components.
 * Note: CameraViewModel currently uses manual instantiation (Step 5b);
 *       this module enables future injection and validates the DI graph.
 */
@Module
@InstallIn(ViewModelComponent::class)
object FocusModule {

    @Provides
    fun provideGeometryMapper(): GeometryMapper = PreviewViewGeometryMapper()

    @Provides
    fun provideTargetTracker(): TargetTracker = DefaultTargetTracker(
        iouThreshold = 0.5f,
        targetTtlMs = 300L,
    )

    @Provides
    fun provideEyeAfPolicyResolver(): EyeAfPolicyResolver = DefaultEyeAfPolicyResolver()

    @Provides
    fun provideFocusController(cameraRepository: CameraRepository): FocusController =
        CameraXFocusController(
            getCameraControl = { cameraRepository.getCameraControl() },
            getMeteringPointFactory = { cameraRepository.getMeteringPointFactory() },
        )
}
