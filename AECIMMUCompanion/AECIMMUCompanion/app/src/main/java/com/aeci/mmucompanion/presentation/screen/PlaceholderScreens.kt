@file:OptIn(ExperimentalMaterial3Api::class)

package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import com.aeci.mmucompanion.core.util.FileShareManager
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aeci.mmucompanion.domain.model.Form
import com.aeci.mmucompanion.domain.model.FormType
import com.aeci.mmucompanion.presentation.component.AECIIcons
import com.aeci.mmucompanion.presentation.viewmodel.EquipmentScreenViewModel
import androidx.navigation.NavHostController
import com.aeci.mmucompanion.R
import com.aeci.mmucompanion.domain.model.Equipment
import com.aeci.mmucompanion.domain.model.EquipmentStatus
import com.aeci.mmucompanion.domain.model.EquipmentType
import com.aeci.mmucompanion.presentation.component.DynamicFormRenderer
import com.aeci.mmucompanion.presentation.component.FormSectionCard
import com.aeci.mmucompanion.presentation.component.FormSectionCard
import com.aeci.mmucompanion.presentation.component.ReportActionDialog
import com.aeci.mmucompanion.presentation.component.getFileName
import com.aeci.mmucompanion.presentation.viewmodel.FormViewModel
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image

// Placeholder screens - These would be fully implemented based on the requirements

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormsListScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    
    val tabs = listOf("All Forms", "My Forms", "Drafts", "Submitted")
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search forms...") },
            leadingIcon = { Icon(AECIIcons.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> AllFormsContent(navController, searchQuery)
            1 -> MyFormsContent(navController, searchQuery)
            2 -> DraftsContent(navController, searchQuery)
            3 -> SubmittedContent(navController, searchQuery)
        }
    }
}

@Composable
private fun AllFormsContent(
    navController: NavHostController,
    searchQuery: String
) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick Create Section
        item {
            Text(
                text = "Quick Create",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val quickForms = listOf(
                    "Daily Log" to "MMU_DAILY_LOG",
                    "Quality Report" to "MMU_QUALITY_REPORT",
                    "Pump Check" to "PUMP_90_DAY_INSPECTION",
                    "Maintenance" to "MMU_CHASSIS_MAINTENANCE",
                    "Blast Report" to "BLAST_REPORT"
                )
                
                items(quickForms) { (title, formType) ->
                    QuickCreateCard(
                        title = title,
                        onClick = { 
                            if (formType == "BLAST_REPORT") {
                                navController.navigate("blast_reports")
                            } else {
                                navController.navigate("forms/create/$formType")
                            }
                        }
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "All Form Types",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // All available form types
        val allFormTypes = listOf(
            FormTypeItem("Blast Report Generator", "Generate comprehensive blast analysis", "BLAST_REPORT", AECIIcons.Analytics),
            FormTypeItem("MMU Daily Production Log", "Track daily production metrics", "MMU_DAILY_LOG", AECIIcons.Today),
            FormTypeItem("MMU Quality Report", "Quality control inspection", "MMU_QUALITY_REPORT", AECIIcons.Assessment),
            FormTypeItem("Pump Weekly Checklist", "Weekly pump maintenance", "PUMP_WEEKLY_CHECK", AECIIcons.Construction),
            FormTypeItem("90-Day Pump Inspection", "Quarterly pump inspection", "PUMP_90_DAY_INSPECTION", AECIIcons.Schedule),
            FormTypeItem("Fire Extinguisher Check", "Monthly safety inspection", "FIRE_EXTINGUISHER_INSPECTION", AECIIcons.FireExtinguisher),
            FormTypeItem("MMU Handover Certificate", "Shift handover documentation", "MMU_HANDOVER_CERTIFICATE", AECIIcons.Assignment),
            FormTypeItem("Pre-task Safety Check", "Pre-work safety assessment", "PRETASK_SAFETY", AECIIcons.Security),
            FormTypeItem("Blast Hole Log", "Drilling operation record", "BLAST_HOLE_LOG", AECIIcons.Construction),
            FormTypeItem("Job Card", "Work order documentation", "JOB_CARD", AECIIcons.Work),
            FormTypeItem("MMU Chassis Maintenance", "Chassis inspection record", "MMU_CHASSIS_MAINTENANCE", AECIIcons.Construction),
            FormTypeItem("On-Bench MMU Inspection", "Bench inspection checklist", "ON_BENCH_MMU_INSPECTION", AECIIcons.Checklist),
            FormTypeItem("PC Pump Pressure Test", "High/Low pressure testing", "PC_PUMP_PRESSURE_TEST", AECIIcons.Speed),
            FormTypeItem("Monthly Process Record", "Monthly process documentation", "MONTHLY_PROCESS_MAINTENANCE", AECIIcons.CalendarMonth),
            FormTypeItem("Availability & Utilization", "Equipment availability report", "AVAILABILITY_UTILIZATION", AECIIcons.Analytics)
        )
        
        items(allFormTypes) { formType ->
            FormTypeCard(
                formType = formType,
                onClick = { 
                    if (formType.id == "BLAST_REPORT") {
                        navController.navigate("blast_reports")
                    } else {
                        navController.navigate("forms/create/${formType.id}")
                    }
                }
            )
        }
    }
}

@Composable
private fun MyFormsContent(
    navController: NavHostController,
    searchQuery: String
) {
    // Mock data for demonstration
    val myForms = remember {
        listOf(
            FormListItem("Daily Log - Zone A", "mmu_daily_log", "COMPLETED", System.currentTimeMillis() - 3600000),
            FormListItem("Pump Check - Unit 002", "pump_inspection", "IN_PROGRESS", System.currentTimeMillis() - 7200000),
            FormListItem("Quality Report - Batch 145", "quality_report", "DRAFT", System.currentTimeMillis() - 86400000)
        )
    }
    
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (myForms.isEmpty()) {
            item {
                EmptyStateCard("No forms assigned to you")
            }
        } else {
            items(myForms) { form ->
                FormListCard(
                    form = form,
                    onClick = { navController.navigate("forms/edit/${form.id}") }
                )
            }
        }
    }
}

@Composable
private fun DraftsContent(
    navController: NavHostController,
    searchQuery: String
) {
    // Mock draft forms
    val drafts = remember {
        listOf(
            FormListItem("Daily Log - Zone B", "mmu_daily_log", "DRAFT", System.currentTimeMillis() - 86400000),
            FormListItem("Maintenance Record - MMU003", "maintenance_record", "DRAFT", System.currentTimeMillis() - 172800000)
        )
    }
    
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (drafts.isEmpty()) {
            item {
                EmptyStateCard("No draft forms")
            }
        } else {
            items(drafts) { form ->
                FormListCard(
                    form = form,
                    onClick = { navController.navigate("forms/edit/${form.id}") }
                )
            }
        }
    }
}

@Composable
private fun SubmittedContent(
    navController: NavHostController,
    searchQuery: String
) {
    // Mock submitted forms
    val submittedForms = remember {
        listOf(
            FormListItem("Daily Log - Zone A", "mmu_daily_log", "COMPLETED", System.currentTimeMillis() - 3600000),
            FormListItem("Quality Report - Batch 144", "quality_report", "COMPLETED", System.currentTimeMillis() - 90000000)
        )
    }
    
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (submittedForms.isEmpty()) {
            item {
                EmptyStateCard("No submitted forms")
            }
        } else {
            items(submittedForms) { form ->
                FormListCard(
                    form = form,
                    onClick = { navController.navigate("forms/edit/${form.id}") }
                )
            }
        }
    }
}

