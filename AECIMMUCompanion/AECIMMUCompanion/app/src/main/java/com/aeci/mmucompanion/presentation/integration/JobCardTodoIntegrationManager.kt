package com.aeci.mmucompanion.presentation.integration

import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.domain.repository.*
import com.aeci.mmucompanion.domain.usecase.*
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Integration Manager for Job Cards and Todo Tasks
 * Provides comprehensive workflow management between job cards, forms, and tasks
 */
@Singleton
class JobCardTodoIntegrationManager @Inject constructor(
    private val jobCardRepository: JobCardRepository,
    private val todoRepository: TodoRepository,
    private val formRepository: FormRepository,
    private val timeTrackingRepository: TimeTrackingRepository,
    private val formUseCases: FormUseCases,
    private val pdfGenerationService: ComprehensivePdfGenerationService
) {

    /**
     * Create a complete job card workflow with linked forms and todos
     */
    suspend fun createJobCardWorkflow(
        jobCardRequest: JobCardWorkflowRequest
    ): Result<JobCardWorkflowResponse> {
        return try {
            // 1. Create the main job card
            val jobCard = createJobCard(jobCardRequest)
            
            // 2. Create related form
            val form = createRelatedForm(jobCard, jobCardRequest.formType)
            
            // 3. Create related todos/tasks
            val todos = createRelatedTodos(jobCard, jobCardRequest.todoTasks)
            
            // 4. Set up time tracking
            val timeTracker = initializeTimeTracking(jobCard)
            
            // 5. Create workflow response
            val response = JobCardWorkflowResponse(
                jobCard = jobCard,
                relatedForm = form,
                relatedTodos = todos,
                timeTracker = timeTracker,
                workflowStatus = WorkflowStatus.CREATED
            )
            
            Result.success(response)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Complete job card workflow with form submission and PDF generation
     */
    suspend fun completeJobCardWorkflow(
        jobCardId: String,
        completionData: JobCardCompletionData
    ): Result<JobCardWorkflowResponse> {
        return try {
            // 1. Update job card status
            val jobCard = updateJobCardCompletion(jobCardId, completionData)
            
            // 2. Submit related form
            val form = submitRelatedForm(jobCard.relatedFormId, completionData.formData)
            
            // 3. Complete related todos
            val completedTodos = completeRelatedTodos(jobCardId)
            
            // 4. Generate final PDF report
            val pdfResult = generateCompletionReport(jobCard, form)
            
            // 5. Update time tracking
            val finalTimeEntry = finalizeTimeTracking(jobCardId)
            
            val response = JobCardWorkflowResponse(
                jobCard = jobCard,
                relatedForm = form,
                relatedTodos = completedTodos,
                timeTracker = finalTimeEntry,
                workflowStatus = WorkflowStatus.COMPLETED,
                generatedReportPath = pdfResult.getOrNull()
            )
            
            Result.success(response)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get comprehensive workflow status
     */
    suspend fun getWorkflowStatus(jobCardId: String): Flow<WorkflowStatusUpdate> {
        return flow {
            val jobCard = jobCardRepository.getJobCardById(jobCardId)
            val relatedTodos = todoRepository.getTodosByJobCard(jobCardId)
            val timeEntries = timeTrackingRepository.getTimeEntriesForTodo(jobCardId)
            val form = jobCard?.relatedFormId?.let { formRepository.getFormById(it) }
            
            val status = WorkflowStatusUpdate(
                jobCardStatus = jobCard?.status ?: JobCardStatus.PENDING,
                formStatus = form?.status ?: FormStatus.DRAFT,
                todoProgress = calculateTodoProgress(relatedTodos),
                totalTimeSpent = calculateTotalTime(timeEntries),
                completionPercentage = calculateOverallProgress(jobCard, form, relatedTodos)
            )
            
            emit(status)
        }
    }

    /**
     * Auto-create todos from job card requirements
     */
    suspend fun autoCreateTodosFromJobCard(jobCard: JobCard): List<Todo> {
        val todos = mutableListOf<Todo>()
        
        // Create equipment preparation todo
        if (jobCard.equipmentId != null) {
            todos.add(createEquipmentPreparationTodo(jobCard))
        }
        
        // Create safety check todos
        if (jobCard.safetyRequirements.isNotEmpty()) {
            todos.add(createSafetyCheckTodo(jobCard))
        }
        
        // Create parts procurement todos
        if (jobCard.partsRequired.isNotEmpty()) {
            todos.addAll(createPartsAcquisitionTodos(jobCard))
        }
        
        // Create work execution todo
        todos.add(createWorkExecutionTodo(jobCard))
        
        // Create quality check todo
        todos.add(createQualityCheckTodo(jobCard))
        
        // Create documentation todo
        todos.add(createDocumentationTodo(jobCard))
        
        // Save all todos
        todos.forEach { todo ->
            todoRepository.insertTodo(todo)
        }
        
        return todos
    }

    /**
     * Link existing todo to job card
     */
    suspend fun linkTodoToJobCard(todoId: String, jobCardId: String): Result<Unit> {
        return try {
            val todo = todoRepository.getTodoById(todoId)
            val updatedTodo = todo?.copy(jobCardId = jobCardId)
            
            if (updatedTodo != null) {
                todoRepository.updateTodo(updatedTodo)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Todo not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create form-specific todos based on form type
     */
    suspend fun createFormSpecificTodos(form: DigitalForm, jobCardId: String?): List<Todo> {
        return when (form.formType) {
            FormType.FIRE_EXTINGUISHER_INSPECTION -> createFireExtinguisherInspectionTodos(form, jobCardId)
            FormType.PUMP_INSPECTION_90_DAY -> createPumpInspectionTodos(form, jobCardId)
            FormType.TIMESHEET -> createTimesheetTodos(form, jobCardId)
            FormType.BLAST_HOLE_LOG -> createBlastHoleLogTodos(form, jobCardId)
            FormType.JOB_CARD -> createJobCardFormTodos(form, jobCardId)
            else -> emptyList()
        }
    }

    // Private helper methods
    private suspend fun createJobCard(request: JobCardWorkflowRequest): JobCard {
        val jobCard = JobCard(
            id = UUID.randomUUID().toString(),
            title = request.title,
            description = request.description,
            status = JobCardStatus.PENDING,
            priority = request.priority,
            category = request.category,
            equipmentId = request.equipmentId,
            equipmentName = request.equipmentName,
            siteLocation = request.siteLocation,
            assignedTo = request.assignedTo,
            assignedToName = request.assignedToName,
            createdBy = request.createdBy,
            createdByName = request.createdByName,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            dueDate = request.dueDate,
            completedDate = null,
            estimatedHours = request.estimatedHours,
            actualHours = null,
            partsRequired = request.partsRequired,
            toolsRequired = request.toolsRequired,
            safetyRequirements = request.safetyRequirements,
            workInstructions = request.workInstructions,
            notes = request.notes,
            attachments = emptyList(),
            photos = emptyList(),
            relatedTaskId = null,
            relatedFormId = null
        )
        
        return jobCardRepository.createJobCard(jobCard)
    }

    private suspend fun createRelatedForm(jobCard: JobCard, formType: FormType): DigitalForm {
        val form = formUseCases.createForm(formType, jobCard.siteLocation, jobCard.equipmentId ?: "")
        
        // Link form to job card
        val updatedJobCard = jobCard.copy(relatedFormId = form.id)
        jobCardRepository.updateJobCard(updatedJobCard)
        
        return form
    }

    private suspend fun createRelatedTodos(jobCard: JobCard, todoRequests: List<TodoRequest>): List<Todo> {
        val todos = mutableListOf<Todo>()
        
        // Create todos from requests
        todoRequests.forEach { request ->
            val todo = Todo(
                id = UUID.randomUUID().toString(),
                title = request.title,
                description = request.description,
                priority = request.priority,
                category = request.category,
                jobCardId = jobCard.id,
                createdByUserId = jobCard.createdBy,
                assignedToUserId = request.assignedTo ?: jobCard.assignedTo,
                estimatedHours = request.estimatedHours,
                equipmentId = jobCard.equipmentId,
                dueDate = request.dueDate?.atStartOfDay()?.toEpochSecond(java.time.ZoneOffset.UTC)?.times(1000)
            )
            
            todoRepository.insertTodo(todo)
            todos.add(todo)
        }
        
        // Auto-create standard todos
        val autoTodos = autoCreateTodosFromJobCard(jobCard)
        todos.addAll(autoTodos)
        
        return todos
    }

    private suspend fun initializeTimeTracking(jobCard: JobCard): TaskTimeEntry? {
        // Initialize time tracking if assigned technician starts work
        return if (jobCard.assignedTo != null) {
            val timeEntry = TaskTimeEntry(
                id = UUID.randomUUID().toString(),
                taskId = jobCard.id,
                taskType = "JOB_CARD",
                startTime = LocalDateTime.now(),
                endTime = null,
                description = "Job Card: ${jobCard.title}",
                userId = jobCard.assignedTo
            )
            
            timeTrackingRepository.startTimeEntry(timeEntry)
            timeEntry
        } else null
    }

    private fun createEquipmentPreparationTodo(jobCard: JobCard): Todo {
        return Todo(
            id = UUID.randomUUID().toString(),
            title = "Prepare Equipment: ${jobCard.equipmentName}",
            description = "Prepare and inspect equipment ${jobCard.equipmentId} before work begins",
            priority = TodoPriority.HIGH,
            category = TodoCategory.MAINTENANCE,
            jobCardId = jobCard.id,
            createdByUserId = jobCard.createdBy,
            assignedToUserId = jobCard.assignedTo,
            equipmentId = jobCard.equipmentId,
            estimatedHours = 0.5
        )
    }

    private fun createSafetyCheckTodo(jobCard: JobCard): Todo {
        return Todo(
            id = UUID.randomUUID().toString(),
            title = "Safety Requirements Check",
            description = "Verify all safety requirements: ${jobCard.safetyRequirements.joinToString(", ")}",
            priority = TodoPriority.HIGH,
            category = TodoCategory.SAFETY,
            jobCardId = jobCard.id,
            createdByUserId = jobCard.createdBy,
            assignedToUserId = jobCard.assignedTo,
            estimatedHours = 0.25
        )
    }

    private fun createPartsAcquisitionTodos(jobCard: JobCard): List<Todo> {
        return jobCard.partsRequired.map { part ->
            Todo(
                id = UUID.randomUUID().toString(),
                title = "Acquire Part: ${part.partName}",
                description = "Obtain ${part.quantity}x ${part.partName} (${part.partNumber})",
                priority = TodoPriority.MEDIUM,
                category = TodoCategory.MAINTENANCE,
                jobCardId = jobCard.id,
                createdByUserId = jobCard.createdBy,
                assignedToUserId = jobCard.assignedTo,
                estimatedHours = 0.5
            )
        }
    }

    private fun createWorkExecutionTodo(jobCard: JobCard): Todo {
        return Todo(
            id = UUID.randomUUID().toString(),
            title = "Execute Work: ${jobCard.title}",
            description = jobCard.description,
            priority = TodoPriority.HIGH,
            category = TodoCategory.MAINTENANCE,
            jobCardId = jobCard.id,
            createdByUserId = jobCard.createdBy,
            assignedToUserId = jobCard.assignedTo,
            estimatedHours = jobCard.estimatedHours
        )
    }

    private fun createQualityCheckTodo(jobCard: JobCard): Todo {
        return Todo(
            id = UUID.randomUUID().toString(),
            title = "Quality Check",
            description = "Perform quality inspection and validation of completed work",
            priority = TodoPriority.HIGH,
                            category = TodoCategory.AUDIT,
            jobCardId = jobCard.id,
            createdByUserId = jobCard.createdBy,
            assignedToUserId = jobCard.assignedTo,
            estimatedHours = 0.5
        )
    }

    private fun createDocumentationTodo(jobCard: JobCard): Todo {
        return Todo(
            id = UUID.randomUUID().toString(),
            title = "Complete Documentation",
            description = "Fill out job card form and generate completion report",
            priority = TodoPriority.MEDIUM,
            category = TodoCategory.DOCUMENTATION,
            jobCardId = jobCard.id,
            createdByUserId = jobCard.createdBy,
            assignedToUserId = jobCard.assignedTo,
            estimatedHours = 0.25
        )
    }

    private suspend fun createFireExtinguisherInspectionTodos(form: DigitalForm, jobCardId: String?): List<Todo> {
        return listOf(
            Todo(
                id = UUID.randomUUID().toString(),
                title = "Fire Extinguisher Visual Inspection",
                description = "Perform visual inspection checklist",
                priority = TodoPriority.HIGH,
                category = TodoCategory.SAFETY,
                jobCardId = jobCardId,
                formId = form.id,
                createdByUserId = form.createdBy,
                estimatedHours = 0.5
            ),
            Todo(
                id = UUID.randomUUID().toString(),
                title = "Update Inspection Records",
                description = "Complete and submit fire extinguisher inspection form",
                priority = TodoPriority.MEDIUM,
                category = TodoCategory.DOCUMENTATION,
                jobCardId = jobCardId,
                formId = form.id,
                createdByUserId = form.createdBy,
                estimatedHours = 0.25
            )
        )
    }

    private suspend fun createPumpInspectionTodos(form: DigitalForm, jobCardId: String?): List<Todo> {
        return listOf(
            Todo(
                id = UUID.randomUUID().toString(),
                title = "90-Day Pump System Inspection",
                description = "Complete comprehensive pump system inspection",
                priority = TodoPriority.HIGH,
                category = TodoCategory.MAINTENANCE,
                jobCardId = jobCardId,
                formId = form.id,
                createdByUserId = form.createdBy,
                estimatedHours = 2.0
            ),
            Todo(
                id = UUID.randomUUID().toString(),
                title = "Pump Pressure Testing",
                description = "Perform pressure tests and record results",
                priority = TodoPriority.HIGH,
                category = TodoCategory.INSPECTION,
                jobCardId = jobCardId,
                formId = form.id,
                createdByUserId = form.createdBy,
                estimatedHours = 1.0
            )
        )
    }

    private suspend fun createTimesheetTodos(form: DigitalForm, jobCardId: String?): List<Todo> {
        return listOf(
            Todo(
                id = UUID.randomUUID().toString(),
                title = "Complete Weekly Timesheet",
                description = "Fill out timesheet for the week",
                priority = TodoPriority.MEDIUM,
                category = TodoCategory.DOCUMENTATION,
                jobCardId = jobCardId,
                formId = form.id,
                createdByUserId = form.createdBy,
                estimatedHours = 0.25
            )
        )
    }

    private suspend fun createBlastHoleLogTodos(form: DigitalForm, jobCardId: String?): List<Todo> {
        return listOf(
            Todo(
                id = UUID.randomUUID().toString(),
                title = "Record Blast Hole Data",
                description = "Log blast hole information and explosive details",
                priority = TodoPriority.HIGH,
                category = TodoCategory.DOCUMENTATION,
                jobCardId = jobCardId,
                formId = form.id,
                createdByUserId = form.createdBy,
                estimatedHours = 0.5
            )
        )
    }

    private suspend fun createJobCardFormTodos(form: DigitalForm, jobCardId: String?): List<Todo> {
        return listOf(
            Todo(
                id = UUID.randomUUID().toString(),
                title = "Complete Job Card Form",
                description = "Fill out detailed job card information",
                priority = TodoPriority.HIGH,
                category = TodoCategory.DOCUMENTATION,
                jobCardId = jobCardId,
                formId = form.id,
                createdByUserId = form.createdBy,
                estimatedHours = 0.5
            )
        )
    }

    private suspend fun updateJobCardCompletion(jobCardId: String, completionData: JobCardCompletionData): JobCard {
        val jobCard = jobCardRepository.getJobCardById(jobCardId)
            ?: throw IllegalArgumentException("Job card not found")
        
        val updatedJobCard = jobCard.copy(
            status = JobCardStatus.COMPLETED,
            completedDate = LocalDateTime.now().toLocalDate(),
            actualHours = completionData.actualHours,
            notes = "${jobCard.notes ?: ""}\n${completionData.completionNotes}",
            updatedAt = LocalDateTime.now()
        )
        
        return jobCardRepository.updateJobCard(updatedJobCard)
    }

    private suspend fun submitRelatedForm(formId: String?, formData: Map<String, Any>): DigitalForm? {
        return if (formId != null) {
            val form = formRepository.getFormById(formId)
            form?.let {
                // Update form with completion data and submit
                val updatedForm = it.copy(status = FormStatus.SUBMITTED)
                formRepository.updateForm(updatedForm)
                updatedForm
            }
        } else null
    }

    private suspend fun completeRelatedTodos(jobCardId: String): List<Todo> {
        val todos = todoRepository.getTodosByJobCard(jobCardId)
        return todos.map { todo ->
            val completedTodo = todo.copy(
                isCompleted = true,
                progressPercentage = 100,
                completedAt = System.currentTimeMillis()
            )
            todoRepository.updateTodo(completedTodo)
            completedTodo
        }
    }

    private suspend fun generateCompletionReport(jobCard: JobCard, form: DigitalForm?): Result<String> {
        return if (form != null) {
            pdfGenerationService.generateReport(form)
        } else {
            Result.failure(Exception("No form to generate report from"))
        }
    }

    private suspend fun finalizeTimeTracking(jobCardId: String): TaskTimeEntry? {
        val timeEntries = timeTrackingRepository.getTimeEntriesForTodo(jobCardId)
        val activeEntry = timeEntries.find { it.endTime == null }
        
        return if (activeEntry != null) {
            val completedEntry = activeEntry.copy(endTime = LocalDateTime.now())
            timeTrackingRepository.endTimeEntry(completedEntry)
            completedEntry
        } else null
    }

    private fun calculateTodoProgress(todos: List<Todo>): Double {
        if (todos.isEmpty()) return 0.0
        val completedCount = todos.count { it.isCompleted }
        return (completedCount.toDouble() / todos.size) * 100
    }

    private fun calculateTotalTime(timeEntries: List<TaskTimeEntry>): Long {
        return timeEntries.filter { it.endTime != null }.sumOf { entry ->
            val start = entry.startTime
            val end = entry.endTime ?: LocalDateTime.now()
            java.time.Duration.between(start, end).toMinutes()
        }
    }

    private fun calculateOverallProgress(jobCard: JobCard?, form: DigitalForm?, todos: List<Todo>): Int {
        if (jobCard == null) return 0
        
        val jobCardProgress = when (jobCard.status) {
            JobCardStatus.PENDING -> 0
            JobCardStatus.IN_PROGRESS -> 50
            JobCardStatus.COMPLETED -> 100
            JobCardStatus.CANCELLED -> 0
            JobCardStatus.ON_HOLD -> 25
        }
        
        val formProgress = when (form?.status) {
            FormStatus.DRAFT -> 0
            FormStatus.IN_PROGRESS -> 50
            FormStatus.SUBMITTED -> 100
            FormStatus.APPROVED -> 100
            FormStatus.REJECTED -> 0
            null -> 0
        }
        
        val todoProgress = calculateTodoProgress(todos).toInt()
        
        // Weighted average: job card 50%, form 30%, todos 20%
        return ((jobCardProgress * 0.5) + (formProgress * 0.3) + (todoProgress * 0.2)).toInt()
    }
}

// Data classes for workflow management
data class JobCardWorkflowRequest(
    val title: String,
    val description: String,
    val priority: JobCardPriority,
    val category: JobCardCategory,
    val equipmentId: String?,
    val equipmentName: String,
    val siteLocation: String,
    val assignedTo: String?,
    val assignedToName: String?,
    val createdBy: String,
    val createdByName: String,
    val dueDate: java.time.LocalDate?,
    val estimatedHours: Double?,
    val partsRequired: List<JobCardPart>,
    val toolsRequired: List<String>,
    val safetyRequirements: List<String>,
    val workInstructions: String?,
    val notes: String?,
    val formType: FormType,
    val todoTasks: List<TodoRequest>
)

data class TodoRequest(
    val title: String,
    val description: String,
    val priority: TodoPriority,
    val category: TodoCategory,
    val assignedTo: String?,
    val estimatedHours: Double?,
    val dueDate: java.time.LocalDate?
)

data class JobCardCompletionData(
    val actualHours: Double,
    val completionNotes: String,
    val formData: Map<String, Any>
)

data class JobCardWorkflowResponse(
    val jobCard: JobCard,
    val relatedForm: DigitalForm?,
    val relatedTodos: List<Todo>,
    val timeTracker: TaskTimeEntry?,
    val workflowStatus: WorkflowStatus,
    val generatedReportPath: String? = null
)

data class WorkflowStatusUpdate(
    val jobCardStatus: JobCardStatus,
    val formStatus: FormStatus,
    val todoProgress: Double,
    val totalTimeSpent: Long,
    val completionPercentage: Int
)

enum class WorkflowStatus {
    CREATED, IN_PROGRESS, COMPLETED, CANCELLED, ON_HOLD
}