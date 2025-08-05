package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.Site
import com.aeci.mmucompanion.domain.model.SiteWithStats
import com.aeci.mmucompanion.domain.repository.SiteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllSitesUseCase @Inject constructor(
    private val repository: SiteRepository
) {
    operator fun invoke(): Flow<List<Site>> = repository.getAllSites()
}

class GetAllSitesWithStatsUseCase @Inject constructor(
    private val repository: SiteRepository
) {
    operator fun invoke(): Flow<List<SiteWithStats>> = repository.getAllSitesWithStats()
}

class GetSiteByIdUseCase @Inject constructor(
    private val repository: SiteRepository
) {
    suspend operator fun invoke(id: String): Site? = repository.getSiteById(id)
}

class GetSiteByCodeUseCase @Inject constructor(
    private val repository: SiteRepository
) {
    suspend operator fun invoke(code: String): Site? = repository.getSiteByCode(code)
}

class GetActiveSitesUseCase @Inject constructor(
    private val repository: SiteRepository
) {
    operator fun invoke(): Flow<List<Site>> = repository.getActiveSites()
}

class GetHeadOfficeUseCase @Inject constructor(
    private val repository: SiteRepository
) {
    suspend operator fun invoke(): Site? = repository.getHeadOffice()
}

class AddSiteUseCase @Inject constructor(
    private val repository: SiteRepository
) {
    suspend operator fun invoke(site: Site) = repository.insertSite(site)
}

class UpdateSiteUseCase @Inject constructor(
    private val repository: SiteRepository
) {
    suspend operator fun invoke(site: Site) = repository.updateSite(site)
}

class DeleteSiteUseCase @Inject constructor(
    private val repository: SiteRepository
) {
    suspend operator fun invoke(siteId: String) = repository.deleteSite(siteId)
}

class ValidateSiteCodeUseCase @Inject constructor(
    private val repository: SiteRepository
) {
    suspend operator fun invoke(code: String, excludeId: String? = null): Boolean {
        val existingSite = repository.getSiteByCode(code)
        return existingSite == null || existingSite.id == excludeId
    }
}

data class SiteUseCases(
    val getAllSites: GetAllSitesUseCase,
    val getAllSitesWithStats: GetAllSitesWithStatsUseCase,
    val getSiteById: GetSiteByIdUseCase,
    val getSiteByCode: GetSiteByCodeUseCase,
    val getActiveSites: GetActiveSitesUseCase,
    val getHeadOffice: GetHeadOfficeUseCase,
    val addSite: AddSiteUseCase,
    val updateSite: UpdateSiteUseCase,
    val deleteSite: DeleteSiteUseCase,
    val validateSiteCode: ValidateSiteCodeUseCase
)

