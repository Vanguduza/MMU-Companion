package com.aeci.mmucompanion.data.local.dao

import androidx.room.*
import com.aeci.mmucompanion.data.local.entity.SiteEntity
import com.aeci.mmucompanion.domain.model.SiteWithStats
import kotlinx.coroutines.flow.Flow

@Dao
interface SiteDao {
    
    @Query("SELECT * FROM sites ORDER BY isHeadOffice DESC, name ASC")
    fun getAllSites(): Flow<List<SiteEntity>>
    
    @Query("SELECT * FROM sites ORDER BY isHeadOffice DESC, name ASC")
    fun getAllSitesWithStats(): Flow<List<SiteEntity>>
    
    @Query("SELECT * FROM sites WHERE id = :id")
    suspend fun getSiteById(id: String): SiteEntity?
    
    @Query("SELECT * FROM sites WHERE code = :code")
    suspend fun getSiteByCode(code: String): SiteEntity?
    
    @Query("SELECT * FROM sites WHERE isActive = 1 ORDER BY isHeadOffice DESC, name ASC")
    fun getActiveSites(): Flow<List<SiteEntity>>
    
    @Query("SELECT * FROM sites WHERE isHeadOffice = 1 AND isActive = 1 LIMIT 1")
    suspend fun getHeadOffice(): SiteEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSite(site: SiteEntity)
    
    @Update
    suspend fun updateSite(site: SiteEntity)
    
    @Query("DELETE FROM sites WHERE id = :siteId")
    suspend fun deleteSite(siteId: String)
    
    @Query("SELECT COUNT(*) FROM sites WHERE code = :code AND id != :excludeId")
    suspend fun countSitesWithCode(code: String, excludeId: String = ""): Int
    
    @Query("SELECT COUNT(*) FROM sites WHERE isHeadOffice = 1 AND id != :excludeId")
    suspend fun countHeadOffices(excludeId: String = ""): Int
    
    @Transaction
    suspend fun insertSiteWithValidation(site: SiteEntity) {
        // Validate unique code
        val codeExists = countSitesWithCode(site.code, site.id) > 0
        if (codeExists) {
            throw IllegalArgumentException("Site code '${site.code}' already exists")
        }
        
        // If this is a head office, ensure no other head office exists
        if (site.isHeadOffice) {
            val headOfficeExists = countHeadOffices(site.id) > 0
            if (headOfficeExists) {
                throw IllegalArgumentException("A head office already exists")
            }
        }
        
        insertSite(site)
    }
    
    @Transaction
    suspend fun updateSiteWithValidation(site: SiteEntity) {
        // Validate unique code
        val codeExists = countSitesWithCode(site.code, site.id) > 0
        if (codeExists) {
            throw IllegalArgumentException("Site code '${site.code}' already exists")
        }
        
        // If this is a head office, ensure no other head office exists
        if (site.isHeadOffice) {
            val headOfficeExists = countHeadOffices(site.id) > 0
            if (headOfficeExists) {
                throw IllegalArgumentException("A head office already exists")
            }
        }
        
        updateSite(site)
    }
}