@Composable
private fun QuickCreateCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(120.dp)
            .height(80.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FormTypeCard(
    formType: FormTypeItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                formType.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formType.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formType.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                AECIIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FormListCard(
    form: FormListItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = form.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = form.type.replace("_", " ").uppercase(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    color = when (form.status) {
                        "COMPLETED" -> MaterialTheme.colorScheme.primaryContainer
                        "IN_PROGRESS" -> MaterialTheme.colorScheme.tertiaryContainer
                        "DRAFT" -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = form.status.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Modified: ${placeholderFormatDate(form.lastModified)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class FormTypeItem(
    val title: String,
    val description: String,
    val id: String,
    val icon: ImageVector
)

data class FormListItem(
    val title: String,
    val type: String,
    val status: String,
    val lastModified: Long,
    val id: String = "$type-${System.currentTimeMillis()}"
)

@Composable
fun FormCreationScreen(
    navController: NavHostController,
    formType: String?,
    modifier: Modifier = Modifier
) {
    val formViewModel: FormViewModel = hiltViewModel()
    val uiState by formViewModel.uiState.collectAsState()
    val formTemplate by formViewModel.formTemplate.collectAsState()
    val formData by formViewModel.formData.collectAsState()
    val currentForm by formViewModel.currentForm.collectAsState()
    val validationErrors by formViewModel.validationErrors.collectAsState()
    val exportPath by formViewModel.exportPath.collectAsState()
    val showReportActionDialog by formViewModel.showReportActionDialog.collectAsState()
    
    LaunchedEffect(formType) {
        formType?.let { typeString ->
            try {
                val formTypeEnum = FormType.valueOf(typeString)
                formViewModel.initializeForm(formTypeEnum)
            } catch (e: Exception) {
                // Handle invalid form type, use default
                formViewModel.initializeForm(FormType.MMU_DAILY_LOG)
            }
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // App Bar
        Surface(
            color = MaterialTheme.colorScheme.primary
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        text = "Create Form",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Row {
                    TextButton(
                        onClick = { formViewModel.saveDraft() }
                    ) {
                        Text(
                            text = "Save Draft",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Button(
                        onClick = { formViewModel.submitForm() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
        
        // Form Content
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (formTemplate != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                // Form Header
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = formTemplate?.name ?: "Form",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (!formTemplate?.description.isNullOrBlank()) {
                                Text(
                                    text = formTemplate?.description ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
                
                // Dynamic Form Fields - Individual Sections
                formTemplate?.let { template ->
                    template.sections.forEach { section ->
                        item {
                            FormSectionCard(
                                section = section,
                                formData = formData,
                                validationErrors = validationErrors.associate { it.field to it.message },
                                onFieldValueChanged = { fieldId, value ->
                                    formViewModel.updateField(fieldId, value)
                                },
                                onPhotoCapture = { fieldId -> 
                                    // TODO: Implement photo capture
                                },
                                onDatePicker = { fieldId, date ->
                                    // TODO: Implement date picker
                                },
                                onTimePicker = { fieldId, time ->
                                    // TODO: Implement time picker
                                }
                            )
                        }
                    }
                }
                
                // Form Actions
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { formViewModel.saveDraft() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(AECIIcons.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Draft")
                        }
                        
                        Button(
                            onClick = { formViewModel.submitForm() },
                            modifier = Modifier.weight(1f),
                            enabled = true // TODO: Add form validation
                        ) {
                            Icon(AECIIcons.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit")
                        }
                    }
                }
            }
            }
        }
    }
    
    // Report Action Dialog
    if (showReportActionDialog && exportPath != null) {
        ReportActionDialog(
            reportPath = exportPath,
            reportName = exportPath?.getFileName() ?: "Report",
            onDismiss = { formViewModel.dismissReportActionDialog() }
        )
    }
    
    // Handle state changes
    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted && !showReportActionDialog) {
            navController.navigateUp()
        }
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar
        }
    }
}

@Composable
fun FormEditScreen(
    navController: NavHostController,
    formId: String?,
    modifier: Modifier = Modifier
) {
    val formViewModel: FormViewModel = hiltViewModel()
    val uiState by formViewModel.uiState.collectAsState()
    val formTemplate by formViewModel.formTemplate.collectAsState()
    val formData by formViewModel.formData.collectAsState()
    val currentForm by formViewModel.currentForm.collectAsState()
    val validationErrors by formViewModel.validationErrors.collectAsState()
    val exportPath by formViewModel.exportPath.collectAsState()
    val showReportActionDialog by formViewModel.showReportActionDialog.collectAsState()
    
    LaunchedEffect(formId) {
        formId?.let {
            formViewModel.loadForm(it)
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // App Bar
        Surface(
            color = MaterialTheme.colorScheme.primary
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Column {
                        Text(
                            text = "Edit Form",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "ID: $formId",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Row {
                    IconButton(
                        onClick = { 
                            // Share/Export functionality
                            formViewModel.exportToPdf("current_form_id")
                        }
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Export",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    if (currentForm?.status == com.aeci.mmucompanion.domain.model.FormStatus.DRAFT) {
                        TextButton(
                            onClick = { formViewModel.saveDraft() }
                        ) {
                            Text(
                                text = "Save",
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Button(
                            onClick = { formViewModel.submitForm() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("Submit")
                        }
                    }
                }
            }
        }
        
        // Form Content
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (currentForm != null && formTemplate != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                // Logo at the top
                item {
                    Image(
                        painter = painterResource(id = R.drawable.aeci_logo),
                        contentDescription = "AECI Logo",
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        alignment = Alignment.Center
                    )
                }
                
                // Form Status Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (currentForm!!.status) {
                                com.aeci.mmucompanion.domain.model.FormStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                                com.aeci.mmucompanion.domain.model.FormStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiaryContainer
                                com.aeci.mmucompanion.domain.model.FormStatus.DRAFT -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = formTemplate!!.name,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Text(
                                        text = "Created: ${currentForm!!.createdAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (currentForm!!.equipmentId != null) {
                                        Text(
                                            text = "Equipment: ${currentForm!!.equipmentId}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = currentForm!!.status.name.replace("_", " "),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Dynamic Form Fields (read-only if completed) - Individual Sections
                formTemplate!!.sections.forEach { section ->
                    item {
                        FormSectionCard(
                            section = section,
                            formData = formData,
                            validationErrors = validationErrors.associate { it.field to it.message },
                            onFieldValueChanged = if (currentForm!!.status == com.aeci.mmucompanion.domain.model.FormStatus.DRAFT) {
                                { fieldId, value -> formViewModel.updateField(fieldId, value) }
                            } else { _, _ -> }, // Read-only for completed forms
                            onPhotoCapture = { fieldId -> 
                                // TODO: Implement photo capture
                            },
                            onDatePicker = { fieldId, date ->
                                // TODO: Implement date picker
                            },
                            onTimePicker = { fieldId, time ->
                                // TODO: Implement time picker
                            }
                        )
                    }
                }
                
                // Actions (only for draft forms)
                if (currentForm!!.status == com.aeci.mmucompanion.domain.model.FormStatus.DRAFT) {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { formViewModel.saveDraft() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(AECIIcons.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save Changes")
                            }
                            
                            Button(
                                onClick = { formViewModel.submitForm() },
                                modifier = Modifier.weight(1f),
                                enabled = true // TODO: Add form validation
                            ) {
                                Icon(AECIIcons.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Submit")
                            }
                        }
                    }
                } else {
                    // Export options for completed forms
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { formViewModel.exportToPdf("current_form_id") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(AECIIcons.PictureAsPdf, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Export PDF")
                            }
                            
                            OutlinedButton(
                                onClick = { formViewModel.exportToExcel() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(AECIIcons.TableChart, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Export Excel")
                            }
                        }
                    }
                }
            }
            }
        } else {
            // Error state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        AECIIcons.Error,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Form not found",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { navController.navigateUp() }
                    ) {
                        Text("Go Back")
                    }
                }
            }
        }
    }
    
    // Handle state changes
    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            navController.navigateUp()
        }
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar
        }
    }
}

@Composable
fun EquipmentListScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: EquipmentScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Equipment types for filtering
    val equipmentTypes = remember(state.equipment) {
        listOf("All") + state.equipment.map { it.type.name }.distinct().sorted()
    }
    
    // Group equipment by type for better organization  
    val equipmentGroups = remember(state.equipment, state.selectedType) {
        val filteredList = if (state.selectedType == "All") {
            state.equipment
        } else {
            state.equipment.filter { it.type.name == state.selectedType }
        }
        
        // Group by equipment type
        filteredList.groupBy { it.type.name }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Current Site Display
        state.currentUserSite?.let { site ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Current Site: ${site.name}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Search Bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            label = { Text("Search equipment...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Filter Chips Row 1: Status Filter
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("All", "OPERATIONAL", "MAINTENANCE", "BREAKDOWN")
            items(filters) { filter ->
                FilterChip(
                    selected = state.selectedStatus == filter,
                    onClick = { viewModel.updateSelectedStatus(filter) },
                    label = { Text(filter.replace("_", " ")) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Filter Chips Row 2: Equipment Type Filter
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(equipmentTypes) { type ->
                FilterChip(
                    selected = state.selectedType == type,
                    onClick = { viewModel.updateSelectedType(type) },
                    label = { Text(type.replace("_", " ")) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Loading State
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // Error State
        state.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Equipment List - Grouped by Type
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (equipmentGroups.isEmpty() && !state.isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (state.searchQuery.isBlank()) "No equipment found at this site" else "No equipment matches your search",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            } else {
                equipmentGroups.forEach { (groupType, equipmentList) ->
                    item {
                        // Group Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${groupType.replace("_", " ")} (${equipmentList.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            HorizontalDivider(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                        }
                    }
                    
                    items(equipmentList) { equipment ->
                        PlaceholderEquipmentCard(
                            equipment = equipment,
                            onClick = { navController.navigate("equipment/${equipment.id}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderEquipmentCard(
    equipment: Equipment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Equipment Image
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (equipment.imageUri != null) {
                        AsyncImage(
                            model = equipment.imageUri,
                            contentDescription = "Equipment image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = when (equipment.type) {
                                EquipmentType.MMU -> Icons.Default.Factory
                                EquipmentType.PUMP -> Icons.Default.WaterDrop
                                EquipmentType.CONVEYOR -> Icons.Default.LinearScale
                                EquipmentType.CRUSHER -> Icons.Default.Construction
                                EquipmentType.SEPARATOR -> Icons.Default.FilterAlt
                                EquipmentType.CLASSIFIER -> Icons.Default.Sort
                                EquipmentType.OTHER -> Icons.Default.Engineering
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = equipment.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${equipment.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Location: ${equipment.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Type: ${equipment.type.name.replace("_", " ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    color = when (equipment.status) {
                        EquipmentStatus.OPERATIONAL -> MaterialTheme.colorScheme.primaryContainer
                        EquipmentStatus.MAINTENANCE -> MaterialTheme.colorScheme.tertiaryContainer  
                        EquipmentStatus.BREAKDOWN -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = equipment.status.name.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (equipment.status) {
                            EquipmentStatus.OPERATIONAL -> MaterialTheme.colorScheme.onPrimaryContainer
                            EquipmentStatus.MAINTENANCE -> MaterialTheme.colorScheme.onTertiaryContainer
                            EquipmentStatus.BREAKDOWN -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Last Inspection",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = placeholderFormatDate(equipment.lastMaintenanceDate ?: 0),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Column {
                    Text(
                        text = "Next Maintenance", 
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = placeholderFormatDate(equipment.nextMaintenanceDate ?: 0),
                        style = MaterialTheme.typography.bodySmall,
                        color = if ((equipment.nextMaintenanceDate ?: 0) < System.currentTimeMillis()) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

private fun placeholderFormatDate(timestamp: Long): String {
    if (timestamp == 0L) return "Not set"
    val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return formatter.format(java.util.Date(timestamp))
}

@Composable
fun EquipmentDetailsScreen(
    navController: NavHostController,
    equipmentId: String?,
    modifier: Modifier = Modifier
) {
    // Mock equipment data - this would come from ViewModel
    val equipment = remember {
        Equipment(
            id = equipmentId ?: "MMU001",
            name = "Mobile Mining Unit 001",
            type = EquipmentType.MMU,
            status = EquipmentStatus.OPERATIONAL,
            location = "Zone A",
            model = "Caterpillar 994K",
            serialNumber = "CAT994K-2023-001",
            manufacturer = "Caterpillar Inc.",
            installationDate = System.currentTimeMillis() - 31536000000L, // 1 year ago
            lastMaintenanceDate = System.currentTimeMillis() - 86400000L, // 1 day ago
            nextMaintenanceDate = System.currentTimeMillis() + 604800000L, // 7 days from now
            operatingParameters = mapOf(
                "Engine Power" to "850 HP",
                "Operating Weight" to "102,000 kg",
                "Bucket Capacity" to "12.3 m³",
                "Max Speed" to "65 km/h"
            ),
            specifications = mapOf(
                "Engine Power" to "850 HP",
                "Operating Weight" to "102,000 kg",
                "Bucket Capacity" to "12.3 m³",
                "Max Speed" to "65 km/h"
            ),
            siteId = "site_001"
        )
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Maintenance", "Inspections", "Documents")

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = equipment.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "ID: ${equipment.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Surface(
                        color = when (equipment.status) {
                            EquipmentStatus.OPERATIONAL -> MaterialTheme.colorScheme.primary
                            EquipmentStatus.MAINTENANCE -> MaterialTheme.colorScheme.tertiary
                            EquipmentStatus.BREAKDOWN -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.secondary
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = equipment.status.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Placeholder for image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
                    Icon(
                        imageVector = AECIIcons.Image,
                        contentDescription = "Equipment Image",
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
        }

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Tab Content
        when (selectedTab) {
            0 -> OverviewTab(equipment)
            1 -> MaintenanceTab(equipment)
            2 -> InspectionsTab(equipment)
            3 -> DocumentsTab(equipment)
        }
    }
}

@Composable
private fun OverviewTab(equipment: Equipment) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Uptime",
                    value = "98.5%",
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.primary
                )
                QuickStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Last Service",
                    value = "2 days ago",
                    icon = Icons.Default.Build,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        
        // Equipment Details
        item {
            Text(
                text = "Equipment Details",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    PlaceholderDetailRow("Equipment ID", equipment.id)
                    PlaceholderDetailRow("Model", equipment.model ?: "N/A")
                    PlaceholderDetailRow("Manufacturer", equipment.manufacturer ?: "N/A")
                    PlaceholderDetailRow("Installation Date", placeholderFormatDate(equipment.installationDate ?: 0))
                }
            }
        }
        
        // Specifications
        item {
            Text(
                text = "Specifications",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    equipment.specifications?.forEach { (key, value) ->
                        PlaceholderDetailRow(key, value.toString())
                    }
                }
            }
        }
    }
}

@Composable
private fun MaintenanceTab(equipment: Equipment) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Next Maintenance
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            AECIIcons.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Next Scheduled Maintenance",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = placeholderFormatDate(equipment.nextMaintenanceDate ?: 0),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    
                    val daysUntil = ((equipment.nextMaintenanceDate ?: 0) - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)
                    Text(
                        text = if (daysUntil > 0) "$daysUntil days remaining" else "${-daysUntil} days overdue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        // Maintenance History
        item {
            Text(
                text = "Maintenance History",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        items(3) { index ->
            MaintenanceHistoryCard(
                date = System.currentTimeMillis() - (index + 1) * 2592000000L, // Monthly intervals
                type = when (index) {
                    0 -> "Routine Service"
                    1 -> "Oil Change"
                    else -> "Belt Replacement"
                },
                technician = "John Smith",
                notes = "Completed successfully. All systems operational."
            )
        }
    }
}

@Composable
private fun InspectionsTab(equipment: Equipment) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Create New Inspection Button
        item {
            Button(
                onClick = { /* Navigate to inspection form */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Inspection")
            }
        }
        
        // Recent Inspections
        item {
            Text(
                text = "Recent Inspections",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        items(5) { index ->
            InspectionCard(
                date = System.currentTimeMillis() - index * 86400000L, // Daily intervals
                inspector = "Jane Doe",
                type = "Daily Inspection",
                status = if (index == 0) "COMPLETED" else "PENDING",
                findings = if (index == 1) "Minor oil leak detected" else "No issues found"
            )
        }
    }
}

@Composable 
private fun DocumentsTab(equipment: Equipment) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Equipment Documents",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        val documents = listOf(
            "Operator Manual" to "PDF",
            "Maintenance Schedule" to "PDF", 
            "Warranty Certificate" to "PDF",
            "Installation Photos" to "Images",
            "Safety Guidelines" to "PDF"
        )
        
        items(documents) { (name, type) ->
            DocumentCard(
                name = name,
                type = type,
                onClick = { /* Open document */ }
            )
        }
    }
}

@Composable
private fun QuickStatCard(
    title: String,
    value: String, 
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlaceholderDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium
        )
        Text(text = value)
    }
}

@Composable
private fun MaintenanceHistoryCard(
    date: Long,
    type: String,
    technician: String,
    notes: String
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = type,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = placeholderFormatDate(date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Technician: $technician",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun InspectionCard(
    date: Long,
    inspector: String,
    type: String,
    status: String,
    findings: String
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = type,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Inspector: $inspector",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = placeholderFormatDate(date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    color = if (status == "COMPLETED") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            if (findings.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Findings: $findings",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun DocumentCard(
    name: String,
    type: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (type) {
                    "PDF" -> AECIIcons.PictureAsPdf
                    "Images" -> AECIIcons.Image
                    else -> AECIIcons.InsertDriveFile
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = type,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                AECIIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ReportsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Analytics", "Export", "Trends", "Compliance")
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = "Reports & Analytics",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Tab Content
        when (selectedTab) {
            0 -> AnalyticsTab()
            1 -> ExportTab(navController = navController)
            2 -> TrendsTab()
            3 -> ComplianceTab()
        }
    }
}

@Composable
private fun AnalyticsTab() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Stats
        item {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AnalyticsCard(
                        title = "Forms This Month",
                        value = "245",
                        change = "+12%",
                        isPositive = true,
                        icon = AECIIcons.Assignment
                    )
                }
                item {
                    AnalyticsCard(
                        title = "Equipment Uptime",
                        value = "96.8%",
                        change = "+2.1%",
                        isPositive = true,
                        icon = AECIIcons.Speed
                    )
                }
                item {
                    AnalyticsCard(
                        title = "Compliance Score",
                        value = "94%",
                        change = "-1.5%",
                        isPositive = false,
                        icon = AECIIcons.Shield
                    )
                }
            }
        }
        
        // Form Completion Chart
        item {
            Text(
                text = "Form Completion Trends",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Last 7 Days",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Simple chart representation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val chartData = listOf(12, 15, 8, 20, 18, 25, 22)
                        chartData.forEachIndexed { index, value ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height((value * 4).dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                ) {}
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Equipment Status Distribution
        item {
            Text(
                text = "Equipment Status",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    PlaceholderEquipmentStatusRow("Operational", 85, MaterialTheme.colorScheme.primary)
                    PlaceholderEquipmentStatusRow("Maintenance", 12, MaterialTheme.colorScheme.tertiary)
                    PlaceholderEquipmentStatusRow("Out of Service", 3, MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun ExportTab(navController: NavHostController) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Export Reports",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Date Range Selector
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Date Range",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = "2024-01-01",
                            onValueChange = { },
                            label = { Text("From") },
                            modifier = Modifier.weight(1f),
                            readOnly = true,
                            trailingIcon = {
                                Icon(AECIIcons.CalendarToday, contentDescription = null)
                            }
                        )
                        OutlinedTextField(
                            value = "2024-01-31",
                            onValueChange = { },
                            label = { Text("To") },
                            modifier = Modifier.weight(1f),
                            readOnly = true,
                            trailingIcon = {
                                Icon(AECIIcons.CalendarToday, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }
        
        // Export Options
        item {
            Text(
                text = "Available Reports",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            val exportOptions = listOf(
                ExportOption("Form Summary Report", "Complete overview of all forms", "PDF, Excel"),
                ExportOption("Equipment Report", "Equipment status and maintenance", "PDF, Excel"), 
                ExportOption("Compliance Report", "Safety and compliance metrics", "PDF"),
                ExportOption("Production Report", "Daily production statistics", "Excel"),
                ExportOption("Maintenance Schedule", "Upcoming maintenance tasks", "PDF, Excel")
            )
            
            exportOptions.forEach { option ->
                ExportOptionCard(
                    option = option,
                    onExportClick = { navController.navigate("export") }
                )
            }
        }
    }
}

@Composable
private fun TrendsTab() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Trend Analysis",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Production Trends
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Production Trends",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TrendIndicator("This Month", "1,245 tons", "+8.5%", true)
                        TrendIndicator("This Week", "312 tons", "+12.1%", true)
                        TrendIndicator("Yesterday", "67 tons", "-3.2%", false)
                    }
                }
            }
        }
        
        // Safety Trends
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Safety Metrics",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TrendIndicator("Incidents", "0", "0%", true)
                        TrendIndicator("Safety Score", "98%", "+1.2%", true)
                        TrendIndicator("Compliance", "96%", "-0.8%", false)
                    }
                }
            }
        }
        
        // Efficiency Trends
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Efficiency Metrics",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TrendIndicator("Equipment Utilization", "87%", "+3.5%", true)
                        TrendIndicator("Form Completion Rate", "94%", "+2.1%", true)
                        TrendIndicator("Response Time", "12 min", "-15%", true)
                    }
                }
            }
        }
    }
}

@Composable
private fun ComplianceTab() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Compliance Overview",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Overall Compliance Score
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Overall Compliance Score",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "94%",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Excellent",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        // Compliance Categories
        item {
            Text(
                text = "By Category",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            val complianceCategories = listOf(
                ComplianceCategory("Safety Inspections", 98, MaterialTheme.colorScheme.primary),
                ComplianceCategory("Equipment Maintenance", 92, MaterialTheme.colorScheme.tertiary),
                ComplianceCategory("Documentation", 96, MaterialTheme.colorScheme.secondary),
                ComplianceCategory("Training Records", 89, MaterialTheme.colorScheme.error)
            )
            
            complianceCategories.forEach { category ->
                ComplianceCategoryCard(category)
            }
        }
        
        // Recent Issues
        item {
            Text(
                text = "Recent Issues",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            val issues = listOf(
                "MMU-003: Overdue maintenance documentation",
                "PUMP-007: Missing safety inspection record",
                "Zone-B: Fire extinguisher check not completed"
            )
            
            if (issues.isEmpty()) {
                Card {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No compliance issues",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            } else {
                issues.forEach { issue ->
                    Card {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = issue,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsCard(
    title: String,
    value: String,
    change: String,
    isPositive: Boolean,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.width(150.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = change,
                style = MaterialTheme.typography.labelSmall,
                color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun PlaceholderEquipmentStatusRow(
    label: String,
    percentage: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        LinearProgressIndicator(
            progress = percentage / 100f,
            color = color,
            modifier = Modifier
                .width(100.dp)
                .height(8.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExportOptionCard(
    option: ExportOption,
    onExportClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = option.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = option.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Formats: ${option.formats}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Button(
                    onClick = onExportClick,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text("Export")
                }
            }
        }
    }
}

@Composable
private fun TrendIndicator(
    label: String,
    value: String,
    change: String,
    isPositive: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = change,
            style = MaterialTheme.typography.labelSmall,
            color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun ComplianceCategoryCard(category: ComplianceCategory) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall
                )
                LinearProgressIndicator(
                    progress = category.score / 100f,
                    color = category.color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "${category.score}%",
                style = MaterialTheme.typography.titleMedium,
                color = category.color
            )
        }
    }
}

data class ExportOption(
    val title: String,
    val description: String,
    val formats: String
)

data class ComplianceCategory(
    val name: String,
    val score: Int,
    val color: androidx.compose.ui.graphics.Color
)

@Composable
fun SettingsScreen(
    navController: NavHostController,
    authViewModel: com.aeci.mmucompanion.presentation.viewmodel.AuthViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { authViewModel.logout() }
        ) {
            Text("Logout")
        }
    }
}

@Composable
fun FormsCategoryScreen(
    navController: NavHostController,
    category: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "${category.replaceFirstChar { it.uppercase() }} Forms",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Form Types for the category
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(getFormTypesForCategory(category)) { formType ->
                FormTypeCard(
                    formType = formType,
                    onClick = { 
                        navController.navigate("forms/create/${formType.formType}")
                    }
                )
            }
        }
    }
}

@Composable
private fun FormTypeCard(
    formType: FormTypeInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = formType.icon,
                contentDescription = formType.title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formType.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formType.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Icon(
                imageVector = AECIIcons.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

data class FormTypeInfo(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val formType: String
)

fun getFormTypesForCategory(category: String): List<FormTypeInfo> {
    return when (category.lowercase()) {
        "production" -> listOf(
            FormTypeInfo(
                "MMU Daily Log",
                "Record daily mining unit operations and activities",
                AECIIcons.Today,
                "MMU_DAILY_LOG"
            ),
            FormTypeInfo(
                "MMU Quality Report",
                "Document quality control checks and results",
                AECIIcons.Assessment,
                "MMU_QUALITY_REPORT"
            ),
            FormTypeInfo(
                "MMU Handover Certificate",
                "Record shift handover information and status",
                AECIIcons.Assignment,
                "MMU_HANDOVER_CERTIFICATE"
            ),
            FormTypeInfo(
                "Availability & Utilization",
                "Track equipment availability and utilization metrics",
                AECIIcons.Analytics,
                "AVAILABILITY_UTILIZATION"
            ),
            FormTypeInfo(
                "Blast Hole Log",
                "Document blast hole drilling operations",
                AECIIcons.Construction,
                "BLAST_HOLE_LOG"
            ),
            FormTypeInfo(
                "Pre-task Safety",
                "Complete safety checks before task execution",
                AECIIcons.Security,
                "PRETASK_SAFETY"
            )
        )
        "maintenance" -> listOf(
            FormTypeInfo(
                "MMU Chassis Maintenance",
                "Record maintenance activities on MMU chassis",
                AECIIcons.Construction,
                "MMU_CHASSIS_MAINTENANCE"
            ),
            FormTypeInfo(
                "On Bench MMU Inspection",
                "Perform detailed inspection of bench-mounted equipment",
                AECIIcons.Assessment,
                "ON_BENCH_MMU_INSPECTION"
            ),
            FormTypeInfo(
                "90 Day Pump Inspection",
                "Quarterly comprehensive pump system inspection",
                AECIIcons.Schedule,
                "PUMP_90_DAY_INSPECTION"
            ),
            FormTypeInfo(
                "Pump Weekly Check",
                "Weekly routine pump maintenance checks",
                AECIIcons.Checklist,
                "PUMP_WEEKLY_CHECK"
            ),
            FormTypeInfo(
                "PC Pump Pressure Test",
                "Test pump pressure systems and safety mechanisms",
                AECIIcons.Speed,
                "PC_PUMP_PRESSURE_TEST"
            ),
            FormTypeInfo(
                "Fire Extinguisher Inspection",
                "Monthly fire safety equipment inspection",
                AECIIcons.FireExtinguisher,
                "FIRE_EXTINGUISHER_INSPECTION"
            ),
            FormTypeInfo(
                "Monthly Process Maintenance",
                "Monthly maintenance of process equipment",
                AECIIcons.CalendarMonth,
                "MONTHLY_PROCESS_MAINTENANCE"
            ),
            FormTypeInfo(
                "Job Card",
                "Create and track maintenance work orders",
                AECIIcons.Work,
                "JOB_CARD"
            )
        )
        else -> emptyList()
    }
}

@Composable
fun AdminReportsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    initialFilter: String? = null,
    initialTab: String? = null
) {
    val initialTabIndex = when (initialTab) {
        "statistics" -> 3
        "equipment" -> 1
        "users" -> 2
        else -> 0
    }
    var selectedTab by remember { mutableStateOf(initialTabIndex) }
    val tabs = listOf("Form Reports", "Equipment Reports", "User Reports", "System Reports")
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = "Admin Reports",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> AdminFormReportsTab(navController, initialFilter)
            1 -> AdminEquipmentReportsTab(navController, initialFilter)
            2 -> AdminUserReportsTab(navController, initialFilter)
            3 -> AdminSystemReportsTab(navController, initialFilter)
        }
    }
}

@Composable
private fun AdminFormReportsTab(navController: NavHostController, filter: String? = null) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Show filter indicator if active
        if (filter == "downloads") {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            AECIIcons.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Showing Download History",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        
        // Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Forms This Month",
                    value = "1,247",
                    change = "+18%",
                    icon = AECIIcons.Assignment
                )
                AdminSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Completion Rate",
                    value = "94.2%",
                    change = "+2.1%",
                    icon = AECIIcons.Success
                )
            }
        }
        
        // Form Type Breakdown
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Form Submissions by Type",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    val formTypeData = listOf(
                        "Daily Logs" to 456,
                        "Quality Reports" to 234,
                        "Maintenance Forms" to 189,
                        "Safety Inspections" to 167,
                        "Job Cards" to 201
                    )
                    
                    formTypeData.forEach { (type, count) ->
                        AdminReportDataRow(type, count.toString())
                    }
                }
            }
        }
        
        // Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Export functionality */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(AECIIcons.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Report")
                }
                Button(
                    onClick = { /* Generate detailed report */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(AECIIcons.Assessment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Detailed")
                }
            }
        }
    }
}

@Composable
private fun AdminEquipmentReportsTab(navController: NavHostController, filter: String? = null) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Equipment Status Overview
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Equipment Status Overview",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    val statusData = listOf(
                        "Operational" to 85,
                        "Maintenance" to 12,
                        "Out of Service" to 3
                    )
                    
                    statusData.forEach { (status, percentage) ->
                        AdminEquipmentStatusRow(status, percentage)
                    }
                }
            }
        }
        
        // Maintenance Summary
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Maintenance Summary",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    AdminReportDataRow("Scheduled This Week", "8")
                    AdminReportDataRow("Overdue Maintenance", "2")
                    AdminReportDataRow("Completed This Month", "23")
                    AdminReportDataRow("Average Downtime", "4.2 hrs")
                }
            }
        }
    }
}

@Composable
private fun AdminUserReportsTab(navController: NavHostController, filter: String? = null) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Activity Summary
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "User Activity Summary",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    AdminReportDataRow("Active Users", "45")
                    AdminReportDataRow("Average Session Time", "2h 15m")
                    AdminReportDataRow("Forms per User/Day", "6.8")
                    AdminReportDataRow("Login Success Rate", "98.7%")
                }
            }
        }
        
        // Top Performers
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Top Performers This Month",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    AdminUserPerformanceRow("John Smith", "127 forms", "98.5%")
                    AdminUserPerformanceRow("Sarah Johnson", "119 forms", "97.1%")
                    AdminUserPerformanceRow("Mike Wilson", "108 forms", "96.8%")
                }
            }
        }
    }
}

