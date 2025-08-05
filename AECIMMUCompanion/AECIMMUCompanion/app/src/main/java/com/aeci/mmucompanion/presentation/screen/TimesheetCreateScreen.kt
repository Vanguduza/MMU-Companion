package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.presentation.component.AECIIcons
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimesheetCreateScreen(
    navController: NavHostController
) {
    var selectedWeek by remember { mutableStateOf(getCurrentWeek()) }
    var timesheetEntries by remember { mutableStateOf(generateWeeklyEntries(selectedWeek)) }
    var showJobCardPicker by remember { mutableStateOf(false) }
    var selectedEntry by remember { mutableStateOf<TimesheetEntry?>(null) }
    var notes by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Timesheet") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // TODO: Save timesheet
                            isLoading = true
                            // Simulate save operation
                            navController.navigateUp()
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Week Selector
            WeekSelectorCard(
                selectedWeek = selectedWeek,
                onWeekChanged = { week ->
                    selectedWeek = week
                    timesheetEntries = generateWeeklyEntries(week)
                }
            )
            
            // Timesheet Summary
            TimesheetSummaryCard(entries = timesheetEntries)
            
            // Daily Entries
            TimesheetEntriesSection(
                entries = timesheetEntries,
                onEntryUpdated = { updatedEntry ->
                    timesheetEntries = timesheetEntries.map { entry ->
                        if (entry.id == updatedEntry.id) updatedEntry else entry
                    }
                },
                onJobCardSelected = { entry ->
                    selectedEntry = entry
                    showJobCardPicker = true
                }
            )
            
            // Notes Section
            NotesSection(
                notes = notes,
                onNotesChanged = { notes = it }
            )
        }
    }
    
    // Job Card Picker Dialog
    if (showJobCardPicker && selectedEntry != null) {
        JobCardPickerDialog(
            onDismiss = { showJobCardPicker = false },
            onJobCardSelected = { jobCardId ->
                selectedEntry?.let { entry ->
                    val updatedEntry = entry.copy(jobCardId = jobCardId)
                    timesheetEntries = timesheetEntries.map { 
                        if (it.id == entry.id) updatedEntry else it 
                    }
                }
                showJobCardPicker = false
            }
        )
    }
}

