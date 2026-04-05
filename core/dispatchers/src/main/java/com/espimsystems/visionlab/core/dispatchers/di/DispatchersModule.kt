package com.espimsystems.visionlab.core.dispatchers.di

import com.espimsystems.visionlab.core.dispatchers.AppDispatchers
import com.espimsystems.visionlab.core.dispatchers.AppDispatchersImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DispatchersModule {

    @Binds
    @Singleton
    abstract fun bindAppDispatchers(impl: AppDispatchersImpl): AppDispatchers
}