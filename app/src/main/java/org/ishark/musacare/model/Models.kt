package org.ishark.musacare.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "patients", indices = [Index("full_name"), Index("is_archived")])
data class PatientEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "full_name") val fullName: String,
    val age: Int,
    val gender: String,
    val phone: String?,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

@Entity(
    tableName = "sessions",
    foreignKeys = [ForeignKey(entity = PatientEntity::class, parentColumns = ["id"], childColumns = ["patient_id"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("patient_id"), Index("session_time")]
)
data class SessionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "patient_id") val patientId: String,
    @ColumnInfo(name = "session_time") val sessionTime: Long,
    val duration: Int,
    val type: String,
    val notes: String,
)

@Entity(
    tableName = "notes",
    foreignKeys = [ForeignKey(entity = PatientEntity::class, parentColumns = ["id"], childColumns = ["patient_id"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("patient_id"), Index("category")]
)
data class NoteEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "patient_id") val patientId: String,
    val category: String,
    val title: String,
    val content: String,
    val updatedAt: Long,
)

@Entity(
    tableName = "mood_entries",
    foreignKeys = [ForeignKey(entity = PatientEntity::class, parentColumns = ["id"], childColumns = ["patient_id"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("patient_id"), Index("date")]
)
data class MoodEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "patient_id") val patientId: String,
    val date: Long,
    val score: Int,
    val comment: String,
)

@Entity(tableName = "tests_catalog")
data class TestCatalogEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
)

@Entity(
    tableName = "patient_tests",
    foreignKeys = [
        ForeignKey(entity = PatientEntity::class, parentColumns = ["id"], childColumns = ["patient_id"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = TestCatalogEntity::class, parentColumns = ["id"], childColumns = ["test_id"], onDelete = ForeignKey.RESTRICT),
    ],
    indices = [Index("patient_id"), Index("status")]
)
data class PatientTestEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "patient_id") val patientId: String,
    @ColumnInfo(name = "test_id") val testId: String,
    val status: String,
    val plannedDate: Long?,
    val resultSummary: String,
)

@Entity(
    tableName = "reminders",
    foreignKeys = [ForeignKey(entity = PatientEntity::class, parentColumns = ["id"], childColumns = ["patient_id"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("patient_id"), Index("scheduledAt")]
)
data class ReminderEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "patient_id") val patientId: String,
    val title: String,
    val type: String,
    val scheduledAt: Long,
    val status: String,
)