@Composable
private fun AdminSystemReportsTab(navController: NavHostController, filter: String? = null) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // System Health
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "System Health",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    AdminReportDataRow("System Uptime", "99.8%")
                    AdminReportDataRow("Average Response Time", "0.8s")
                    AdminReportDataRow("Error Rate", "0.2%")
                    AdminReportDataRow("Active Connections", "45")
                }
            }
        }
        
        // Storage & Sync
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Data & Synchronization",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    AdminReportDataRow("Total Forms Stored", "15,847")
                    AdminReportDataRow("Sync Success Rate", "99.5%")
                    AdminReportDataRow("Pending Sync", "3")
                    AdminReportDataRow("Backup Status", "Current")
                }
            }
        }
    }
}

@Composable
fun UserManagementScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showAddUserDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "User Management",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Button(
                    onClick = { showAddUserDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add User")
                }
            }
        }
        
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Search and Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search users...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "Admin", "Technician", "Operator", "Inactive")
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val users = getUsersForManagement().filter { user ->
                    (selectedFilter == "All" || user.role == selectedFilter) &&
                    (searchQuery.isBlank() || user.name.contains(searchQuery, ignoreCase = true) ||
                     user.email.contains(searchQuery, ignoreCase = true))
                }
                
                items(users) { user ->
                    UserManagementCard(
                        user = user,
                        onEdit = { /* Edit user */ },
                        onToggleStatus = { /* Toggle active status */ },
                        onResetPassword = { /* Reset password */ }
                    )
                }
            }
        }
    }
    
    // Add User Dialog
    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onConfirm = { name, email, role ->
                // Add user logic
                showAddUserDialog = false
            }
        )
    }
}

