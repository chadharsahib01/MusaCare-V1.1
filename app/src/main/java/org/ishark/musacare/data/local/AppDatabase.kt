package org.ishark.musacare.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import org.ishark.musacare.model.*

@Database(
    entities = [
        PatientEntity::class,
        SessionEntity::class,
        NoteEntity::class,
        MoodEntity::class,
        TestCatalogEntity::class,
        PatientTestEntity::class,
        ReminderEntity::class,
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao
}
