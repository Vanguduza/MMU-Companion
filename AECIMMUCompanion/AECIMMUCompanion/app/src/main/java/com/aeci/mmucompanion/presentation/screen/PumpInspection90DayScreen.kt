package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.aeci.mmucompanion.domain.model.*
import com.aeci.mmucompanion.presentation.component.DatePickerComponent
import com.aeci.mmucompanion.presentation.component.ImagePickerComponent
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PumpInspection90DayScreen(
    navController: NavHostController,
    equipmentId: String? = null
) {
    // Form state variables based on the specifications
    var inspectionDate by remember { mutableStateOf(LocalDate.now()) }
    var inspectorName by remember { mutableStateOf("") }
    var equipmentIdField by remember { mutableStateOf(equipmentId ?: "") }
    var serialNumber by remember { mutableStateOf("") }
    var pumpLocation by remember { mutableStateOf("") }
    var serviceHours by remember { mutableStateOf("") }
    
    // Inspection checklist items
    var motorMountingBolts by remember { mutableStateOf(InspectionResult.NOT_CHECKED) }
    var motorBearing by remember { mutableStateOf(InspectionResult.NOT_CHECKED) }
    var motorFanCover by remember { mutableStateOf(InspectionResult.NOT_CHECKED) }
    var pumpCouplingAlignment by remember { mutableStateOf(InspectionResult.NOT_CHECKED) }
    var pumpMountingBolts by remember { mutableStateOf(InspectionResult.NOT_CHECKED) }
    var pumpSealCondition by remember { mutableStateOf(InspectionResult.NOT_CHECKED) }
    var pipeworkSupport by remember { mutableStateOf(InspectionResult.NOT_CHECKED) }
    var valveOperation by remember { mutableStateOf(InspectionResult.NOT_CHECKED) }
    var pressureGauges by remember { mutableStateOf(InspectionResult.NOT_CHECKED) }
    var electricalConnections by remember { mutableStateOf(InspectionResult.NOT_CHECKED) }
    
    // Pressure test section
    var pressureTestPerformed by remember { mutableStateOf(false) }
    var pressureTestDate by remember { mutableStateOf<LocalDate?>(null) }
    var testPressureValue by remember { mutableStateOf("") }
    var operatingPressure by remember { mutableStateOf("") }
    var pressureDropRate by remember { mutableStateOf("") }
    var pressureTestResult by remember { mutableStateOf(TestResult.NOT_TESTED) }
    var pressureTestComments by remember { mutableStateOf("") }
    
    // Performance test section
    var flowRateTest by remember { mutableStateOf(false) }
    var flowRateValue by remember { mutableStateOf("") }
    var vibrationTest by remember { mutableStateOf(false) }
    var vibrationLevel by remember { mutableStateOf("") }
    
    // Final section
    var overallCondition by remember { mutableStateOf(InspectionResult.NOT_CHECKED) }
    var nextInspectionDate by remember { mutableStateOf<LocalDate?>(null) }
    var inspectorSignature by remember { mutableStateOf("") }
    var supervisorSignature by remember { mutableStateOf("") }
    var photos by remember { mutableStateOf(listOf<String>()) }
    var additionalComments by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("90 Day Pump System Inspection") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // Save form logic here
                            val form = PumpInspection90DayForm(
                                id = UUID.randomUUID().toString(),
                                createdAt = LocalDateTime.now(),
                                updatedAt = LocalDateTime.now(),
                                inspectionDate = inspectionDate,
                                inspectorName = inspectorName,
                                equipmentId = equipmentIdField,
                                serialNumber = serialNumber,
                                pumpLocation = pumpLocation,
                                serviceHours = serviceHours.toIntOrNull() ?: 0,
                                motorMountingBolts = motorMountingBolts,
                                motorBearing = motorBearing,
                                motorFanCover = motorFanCover,
                                pumpCouplingAlignment = pumpCouplingAlignment,
                                pumpMountingBolts = pumpMountingBolts,
                                pumpSealCondition = pumpSealCondition,
                                pipeworkSupport = pipeworkSupport,
                                valveOperation = valveOperation,
                                pressureGauges = pressureGauges,
                                electricalConnections = electricalConnections,
                                pressureTestPerformed = pressureTestPerformed,
                                pressureTestDate = pressureTestDate,
                                testPressureValue = testPressureValue.toDoubleOrNull(),
                                operatingPressure = operatingPressure.toDoubleOrNull(),
                                pressureDropRate = pressureDropRate.toDoubleOrNull(),
                                pressureTestResult = pressureTestResult,
                                pressureTestComments = pressureTestComments,
                                flowRateTest = flowRateTest,
                                flowRateValue = flowRateValue.toDoubleOrNull(),
                                vibrationTest = vibrationTest,
                                vibrationLevel = vibrationLevel.toDoubleOrNull(),
                                overallCondition = overallCondition,
                                nextInspectionDate = nextInspectionDate,
                                inspectorSignature = inspectorSignature,
                                supervisorSignature = supervisorSignature,
                                photos = photos,
                                additionalComments = additionalComments
                            )
                            // TODO: Save to repository
                            navController.popBackStack()
                        }
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item {
                InspectionHeaderCard(
                    inspectionDate = inspectionDate,
                    onInspectionDateChange = { inspectionDate = it },
                    inspectorName = inspectorName,
                    onInspectorNameChange = { inspectorName = it },
                    equipmentId = equipmentIdField,
                    onEquipmentIdChange = { equipmentIdField = it },
                    serialNumber = serialNumber,
                    onSerialNumberChange = { serialNumber = it },
                    pumpLocation = pumpLocation,
                    onPumpLocationChange = { pumpLocation = it },
                    serviceHours = serviceHours,
                    onServiceHoursChange = { serviceHours = it }
                )
            }
            
            // Visual Inspection Checklist
            item {
                VisualInspectionCard(
                    motorMountingBolts = motorMountingBolts,
                    onMotorMountingBoltsChange = { motorMountingBolts = it },
                    motorBearing = motorBearing,
                    onMotorBearingChange = { motorBearing = it },
                    motorFanCover = motorFanCover,
                    onMotorFanCoverChange = { motorFanCover = it },
                    pumpCouplingAlignment = pumpCouplingAlignment,
                    onPumpCouplingAlignmentChange = { pumpCouplingAlignment = it },
                    pumpMountingBolts = pumpMountingBolts,
                    onPumpMountingBoltsChange = { pumpMountingBolts = it },
                    pumpSealCondition = pumpSealCondition,
                    onPumpSealConditionChange = { pumpSealCondition = it },
                    pipeworkSupport = pipeworkSupport,
                    onPipeworkSupportChange = { pipeworkSupport = it },
                    valveOperation = valveOperation,
                    onValveOperationChange = { valveOperation = it },
                    pressureGauges = pressureGauges,
                    onPressureGaugesChange = { pressureGauges = it },
                    electricalConnections = electricalConnections,
                    onElectricalConnectionsChange = { electricalConnections = it }
                )
            }
            
            // Pressure Test Section
            item {
                PressureTestCard(
                    pressureTestPerformed = pressureTestPerformed,
                    onPressureTestPerformedChange = { pressureTestPerformed = it },
                    pressureTestDate = pressureTestDate,
                    onPressureTestDateChange = { pressureTestDate = it },
                    testPressureValue = testPressureValue,
                    onTestPressureValueChange = { testPressureValue = it },
                    operatingPressure = operatingPressure,
                    onOperatingPressureChange = { operatingPressure = it },
                    pressureDropRate = pressureDropRate,
                    onPressureDropRateChange = { pressureDropRate = it },
                    pressureTestResult = pressureTestResult,
                    onPressureTestResultChange = { pressureTestResult = it },
                    pressureTestComments = pressureTestComments,
                    onPressureTestCommentsChange = { pressureTestComments = it }
                )
            }
            
            // Performance Test Section
            item {
                PerformanceTestCard(
                    flowRateTest = flowRateTest,
                    onFlowRateTestChange = { flowRateTest = it },
                    flowRateValue = flowRateValue,
                    onFlowRateValueChange = { flowRateValue = it },
                    vibrationTest = vibrationTest,
                    onVibrationTestChange = { vibrationTest = it },
                    vibrationLevel = vibrationLevel,
                    onVibrationLevelChange = { vibrationLevel = it }
                )
            }
            
            // Final Assessment Section
            item {
                FinalAssessmentCard(
                    overallCondition = overallCondition,
                    onOverallConditionChange = { overallCondition = it },
                    nextInspectionDate = nextInspectionDate,
                    onNextInspectionDateChange = { nextInspectionDate = it },
                    inspectorSignature = inspectorSignature,
                    onInspectorSignatureChange = { inspectorSignature = it },
                    supervisorSignature = supervisorSignature,
                    onSupervisorSignatureChange = { supervisorSignature = it },
                    additionalComments = additionalComments,
                    onAdditionalCommentsChange = { additionalComments = it }
                )
            }
            
            // Photo Attachment Section
            item {
                PhotoAttachmentCard(
                    photos = photos,
                    onPhotosChange = { photos = it }
                )
            }
        }
    }
}

