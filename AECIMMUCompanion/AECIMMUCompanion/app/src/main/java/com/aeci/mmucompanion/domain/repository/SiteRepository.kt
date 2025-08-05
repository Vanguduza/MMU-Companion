package com.aeci.mmucompanion.domain.repository

import com.aeci.mmucompanion.domain.model.Site
import com.aeci.mmucompanion.domain.model.SiteWithStats
import com.aeci.mmucompanion.domain.model.Equipment
import com.aeci.mmucompanion.domain.model.User
import com.aeci.mmucompanion.domain.model.EquipmentGroup
import kotlinx.coroutines.flow.Flow

interface SiteRepository {
    fun getAllSites(): Flow<List<Site>>
    fun getAllSitesWithStats(): Flow<List<SiteWithStats>>
    suspend fun getSiteById(id: String): Site?
    suspend fun getSiteByCode(code: String): Site?
    fun getActiveSites(): Flow<List<Site>>
    suspend fun getHeadOffice(): Site?
    suspend fun insertSite(site: Site)
    suspend fun updateSite(site: Site)
    suspend fun deleteSite(siteId: String)
}

interface SiteFilteringService {
    suspend fun getEquipmentBySite(siteId: String): Flow<List<Equipment>>
    suspend fun getUsersBySite(siteId: String): Flow<List<User>>
    suspend fun getEquipmentGroupsBySite(siteId: String): Flow<List<EquipmentGroup>>
    suspend fun filterEquipmentByUserSite(userId: String): Flow<List<Equipment>>
    suspend fun getCurrentUserSite(userId: String): String?
    suspend fun canUserAccessSite(userId: String, siteId: String): Boolean
}
