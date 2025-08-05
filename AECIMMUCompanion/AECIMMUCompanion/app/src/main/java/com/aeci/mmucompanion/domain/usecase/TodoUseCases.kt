package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.Todo
import com.aeci.mmucompanion.domain.model.TodoWithDetails
import com.aeci.mmucompanion.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllTodosUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    operator fun invoke(): Flow<List<Todo>> = repository.getAllTodos()
}

class GetAllTodosWithDetailsUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    operator fun invoke(): Flow<List<TodoWithDetails>> = repository.getAllTodosWithDetails()
}

class GetTodoByIdUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(id: String): Todo? = repository.getTodoById(id)
}

class GetTodosByUserUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    operator fun invoke(userId: String): Flow<List<TodoWithDetails>> = repository.getTodosByUser(userId)
}

class GetTodosBySiteUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    operator fun invoke(siteId: String): Flow<List<TodoWithDetails>> = repository.getTodosBySite(siteId)
}

class GetPendingTodosUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    operator fun invoke(): Flow<List<TodoWithDetails>> = repository.getPendingTodos()
}

class GetCompletedTodosUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    operator fun invoke(): Flow<List<TodoWithDetails>> = repository.getCompletedTodos()
}

class GetOverdueTodosUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    operator fun invoke(): Flow<List<TodoWithDetails>> = repository.getOverdueTodos()
}

class AddTodoUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(todo: Todo) = repository.insertTodo(todo)
}

class UpdateTodoUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(todo: Todo) = repository.updateTodo(todo)
}

class DeleteTodoUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(todoId: String) = repository.deleteTodo(todoId)
}

data class TodoUseCases @Inject constructor(
    val getAllTodos: GetAllTodosUseCase,
    val getAllTodosWithDetails: GetAllTodosWithDetailsUseCase,
    val getTodoById: GetTodoByIdUseCase,
    val getTodosByUser: GetTodosByUserUseCase,
    val getTodosBySite: GetTodosBySiteUseCase,
    val getPendingTodos: GetPendingTodosUseCase,
    val getCompletedTodos: GetCompletedTodosUseCase,
    val getOverdueTodos: GetOverdueTodosUseCase,
    val addTodo: AddTodoUseCase,
    val updateTodo: UpdateTodoUseCase,
    val deleteTodo: DeleteTodoUseCase
)

