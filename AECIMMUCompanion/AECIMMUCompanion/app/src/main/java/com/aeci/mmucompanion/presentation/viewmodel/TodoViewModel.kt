package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.model.Todo
import com.aeci.mmucompanion.domain.model.TodoWithDetails
import com.aeci.mmucompanion.domain.usecase.TodoUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodoUiState(
    val todos: List<TodoWithDetails> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val todoUseCases: TodoUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    init {
        loadTodos()
    }

    private fun loadTodos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                todoUseCases.getAllTodosWithDetails()
                    .collect { todos ->
                        _uiState.update { 
                            it.copy(
                                todos = todos.sortedWith(
                                    compareBy<TodoWithDetails> { todo -> todo.todo.isCompleted }
                                        .thenByDescending { todo -> todo.todo.priority.ordinal }
                                        .thenBy { todo -> todo.todo.dueDate ?: Long.MAX_VALUE }
                                        .thenByDescending { todo -> todo.todo.createdAt }
                                ),
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load tasks: ${e.localizedMessage}"
                    ) 
                }
            }
        }
    }

    fun addTodo(todo: Todo) {
        viewModelScope.launch {
            try {
                todoUseCases.addTodo(todo)
                // Todos will be automatically updated through the Flow
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to add task: ${e.localizedMessage}")
                }
            }
        }
    }

    fun updateTodo(todo: Todo) {
        viewModelScope.launch {
            try {
                val updatedTodo = todo.copy(updatedAt = System.currentTimeMillis())
                todoUseCases.updateTodo(updatedTodo)
                // Todos will be automatically updated through the Flow
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to update task: ${e.localizedMessage}")
                }
            }
        }
    }

    fun deleteTodo(todoId: String) {
        viewModelScope.launch {
            try {
                todoUseCases.deleteTodo(todoId)
                // Todos will be automatically updated through the Flow
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to delete task: ${e.localizedMessage}")
                }
            }
        }
    }

    fun toggleTodoComplete(todoId: String) {
        viewModelScope.launch {
            try {
                val currentTodo = _uiState.value.todos.find { it.todo.id == todoId }?.todo
                if (currentTodo != null) {
                    val updatedTodo = currentTodo.copy(
                        isCompleted = !currentTodo.isCompleted,
                        updatedAt = System.currentTimeMillis(),
                        completedAt = if (!currentTodo.isCompleted) System.currentTimeMillis() else null
                    )
                    todoUseCases.updateTodo(updatedTodo)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to update task: ${e.localizedMessage}")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun getTodosByCategory(category: String): List<TodoWithDetails> {
        return _uiState.value.todos.filter { it.todo.category.name == category }
    }

    fun getPendingTodos(): List<TodoWithDetails> {
        return _uiState.value.todos.filter { !it.todo.isCompleted }
    }

    fun getCompletedTodos(): List<TodoWithDetails> {
        return _uiState.value.todos.filter { it.todo.isCompleted }
    }

    fun getOverdueTodos(): List<TodoWithDetails> {
        val now = System.currentTimeMillis()
        return _uiState.value.todos.filter { todoWithDetails ->
            val todo = todoWithDetails.todo
            !todo.isCompleted && todo.dueDate != null && todo.dueDate < now
        }
    }
}