@Composable
fun SystemSettingsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("General", "Security", "Notifications", "Sync", "Backup")
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = "System Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Tab Content
        when (selectedTab) {
            0 -> GeneralSettingsTab()
            1 -> SecuritySettingsTab()
            2 -> NotificationSettingsTab()
            3 -> SyncSettingsTab()
            4 -> BackupSettingsTab()
        }
    }
}

@Composable
fun AuditLogsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedDateRange by remember { mutableStateOf("Last 7 Days") }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = "Audit Logs",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Action Type Filter
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Action Type",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filters = listOf("All", "Login", "Form", "Equipment", "User", "Export")
                        items(filters) { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter) }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Date Range Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val dateRanges = listOf("Today", "Last 7 Days", "Last 30 Days", "All Time")
                dateRanges.forEach { range ->
                    FilterChip(
                        selected = selectedDateRange == range,
                        onClick = { selectedDateRange = range },
                        label = { Text(range) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Audit Log List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val auditLogs = getAuditLogs().filter { log ->
                    selectedFilter == "All" || log.action.contains(selectedFilter, ignoreCase = true)
                }
                
                items(auditLogs) { log ->
                    AuditLogCard(log = log)
                }
            }
        }
    }
}

@Composable
fun EquipmentMaintenanceScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Scheduled", "Overdue", "Completed", "Requests")
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Equipment Maintenance",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Button(
                    onClick = { navController.navigate("maintenance/create") }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Schedule Maintenance")
                }
            }
        }
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> ScheduledMaintenanceTab()
            1 -> OverdueMaintenanceTab()
            2 -> CompletedMaintenanceTab()
            3 -> MaintenanceRequestsTab()
        }
    }
}

