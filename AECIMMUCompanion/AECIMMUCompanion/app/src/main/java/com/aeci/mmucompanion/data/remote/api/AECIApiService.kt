package com.aeci.mmucompanion.data.remote.api

import com.aeci.mmucompanion.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface AECIApiService {
    
    @POST("auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>
    
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body refreshRequest: RefreshTokenRequest
    ): Response<LoginResponse>
    
    @POST("auth/password-reset/request")
    suspend fun sendPasswordResetEmail(
        @Body request: com.aeci.mmucompanion.data.remote.dto.PasswordResetRequest
    ): Response<com.aeci.mmucompanion.data.remote.dto.PasswordResetResponse>
    
    @POST("auth/password-reset/verify")
    suspend fun verifyPasswordResetToken(
        @Body request: com.aeci.mmucompanion.data.remote.dto.PasswordResetVerifyRequest
    ): Response<com.aeci.mmucompanion.data.remote.dto.PasswordResetVerifyResponse>
    
    @POST("auth/password-reset/complete")
    suspend fun completePasswordReset(
        @Body request: com.aeci.mmucompanion.data.remote.dto.PasswordResetCompleteRequest
    ): Response<com.aeci.mmucompanion.data.remote.dto.PasswordResetCompleteResponse>
    
    @GET("forms/{id}")
    suspend fun getForm(
        @Path("id") formId: String,
        @Header("Authorization") token: String
    ): Response<FormResponse>
    
    @POST("forms")
    suspend fun submitForm(
        @Body formRequest: FormSubmissionRequest,
        @Header("Authorization") token: String
    ): Response<FormSubmissionResponse>
    
    @PUT("forms/{id}")
    suspend fun updateForm(
        @Path("id") formId: String,
        @Body formRequest: FormUpdateRequest,
        @Header("Authorization") token: String
    ): Response<FormResponse>
    
    @GET("forms/user/{userId}")
    suspend fun getUserForms(
        @Path("userId") userId: String,
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<FormsListResponse>
    
    @GET("equipment")
    suspend fun getEquipment(
        @Header("Authorization") token: String,
        @Query("location") location: String? = null,
        @Query("type") type: String? = null
    ): Response<EquipmentListResponse>
    
    @GET("equipment/{id}")
    suspend fun getEquipmentById(
        @Path("id") equipmentId: String,
        @Header("Authorization") token: String
    ): Response<EquipmentResponse>
    
    @POST("equipment/{id}/events")
    suspend fun addEquipmentEvent(
        @Path("id") equipmentId: String,
        @Body eventRequest: EquipmentEventRequest,
        @Header("Authorization") token: String
    ): Response<EquipmentEventResponse>
    
    @GET("users")
    suspend fun getUsers(
        @Header("Authorization") token: String,
        @Query("role") role: String? = null,
        @Query("department") department: String? = null
    ): Response<UsersListResponse>
    
    @GET("shifts")
    suspend fun getShifts(
        @Header("Authorization") token: String,
        @Query("location") location: String? = null
    ): Response<ShiftsListResponse>
    
    @POST("sync/forms")
    suspend fun syncForms(
        @Body syncRequest: FormsSyncRequest,
        @Header("Authorization") token: String
    ): Response<SyncResponse>
    
    @GET("reports/availability")
    suspend fun getAvailabilityReport(
        @Header("Authorization") token: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("equipmentId") equipmentId: String? = null
    ): Response<AvailabilityReportResponse>
    
    // ==================== REPORT MANAGEMENT ENDPOINTS ====================
    
    @GET("reports/history")
    suspend fun getReportHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("reportType") reportType: String? = null,
        @Query("generatedBy") generatedBy: String? = null,
        @Query("startDate") startDate: Long? = null,
        @Query("endDate") endDate: Long? = null,
        @Query("format") format: String? = null
    ): ReportHistoryResponse
    
    @GET("reports/my-reports")
    suspend fun getMyReports(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): MyReportsResponse
    
    @POST("reports/generate")
    suspend fun generateReport(
        @Body request: ReportGenerationRequestDto
    ): ReportGenerationResponseDto
    
    @Multipart
    @POST("reports/generate")
    suspend fun uploadReport(
        @Part reportFile: MultipartBody.Part,
        @Part("reportType") reportType: RequestBody,
        @Part("reportTitle") reportTitle: RequestBody,
        @Part("format") format: RequestBody,
        @Part("parameters") parameters: RequestBody? = null,
        @Part("formIds") formIds: RequestBody? = null
    ): UploadReportResponse
    
    @GET("reports/download/{reportId}")
    suspend fun downloadReport(
        @Path("reportId") reportId: String
    ): Response<ResponseBody>
    
    @DELETE("reports/{reportId}")
    suspend fun deleteReport(
        @Path("reportId") reportId: String
    ): DeleteReportResponse
    
    @GET("reports/statistics")
    suspend fun getReportStatistics(): ReportStatisticsResponse
    
    // Form endpoints
    @POST("forms")
    suspend fun saveForm(@Body form: FormDto): FormDto
    
    @GET("forms/{id}")
    suspend fun getFormById(@Path("id") id: String): FormDto
    
    @GET("forms")
    suspend fun getFormsByUser(
        @Query("user_id") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): List<FormDto>
    
    @GET("forms")
    suspend fun getFormsByType(
        @Query("type") formType: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): List<FormDto>
    
    @GET("forms")
    suspend fun getFormsByStatus(
        @Query("status") status: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): List<FormDto>
    
    @GET("forms")
    suspend fun getFormsByEquipment(
        @Query("equipment_id") equipmentId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): List<FormDto>
    
    @PUT("forms/{id}")
    suspend fun updateForm(@Path("id") id: String, @Body form: FormDto): FormDto
    
    @DELETE("forms/{id}")
    suspend fun deleteForm(@Path("id") id: String): Response<Unit>
    
    // Job Card endpoints
    @POST("job-cards")
    suspend fun saveJobCard(@Body jobCard: JobCardDto): JobCardDto
    
    @GET("job-cards/{id}")
    suspend fun getJobCardById(@Path("id") id: String): JobCardDto
    
    @GET("job-cards")
    suspend fun getJobCardsByUser(
        @Query("user_id") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): List<JobCardDto>
    
    @GET("job-cards")
    suspend fun getJobCardsByStatus(
        @Query("status") status: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): List<JobCardDto>
    
    @GET("job-cards")
    suspend fun getJobCardsByEquipment(
        @Query("equipment_id") equipmentId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): List<JobCardDto>
    
    @PUT("job-cards/{id}")
    suspend fun updateJobCard(@Path("id") id: String, @Body jobCard: JobCardDto): JobCardDto
    
    @DELETE("job-cards/{id}")
    suspend fun deleteJobCard(@Path("id") id: String): Response<Unit>
    
    @POST("job-cards/bulk-assign")
    suspend fun bulkAssignJobCards(
        @Body request: BulkAssignJobCardsRequest
    ): BulkAssignJobCardsResponse
    
    @POST("job-cards/bulk-update-status")
    suspend fun bulkUpdateJobCardStatus(
        @Body request: BulkUpdateJobCardStatusRequest
    ): BulkUpdateJobCardStatusResponse
    
    @GET("job-cards/statistics")
    suspend fun getJobCardStatistics(): JobCardStatisticsResponse
    
    @POST("job-cards/export")
    suspend fun exportJobCards(
        @Body request: JobCardExportRequestDto
    ): JobCardExportResponseDto
}

