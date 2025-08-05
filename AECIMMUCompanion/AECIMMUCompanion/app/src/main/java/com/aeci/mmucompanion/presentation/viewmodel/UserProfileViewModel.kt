package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.model.User
import com.aeci.mmucompanion.domain.usecase.GetCurrentUserUseCase
import com.aeci.mmucompanion.domain.usecase.UpdateUserProfileUseCase
import com.aeci.mmucompanion.domain.usecase.ChangePasswordUseCase
import com.aeci.mmucompanion.domain.usecase.UpdateBiometricSettingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val updateBiometricSettingUseCase: UpdateBiometricSettingUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = getCurrentUserUseCase()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentUser = currentUser
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load user profile"
                )
            }
        }
    }
    
    fun updateProfile(
        fullName: String,
        email: String,
        department: String,
        shiftPattern: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = _uiState.value.currentUser
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(
                        fullName = fullName,
                        email = email,
                        department = department,
                        shiftPattern = shiftPattern
                    )
                    
                    updateUserProfileUseCase(updatedUser)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = updatedUser,
                        successMessage = "Profile updated successfully"
                    )
                    
                    // Clear success message after 3 seconds
                    clearSuccessMessage()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update profile"
                )
            }
        }
    }
    
    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = _uiState.value.currentUser
                if (currentUser != null) {
                    changePasswordUseCase(currentUser.id, currentPassword, newPassword)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Password changed successfully"
                    )
                    
                    // Clear success message after 3 seconds
                    clearSuccessMessage()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to change password"
                )
            }
        }
    }
    
    fun updateBiometricSetting(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentUser = _uiState.value.currentUser
                if (currentUser != null) {
                    updateBiometricSettingUseCase(currentUser.id, enabled)
                    
                    val updatedUser = currentUser.copy(biometricEnabled = enabled)
                    _uiState.value = _uiState.value.copy(
                        currentUser = updatedUser,
                        successMessage = if (enabled) "Biometric login enabled" else "Biometric login disabled"
                    )
                    
                    // Clear success message after 3 seconds
                    clearSuccessMessage()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update biometric setting"
                )
            }
        }
    }
    
    fun updateProfileImage(imageUri: String) {
        viewModelScope.launch {
            try {
                val currentUser = _uiState.value.currentUser
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(profileImageUri = imageUri)
                    updateUserProfileUseCase(updatedUser)
                    
                    _uiState.value = _uiState.value.copy(
                        currentUser = updatedUser,
                        successMessage = "Profile picture updated successfully"
                    )
                    
                    // Clear success message after 3 seconds
                    clearSuccessMessage()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update profile picture"
                )
            }
        }
    }
    
    fun showError(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun clearSuccessMessage() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _uiState.value = _uiState.value.copy(successMessage = null)
        }
    }
    
    fun refreshProfile() {
        loadUserProfile()
    }
}

data class UserProfileUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val successMessage: String? = null,
    val error: String? = null
)