@Composable
fun WeekSelectorCard(
    selectedWeek: TimesheetPeriod,
    onWeekChanged: (TimesheetPeriod) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Week Period",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        val prevWeek = getPreviousWeek(selectedWeek)
                        onWeekChanged(prevWeek)
                    }
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Previous Week",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Week ${selectedWeek.weekNumber}, ${selectedWeek.year}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${selectedWeek.startDate.format(DateTimeFormatter.ofPattern("MMM dd"))} - ${selectedWeek.endDate.format(DateTimeFormatter.ofPattern("MMM dd"))}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                
                IconButton(
                    onClick = { 
                        val nextWeek = getNextWeek(selectedWeek)
                        onWeekChanged(nextWeek)
                    }
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Next Week",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun TimesheetSummaryCard(entries: List<TimesheetEntry>) {
    val totalHours = entries.sumOf { it.totalHours }
    val regularHours = entries.sumOf { it.regularHours }
    val overtimeHours = entries.sumOf { it.overtimeHours }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Week Summary",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = "Total Hours",
                    value = String.format("%.1f", totalHours),
                    color = MaterialTheme.colorScheme.primary
                )
                SummaryItem(
                    label = "Regular",
                    value = String.format("%.1f", regularHours),
                    color = MaterialTheme.colorScheme.tertiary
                )
                SummaryItem(
                    label = "Overtime",
                    value = String.format("%.1f", overtimeHours),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun SummaryItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TimesheetEntriesSection(
    entries: List<TimesheetEntry>,
    onEntryUpdated: (TimesheetEntry) -> Unit,
    onJobCardSelected: (TimesheetEntry) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Daily Entries",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            entries.forEach { entry ->
                TimesheetEntryRow(
                    entry = entry,
                    onEntryUpdated = onEntryUpdated,
                    onJobCardSelected = { onJobCardSelected(entry) }
                )
                if (entry != entries.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun TimesheetEntryRow(
    entry: TimesheetEntry,
    onEntryUpdated: (TimesheetEntry) -> Unit,
    onJobCardSelected: () -> Unit
) {
    var clockInTime by remember { mutableStateOf(entry.clockInTime?.toString() ?: "") }
    var clockOutTime by remember { mutableStateOf(entry.clockOutTime?.toString() ?: "") }
    var activities by remember { mutableStateOf(entry.activities.joinToString(", ")) }
    var entryNotes by remember { mutableStateOf(entry.notes ?: "") }
    
    Column {
        // Date and Day
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = entry.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = entry.date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Total hours for the day
            Text(
                text = "${String.format("%.1f", entry.totalHours)}h",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Time inputs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = clockInTime,
                onValueChange = { 
                    clockInTime = it
                    updateEntry(entry, clockInTime, clockOutTime, activities, entryNotes, onEntryUpdated)
                },
                label = { Text("Clock In") },
                placeholder = { Text("09:00") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            OutlinedTextField(
                value = clockOutTime,
                onValueChange = { 
                    clockOutTime = it
                    updateEntry(entry, clockInTime, clockOutTime, activities, entryNotes, onEntryUpdated)
                },
                label = { Text("Clock Out") },
                placeholder = { Text("17:00") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Activities and Job Card
        OutlinedTextField(
            value = activities,
            onValueChange = { 
                activities = it
                updateEntry(entry, clockInTime, clockOutTime, activities, entryNotes, onEntryUpdated)
            },
            label = { Text("Activities") },
            placeholder = { Text("Maintenance, Inspection, etc.") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = onJobCardSelected) {
                    Icon(
                        AECIIcons.Assignment,
                        contentDescription = "Select Job Card"
                    )
                }
            }
        )
        
        if (entryNotes.isNotEmpty() || entry == entry) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = entryNotes,
                onValueChange = { 
                    entryNotes = it
                    updateEntry(entry, clockInTime, clockOutTime, activities, entryNotes, onEntryUpdated)
                },
                label = { Text("Notes") },
                placeholder = { Text("Additional notes for this day") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )
        }
    }
}

@Composable
fun NotesSection(
    notes: String,
    onNotesChanged: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Week Notes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChanged,
                label = { Text("Additional Notes") },
                placeholder = { Text("Any additional notes for this week...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }
    }
}

@Composable
fun JobCardPickerDialog(
    onDismiss: () -> Unit,
    onJobCardSelected: (String) -> Unit
) {
    // Mock job cards - in real implementation, these would come from a ViewModel
    val mockJobCards = listOf(
        "JC001 - MMU Maintenance",
        "JC002 - Pump Inspection", 
        "JC003 - Safety Check",
        "JC004 - Equipment Calibration"
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Job Card",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(mockJobCards) { jobCard ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                onClick = { onJobCardSelected(jobCard.substringBefore(" -")) }
                            ) {
                                Text(
                                    text = jobCard,
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

// Helper functions
private fun getCurrentWeek(): TimesheetPeriod {
    val now = LocalDate.now()
    val startOfWeek = now.minusDays(now.dayOfWeek.value.toLong() - 1)
    val endOfWeek = startOfWeek.plusDays(6)
    
    return TimesheetPeriod(
        startDate = startOfWeek,
        endDate = endOfWeek,
        weekNumber = now.dayOfYear / 7 + 1,
        year = now.year
    )
}

private fun getPreviousWeek(current: TimesheetPeriod): TimesheetPeriod {
    val previousStart = current.startDate.minusDays(7)
    val previousEnd = current.endDate.minusDays(7)
    
    return TimesheetPeriod(
        startDate = previousStart,
        endDate = previousEnd,
        weekNumber = if (current.weekNumber > 1) current.weekNumber - 1 else 52,
        year = if (current.weekNumber > 1) current.year else current.year - 1
    )
}

private fun getNextWeek(current: TimesheetPeriod): TimesheetPeriod {
    val nextStart = current.startDate.plusDays(7)
    val nextEnd = current.endDate.plusDays(7)
    
    return TimesheetPeriod(
        startDate = nextStart,
        endDate = nextEnd,
        weekNumber = if (current.weekNumber < 52) current.weekNumber + 1 else 1,
        year = if (current.weekNumber < 52) current.year else current.year + 1
    )
}

private fun generateWeeklyEntries(week: TimesheetPeriod): List<TimesheetEntry> {
    val entries = mutableListOf<TimesheetEntry>()
    var currentDate = week.startDate
    
    repeat(7) { index ->
        entries.add(
            TimesheetEntry(
                id = "entry_${week.weekNumber}_$index",
                timesheetId = "timesheet_${week.weekNumber}",
                date = currentDate,
                shiftType = ShiftType.DAY,
                clockInTime = null,
                clockOutTime = null,
                totalHours = 0.0,
                regularHours = 0.0,
                overtimeHours = 0.0,
                jobCardId = null,
                equipmentId = null,
                activities = emptyList(),
                notes = null,
                location = "Mining Site",
                isAbsent = false,
                absenceReason = null
            )
        )
        currentDate = currentDate.plusDays(1)
    }
    
    return entries
}

private fun updateEntry(
    entry: TimesheetEntry,
    clockInTime: String,
    clockOutTime: String,
    activities: String,
    notes: String,
    onEntryUpdated: (TimesheetEntry) -> Unit
) {
    try {
        val clockIn = if (clockInTime.isNotBlank()) LocalTime.parse(clockInTime) else null
        val clockOut = if (clockOutTime.isNotBlank()) LocalTime.parse(clockOutTime) else null
        
        val totalHours = if (clockIn != null && clockOut != null) {
            val duration = java.time.Duration.between(clockIn, clockOut)
            duration.toMinutes() / 60.0 - 0.5 // Subtract 30 min break
        } else 0.0
        
        val regularHours = minOf(totalHours, 8.0)
        val overtimeHours = maxOf(0.0, totalHours - 8.0)
        
        val updatedEntry = entry.copy(
            clockInTime = clockIn,
            clockOutTime = clockOut,
            totalHours = totalHours,
            regularHours = regularHours,
            overtimeHours = overtimeHours,
            activities = activities.split(",").map { it.trim() }.filter { it.isNotBlank() },
            notes = notes.takeIf { it.isNotBlank() }
        )
        
        onEntryUpdated(updatedEntry)
    } catch (e: Exception) {
        // Handle time parsing errors gracefully
    }
}
