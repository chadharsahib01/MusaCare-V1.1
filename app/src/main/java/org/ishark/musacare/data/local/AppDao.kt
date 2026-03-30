package org.ishark.musacare.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.ishark.musacare.model.*

@Dao
interface AppDao {
    @Query("SELECT * FROM patients WHERE is_archived = 0 ORDER BY updated_at DESC")
    fun observePatients(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    suspend fun getPatient(id: String): PatientEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPatient(item: PatientEntity)

    @Update
    suspend fun updatePatient(item: PatientEntity)

    @Query("SELECT * FROM sessions WHERE patient_id = :patientId ORDER BY session_time DESC")
    fun observeSessions(patientId: String): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(item: SessionEntity)

    @Query("SELECT * FROM notes WHERE patient_id = :patientId ORDER BY updatedAt DESC")
    fun observeNotes(patientId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :q || '%' OR content LIKE '%' || :q || '%' ORDER BY updatedAt DESC")
    fun searchNotes(q: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNote(item: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMood(item: MoodEntity)

    @Query("SELECT * FROM mood_entries WHERE patient_id = :patientId ORDER BY date ASC")
    fun observeMood(patientId: String): Flow<List<MoodEntity>>

    @Query("SELECT * FROM tests_catalog ORDER BY name ASC")
    fun observeTestsCatalog(): Flow<List<TestCatalogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCatalog(items: List<TestCatalogEntity>)

    @Query("SELECT * FROM patient_tests WHERE patient_id = :patientId ORDER BY plannedDate DESC")
    fun observePatientTests(patientId: String): Flow<List<PatientTestEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPatientTest(item: PatientTestEntity)

    @Update
    suspend fun updatePatientTest(item: PatientTestEntity)

    @Query("SELECT * FROM reminders WHERE patient_id = :patientId ORDER BY scheduledAt ASC")
    fun observeReminders(patientId: String): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReminder(item: ReminderEntity)

    @Update
    suspend fun updateReminder(item: ReminderEntity)
}
