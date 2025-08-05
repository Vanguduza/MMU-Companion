package com.aeci.mmucompanion.domain.model.forms

import com.aeci.mmucompanion.domain.model.*
import java.time.LocalDateTime

/**
 * UOR (Unusual Occurrence Report) Template
 * Code-defined template that programmatically replicates the original PDF layout
 */
class UORTemplate : DigitalFormTemplate {
    override val templateId: String = "UOR_TEMPLATE_V1"
    override val title: String = "Unusual Occurrence Report (UOR)"
    override val version: String = "1.0"
    override val formType: FormType = FormType.UOR_REPORT
    override val pdfFileName: String = "UOR[1].pdf"
    
    // Header and logo coordinates for programmatic PDF generation
    override val logoCoordinates: List<LogoCoordinate> = listOf(
        LogoCoordinate(logoType = "company", x = 50f, y = 750f, width = 120f, height = 60f, imagePath = "assets/images/aeci-logo.png")
    )
    
    override val staticTextCoordinates: List<StaticTextCoordinate> = listOf(
        StaticTextCoordinate(text = "UNUSUAL OCCURRENCE REPORT", x = 200f, y = 780f, fontSize = 16f, fontWeight = "bold"),
        StaticTextCoordinate(text = "AECI Mining Explosives", x = 200f, y = 760f, fontSize = 12f)
    )
    
    override val headerCoordinates: List<HeaderCoordinate> = listOf(
        HeaderCoordinate(x = 50f, y = 720f, width = 500f, height = 30f, label = "INCIDENT DETAILS")
    )
    
    override val formRelationships: List<FormRelationship> = listOf(
        FormRelationship("siteId", FormType.SAFETY, "relatedUorId", RelationshipType.LOOKUP)
    )
    
    override fun getFormTemplate(): FormDefinition {
        return FormDefinition(
            id = templateId,
            name = title,
            description = "Report for documenting unusual occurrences and safety incidents",
            sections = listOf(
                // Header Section
                FormSection(
                    id = "header_section",
                    title = "Report Information",
                    fields = listOf(
                        FormField(
                            fieldName = "reportNumber",
                            label = "Report Number",
                            fieldType = FormFieldType.TEXT,
                            isRequired = true,
                            placeholder = "UOR-2024-001"
                        ),
                        FormField(
                            fieldName = "reportDate",
                            label = "Report Date",
                            fieldType = FormFieldType.DATE,
                            isRequired = true,
                            defaultValue = LocalDateTime.now().toString()
                        ),
                        FormField(
                            fieldName = "siteName",
                            label = "Site Name",
                            fieldType = FormFieldType.TEXT,
                            isRequired = true,
                            placeholder = "Enter site name"
                        ),
                        FormField(
                            fieldName = "reportedBy",
                            label = "Reported By",
                            fieldType = FormFieldType.TEXT,
                            isRequired = true,
                            placeholder = "Employee name"
                        )
                    )
                ),
                
                // Incident Details Section
                FormSection(
                    id = "incident_section",
                    title = "Incident Details",
                    fields = listOf(
                        FormField(
                            fieldName = "incidentDate",
                            label = "Date of Incident",
                            fieldType = FormFieldType.DATE,
                            isRequired = true
                        ),
                        FormField(
                            fieldName = "incidentTime",
                            label = "Time of Incident",
                            fieldType = FormFieldType.TIME,
                            isRequired = true
                        ),
                        FormField(
                            fieldName = "location",
                            label = "Location",
                            fieldType = FormFieldType.TEXT,
                            isRequired = true,
                            placeholder = "Specific location where incident occurred"
                        ),
                        FormField(
                            fieldName = "incidentType",
                            label = "Type of Incident",
                            fieldType = FormFieldType.DROPDOWN,
                            isRequired = true,
                            options = listOf("Near Miss", "Injury", "Property Damage", "Environmental", "Security", "Fire")
                        ),
                        FormField(
                            fieldName = "severityLevel",
                            label = "Severity Level",
                            fieldType = FormFieldType.DROPDOWN,
                            isRequired = true,
                            options = listOf("Low", "Medium", "High", "Critical")
                        )
                    )
                ),
                
                // Description Section
                FormSection(
                    id = "description_section",
                    title = "Incident Description",
                    fields = listOf(
                        FormField(
                            fieldName = "incidentDescription",
                            label = "Detailed Description",
                            fieldType = FormFieldType.TEXTAREA,
                            isRequired = true,
                            placeholder = "Provide a detailed description of what happened"
                        ),
                        FormField(
                            fieldName = "immediateActions",
                            label = "Immediate Actions Taken",
                            fieldType = FormFieldType.TEXTAREA,
                            isRequired = true,
                            placeholder = "Describe immediate actions taken"
                        ),
                        FormField(
                            fieldName = "witnessesPresent",
                            label = "Witnesses Present",
                            fieldType = FormFieldType.CHECKBOX,
                            isRequired = false
                        ),
                        FormField(
                            fieldName = "witnessNames",
                            label = "Witness Names",
                            fieldType = FormFieldType.TEXTAREA,
                            isRequired = false,
                            placeholder = "List witness names and contact information"
                        )
                    )
                ),
                
                // Investigation Section
                FormSection(
                    id = "investigation_section",
                    title = "Investigation & Root Cause",
                    fields = listOf(
                        FormField(
                            fieldName = "rootCause",
                            label = "Root Cause Analysis",
                            fieldType = FormFieldType.TEXTAREA,
                            isRequired = false,
                            placeholder = "Identify the root cause of the incident"
                        ),
                        FormField(
                            fieldName = "contributingFactors",
                            label = "Contributing Factors",
                            fieldType = FormFieldType.TEXTAREA,
                            isRequired = false,
                            placeholder = "List any contributing factors"
                        ),
                        FormField(
                            fieldName = "correctiveActions",
                            label = "Corrective Actions",
                            fieldType = FormFieldType.TEXTAREA,
                            isRequired = true,
                            placeholder = "Describe corrective actions to prevent recurrence"
                        ),
                        FormField(
                            fieldName = "actionOwner",
                            label = "Action Owner",
                            fieldType = FormFieldType.TEXT,
                            isRequired = true,
                            placeholder = "Person responsible for implementing actions"
                        ),
                        FormField(
                            fieldName = "targetCompletionDate",
                            label = "Target Completion Date",
                            fieldType = FormFieldType.DATE,
                            isRequired = true
                        )
                    )
                ),
                
                // Approval Section
                FormSection(
                    id = "approval_section",
                    title = "Approvals",
                    fields = listOf(
                        FormField(
                            fieldName = "supervisorName",
                            label = "Supervisor Name",
                            fieldType = FormFieldType.TEXT,
                            isRequired = true,
                            placeholder = "Immediate supervisor"
                        ),
                        FormField(
                            fieldName = "supervisorSignature",
                            label = "Supervisor Signature",
                            fieldType = FormFieldType.SIGNATURE,
                            isRequired = true
                        ),
                        FormField(
                            fieldName = "managerName",
                            label = "Manager Name",
                            fieldType = FormFieldType.TEXT,
                            isRequired = true,
                            placeholder = "Site manager"
                        ),
                        FormField(
                            fieldName = "managerSignature",
                            label = "Manager Signature",
                            fieldType = FormFieldType.SIGNATURE,
                            isRequired = true
                        ),
                        FormField(
                            fieldName = "approvalDate",
                            label = "Approval Date",
                            fieldType = FormFieldType.DATE,
                            isRequired = true
                        )
                    )
                )
            )
        )
    }
    
