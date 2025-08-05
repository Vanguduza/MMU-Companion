package com.aeci.mmucompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.domain.repository.*
import com.aeci.mmucompanion.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

/**
 * Comprehensive ViewModel for Job Card Integration
 * Handles job cards, related forms, todos, and time tracking
 */
@HiltViewModel
class JobCardIntegrationViewModel @Inject constructor(
    private val jobCardRepository: JobCardRepository,
    private val todoRepository: TodoRepository,
    private val formRepository: FormRepository,
    private val timeTrackingRepository: TimeTrackingRepository,
    private val formUseCases: FormUseCases,
    private val pdfGenerationService: PdfGenerationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobCardIntegrationUiState())
    val uiState: StateFlow<JobCardIntegrationUiState> = _uiState.asStateFlow()

    fun loadJobCard(jobCardId: String) {
        viewModelScope.launch {
            try {
                val jobCard = jobCardRepository.getJobCardById(jobCardId)
                _uiState.value = _uiState.value.copy(
                    jobCard = jobCard,
                    isLoading = false
                )
                
                // Load related data
                jobCard?.let {
                    loadRelatedForms(it.relatedFormId)
                    loadRelatedTodos(jobCardId)
                    loadTimeEntries(jobCardId)
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun initializeNewJobCard(formType: FormType) {
        val newJobCard = JobCard(
            id = UUID.randomUUID().toString(),
            title = "",
            description = "",
            status = JobCardStatus.PENDING,
            priority = JobCardPriority.MEDIUM,
            category = JobCardCategory.PREVENTIVE_MAINTENANCE,
            equipmentId = null,
            equipmentName = "",
            siteLocation = "",
            assignedTo = null,
            assignedToName = null,
            createdBy = "current_user", // TODO: Get from session
            createdByName = "Current User",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            dueDate = null,
            completedDate = null,
            estimatedHours = null,
            actualHours = null,
            partsRequired = emptyList(),
            toolsRequired = emptyList(),
            safetyRequirements = emptyList(),
            workInstructions = null,
            notes = null,
            attachments = emptyList(),
            photos = emptyList(),
            relatedTaskId = null,
            relatedFormId = null
        )
        
        _uiState.value = _uiState.value.copy(
            jobCard = newJobCard,
            isLoading = false
        )
    }

    fun updateJobCardField(fieldName: String, value: Any) {
        val currentJobCard = _uiState.value.jobCard ?: return
        
        val updatedJobCard = when (fieldName) {
            "title" -> currentJobCard.copy(title = value as String)
            "description" -> currentJobCard.copy(description = value as String)
            "equipmentId" -> currentJobCard.copy(equipmentId = value as String)
            "equipmentName" -> currentJobCard.copy(equipmentName = value as String)
            "priority" -> currentJobCard.copy(priority = value as JobCardPriority)
            "category" -> currentJobCard.copy(category = value as JobCardCategory)
            "workInstructions" -> currentJobCard.copy(workInstructions = value as String)
            "notes" -> currentJobCard.copy(notes = value as String)
            else -> currentJobCard
        }
        
        _uiState.value = _uiState.value.copy(
            jobCard = updatedJobCard.copy(updatedAt = LocalDateTime.now())
        )
    }

    fun createJobCard() {
        viewModelScope.launch {
            try {
                val jobCard = _uiState.value.jobCard ?: return@launch
                
                // Create the job card
                val createdJobCard = jobCardRepository.createJobCard(jobCard)
                
                // Create related job card form
                val jobCardForm = formUseCases.createForm(
                    FormType.JOB_CARD,
                    createdJobCard.siteLocation,
                    createdJobCard.equipmentId ?: ""
                )
                
                // Link the form to the job card
                val linkedJobCard = createdJobCard.copy(relatedFormId = jobCardForm.id)
                jobCardRepository.updateJobCard(linkedJobCard)
                
                _uiState.value = _uiState.value.copy(
                    jobCard = linkedJobCard,
                    relatedForms = listOf(jobCardForm)
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun submitJobCard() {
        viewModelScope.launch {
            try {
                val jobCard = _uiState.value.jobCard ?: return@launch
                
                // Update job card status to completed
                val completedJobCard = jobCard.copy(
                    status = JobCardStatus.COMPLETED,
                    completedDate = LocalDateTime.now().toLocalDate(),
                    updatedAt = LocalDateTime.now()
                )
                
                jobCardRepository.updateJobCard(completedJobCard)
                
                // Generate PDF report
                generateJobCardPDF(completedJobCard)
                
                // Complete related todos
                completeRelatedTodos(jobCard.id)
                
                _uiState.value = _uiState.value.copy(jobCard = completedJobCard)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun createRelatedForm(formType: FormType) {
        viewModelScope.launch {
            try {
                val jobCard = _uiState.value.jobCard ?: return@launch
                
                val form = formUseCases.createForm(
                    formType,
                    jobCard.siteLocation,
                    jobCard.equipmentId ?: ""
                )
                
                val updatedForms = _uiState.value.relatedForms + form
                _uiState.value = _uiState.value.copy(relatedForms = updatedForms)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun createTodoFromJobCard() {
        viewModelScope.launch {
            try {
                val jobCard = _uiState.value.jobCard ?: return@launch
                
                val todo = Todo(
                    id = UUID.randomUUID().toString(),
                    title = "Complete: ${jobCard.title}",
                    description = jobCard.description,
                    priority = when (jobCard.priority) {
                        JobCardPriority.LOW -> TodoPriority.LOW
                        JobCardPriority.MEDIUM -> TodoPriority.MEDIUM
                        JobCardPriority.HIGH -> TodoPriority.HIGH
                        JobCardPriority.URGENT -> TodoPriority.HIGH
                    },
                    category = TodoCategory.MAINTENANCE,
                    jobCardId = jobCard.id,
                    createdByUserId = jobCard.createdBy,
                    assignedToUserId = jobCard.assignedTo,
                    estimatedHours = jobCard.estimatedHours,
                    equipmentId = jobCard.equipmentId
                )
                
                todoRepository.createTodo(todo)
                
                val updatedTodos = _uiState.value.relatedTodos + todo
                _uiState.value = _uiState.value.copy(relatedTodos = updatedTodos)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun startTimeTracking() {
        viewModelScope.launch {
            try {
                val jobCard = _uiState.value.jobCard ?: return@launch
                
                val timeEntry = TaskTimeEntry(
                    id = UUID.randomUUID().toString(),
                    userId = jobCard.assignedTo ?: jobCard.createdBy,
                    date = LocalDate.now(),
                    startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                    endTime = null,
                    jobCode = jobCard.jobCode ?: "JOB_CARD",
                    description = "Working on ${jobCard.title}",
                    regularHours = 0.0,
                    overtimeHours = 0.0,
                    isActive = true,
                    todoId = null,
                    jobCardId = jobCard.id,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                
                timeTrackingRepository.startTimeEntry(timeEntry)
                
                val updatedEntries = _uiState.value.timeEntries + timeEntry
                _uiState.value = _uiState.value.copy(
                    timeEntries = updatedEntries,
                    activeTimeEntry = timeEntry
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun stopTimeTracking() {
        viewModelScope.launch {
            try {
                val activeEntry = _uiState.value.activeTimeEntry ?: return@launch
                
                val completedEntry = activeEntry.copy(endTime = LocalDateTime.now())
                timeTrackingRepository.endTimeEntry(completedEntry)
                
                val updatedEntries = _uiState.value.timeEntries.map { 
                    if (it.id == activeEntry.id) completedEntry else it
                }
                
                val totalTime = calculateTotalTime(updatedEntries)
                
                _uiState.value = _uiState.value.copy(
                    timeEntries = updatedEntries,
                    activeTimeEntry = null,
                    totalTimeSpent = totalTime
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun loadRelatedForms(formId: String?) {
        viewModelScope.launch {
            try {
                val forms = if (formId != null) {
                    listOfNotNull(formRepository.getFormById(formId))
                } else {
                    emptyList()
                }
                
                _uiState.value = _uiState.value.copy(relatedForms = forms)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun loadRelatedTodos(jobCardId: String) {
        viewModelScope.launch {
            try {
                val todos = todoRepository.getTodosByJobCard(jobCardId)
                _uiState.value = _uiState.value.copy(relatedTodos = todos)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun loadTimeEntries(jobCardId: String) {
        viewModelScope.launch {
            try {
                val timeEntries = timeTrackingRepository.getTimeEntriesByTask(jobCardId)
                val totalTime = calculateTotalTime(timeEntries)
                val activeEntry = timeEntries.find { it.endTime == null }
                
                _uiState.value = _uiState.value.copy(
                    timeEntries = timeEntries,
                    totalTimeSpent = totalTime,
                    activeTimeEntry = activeEntry
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun generateJobCardPDF(jobCard: JobCard) {
        viewModelScope.launch {
            try {
                // Get the related form
                val form = if (jobCard.relatedFormId != null) {
                    formRepository.getFormById(jobCard.relatedFormId)
                } else null
                
                // Generate PDF using the enhanced template
                if (form != null) {
                    pdfGenerationService.generateReport(form)
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "PDF generation failed: ${e.message}")
            }
        }
    }

    private fun completeRelatedTodos(jobCardId: String) {
        viewModelScope.launch {
            try {
                val todos = _uiState.value.relatedTodos
                todos.forEach { todo ->
                    val completedTodo = todo.copy(
                        isCompleted = true,
                        progressPercentage = 100,
                        completedAt = System.currentTimeMillis()
                    )
                    todoRepository.updateTodo(completedTodo)
                }
                
                _uiState.value = _uiState.value.copy(
                    relatedTodos = todos.map { it.copy(isCompleted = true, progressPercentage = 100) }
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun calculateTotalTime(timeEntries: List<TaskTimeEntry>): Long {
        return timeEntries.filter { it.endTime != null }.sumOf { entry ->
            val start = entry.startTime
            val end = entry.endTime ?: LocalDateTime.now()
            java.time.Duration.between(start, end).toMinutes()
        }
    }
}

data class JobCardIntegrationUiState(
    val jobCard: JobCard? = null,
    val relatedForms: List<DigitalForm> = emptyList(),
    val relatedTodos: List<Todo> = emptyList(),
    val timeEntries: List<TaskTimeEntry> = emptyList(),
    val activeTimeEntry: TaskTimeEntry? = null,
    val totalTimeSpent: Long = 0L,
    val isLoading: Boolean = true,
    val error: String? = null
)