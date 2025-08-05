package com.aeci.mmucompanion.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.aeci.mmucompanion.data.local.dao.*
import com.aeci.mmucompanion.data.local.entity.*
import com.aeci.mmucompanion.domain.model.Todo

@Database(
    entities = [
        FormEntity::class,
        UserEntity::class,
        EquipmentEntity::class,
        ShiftEntity::class,
        JobCardEntity::class,
        ReportEntity::class,
        SiteEntity::class,
        Todo::class,
        TaskTimeEntryEntity::class,
        TodoCommentEntity::class,
        TodoAttachmentEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MMUDatabase : RoomDatabase() {
    abstract fun formDao(): FormDao
    abstract fun userDao(): UserDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun shiftDao(): ShiftDao
    abstract fun jobCardDao(): JobCardDao
    abstract fun reportDao(): ReportDao
    abstract fun siteDao(): SiteDao
    abstract fun todoDao(): TodoDao
    abstract fun taskTimeEntryDao(): TaskTimeEntryDao
    abstract fun todoCommentDao(): TodoCommentDao
    abstract fun todoAttachmentDao(): TodoAttachmentDao

    companion object {
        @Volatile
        private var INSTANCE: MMUDatabase? = null

        fun getDatabase(context: Context): MMUDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MMUDatabase::class.java,
                    "mmu_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
