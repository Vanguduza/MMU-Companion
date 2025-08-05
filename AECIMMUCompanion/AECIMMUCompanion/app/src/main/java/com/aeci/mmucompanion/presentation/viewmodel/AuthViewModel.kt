package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.model.User
import com.aeci.mmucompanion.domain.usecase.*
import com.aeci.mmucompanion.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authenticateUserUseCase: AuthenticateUserUseCase,
    private val authenticateWithBiometricUseCase: AuthenticateWithBiometricUseCase,
    private val authenticateWithPinUseCase: AuthenticateWithPinUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    private var isInitialized = false
    
    init {
        // Defer initialization until actually needed to improve startup time
        // The first UI access will trigger initialization
    }
    
    private fun ensureInitialized() {
        if (!isInitialized) {
            isInitialized = true
            viewModelScope.launch {
                try {
                    checkCurrentUser()
                } catch (e: Exception) {
                    android.util.Log.e("AuthViewModel", "Error during initialization", e)
                    // Don't update state on error, let user try to login manually
                }
            }
        }
    }
    
    fun login(username: String, password: String) {
        ensureInitialized()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authenticateUserUseCase(username, password)
                .onSuccess { user ->
                    // Check if user requires password change
                    viewModelScope.launch {
                        try {
                            val requiresPasswordChange = userRepository.requiresPasswordChange(user.id)
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                currentUser = user,
                                requiresPasswordChange = requiresPasswordChange
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("AuthViewModel", "Error checking password change requirement", e)
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                currentUser = user,
                                requiresPasswordChange = false
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Authentication failed"
                    )
                }
        }
    }
    
    fun authenticateWithBiometric(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authenticateWithBiometricUseCase(userId)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentUser = user
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Biometric authentication failed"
                    )
                }
        }
    }
    
    fun authenticateWithPin(userId: String, pin: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authenticateWithPinUseCase(userId, pin)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentUser = user
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "PIN authentication failed"
                    )
                }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            try {
                logoutUseCase()
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error during logout", e)
            }
            _uiState.value = AuthUiState()
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private suspend fun checkCurrentUser() {
        try {
            val currentUser = getCurrentUserUseCase()
            if (currentUser != null) {
                val requiresPasswordChange = userRepository.requiresPasswordChange(currentUser.id)
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = true,
                    currentUser = currentUser,
                    requiresPasswordChange = requiresPasswordChange
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthViewModel", "Error checking current user", e)
            // Don't update state on error, let user try to login manually
        }
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val currentUser: User? = null,
    val requiresPasswordChange: Boolean = false,
    val error: String? = null
)
