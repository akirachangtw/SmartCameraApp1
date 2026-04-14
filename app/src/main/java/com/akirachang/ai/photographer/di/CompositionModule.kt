package com.akirachang.ai.photographer.di

import com.akirachang.ai.photographer.domain.composition.CompositionRule
import com.akirachang.ai.photographer.domain.composition.rules.EyeLineRule
import com.akirachang.ai.photographer.domain.composition.rules.GazeRule
import com.akirachang.ai.photographer.domain.composition.rules.HeadroomRule
import com.akirachang.ai.photographer.domain.composition.rules.ThirdsRule
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds

@Module
@InstallIn(SingletonComponent::class)
abstract class CompositionModule {

    @Multibinds
    abstract fun bindCompositionRules(): Set<@JvmSuppressWildcards CompositionRule>

    @Binds
    @IntoSet
    abstract fun bindThirdsRule(rule: ThirdsRule): CompositionRule

    @Binds
    @IntoSet
    abstract fun bindHeadroomRule(rule: HeadroomRule): CompositionRule

    @Binds
    @IntoSet
    abstract fun bindEyeLineRule(rule: EyeLineRule): CompositionRule

    @Binds
    @IntoSet
    abstract fun bindGazeRule(rule: GazeRule): CompositionRule
}