@Composable
fun JobCardManagementScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Active", "Assigned", "Completed", "Templates")
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Job Card Management",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Button(
                    onClick = { navController.navigate("job_cards/create") }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Job Card")
                }
            }
        }
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> ActiveJobCardsTab(navController)
            1 -> AssignedJobCardsTab(navController)
            2 -> CompletedJobCardsTab(navController)
            3 -> JobCardTemplatesTab(navController)
        }
    }
}

@Composable
fun TaskProgressUpdateScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("All") }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = "Task Progress Update",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search tasks...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Status Filter
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statuses = listOf("All", "Not Started", "In Progress", "Completed", "On Hold")
                items(statuses) { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        label = { Text(status) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tasks List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tasks = getTasksForProgressUpdate().filter { task ->
                    (selectedStatus == "All" || task.status == selectedStatus) &&
                    (searchQuery.isBlank() || task.title.contains(searchQuery, ignoreCase = true))
                }
                
                items(tasks) { task ->
                    TaskProgressCard(
                        task = task,
                        onUpdateProgress = { newProgress ->
                            // Update task progress logic
                        },
                        onUpdateStatus = { newStatus ->
                            // Update task status logic
                        }
                    )
                }
            }
        }
    }
}

// Supporting composables and data classes follow...

@Composable
private fun AdminSummaryCard(
    title: String,
    value: String,
    change: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = change,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (change.startsWith("+")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AdminReportDataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AdminEquipmentStatusRow(status: String, percentage: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        LinearProgressIndicator(
            progress = percentage / 100f,
            modifier = Modifier
                .width(100.dp)
                .height(8.dp),
            color = when (status) {
                "Operational" -> MaterialTheme.colorScheme.primary
                "Maintenance" -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.error
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AdminUserPerformanceRow(name: String, formsCount: String, accuracy: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = formsCount,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = accuracy,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun UserManagementCard(
    user: UserManagementData,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit,
    onResetPassword: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Role: ${user.role}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    color = if (user.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (user.isActive) "Active" else "Inactive",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit")
                }
                OutlinedButton(
                    onClick = onToggleStatus,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (user.isActive) "Deactivate" else "Activate")
                }
                OutlinedButton(
                    onClick = onResetPassword,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset Password")
                }
            }
        }
    }
}

@Composable
private fun AddUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Technician") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New User") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = selectedRole,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Role") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, email, selectedRole) },
                enabled = name.isNotBlank() && email.isNotBlank()
            ) {
                Text("Add User")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun GeneralSettingsTab() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSection("Application") {
                SettingItem("App Version", "1.0.0")
                SettingItem("Build Number", "100")
                SettingItem("Last Updated", "2024-01-15")
            }
        }
        
        item {
            SettingsSection("Server Connection") {
                SettingItem("Server URL", "192.168.1.100:3000")
                SettingItem("Connection Status", "Connected")
                SettingItem("Last Sync", "2 minutes ago")
            }
        }
    }
}

