package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.model.Notification
import com.aeci.mmucompanion.domain.model.NotificationType
import com.aeci.mmucompanion.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationUiState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val error: String? = null,
    val selectedFilter: NotificationType? = null
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val getUnreadNotificationsUseCase: GetUnreadNotificationsUseCase,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase,
    private val markAllNotificationsAsReadUseCase: MarkAllNotificationsAsReadUseCase,
    private val deleteNotificationUseCase: DeleteNotificationUseCase,
    private val getUnreadNotificationCountUseCase: GetUnreadNotificationCountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    // Mock current user ID - in real app, get from auth service
    private val currentUserId = "current_user_id"

    init {
        loadNotifications()
        loadUnreadCount()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                getNotificationsUseCase(currentUserId)
                    .catch { e ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = e.message ?: "Failed to load notifications"
                            )
                        }
                    }
                    .collect { notifications ->
                        val filteredNotifications = if (_uiState.value.selectedFilter != null) {
                            notifications.filter { it.type == _uiState.value.selectedFilter }
                        } else {
                            notifications
                        }
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                notifications = filteredNotifications,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Failed to load notifications"
                    )
                }
            }
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            try {
                val count = getUnreadNotificationCountUseCase(currentUserId)
                _uiState.update { it.copy(unreadCount = count) }
            } catch (e: Exception) {
                // Handle error silently for unread count
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                markNotificationAsReadUseCase(notificationId)
                loadNotifications()
                loadUnreadCount()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "Failed to mark notification as read")
                }
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                markAllNotificationsAsReadUseCase(currentUserId)
                loadNotifications()
                loadUnreadCount()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "Failed to mark all notifications as read")
                }
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                deleteNotificationUseCase(notificationId)
                loadNotifications()
                loadUnreadCount()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "Failed to delete notification")
                }
            }
        }
    }

    fun filterByType(type: NotificationType?) {
        _uiState.update { it.copy(selectedFilter = type) }
        loadNotifications()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refresh() {
        loadNotifications()
        loadUnreadCount()
    }
} 
