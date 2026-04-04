package com.espimsystems.visionlab.core.camera.di

import com.espimsystems.visionlab.core.camera.CameraFrameSource
import com.espimsystems.visionlab.core.common.domain.repository.FrameSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CameraModule {

    @Binds
    @Singleton
    abstract fun bindFrameSource(impl: CameraFrameSource): FrameSource
}
