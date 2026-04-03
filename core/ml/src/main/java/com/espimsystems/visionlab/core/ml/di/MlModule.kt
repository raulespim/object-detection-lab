package com.espimsystems.visionlab.core.ml.di

import com.espimsystems.visionlab.core.common.domain.repository.DetectorEngine
import com.espimsystems.visionlab.core.ml.TFLiteDetectorEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MlModule {

    @Binds
    @Singleton
    abstract fun bindDetectorEngine(impl: TFLiteDetectorEngine): DetectorEngine
}
