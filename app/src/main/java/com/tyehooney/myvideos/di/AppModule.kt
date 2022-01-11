package com.tyehooney.myvideos.di

import com.tyehooney.myvideos.data.MyVideosRepositoryImpl
import com.tyehooney.myvideos.domain.repository.MyVideosRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface AppModule {

    @Singleton
    @Binds
    fun provideMyVideosRepository(repository: MyVideosRepositoryImpl): MyVideosRepository
}