@Composable
private fun SecuritySettingsTab() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSection("Authentication") {
                SettingSwitchItem("Require Biometric Auth", true) { }
                SettingSwitchItem("Auto-logout", true) { }
                SettingItem("Session Timeout", "30 minutes")
            }
        }
        
        item {
            SettingsSection("Password Policy") {
                SettingItem("Minimum Length", "8 characters")
                SettingSwitchItem("Require Special Characters", true) { }
                SettingSwitchItem("Force Password Change", false) { }
            }
        }
    }
}

@Composable
private fun NotificationSettingsTab() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSection("Push Notifications") {
                SettingSwitchItem("Form Reminders", true) { }
                SettingSwitchItem("Maintenance Alerts", true) { }
                SettingSwitchItem("System Updates", false) { }
            }
        }
        
        item {
            SettingsSection("Email Notifications") {
                SettingSwitchItem("Daily Reports", true) { }
                SettingSwitchItem("Critical Alerts", true) { }
                SettingSwitchItem("Weekly Summary", false) { }
            }
        }
    }
}

@Composable
private fun SyncSettingsTab() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSection("Automatic Sync") {
                SettingSwitchItem("Auto Sync", true) { }
                SettingItem("Sync Interval", "Every 5 minutes")
                SettingSwitchItem("Sync on WiFi Only", true) { }
            }
        }
        
        item {
            SettingsSection("Manual Actions") {
                Button(
                    onClick = { /* Force sync */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Force Sync Now")
                }
            }
        }
    }
}

@Composable
private fun BackupSettingsTab() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSection("Backup Configuration") {
                SettingSwitchItem("Auto Backup", true) { }
                SettingItem("Backup Frequency", "Daily")
                SettingItem("Retention Period", "30 days")
            }
        }
        
        item {
            SettingsSection("Manual Backup") {
                Button(
                    onClick = { /* Create backup */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Backup Now")
                }
            }
        }
    }
}

@Composable
private fun AuditLogCard(log: AuditLogData) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = log.action,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "User: ${log.userName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (log.details.isNotBlank()) {
                        Text(
                            text = log.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Text(
                    text = placeholderFormatDate(log.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ScheduledMaintenanceTab() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val scheduledMaintenance = getScheduledMaintenance()
        
        if (scheduledMaintenance.isEmpty()) {
            item {
                EmptyStateCard("No scheduled maintenance")
            }
        } else {
            items(scheduledMaintenance) { maintenance ->
                MaintenanceCard(maintenance = maintenance)
            }
        }
    }
}

@Composable
private fun OverdueMaintenanceTab() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val overdueMaintenance = getOverdueMaintenance()
        
        if (overdueMaintenance.isEmpty()) {
            item {
                EmptyStateCard("No overdue maintenance")
            }
        } else {
            items(overdueMaintenance) { maintenance ->
                MaintenanceCard(
                    maintenance = maintenance,
                    isOverdue = true
                )
            }
        }
    }
}

@Composable
private fun CompletedMaintenanceTab() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val completedMaintenance = getCompletedMaintenance()
        
        if (completedMaintenance.isEmpty()) {
            item {
                EmptyStateCard("No completed maintenance")
            }
        } else {
            items(completedMaintenance) { maintenance ->
                MaintenanceCard(
                    maintenance = maintenance,
                    isCompleted = true
                )
            }
        }
    }
}

@Composable
private fun MaintenanceRequestsTab() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val maintenanceRequests = getMaintenanceRequests()
        
        if (maintenanceRequests.isEmpty()) {
            item {
                EmptyStateCard("No maintenance requests")
            }
        } else {
            items(maintenanceRequests) { request ->
                MaintenanceRequestCard(request = request)
            }
        }
    }
}

@Composable
private fun ActiveJobCardsTab(navController: NavHostController) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val activeJobCards = getActiveJobCards()
        
        if (activeJobCards.isEmpty()) {
            item {
                EmptyStateCard("No active job cards")
            }
        } else {
            items(activeJobCards) { jobCard ->
                JobCardManagementCard(
                    jobCard = jobCard,
                    onView = { navController.navigate("job_cards/view/${jobCard.id}") },
                    onEdit = { navController.navigate("job_cards/edit/${jobCard.id}") }
                )
            }
        }
    }
}

@Composable
private fun AssignedJobCardsTab(navController: NavHostController) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val assignedJobCards = getAssignedJobCards()
        
        if (assignedJobCards.isEmpty()) {
            item {
                EmptyStateCard("No assigned job cards")
            }
        } else {
            items(assignedJobCards) { jobCard ->
                JobCardManagementCard(
                    jobCard = jobCard,
                    onView = { navController.navigate("job_cards/view/${jobCard.id}") },
                    onEdit = { navController.navigate("job_cards/edit/${jobCard.id}") }
                )
            }
        }
    }
}