@Composable
private fun InspectionHeaderCard(
    inspectionDate: LocalDate,
    onInspectionDateChange: (LocalDate) -> Unit,
    inspectorName: String,
    onInspectorNameChange: (String) -> Unit,
    equipmentId: String,
    onEquipmentIdChange: (String) -> Unit,
    serialNumber: String,
    onSerialNumberChange: (String) -> Unit,
    pumpLocation: String,
    onPumpLocationChange: (String) -> Unit,
    serviceHours: String,
    onServiceHoursChange: (String) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Inspection Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DatePickerComponent(
                        label = "Inspection Date",
                        selectedDate = inspectionDate,
                        onDateSelected = onInspectionDateChange
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = inspectorName,
                        onValueChange = onInspectorNameChange,
                        label = { Text("Inspector Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = equipmentId,
                        onValueChange = onEquipmentIdChange,
                        label = { Text("Equipment ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = serialNumber,
                        onValueChange = onSerialNumberChange,
                        label = { Text("Serial Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = pumpLocation,
                        onValueChange = onPumpLocationChange,
                        label = { Text("Pump Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = serviceHours,
                        onValueChange = onServiceHoursChange,
                        label = { Text("Service Hours") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun VisualInspectionCard(
    motorMountingBolts: InspectionResult,
    onMotorMountingBoltsChange: (InspectionResult) -> Unit,
    motorBearing: InspectionResult,
    onMotorBearingChange: (InspectionResult) -> Unit,
    motorFanCover: InspectionResult,
    onMotorFanCoverChange: (InspectionResult) -> Unit,
    pumpCouplingAlignment: InspectionResult,
    onPumpCouplingAlignmentChange: (InspectionResult) -> Unit,
    pumpMountingBolts: InspectionResult,
    onPumpMountingBoltsChange: (InspectionResult) -> Unit,
    pumpSealCondition: InspectionResult,
    onPumpSealConditionChange: (InspectionResult) -> Unit,
    pipeworkSupport: InspectionResult,
    onPipeworkSupportChange: (InspectionResult) -> Unit,
    valveOperation: InspectionResult,
    onValveOperationChange: (InspectionResult) -> Unit,
    pressureGauges: InspectionResult,
    onPressureGaugesChange: (InspectionResult) -> Unit,
    electricalConnections: InspectionResult,
    onElectricalConnectionsChange: (InspectionResult) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Visual Inspection Checklist",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            InspectionResultRow("Motor mounting bolts", motorMountingBolts, onMotorMountingBoltsChange)
            InspectionResultRow("Motor bearing condition", motorBearing, onMotorBearingChange)
            InspectionResultRow("Motor fan cover", motorFanCover, onMotorFanCoverChange)
            InspectionResultRow("Pump coupling alignment", pumpCouplingAlignment, onPumpCouplingAlignmentChange)
            InspectionResultRow("Pump mounting bolts", pumpMountingBolts, onPumpMountingBoltsChange)
            InspectionResultRow("Pump seal condition", pumpSealCondition, onPumpSealConditionChange)
            InspectionResultRow("Pipework support", pipeworkSupport, onPipeworkSupportChange)
            InspectionResultRow("Valve operation", valveOperation, onValveOperationChange)
            InspectionResultRow("Pressure gauges", pressureGauges, onPressureGaugesChange)
            InspectionResultRow("Electrical connections", electricalConnections, onElectricalConnectionsChange)
        }
    }
}

// Data classes for the form
data class PumpInspection90DayForm(
    val id: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val inspectionDate: LocalDate,
    val inspectorName: String,
    val equipmentId: String,
    val serialNumber: String,
    val pumpLocation: String,
    val serviceHours: Int,
    val motorMountingBolts: InspectionResult,
    val motorBearing: InspectionResult,
    val motorFanCover: InspectionResult,
    val pumpCouplingAlignment: InspectionResult,
    val pumpMountingBolts: InspectionResult,
    val pumpSealCondition: InspectionResult,
    val pipeworkSupport: InspectionResult,
    val valveOperation: InspectionResult,
    val pressureGauges: InspectionResult,
    val electricalConnections: InspectionResult,
    val pressureTestPerformed: Boolean,
    val pressureTestDate: LocalDate?,
    val testPressureValue: Double?,
    val operatingPressure: Double?,
    val pressureDropRate: Double?,
    val pressureTestResult: TestResult,
    val pressureTestComments: String,
    val flowRateTest: Boolean,
    val flowRateValue: Double?,
    val vibrationTest: Boolean,
    val vibrationLevel: Double?,
    val overallCondition: InspectionResult,
    val nextInspectionDate: LocalDate?,
    val inspectorSignature: String,
    val supervisorSignature: String,
    val photos: List<String>,
    val additionalComments: String
)

enum class InspectionResult {
    NOT_CHECKED,
    SATISFACTORY,
    ATTENTION_REQUIRED,
    IMMEDIATE_ACTION
}

enum class TestResult {
    NOT_TESTED,
    PASS,
    FAIL
}

@Composable
private fun InspectionResultRow(
    label: String,
    result: InspectionResult,
    onResultChange: (InspectionResult) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(2f),
            style = MaterialTheme.typography.bodyMedium
        )
        
        Row(
            modifier = Modifier.weight(3f),
            horizontalArrangement = Arrangement.End
        ) {
            InspectionResult.values().forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    RadioButton(
                        selected = result == option,
                        onClick = { onResultChange(option) }
                    )
                    Text(
                        text = when (option) {
                            InspectionResult.NOT_CHECKED -> "N/C"
                            InspectionResult.SATISFACTORY -> "SAT"
                            InspectionResult.ATTENTION_REQUIRED -> "ATT"
                            InspectionResult.IMMEDIATE_ACTION -> "ACT"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun PressureTestCard(
    pressureTestPerformed: Boolean,
    onPressureTestPerformedChange: (Boolean) -> Unit,
    pressureTestDate: LocalDate?,
    onPressureTestDateChange: (LocalDate?) -> Unit,
    testPressureValue: String,
    onTestPressureValueChange: (String) -> Unit,
    operatingPressure: String,
    onOperatingPressureChange: (String) -> Unit,
    pressureDropRate: String,
    onPressureDropRateChange: (String) -> Unit,
    pressureTestResult: TestResult,
    onPressureTestResultChange: (TestResult) -> Unit,
    pressureTestComments: String,
    onPressureTestCommentsChange: (String) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Pressure Test",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = pressureTestPerformed,
                    onCheckedChange = onPressureTestPerformedChange
                )
                Text("Pressure test performed")
            }
            
            if (pressureTestPerformed) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        pressureTestDate?.let { date ->
                            DatePickerComponent(
                                label = "Test Date",
                                selectedDate = date,
                                onDateSelected = onPressureTestDateChange
                            )
                        }
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = testPressureValue,
                            onValueChange = onTestPressureValueChange,
                            label = { Text("Test Pressure (Bar)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = operatingPressure,
                            onValueChange = onOperatingPressureChange,
                            label = { Text("Operating Pressure (Bar)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = pressureDropRate,
                            onValueChange = onPressureDropRateChange,
                            label = { Text("Pressure Drop Rate (Bar/min)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Test Result:")
                Row {
                    TestResult.values().forEach { result ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            RadioButton(
                                selected = pressureTestResult == result,
                                onClick = { onPressureTestResultChange(result) }
                            )
                            Text(
                                text = when (result) {
                                    TestResult.NOT_TESTED -> "Not Tested"
                                    TestResult.PASS -> "Pass"
                                    TestResult.FAIL -> "Fail"
                                }
                            )
                        }
                    }
                }
                
                if (pressureTestResult == TestResult.FAIL) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pressureTestComments,
                        onValueChange = onPressureTestCommentsChange,
                        label = { Text("Test Comments") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceTestCard(
    flowRateTest: Boolean,
    onFlowRateTestChange: (Boolean) -> Unit,
    flowRateValue: String,
    onFlowRateValueChange: (String) -> Unit,
    vibrationTest: Boolean,
    onVibrationTestChange: (Boolean) -> Unit,
    vibrationLevel: String,
    onVibrationLevelChange: (String) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Performance Tests",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = flowRateTest,
                    onCheckedChange = onFlowRateTestChange
                )
                Text("Flow rate test performed")
            }
            
            if (flowRateTest) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = flowRateValue,
                    onValueChange = onFlowRateValueChange,
                    label = { Text("Flow Rate (L/min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = vibrationTest,
                    onCheckedChange = onVibrationTestChange
                )
                Text("Vibration test performed")
            }
            
            if (vibrationTest) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = vibrationLevel,
                    onValueChange = onVibrationLevelChange,
                    label = { Text("Vibration Level (mm/s)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FinalAssessmentCard(
    overallCondition: InspectionResult,
    onOverallConditionChange: (InspectionResult) -> Unit,
    nextInspectionDate: LocalDate?,
    onNextInspectionDateChange: (LocalDate?) -> Unit,
    inspectorSignature: String,
    onInspectorSignatureChange: (String) -> Unit,
    supervisorSignature: String,
    onSupervisorSignatureChange: (String) -> Unit,
    additionalComments: String,
    onAdditionalCommentsChange: (String) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Final Assessment",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Overall Condition:")
            Row {
                InspectionResult.values().forEach { result ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        RadioButton(
                            selected = overallCondition == result,
                            onClick = { onOverallConditionChange(result) }
                        )
                        Text(
                            text = when (result) {
                                InspectionResult.NOT_CHECKED -> "N/C"
                                InspectionResult.SATISFACTORY -> "Satisfactory"
                                InspectionResult.ATTENTION_REQUIRED -> "Attention Required"
                                InspectionResult.IMMEDIATE_ACTION -> "Immediate Action"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            nextInspectionDate?.let { date ->
                DatePickerComponent(
                    label = "Next Inspection Date",
                    selectedDate = date,
                    onDateSelected = onNextInspectionDateChange
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = inspectorSignature,
                        onValueChange = onInspectorSignatureChange,
                        label = { Text("Inspector Signature") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = supervisorSignature,
                        onValueChange = onSupervisorSignatureChange,
                        label = { Text("Supervisor Signature") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = additionalComments,
                onValueChange = onAdditionalCommentsChange,
                label = { Text("Additional Comments") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
    }
}

@Composable
private fun PhotoAttachmentCard(
    photos: List<String>,
    onPhotosChange: (List<String>) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Photo Attachments",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            ImagePickerComponent(
                imageUris = photos.map { android.net.Uri.parse(it) },
                onImageRemoved = { },
                onImagesSelected = { uris -> onPhotosChange(uris.map { it.toString() }) },
                maxImages = 10,
                allowMultiple = true
            )
        }
    }
}
