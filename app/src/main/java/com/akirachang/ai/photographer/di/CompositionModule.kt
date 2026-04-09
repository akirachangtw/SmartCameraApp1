package com.akirachang.ai.photographer.di

import com.akirachang.ai.photographer.domain.composition.CompositionRule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds

@Module
@InstallIn(SingletonComponent::class)
abstract class CompositionModule {

    @Multibinds
    abstract fun bindCompositionRules(): Set<@JvmSuppressWildcards CompositionRule>
}