// Request/Response data classes
data class LoginRequest(
    val username: String,
    val password: String,
    val deviceId: String
)

data class LoginResponse(
    val success: Boolean,
    val token: String,
    val refreshToken: String,
    val user: UserApiModel,
    val permissions: List<String>,
    val expiresIn: Long
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class UserApiModel(
    val id: String,
    val username: String,
    val fullName: String,
    val email: String,
    val role: String,
    val department: String,
    val shiftPattern: String,
    val isActive: Boolean
)

data class FormResponse(
    val success: Boolean,
    val form: FormApiModel
)

data class FormApiModel(
    val id: String,
    val type: String,
    val title: String,
    val description: String,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?,
    val status: String,
    val userId: String,
    val equipmentId: String?,
    val shiftId: String?,
    val locationId: String?,
    val formData: Map<String, Any>,
    val attachments: List<String>
)

data class FormSubmissionRequest(
    val type: String,
    val equipmentId: String?,
    val shiftId: String?,
    val locationId: String?,
    val formData: Map<String, Any>,
    val attachments: List<String> = emptyList()
)

data class FormSubmissionResponse(
    val success: Boolean,
    val formId: String,
    val message: String
)

data class FormUpdateRequest(
    val formData: Map<String, Any>,
    val attachments: List<String> = emptyList(),
    val status: String? = null
)

data class FormsListResponse(
    val success: Boolean,
    val forms: List<FormApiModel>,
    val totalCount: Int,
    val page: Int,
    val hasMore: Boolean
)

data class EquipmentListResponse(
    val success: Boolean,
    val equipment: List<EquipmentApiModel>
)

data class EquipmentResponse(
    val success: Boolean,
    val equipment: EquipmentApiModel
)

data class EquipmentApiModel(
    val id: String,
    val name: String,
    val type: String,
    val model: String,
    val serialNumber: String,
    val location: String,
    val status: String,
    val manufacturer: String,
    val installationDate: Long,
    val lastMaintenanceDate: Long?,
    val nextMaintenanceDate: Long?,
    val specifications: Map<String, Any>,
    val operatingParameters: Map<String, Any>
)

data class EquipmentEventRequest(
    val eventType: String,
    val description: String,
    val severity: String,
    val formId: String? = null
)

data class EquipmentEventResponse(
    val success: Boolean,
    val eventId: String,
    val message: String
)

data class UsersListResponse(
    val success: Boolean,
    val users: List<UserApiModel>
)

data class ShiftsListResponse(
    val success: Boolean,
    val shifts: List<ShiftApiModel>
)

data class ShiftApiModel(
    val id: String,
    val name: String,
    val startTime: String,
    val endTime: String,
    val duration: Int,
    val type: String,
    val location: String,
    val supervisorId: String?
)

data class FormsSyncRequest(
    val forms: List<FormSyncItem>
)

data class FormSyncItem(
    val id: String,
    val type: String,
    val formData: Map<String, Any>,
    val status: String,
    val lastModified: Long
)

data class SyncResponse(
    val success: Boolean,
    val syncedCount: Int,
    val failedCount: Int,
    val errors: List<String> = emptyList()
)

data class AvailabilityReportResponse(
    val success: Boolean,
    val reportData: AvailabilityReportData
)

data class AvailabilityReportData(
    val totalHours: Double,
    val operationalHours: Double,
    val maintenanceHours: Double,
    val downtime: Double,
    val availability: Double,
    val utilization: Double,
    val equipmentBreakdown: List<EquipmentAvailability>
)

data class EquipmentAvailability(
    val equipmentId: String,
    val equipmentName: String,
    val availability: Double,
    val utilization: Double,
    val totalDowntime: Double
)
