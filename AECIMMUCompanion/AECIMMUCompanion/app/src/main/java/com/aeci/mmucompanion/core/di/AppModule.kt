package com.aeci.mmucompanion.core.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.aeci.mmucompanion.presentation.component.PDFTemplateManager
import com.aeci.mmucompanion.core.util.MobileServerConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .create()
    }
    
    @Provides
    @Singleton
    fun provideMobileServerConfig(@ApplicationContext context: Context): MobileServerConfig {
        return MobileServerConfig(context)
    }
    
    @Provides
    fun provideUpdateEquipmentStatusUseCase(
        equipmentRepository: com.aeci.mmucompanion.domain.repository.EquipmentRepository
    ): com.aeci.mmucompanion.domain.usecase.UpdateEquipmentStatusUseCase {
        return com.aeci.mmucompanion.domain.usecase.UpdateEquipmentStatusUseCase(equipmentRepository)
    }
    
    @Provides
    fun provideGetAllEquipmentUseCase(
        equipmentRepository: com.aeci.mmucompanion.domain.repository.EquipmentRepository
    ): com.aeci.mmucompanion.domain.usecase.GetAllEquipmentUseCase {
        return com.aeci.mmucompanion.domain.usecase.GetAllEquipmentUseCase(equipmentRepository)
    }
    
    @Provides
    fun provideGetEquipmentByIdUseCase(
        equipmentRepository: com.aeci.mmucompanion.domain.repository.EquipmentRepository
    ): com.aeci.mmucompanion.domain.usecase.GetEquipmentByIdUseCase {
        return com.aeci.mmucompanion.domain.usecase.GetEquipmentByIdUseCase(equipmentRepository)
    }
    
    @Provides
    @Singleton
    fun provideExcelExporter(@ApplicationContext context: Context): com.aeci.mmucompanion.core.util.ExcelExporter {
        return com.aeci.mmucompanion.core.util.ExcelExporter(context)
    }
    
    @Provides
    @Singleton
    fun providePDFTemplateManager(@ApplicationContext context: Context): PDFTemplateManager {
        return PDFTemplateManager(context)
    }
}
