package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.model.Task
import com.aeci.mmucompanion.domain.model.TaskStatus
import com.aeci.mmucompanion.domain.model.TaskPriority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    // TODO: Add task repository when available
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()
    
    init {
        loadTasks()
    }
    
    private fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // TODO: Load tasks from repository
                val mockTasks = listOf(
                    Task(
                        id = "task_001",
                        title = "Equipment Inspection",
                        description = "Inspect MMU equipment for maintenance needs",
                        status = TaskStatus.PENDING,
                        priority = TaskPriority.HIGH,
                        assignedTo = "user_001",
                        dueDate = System.currentTimeMillis() + 86400000, // 1 day
                        completedDate = null,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        siteId = "site_001"
                    )
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    tasks = mockTasks
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun refreshTasks() {
        loadTasks()
    }
}
