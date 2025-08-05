package com.aeci.mmucompanion.core.util

import com.aeci.mmucompanion.data.templates.FormTemplates
import com.aeci.mmucompanion.domain.model.FormTemplate
import com.aeci.mmucompanion.domain.model.FormType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FormTemplateProvider @Inject constructor() {
    
    fun getFormTemplate(type: FormType): FormTemplate? {
        return FormTemplates.




        getFormTemplate(type)
    }

    fun getAllTemplates(): List<FormTemplate> {
        return FormTemplates.getAllTemplates()
    }
}
