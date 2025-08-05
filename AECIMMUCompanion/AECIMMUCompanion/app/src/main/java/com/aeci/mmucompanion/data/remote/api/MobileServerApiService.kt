package com.aeci.mmucompanion.data.remote.api

import com.aeci.mmucompanion.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface MobileServerApiService {
    
    // Authentication
    @POST("api/auth/login")
    suspend fun login(@Body request: MobileLoginRequest): Response<MobileLoginResponse>
    
    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: MobileRefreshTokenRequest): Response<MobileLoginResponse>
    
    // Server health and status
    @GET("health")
    suspend fun getHealth(): Response<ServerHealthResponse>
    
    @GET("api/server/status")
    suspend fun getServerStatus(): Response<ServerStatusResponse>
    
    // User management
    @GET("api/users")
    suspend fun getUsers(): Response<List<UserDto>>
    
    @POST("api/users")
    suspend fun createUser(@Body user: CreateUserRequest): Response<CreateUserResponse>
    
    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body user: UpdateUserRequest): Response<UserDto>
    
    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>
    
    // Equipment management
    @GET("api/equipment")
    suspend fun getEquipment(): Response<List<EquipmentDto>>
    
    @POST("api/equipment")
    suspend fun createEquipment(@Body equipment: CreateEquipmentRequest): Response<CreateEquipmentResponse>
    
    @PUT("api/equipment/{id}")
    suspend fun updateEquipment(@Path("id") id: String, @Body equipment: UpdateEquipmentRequest): Response<EquipmentDto>
    
    @DELETE("api/equipment/{id}")
    suspend fun deleteEquipment(@Path("id") id: String): Response<Unit>
    
    // Forms management
    @GET("api/forms")
    suspend fun getForms(
        @Query("status") status: String? = null,
        @Query("formType") formType: String? = null,
        @Query("equipmentId") equipmentId: String? = null
    ): Response<List<FormDto>>
    
    @POST("api/forms")
    suspend fun createForm(@Body form: CreateFormRequest): Response<CreateFormResponse>
    
    @PUT("api/forms/{id}")
    suspend fun updateForm(@Path("id") id: String, @Body form: UpdateFormRequest): Response<FormDto>
    
    @DELETE("api/forms/{id}")
    suspend fun deleteForm(@Path("id") id: String): Response<Unit>
    
    // File upload
    @Multipart
    @POST("api/upload")
    suspend fun uploadFile(@Part file: okhttp3.MultipartBody.Part): Response<FileUploadResponse>
    
    // Data synchronization
    @GET("api/sync/status")
    suspend fun getSyncStatus(@Query("since") since: String? = null): Response<SyncStatusResponse>
    
    @POST("api/sync/push")
    suspend fun pushSyncData(@Body data: SyncPushRequest): Response<SyncPushResponse>
    
    @GET("api/sync/pull")
    suspend fun pullSyncData(@Query("since") since: String? = null): Response<SyncPullResponse>
    
    // Backup and export
    @POST("api/server/backup")
    suspend fun createBackup(): Response<BackupResponse>
    
    @GET("api/export/forms")
    suspend fun exportForms(
        @Query("format") format: String = "pdf",
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null
    ): Response<ExportResponse>
}

// DTOs for mobile server communication
data class MobileLoginRequest(
    val username: String,
    val password: String
)

data class MobileLoginResponse(
    val token: String,
    val user: UserDto
)

data class MobileRefreshTokenRequest(
    val token: String
)

data class UserDto(
    val id: Int,
    val username: String,
    val fullName: String,
    val role: String,
    val department: String?,
    val phone: String?,
    val createdAt: String,
    val lastLogin: String?,
    val isActive: Boolean
)

data class CreateUserRequest(
    val username: String,
    val password: String,
    val fullName: String,
    val role: String,
    val department: String?,
    val phone: String?
)

data class CreateUserResponse(
    val id: Int,
    val message: String
)

data class UpdateUserRequest(
    val fullName: String?,
    val role: String?,
    val department: String?,
    val phone: String?,
    val isActive: Boolean?
)

data class EquipmentDto(
    val id: String,
    val name: String,
    val type: String,
    val location: String?,
    val status: String,
    val lastMaintenance: String?,
    val nextMaintenance: String?,
    val createdAt: String,
    val updatedAt: String
)

data class CreateEquipmentRequest(
    val name: String,
    val type: String,
    val location: String?,
    val status: String?
)

data class CreateEquipmentResponse(
    val id: String,
    val message: String
)

data class UpdateEquipmentRequest(
    val name: String?,
    val type: String?,
    val location: String?,
    val status: String?,
    val lastMaintenance: String?,
    val nextMaintenance: String?
)

data class FormDto(
    val id: String,
    val formType: String,
    val title: String,
    val createdBy: Int,
    val assignedTo: Int?,
    val equipmentId: String?,
    val status: String,
    val formData: String?,
    val attachments: String?,
    val createdAt: String,
    val updatedAt: String,
    val completedAt: String?,
    val createdByName: String?,
    val assignedToName: String?,
    val equipmentName: String?
)

data class CreateFormRequest(
    val formType: String,
    val title: String,
    val assignedTo: Int?,
    val equipmentId: String?,
    val formData: String?
)

data class CreateFormResponse(
    val id: String,
    val message: String
)

data class UpdateFormRequest(
    val title: String?,
    val assignedTo: Int?,
    val equipmentId: String?,
    val status: String?,
    val formData: String?,
    val completedAt: String?
)

data class FileUploadResponse(
    val filename: String,
    val originalName: String,
    val size: Long,
    val path: String
)

data class SyncStatusResponse(
    val lastSync: String,
    val changes: List<SyncLogEntry>
)

data class SyncLogEntry(
    val id: Int,
    val tableName: String,
    val operation: String,
    val recordId: String,
    val data: String?,
    val timestamp: String
)

data class SyncPushRequest(
    val table: String,
    val operation: String,
    val recordId: String,
    val data: Any
)

data class SyncPushResponse(
    val success: Boolean,
    val message: String
)

data class SyncPullResponse(
    val data: Map<String, List<Any>>,
    val lastSync: String
)

data class BackupResponse(
    val message: String,
    val file: String
)

data class ExportResponse(
    val filename: String,
    val downloadUrl: String,
    val format: String,
    val recordCount: Int
)

data class ServerHealthResponse(
    val status: String,
    val timestamp: String,
    val uptime: Double,
    val version: String
)

data class ServerStatusResponse(
    val status: String,
    val port: Int,
    val timestamp: String,
    val uptime: Double,
    val memory: MemoryUsage,
    val platform: String,
    val nodeVersion: String
)

data class MemoryUsage(
    val rss: Long,
    val heapTotal: Long,
    val heapUsed: Long,
    val external: Long
) 