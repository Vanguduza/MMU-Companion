package com.aeci.mmucompanion.di

import com.aeci.mmucompanion.data.repository.BlastReportRepository
import com.aeci.mmucompanion.data.repository.impl.BlastReportRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BlastReportModule {

    @Binds
    @Singleton
    abstract fun bindBlastReportRepository(
        blastReportRepositoryImpl: BlastReportRepositoryImpl
    ): BlastReportRepository
}
