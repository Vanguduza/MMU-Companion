package com.aeci.mmucompanion.core.di

import android.content.Context
import androidx.room.Room
import com.aeci.mmucompanion.data.local.MMUDatabase
import com.aeci.mmucompanion.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideMMUDatabase(@ApplicationContext context: Context): MMUDatabase {
        return try {
            Room.databaseBuilder(
                context.applicationContext,
                MMUDatabase::class.java,
                "mmu_database"
            )
                .fallbackToDestructiveMigration(true)
                .build()
        } catch (e: Exception) {
            android.util.Log.e("DatabaseModule", "Error creating database", e)
            // Fallback to destructive migration if database creation fails
            Room.databaseBuilder(
            context.applicationContext,
            MMUDatabase::class.java,
            "mmu_database"
        )
            .fallbackToDestructiveMigration(true)
            .build()
        }
    }
    
    @Provides
    fun provideFormDao(database: MMUDatabase): FormDao = database.formDao()
    
    @Provides
    fun provideUserDao(database: MMUDatabase): UserDao = database.userDao()
    
    @Provides
    fun provideEquipmentDao(database: MMUDatabase): EquipmentDao = database.equipmentDao()
    
    @Provides
    fun provideShiftDao(database: MMUDatabase): ShiftDao = database.shiftDao()
    
    @Provides
    fun provideReportDao(database: MMUDatabase): ReportDao = database.reportDao()
    
    @Provides
    fun provideJobCardDao(database: MMUDatabase): JobCardDao = database.jobCardDao()
    
    @Provides
    fun provideSiteDao(database: MMUDatabase): SiteDao = database.siteDao()
    
    @Provides
    fun provideTodoDao(database: MMUDatabase): TodoDao = database.todoDao()
    
    @Provides
    fun provideTimeEntryDao(database: MMUDatabase): TaskTimeEntryDao = database.taskTimeEntryDao()
    
    @Provides
    fun provideTodoCommentDao(database: MMUDatabase): TodoCommentDao = database.todoCommentDao()
    
    @Provides
    fun provideTodoAttachmentDao(database: MMUDatabase): TodoAttachmentDao = database.todoAttachmentDao()
}
