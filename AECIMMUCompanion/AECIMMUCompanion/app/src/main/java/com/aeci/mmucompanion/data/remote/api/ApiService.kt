package com.aeci.mmucompanion.data.remote.api

import com.aeci.mmucompanion.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Health and Network
    @GET("api/health")
    suspend fun getHealth(): Response<ApiResponse<Any>>
    
    @GET("api/network-info")
    suspend fun getNetworkInfo(): Response<NetworkInfoResponse>
    
    // Authentication
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("api/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<String>>
    
    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<ApiResponse<UserDto>>
    
    // Users (Admin only)
    @GET("api/users")
    suspend fun getUsers(): Response<ApiResponse<List<UserDto>>>
    
    @POST("api/users")
    suspend fun createUser(@Body request: CreateUserRequest): Response<ApiResponse<String>>
    
    // Todos
    @GET("api/todos")
    suspend fun getTodos(
        @Query("category") category: String? = null,
        @Query("priority") priority: String? = null,
        @Query("status") status: String? = null,
        @Query("assigned_to") assignedTo: String? = null,
        @Query("site_id") siteId: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<TodosResponse>
    
    @GET("api/todos/{id}")
    suspend fun getTodo(@Path("id") todoId: String): Response<ApiResponse<TodoDto>>
    
    @POST("api/todos")
    suspend fun createTodo(@Body request: CreateTodoRequest): Response<ApiResponse<String>>
    
    @PUT("api/todos/{id}")
    suspend fun updateTodo(
        @Path("id") todoId: String,
        @Body request: UpdateTodoRequest
    ): Response<ApiResponse<String>>
    
    @DELETE("api/todos/{id}")
    suspend fun deleteTodo(@Path("id") todoId: String): Response<ApiResponse<String>>
    
    // Todo Comments
    @POST("api/todos/{id}/comments")
    suspend fun addComment(
        @Path("id") todoId: String,
        @Body request: AddCommentRequest
    ): Response<ApiResponse<String>>
    
    // Todo Time Tracking
    @POST("api/todos/{id}/time/start")
    suspend fun startTimeTracking(
        @Path("id") todoId: String,
        @Body request: StartTimeTrackingRequest
    ): Response<ApiResponse<String>>
    
    @PUT("api/todos/{id}/time/{entryId}/stop")
    suspend fun stopTimeTracking(
        @Path("id") todoId: String,
        @Path("entryId") entryId: String
    ): Response<ApiResponse<Map<String, Any>>>
    
    // Todo Analytics
    @GET("api/todos/analytics")
    suspend fun getTodoAnalytics(): Response<ApiResponse<TodoAnalyticsDto>>
    
    // Bulk Operations
    @PUT("api/todos/bulk")
    suspend fun bulkUpdateTodos(@Body request: BulkUpdateTodosRequest): Response<ApiResponse<String>>
}
