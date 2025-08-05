package com.aeci.mmucompanion.domain.usecase

import com.aeci.mmucompanion.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FormValidationService @Inject constructor() {
    
    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val errors: List<ValidationError>) : ValidationResult()
    }
    
    data class ValidationError(
        val field: String,
        val message: String,
        val severity: ErrorSeverity = ErrorSeverity.ERROR
    )
    
    enum class ErrorSeverity {
        WARNING,
        ERROR,
        CRITICAL
    }
    
    /**
     * Validates a digital DigitalForm based on its type and specific requirements
     */
    fun validateForm(DigitalForm: DigitalForm): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Common validations for all forms
        validateCommonFields(DigitalForm, errors)
        
        // Type-specific validations
        when (DigitalForm) {
            is BlastHoleLogForm -> validateBlastHoleLog(DigitalForm, errors)
            is MmuQualityReportForm -> validateMmuQualityReport(DigitalForm, errors)
            is MmuProductionDailyLogForm -> validateMmuProductionDailyLog(DigitalForm, errors)
            is PumpInspection90DayForm -> validatePumpInspection90Day(DigitalForm, errors)
            // Add other DigitalForm types
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    private fun validateCommonFields(DigitalForm: DigitalForm, errors: MutableList<ValidationError>) {
        if (digitalForm.siteLocation.isBlank()) {
            errors.add(ValidationError("siteId", "Site selection is required"))
        }
        
        if (digitalForm.createdBy.isBlank()) {
            errors.add(ValidationError("createdBy", "Creator information is required"))
        }
    }
    
    private fun validateBlastHoleLog(DigitalForm: BlastHoleLogForm, errors: MutableList<ValidationError>) {
        // Validate blast details
        if (digitalForm.blastNumber.isBlank()) {
            errors.add(ValidationError("blastNumber", "Blast number is required"))
        }
        
        if (digitalForm.holes.isEmpty()) {
            errors.add(ValidationError("holes", "At least one hole must be recorded"))
        }
        
        // Validate individual holes
        digitalForm.holes.forEachIndexed { index, hole ->
            validateBlastHole(hole, index, errors)
        }
        
        // Validate emulsion calculations
        val calculatedTotal = digitalForm.holes.sumOf { it.emulsionAmount }
        if (kotlin.math.abs(digitalForm.totalEmulsionUsed - calculatedTotal) > 0.01) {
            errors.add(ValidationError(
                "totalEmulsionUsed",
                "Total emulsion used (${digitalForm.totalEmulsionUsed}) doesn't match sum of individual holes ($calculatedTotal)",
                ErrorSeverity.WARNING
            ))
        }
        
        // Validate timing
        if (digitalForm.blastTime != null && digitalForm.blastTime!!.isAfter(java.time.LocalDateTime.now())) {
            errors.add(ValidationError(
                "blastTime",
                "Blast time cannot be in the future"
            ))
        }
    }
    
    private fun validateBlastHole(hole: BlastHole, index: Int, errors: MutableList<ValidationError>) {
        if (hole.holeNumber.isBlank()) {
            errors.add(ValidationError("holes[$index].holeNumber", "Hole number is required"))
        }
        
        if (hole.depth <= 0) {
            errors.add(ValidationError("holes[$index].depth", "Hole depth must be greater than 0"))
        }
        
        if (hole.diameter <= 0) {
            errors.add(ValidationError("holes[$index].diameter", "Hole diameter must be greater than 0"))
        }
        
        if (hole.emulsionAmount < 0) {
            errors.add(ValidationError("holes[$index].emulsionAmount", "Emulsion amount cannot be negative"))
        }
        
        // Validate emulsion amount based on hole volume (basic check)
        val holeVolume = calculateHoleVolume(hole.depth, hole.diameter)
        val maxEmulsion = holeVolume * 1.2 // Allow 20% overfill
        
        if (hole.emulsionAmount > maxEmulsion) {
            errors.add(ValidationError(
                "holes[$index].emulsionAmount",
                "Emulsion amount (${hole.emulsionAmount}) exceeds reasonable limit for hole volume ($maxEmulsion)",
                ErrorSeverity.WARNING
            ))
        }
    }
    
    private fun validateMmuQualityReport(DigitalForm: MmuQualityReportForm, errors: MutableList<ValidationError>) {
        if (digitalForm.reportNumber.isBlank()) {
            errors.add(ValidationError("reportNumber", "Report number is required"))
        }
        
        if (digitalForm.shiftSupervisor.isBlank()) {
            errors.add(ValidationError("shiftSupervisor", "Shift supervisor is required"))
        }
        
        // Validate production targets vs actual
        if (digitalForm.actualEmulsionProduction > digitalForm.targetEmulsionProduction * 1.5) {
            errors.add(ValidationError(
                "actualEmulsionProduction",
                "Actual production significantly exceeds target - please verify",
                ErrorSeverity.WARNING
            ))
        }
        
        // Validate quality grade
        val validGrades = listOf("A", "B", "C", "D", "FAIL")
        if (digitalForm.qualityGrade.isNotBlank() && !validGrades.contains(digitalForm.qualityGrade.uppercase())) {
            errors.add(ValidationError(
                "qualityGrade",
                "Quality grade must be one of: ${validGrades.joinToString(", ")}"
            ))
        }
        
        // Validate density tests
        digitalForm.densityTests.forEach { test ->
            if (test.density < 0.8 || test.density > 1.5) {
                errors.add(ValidationError(
                    "densityTests",
                    "Density reading ${test.density} is outside normal range (0.8-1.5)",
                    ErrorSeverity.WARNING
                ))
            }
        }
        
        // Validate temperature readings
        if (digitalForm.temperatureReading < -10 || digitalForm.temperatureReading > 60) {
            errors.add(ValidationError(
                "temperatureReading",
                "Temperature reading is outside normal range (-10°C to 60°C)",
                ErrorSeverity.WARNING
            ))
        }
        
        // Validate pH level
        if (digitalForm.phLevel < 0 || digitalForm.phLevel > 14) {
            errors.add(ValidationError(
                "phLevel",
                "pH level must be between 0 and 14"
            ))
        }
    }
    
    private fun validateMmuProductionDailyLog(digitalForm: MmuProductionDailyLogForm, errors: MutableList<ValidationError>) {
        if (digitalForm.operatorName.isBlank()) {
            errors.add(ValidationError("operatorName", "Operator name is required"))
        }
        
        if (digitalForm.supervisorName.isBlank()) {
            errors.add(ValidationError("supervisorName", "Supervisor name is required"))
        }
        
        // Validate time entries (convert String times to LocalTime for comparison)
        try {
            val startTime = java.time.LocalTime.parse(digitalForm.startTime)
            val endTime = java.time.LocalTime.parse(digitalForm.endTime)
            
            if (endTime.isBefore(startTime)) {
                errors.add(ValidationError("endTime", "End time cannot be before start time"))
            }
            
            // Validate operating hours
            val calculatedHours = java.time.Duration.between(startTime, endTime).toHours().toDouble()
            if (kotlin.math.abs(digitalForm.totalOperatingHours - calculatedHours) > 0.5) {
                errors.add(ValidationError(
                    "totalOperatingHours",
                    "Operating hours (${digitalForm.totalOperatingHours}) doesn't match time difference ($calculatedHours)",
                    ErrorSeverity.WARNING
                ))
            }
        } catch (e: Exception) {
            errors.add(ValidationError("startTime", "Invalid time format"))
            errors.add(ValidationError("endTime", "Invalid time format"))
        }
        
        // Validate production efficiency
        if (digitalForm.actualProduction > digitalForm.productionTarget * 1.3) {
            errors.add(ValidationError(
                "actualProduction",
                "Actual production significantly exceeds target - please verify",
                ErrorSeverity.WARNING
            ))
        }
        
        // Validate temperature
        if (digitalForm.operatingTemperature < -20 || digitalForm.operatingTemperature > 80) {
            errors.add(ValidationError(
                "operatingTemperature",
                "Operating temperature is outside normal range (-20°C to 80°C)",
                ErrorSeverity.WARNING
            ))
        }
    }
    
    private fun validatePumpInspection90Day(DigitalForm: PumpInspection90DayForm, errors: MutableList<ValidationError>) {
        if (digitalForm.inspectorName.isBlank()) {
            errors.add(ValidationError("inspectorName", "Inspector name is required"))
        }
        
        if (digitalForm.pumpSerialNumber.isBlank()) {
            errors.add(ValidationError("pumpSerialNumber", "Pump serial number is required"))
        }
        
        // Validate inspection items
        if (digitalForm.visualInspectionItems.isEmpty()) {
            errors.add(ValidationError("visualInspectionItems", "Visual inspection items are required"))
        }
        
        // Check for critical safety issues
        val criticalIssues = digitalForm.visualInspectionItems.filter { 
            !it.passed && it.notes.contains("safety", ignoreCase = true)
        }
        
        if (criticalIssues.isNotEmpty()) {
            errors.add(ValidationError(
                "visualInspectionItems",
                "Critical safety issues identified - immediate action required",
                ErrorSeverity.CRITICAL
            ))
        }
        
        // Validate pressure test results
        digitalForm.pressureTests.forEach { test ->
            if (test.testPressure <= 0) {
                errors.add(ValidationError(
                    "pressureTests",
                    "Test pressure must be greater than 0"
                ))
            }
            
            if (!test.passed && test.notes.isBlank()) {
                errors.add(ValidationError(
                    "pressureTests",
                    "Failed pressure test requires detailed notes"
                ))
            }
        }
    }
    
    private fun calculateHoleVolume(depth: Double, diameter: Double): Double {
        val radius = diameter / 2
        return kotlin.math.PI * radius * radius * depth / 1000000 // Convert to cubic meters
    }
    
    /**
     * Validates DigitalForm dependencies and relationships
     */
    fun validateFormRelationships(DigitalForm: DigitalForm, relatedForms: List<DigitalForm>): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        when (DigitalForm) {
            is BlastHoleLogForm -> {
                validateBlastHoleRelationships(DigitalForm, relatedForms, errors)
            }
            is MmuQualityReportForm -> {
                validateQualityReportRelationships(DigitalForm, relatedForms, errors)
            }
            is MmuProductionDailyLogForm -> {
                validateProductionLogRelationships(DigitalForm, relatedForms, errors)
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    private fun validateBlastHoleRelationships(
        DigitalForm: BlastHoleLogForm,
        relatedForms: List<DigitalForm>,
        errors: MutableList<ValidationError>
    ) {
        // Check if there are corresponding quality reports for high emulsion usage
        if (digitalForm.totalEmulsionUsed > 1000) { // Threshold for requiring quality report
            val qualityReports = relatedForms.filterIsInstance<MmuQualityReportForm>()
                .filter { it.reportDate == digitalForm.blastDate }
            
            if (qualityReports.isEmpty()) {
                errors.add(ValidationError(
                    "relationships",
                    "High emulsion usage requires corresponding quality report",
                    ErrorSeverity.WARNING
                ))
            }
        }
    }
    
    private fun validateQualityReportRelationships(
        DigitalForm: MmuQualityReportForm,
        relatedForms: List<DigitalForm>,
        errors: MutableList<ValidationError>
    ) {
        // Validate against blast hole logs
        val blastLogs = relatedForms.filterIsInstance<BlastHoleLogForm>()
            .filter { it.blastDate == digitalForm.reportDate }
        
        val totalEmulsionFromBlasts = blastLogs.sumOf { it.totalEmulsionUsed }
        
        if (kotlin.math.abs(digitalForm.emulsionUsedToday - totalEmulsionFromBlasts) > 50) {
            errors.add(ValidationError(
                "emulsionUsedToday",
                "Emulsion usage in quality report (${digitalForm.emulsionUsedToday}) doesn't match blast logs ($totalEmulsionFromBlasts)",
                ErrorSeverity.WARNING
            ))
        }
    }
    
    private fun validateProductionLogRelationships(
        DigitalForm: MmuProductionDailyLogForm,
        relatedForms: List<DigitalForm>,
        errors: MutableList<ValidationError>
    ) {
        // Validate against quality reports
        val qualityReports = relatedForms.filterIsInstance<MmuQualityReportForm>()
            .filter { it.reportDate == digitalForm.logDate }
        
        qualityReports.forEach { qr ->
            if (digitalForm.qualityGradeAchieved != qr.qualityGrade && 
                digitalForm.qualityGradeAchieved.isNotBlank() && 
                qr.qualityGrade.isNotBlank()) {
                errors.add(ValidationError(
                    "qualityGradeAchieved",
                    "Quality grade in production log doesn't match quality report",
                    ErrorSeverity.WARNING
                ))
            }
        }
    }
}