@Composable
private fun CompletedJobCardsTab(navController: NavHostController) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val completedJobCards = getCompletedJobCards()
        
        if (completedJobCards.isEmpty()) {
            item {
                EmptyStateCard("No completed job cards")
            }
        } else {
            items(completedJobCards) { jobCard ->
                JobCardManagementCard(
                    jobCard = jobCard,
                    onView = { navController.navigate("job_cards/view/${jobCard.id}") },
                    onEdit = null // Read-only for completed cards
                )
            }
        }
    }
}

@Composable
private fun JobCardTemplatesTab(navController: NavHostController) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val templates = getJobCardTemplates()
        
        if (templates.isEmpty()) {
            item {
                EmptyStateCard("No job card templates")
            }
        } else {
            items(templates) { template ->
                JobCardTemplateCard(
                    template = template,
                    onUse = { navController.navigate("job_cards/create?template=${template.id}") },
                    onEdit = { navController.navigate("job_cards/template/edit/${template.id}") }
                )
            }
        }
    }
}

@Composable
private fun TaskProgressCard(
    task: TaskProgressData,
    onUpdateProgress: (Int) -> Unit,
    onUpdateStatus: (String) -> Unit
) {
    var showProgressDialog by remember { mutableStateOf(false) }
    
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Assigned to: ${task.assignedTo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Due: ${placeholderFormatDate(task.dueDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    color = when (task.status) {
                        "Completed" -> MaterialTheme.colorScheme.primaryContainer
                        "In Progress" -> MaterialTheme.colorScheme.tertiaryContainer
                        "On Hold" -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = task.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "${task.progress}%",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                LinearProgressIndicator(
                    progress = task.progress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showProgressDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Update Progress")
                }
                
                if (task.status != "Completed") {
                    Button(
                        onClick = { onUpdateStatus("Completed") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Mark Complete")
                    }
                }
            }
        }
    }
    
    if (showProgressDialog) {
        TaskProgressDialog(
            currentProgress = task.progress,
            onDismiss = { showProgressDialog = false },
            onConfirm = { newProgress ->
                onUpdateProgress(newProgress)
                showProgressDialog = false
            }
        )
    }
}

// Data classes
data class UserManagementData(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val isActive: Boolean,
    val lastLogin: Long
)

data class AuditLogData(
    val id: String,
    val action: String,
    val userName: String,
    val timestamp: Long,
    val details: String
)

data class MaintenanceData(
    val id: String,
    val equipmentId: String,
    val equipmentName: String,
    val type: String,
    val scheduledDate: Long,
    val assignedTo: String,
    val status: String,
    val description: String
)

data class MaintenanceRequest(
    val id: String,
    val equipmentId: String,
    val requestedBy: String,
    val priority: String,
    val description: String,
    val requestDate: Long
)

data class JobCardData(
    val id: String,
    val title: String,
    val equipmentId: String,
    val assignedTo: String,
    val status: String,
    val priority: String,
    val createdDate: Long,
    val dueDate: Long
)

data class JobCardTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: String
)

data class TaskProgressData(
    val id: String,
    val title: String,
    val assignedTo: String,
    val status: String,
    val progress: Int,
    val dueDate: Long,
    val description: String
)

// Helper functions to get mock data
private fun getUsersForManagement(): List<UserManagementData> {
    return listOf(
        UserManagementData("1", "John Smith", "john.smith@aeci.com", "Admin", true, System.currentTimeMillis() - 3600000),
        UserManagementData("2", "Sarah Johnson", "sarah.johnson@aeci.com", "Technician", true, System.currentTimeMillis() - 7200000),
        UserManagementData("3", "Mike Wilson", "mike.wilson@aeci.com", "Operator", false, System.currentTimeMillis() - 86400000)
    )
}

private fun getAuditLogs(): List<AuditLogData> {
    return listOf(
        AuditLogData("1", "User Login", "John Smith", System.currentTimeMillis() - 3600000, "Successful login from mobile app"),
        AuditLogData("2", "Form Submission", "Sarah Johnson", System.currentTimeMillis() - 7200000, "Submitted MMU Daily Log for Unit 001"),
        AuditLogData("3", "Equipment Status Update", "Mike Wilson", System.currentTimeMillis() - 10800000, "Updated status for PUMP-002 to Maintenance")
    )
}

private fun getScheduledMaintenance(): List<MaintenanceData> {
    return listOf(
        MaintenanceData("1", "MMU001", "Mobile Mining Unit 001", "Routine Service", System.currentTimeMillis() + 86400000, "John Smith", "Scheduled", "Monthly routine maintenance"),
        MaintenanceData("2", "PUMP002", "Hydraulic Pump 002", "Oil Change", System.currentTimeMillis() + 172800000, "Sarah Johnson", "Scheduled", "Quarterly oil change")
    )
}

private fun getOverdueMaintenance(): List<MaintenanceData> {
    return listOf(
        MaintenanceData("3", "MMU003", "Mobile Mining Unit 003", "Belt Replacement", System.currentTimeMillis() - 86400000, "Mike Wilson", "Overdue", "Replace conveyor belt")
    )
}

private fun getCompletedMaintenance(): List<MaintenanceData> {
    return listOf(
        MaintenanceData("4", "PUMP001", "Hydraulic Pump 001", "Inspection", System.currentTimeMillis() - 172800000, "John Smith", "Completed", "Annual inspection completed")
    )
}

private fun getMaintenanceRequests(): List<MaintenanceRequest> {
    return listOf(
        MaintenanceRequest("1", "MMU002", "Sarah Johnson", "High", "Unusual vibration detected", System.currentTimeMillis() - 3600000),
        MaintenanceRequest("2", "PUMP003", "Mike Wilson", "Medium", "Pressure gauge needs calibration", System.currentTimeMillis() - 7200000)
    )
}

private fun getActiveJobCards(): List<JobCardData> {
    return listOf(
        JobCardData("1", "Pump Maintenance", "PUMP001", "John Smith", "In Progress", "High", System.currentTimeMillis() - 86400000, System.currentTimeMillis() + 86400000),
        JobCardData("2", "Belt Inspection", "MMU001", "Sarah Johnson", "Active", "Medium", System.currentTimeMillis() - 172800000, System.currentTimeMillis() + 172800000)
    )
}

private fun getAssignedJobCards(): List<JobCardData> {
    return listOf(
        JobCardData("3", "Oil Change", "PUMP002", "Mike Wilson", "Assigned", "Low", System.currentTimeMillis() - 3600000, System.currentTimeMillis() + 604800000)
    )
}

private fun getCompletedJobCards(): List<JobCardData> {
    return listOf(
        JobCardData("4", "Filter Replacement", "MMU002", "John Smith", "Completed", "Medium", System.currentTimeMillis() - 604800000, System.currentTimeMillis() - 86400000)
    )
}

private fun getJobCardTemplates(): List<JobCardTemplate> {
    return listOf(
        JobCardTemplate("1", "Routine Maintenance", "Standard routine maintenance template", "Maintenance"),
        JobCardTemplate("2", "Emergency Repair", "Emergency repair template", "Repair"),
        JobCardTemplate("3", "Safety Inspection", "Safety inspection template", "Inspection")
    )
}

private fun getTasksForProgressUpdate(): List<TaskProgressData> {
    return listOf(
        TaskProgressData("1", "Complete pump maintenance", "John Smith", "In Progress", 75, System.currentTimeMillis() + 86400000, "Replace seals and check pressure"),
        TaskProgressData("2", "Inspect conveyor belt", "Sarah Johnson", "Not Started", 0, System.currentTimeMillis() + 172800000, "Visual inspection and tension check"),
        TaskProgressData("3", "Update equipment logs", "Mike Wilson", "Completed", 100, System.currentTimeMillis() - 86400000, "Update maintenance records")
    )
}

// Additional composables
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun SettingItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label)
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingSwitchItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun MaintenanceCard(
    maintenance: MaintenanceData,
    isOverdue: Boolean = false,
    isCompleted: Boolean = false
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = maintenance.type,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Equipment: ${maintenance.equipmentName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Assigned to: ${maintenance.assignedTo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    color = when {
                        isCompleted -> MaterialTheme.colorScheme.primaryContainer
                        isOverdue -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.tertiaryContainer
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = maintenance.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Scheduled: ${placeholderFormatDate(maintenance.scheduledDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (maintenance.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = maintenance.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun MaintenanceRequestCard(request: MaintenanceRequest) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Equipment: ${request.equipmentId}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Requested by: ${request.requestedBy}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = placeholderFormatDate(request.requestDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    color = when (request.priority) {
                        "High" -> MaterialTheme.colorScheme.errorContainer
                        "Medium" -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${request.priority} Priority",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = request.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun JobCardManagementCard(
    jobCard: JobCardData,
    onView: () -> Unit,
    onEdit: (() -> Unit)?
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = jobCard.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Equipment: ${jobCard.equipmentId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Assigned to: ${jobCard.assignedTo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    color = when (jobCard.status) {
                        "Completed" -> MaterialTheme.colorScheme.primaryContainer
                        "In Progress" -> MaterialTheme.colorScheme.tertiaryContainer
                        "Active" -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = jobCard.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onView,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("View")
                }
                
                onEdit?.let { editAction ->
                    Button(
                        onClick = editAction,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Edit")
                    }
                }
            }
        }
    }
}

@Composable
private fun JobCardTemplateCard(
    template: JobCardTemplate,
    onUse: () -> Unit,
    onEdit: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = template.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = "Category: ${template.category}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onUse,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Use Template")
                }
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit")
                }
            }
        }
    }
}

@Composable
private fun TaskProgressDialog(
    currentProgress: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var progress by remember { mutableStateOf(currentProgress.toFloat()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Progress") },
        text = {
            Column {
                Text("Current progress: ${progress.toInt()}%")
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = progress,
                    onValueChange = { progress = it },
                    valueRange = 0f..100f,
                    steps = 19
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(progress.toInt()) }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ReportGenerationScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fileShareManager = remember { FileShareManager(context) }
    
    var selectedReportType by remember { mutableStateOf("Form Summary") }
    var selectedDateRange by remember { mutableStateOf("Last 30 Days") }
    var selectedFormat by remember { mutableStateOf("PDF") }
    var includeCharts by remember { mutableStateOf(true) }
    var includeRawData by remember { mutableStateOf(false) }
    var customStartDate by remember { mutableStateOf("") }
    var customEndDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "Generate Report",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Report Type Selection
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Report Type",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        val reportTypes = listOf(
                            "Form Summary", "Equipment Status", "Maintenance Records", 
                            "User Activity", "Compliance Report", "Production Summary",
                            "Safety Incidents", "System Health", "Custom Report"
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(reportTypes) { reportType ->
                                FilterChip(
                                    selected = selectedReportType == reportType,
                                    onClick = { selectedReportType = reportType },
                                    label = { Text(reportType) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Date Range Selection
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Date Range",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        val dateRanges = listOf(
                            "Today", "Last 7 Days", "Last 30 Days", 
                            "Last 3 Months", "Last Year", "Custom Range"
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(dateRanges) { range ->
                                FilterChip(
                                    selected = selectedDateRange == range,
                                    onClick = { 
                                        selectedDateRange = range
                                        if (range == "Custom Range") {
                                            showDatePicker = true
                                        }
                                    },
                                    label = { Text(range) }
                                )
                            }
                        }
                        
                        if (selectedDateRange == "Custom Range") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = customStartDate,
                                    onValueChange = { customStartDate = it },
                                    label = { Text("Start Date") },
                                    placeholder = { Text("YYYY-MM-DD") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = customEndDate,
                                    onValueChange = { customEndDate = it },
                                    label = { Text("End Date") },
                                    placeholder = { Text("YYYY-MM-DD") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
            
            // Format and Options
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Format & Options",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        // Format Selection
                        Text(
                            text = "Export Format",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val formats = listOf("PDF", "Excel", "CSV", "JSON")
                            formats.forEach { format ->
                                FilterChip(
                                    selected = selectedFormat == format,
                                    onClick = { selectedFormat = format },
                                    label = { Text(format) }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Additional Options
                        Text(
                            text = "Include in Report",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Charts and Graphs")
                            Switch(
                                checked = includeCharts,
                                onCheckedChange = { includeCharts = it }
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Raw Data Tables")
                            Switch(
                                checked = includeRawData,
                                onCheckedChange = { includeRawData = it }
                            )
                        }
                    }
                }
            }
            
            // Preview Section
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Report Preview",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        // Mock preview content
                        ReportPreviewSection(
                            reportType = selectedReportType,
                            dateRange = selectedDateRange,
                            format = selectedFormat
                        )
                    }
                }
            }
            
            // Generate Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        isGenerating = true
                        
                        // Generate and share the report
                        coroutineScope.launch {
                            try {
                                val dateRangeForFile = if (selectedDateRange == "Custom Range") {
                                    "${customStartDate}_to_${customEndDate}"
                                } else {
                                    selectedDateRange.replace(" ", "_")
                                }
                                
                                val reportFile = fileShareManager.generateReportFile(
                                    reportType = selectedReportType,
                                    format = selectedFormat,
                                    dateRange = dateRangeForFile,
                                    includeCharts = includeCharts,
                                    includeRawData = includeRawData
                                )
                                
                                // Simulate generation time
                                kotlinx.coroutines.delay(2000)
                                
                                val success = fileShareManager.saveAndShareReport(reportFile)
                                if (success) {
                                    android.util.Log.i("ReportGeneration", "Report generated and shared successfully")
                                }
                                
                            } catch (e: Exception) {
                                android.util.Log.e("ReportGeneration", "Error generating report", e)
                                android.widget.Toast.makeText(
                                    context, 
                                    "Error generating report: ${e.message}", 
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            } finally {
                                isGenerating = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generating...")
                    } else {
                        Icon(AECIIcons.Assessment, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate $selectedFormat Report")
                    }
                }
                
                if (isGenerating) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Recent Reports
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Recent Reports",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                val recentReports = getRecentReports()
                
                if (recentReports.isEmpty()) {
                    Card {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No recent reports generated",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    recentReports.forEach { report ->
                        RecentReportCard(
                            report = report,
                            onDownload = { /* Download report */ },
                            onShare = { /* Share report */ }
                        )
                    }
                }
            }
        }
    }
    
    // Simulate report generation completion
    LaunchedEffect(isGenerating) {
        if (isGenerating) {
            kotlinx.coroutines.delay(3000) // Simulate generation time
            isGenerating = false
        }
    }
}

@Composable
private fun ReportPreviewSection(
    reportType: String,
    dateRange: String,
    format: String
) {
    Column {
        Text(
            text = "Report: $reportType",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Period: $dateRange",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Format: $format",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Mock data preview based on report type
        when (reportType) {
            "Form Summary" -> {
                Text("Preview: 245 forms submitted, 98% completion rate, 12 pending approvals")
            }
            "Equipment Status" -> {
                Text("Preview: 85% operational, 12% under maintenance, 3% out of service")
            }
            "Maintenance Records" -> {
                Text("Preview: 23 completed, 8 scheduled, 2 overdue maintenance tasks")
            }
            "User Activity" -> {
                Text("Preview: 45 active users, 6.8 avg forms/user/day, 98.7% login success")
            }
            else -> {
                Text("Preview: Data analysis and summary will be generated based on selected criteria")
            }
        }
    }
}

@Composable
private fun RecentReportCard(
    report: RecentReportData,
    onDownload: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = report.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Generated: ${placeholderFormatDate(report.generatedDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Format: ${report.format} • Size: ${report.fileSize}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onDownload) {
                    Icon(
                        AECIIcons.Download,
                        contentDescription = "Download",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// Data class for recent reports
data class RecentReportData(
    val id: String,
    val name: String,
    val type: String,
    val generatedDate: Long,
    val format: String,
    val fileSize: String
)

// Helper function to get recent reports
private fun getRecentReports(): List<RecentReportData> {
    return listOf(
        RecentReportData("1", "Form Summary - January 2024", "Form Summary", System.currentTimeMillis() - 86400000, "PDF", "2.4 MB"),
        RecentReportData("2", "Equipment Status Report", "Equipment Status", System.currentTimeMillis() - 172800000, "Excel", "1.8 MB"),
        RecentReportData("3", "Monthly Maintenance Report", "Maintenance Records", System.currentTimeMillis() - 604800000, "PDF", "3.1 MB")
    )
}