    override fun getValidationRules(): List<ValidationRule> {
        return listOf(
            ValidationRule("reportNumber", "required", "value != null && value.trim().isNotEmpty()", "Report number is required"),
            ValidationRule("reportNumber", "pattern", "value.matches(\"UOR-\\\\d{4}-\\\\d{3}\")", "Report number must follow format UOR-YYYY-NNN"),
            ValidationRule("incidentDescription", "minLength", "value.length >= 50", "Description must be at least 50 characters"),
            ValidationRule("correctiveActions", "required", "value != null && value.trim().isNotEmpty()", "Corrective actions are required")
        )
    }
    
    override fun getRelatedFormUpdates(): List<FormRelationshipUpdate> {
        return listOf(
            FormRelationshipUpdate(
                targetFormType = FormType.SAFETY,
                fieldMappings = mapOf(
                    "siteId" to "relatedSiteId",
                    "incidentDate" to "relatedIncidentDate",
                    "severityLevel" to "relatedSeverity"
                )
            )
        )
    }
    
    override fun getPdfFieldMappings(): Map<String, PdfFieldMapping> {
        return mapOf(
            "reportNumber" to PdfFieldMapping("reportNumber", "reportNumber", FormCoordinate(150f, 720f, 200f, 20f), FormFieldType.TEXT),
            "reportDate" to PdfFieldMapping("reportDate", "reportDate", FormCoordinate(400f, 720f, 150f, 20f), FormFieldType.DATE),
            "siteName" to PdfFieldMapping("siteName", "siteName", FormCoordinate(150f, 690f, 400f, 20f), FormFieldType.TEXT),
            "reportedBy" to PdfFieldMapping("reportedBy", "reportedBy", FormCoordinate(150f, 660f, 200f, 20f), FormFieldType.TEXT),
            "incidentDate" to PdfFieldMapping("incidentDate", "incidentDate", FormCoordinate(150f, 620f, 150f, 20f), FormFieldType.DATE),
            "incidentTime" to PdfFieldMapping("incidentTime", "incidentTime", FormCoordinate(350f, 620f, 100f, 20f), FormFieldType.TEXT),
            "location" to PdfFieldMapping("location", "location", FormCoordinate(150f, 590f, 400f, 20f), FormFieldType.TEXT),
            "incidentType" to PdfFieldMapping("incidentType", "incidentType", FormCoordinate(150f, 560f, 200f, 20f), FormFieldType.TEXT),
            "severityLevel" to PdfFieldMapping("severityLevel", "severityLevel", FormCoordinate(400f, 560f, 150f, 20f), FormFieldType.TEXT),
            "incidentDescription" to PdfFieldMapping("incidentDescription", "incidentDescription", FormCoordinate(50f, 450f, 500f, 80f), FormFieldType.TEXTAREA),
            "immediateActions" to PdfFieldMapping("immediateActions", "immediateActions", FormCoordinate(50f, 350f, 500f, 60f), FormFieldType.TEXTAREA),
            "rootCause" to PdfFieldMapping("rootCause", "rootCause", FormCoordinate(50f, 270f, 500f, 60f), FormFieldType.TEXTAREA),
            "correctiveActions" to PdfFieldMapping("correctiveActions", "correctiveActions", FormCoordinate(50f, 190f, 500f, 60f), FormFieldType.TEXTAREA),
            "supervisorSignature" to PdfFieldMapping("supervisorSignature", "supervisorSignature", FormCoordinate(100f, 120f, 150f, 40f), FormFieldType.SIGNATURE),
            "managerSignature" to PdfFieldMapping("managerSignature", "managerSignature", FormCoordinate(350f, 120f, 150f, 40f), FormFieldType.SIGNATURE)
        )
    }
}