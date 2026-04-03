package com.espimsystems.visionlab.core.cpp.di

import com.espimsystems.visionlab.core.common.domain.repository.ImagePreprocessor
import com.espimsystems.visionlab.core.cpp.NativeImageProcessor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CppModule {

    @Binds
    @Singleton
    abstract fun bindImagePreprocessor(impl: NativeImageProcessor): ImagePreprocessor
}
