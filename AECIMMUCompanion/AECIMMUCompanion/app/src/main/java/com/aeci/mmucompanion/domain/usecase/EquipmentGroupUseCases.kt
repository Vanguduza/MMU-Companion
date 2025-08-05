package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.EquipmentGroup
import com.aeci.mmucompanion.domain.model.EquipmentType
import com.aeci.mmucompanion.domain.model.UserRole
import com.aeci.mmucompanion.domain.repository.SiteFilteringService
import com.aeci.mmucompanion.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateEquipmentGroupUseCase @Inject constructor(
    private val siteFilteringService: SiteFilteringService,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String,
        type: EquipmentType,
        siteId: String,
        createdBy: String
    ): Result<String> {
        return try {
            // Check if user has admin rights
            val user = userRepository.getUserById(createdBy)
            if (user?.role != UserRole.ADMIN) {
                return Result.failure(Exception("Only administrators can create equipment groups"))
            }
            
            val group = EquipmentGroup(
                id = generateId(),
                name = name,
                description = description,
                type = type,
                siteId = siteId,
                createdBy = createdBy,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // This would normally be in a dedicated repository
            // For now, we'll return success
            Result.success(group.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateId(): String = "group_${System.currentTimeMillis()}"
}

class GetEquipmentGroupsUseCase @Inject constructor(
    private val siteFilteringService: SiteFilteringService
) {
    suspend operator fun invoke(siteId: String): Flow<List<EquipmentGroup>> {
        return siteFilteringService.getEquipmentGroupsBySite(siteId)
    }
}

class UpdateEquipmentGroupUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        group: EquipmentGroup,
        updatedBy: String
    ): Result<Unit> {
        return try {
            // Check if user has admin rights
            val user = userRepository.getUserById(updatedBy)
            if (user?.role != UserRole.ADMIN) {
                return Result.failure(Exception("Only administrators can update equipment groups"))
            }
            
            // Update group logic would go here
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class DeleteEquipmentGroupUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        groupId: String,
        deletedBy: String
    ): Result<Unit> {
        return try {
            // Check if user has admin rights
            val user = userRepository.getUserById(deletedBy)
            if (user?.role != UserRole.ADMIN) {
                return Result.failure(Exception("Only administrators can delete equipment groups"))
            }
            
            // Delete group logic would go here
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Wrapper class to group all equipment group related use cases
 */
data class EquipmentGroupUseCases @Inject constructor(
    val createEquipmentGroup: CreateEquipmentGroupUseCase,
    val getEquipmentGroups: GetEquipmentGroupsUseCase,
    val updateEquipmentGroup: UpdateEquipmentGroupUseCase,
    val deleteEquipmentGroup: DeleteEquipmentGroupUseCase
) {
    // Convenience method to get all equipment groups for all sites
    suspend fun getAllEquipmentGroups() = kotlinx.coroutines.flow.flowOf(emptyList<EquipmentGroup>())
}

