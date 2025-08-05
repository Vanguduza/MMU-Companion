package com.aeci.mmucompanion.di

import com.aeci.mmucompanion.data.local.dao.SiteDao
import com.aeci.mmucompanion.data.repository.SiteRepositoryImpl
import com.aeci.mmucompanion.data.repository.SiteFilteringServiceImpl
import com.aeci.mmucompanion.domain.repository.SiteRepository
import com.aeci.mmucompanion.domain.repository.SiteFilteringService
import com.aeci.mmucompanion.domain.usecase.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SiteModule {
    
    @Binds
    @Singleton
    abstract fun bindSiteRepository(
        siteRepositoryImpl: SiteRepositoryImpl
    ): SiteRepository
    
    @Binds
    @Singleton
    abstract fun bindSiteFilteringService(
        siteFilteringServiceImpl: SiteFilteringServiceImpl
    ): SiteFilteringService

    companion object {
        @Provides
        @Singleton
        fun provideSiteUseCases(
            repository: SiteRepository
        ): SiteUseCases {
            return SiteUseCases(
                getAllSites = GetAllSitesUseCase(repository),
                getAllSitesWithStats = GetAllSitesWithStatsUseCase(repository),
                getSiteById = GetSiteByIdUseCase(repository),
                getSiteByCode = GetSiteByCodeUseCase(repository),
                getActiveSites = GetActiveSitesUseCase(repository),
                getHeadOffice = GetHeadOfficeUseCase(repository),
                addSite = AddSiteUseCase(repository),
                updateSite = UpdateSiteUseCase(repository),
                deleteSite = DeleteSiteUseCase(repository),
                validateSiteCode = ValidateSiteCodeUseCase(repository)
            )
        }
    }
}
