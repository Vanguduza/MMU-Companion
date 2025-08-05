package com.aeci.mmucompanion.data.repository

import com.aeci.mmucompanion.data.local.dao.SiteDao
import com.aeci.mmucompanion.data.local.entity.SiteEntity
import com.aeci.mmucompanion.domain.model.Site
import com.aeci.mmucompanion.domain.model.SiteWithStats
import com.aeci.mmucompanion.domain.model.User
import com.aeci.mmucompanion.domain.model.Equipment
import com.aeci.mmucompanion.domain.model.EquipmentGroup
import com.aeci.mmucompanion.domain.repository.SiteRepository
import com.aeci.mmucompanion.domain.repository.SiteFilteringService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SiteRepositoryImpl @Inject constructor(
    private val siteDao: SiteDao
) : SiteRepository {

    override fun getAllSites(): Flow<List<Site>> {
        return siteDao.getAllSites().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllSitesWithStats(): Flow<List<SiteWithStats>> {
        return siteDao.getAllSitesWithStats().map { entities ->
            entities.map { entity ->
                SiteWithStats(
                    site = entity.toDomain(),
                    equipmentCount = 0, // TODO: Implement actual counts when equipment table is linked
                    technicianCount = 0, // TODO: Implement actual counts when user table is linked
                    activeJobCards = 0 // TODO: Implement actual counts when job cards are linked
                )
            }
        }
    }

    override suspend fun getSiteById(id: String): Site? {
        return siteDao.getSiteById(id)?.toDomain()
    }

    override suspend fun getSiteByCode(code: String): Site? {
        return siteDao.getSiteByCode(code)?.toDomain()
    }

    override fun getActiveSites(): Flow<List<Site>> {
        return siteDao.getActiveSites().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getHeadOffice(): Site? {
        return siteDao.getHeadOffice()?.toDomain()
    }

    override suspend fun insertSite(site: Site) {
        try {
            siteDao.insertSiteWithValidation(site.toEntity())
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to create site: ${e.localizedMessage}")
        }
    }

    override suspend fun updateSite(site: Site) {
        try {
            siteDao.updateSiteWithValidation(site.toEntity())
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to update site: ${e.localizedMessage}")
        }
    }

    override suspend fun deleteSite(siteId: String) {
        try {
            // Check if site is head office before deletion
            val site = siteDao.getSiteById(siteId)
            if (site?.isHeadOffice == true) {
                throw IllegalArgumentException("Cannot delete head office")
            }
            
            // TODO: Check for dependencies (equipment, users, etc.)
            // This should be implemented based on your business rules
            
            siteDao.deleteSite(siteId)
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to delete site: ${e.localizedMessage}")
        }
    }

    private fun Site.toEntity(): SiteEntity {
        return SiteEntity(
            id = id,
            name = name,
            code = code,
            address = address,
            city = city,
            province = province,
            country = country,
            postalCode = postalCode,
            contactPerson = contactPerson,
            contactEmail = contactEmail,
            contactPhone = contactPhone,
            isActive = isActive,
            isHeadOffice = isHeadOffice,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun SiteEntity.toDomain(): Site {
        return Site(
            id = id,
            name = name,
            code = code,
            address = address,
            city = city,
            province = province,
            country = country,
            postalCode = postalCode,
            contactPerson = contactPerson,
            contactEmail = contactEmail,
            contactPhone = contactPhone,
            isActive = isActive,
            isHeadOffice = isHeadOffice,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

@Singleton
class SiteFilteringServiceImpl @Inject constructor(
    private val siteRepository: SiteRepository
) : SiteFilteringService {
    
    override suspend fun getEquipmentBySite(siteId: String): Flow<List<Equipment>> {
        // Mock implementation - in real app would query database
        return flowOf(emptyList())
    }
    
    override suspend fun getUsersBySite(siteId: String): Flow<List<User>> {
        // Mock implementation - in real app would query database
        return flowOf(emptyList())
    }
    
    override suspend fun getEquipmentGroupsBySite(siteId: String): Flow<List<EquipmentGroup>> {
        // Mock implementation - in real app would query database
        return flowOf(emptyList())
    }
    
    override suspend fun filterEquipmentByUserSite(userId: String): Flow<List<Equipment>> {
        // Mock implementation - in real app would filter by user's site
        return flowOf(emptyList())
    }
    
    override suspend fun getCurrentUserSite(userId: String): String? {
        // Mock implementation - in real app would get user's current site
        return "site_001"
    }
    
    override suspend fun canUserAccessSite(userId: String, siteId: String): Boolean {
        // Mock implementation - in real app would check user permissions
        return true
    }
}
