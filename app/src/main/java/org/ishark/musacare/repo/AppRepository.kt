package org.ishark.musacare.repo

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import org.ishark.musacare.data.local.AppDao
import org.ishark.musacare.data.local.AuthStore
import java.security.MessageDigest
import org.ishark.musacare.model.*

@Singleton
class AppRepository @Inject constructor(
    private val dao: AppDao,
    private val authStore: AuthStore
) {
    fun observePatients(): Flow<List<PatientEntity>> = dao.observePatients()

    suspend fun isPinSet(): Boolean = authStore.getPinHash() != null

    suspend fun verifyPin(rawPin: String): Boolean {
        val hash = authStore.getPinHash() ?: return false
        return hash == sha256(rawPin)
    }

    suspend fun savePin(rawPin: String) {
        authStore.savePinHash(sha256(rawPin))
    }

    private fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun addPatient(name: String, age: Int, gender: String, phone: String?) {
        val now = System.currentTimeMillis()
        dao.insertPatient(
            PatientEntity(
                id = UUID.randomUUID().toString(),
                fullName = name.trim(),
                age = age,
                gender = gender.trim(),
                phone = phone,
                isArchived = false,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun archivePatient(id: String) {
        val current = dao.getPatient(id) ?: return
        dao.updatePatient(current.copy(isArchived = true, updatedAt = System.currentTimeMillis()))
    }

    fun observeSessions(patientId: String): Flow<List<SessionEntity>> = dao.observeSessions(patientId)
    suspend fun addSession(patientId: String, type: String, duration: Int, notes: String) {
        dao.insertSession(
            SessionEntity(
                id = UUID.randomUUID().toString(),
                patientId = patientId,
                sessionTime = System.currentTimeMillis(),
                duration = duration,
                type = type,
                notes = notes
            )
        )
    }

    fun observeNotes(patientId: String): Flow<List<NoteEntity>> = dao.observeNotes(patientId)
    fun searchNotes(query: String): Flow<List<NoteEntity>> = dao.searchNotes(query)
    suspend fun addNote(patientId: String, category: String, title: String, content: String) {
        dao.insertNote(
            NoteEntity(
                id = UUID.randomUUID().toString(),
                patientId = patientId,
                category = category,
                title = title,
                content = content,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun observeMood(patientId: String): Flow<List<MoodEntity>> = dao.observeMood(patientId)
    suspend fun saveMood(patientId: String, score: Int, comment: String) {
        dao.upsertMood(
            MoodEntity(
                id = UUID.randomUUID().toString(),
                patientId = patientId,
                date = System.currentTimeMillis(),
                score = score,
                comment = comment
            )
        )
    }

    fun observeTestsCatalog(): Flow<List<TestCatalogEntity>> = dao.observeTestsCatalog()
    suspend fun seedTestsIfNeeded() {
        dao.upsertCatalog(
            listOf(
                TestCatalogEntity("wais4", "WAIS-IV", "Cognitive"),
                TestCatalogEntity("mmpi3", "MMPI-3", "Personality"),
                TestCatalogEntity("wiat4", "WIAT-4", "Achievement"),
                TestCatalogEntity("moca", "MoCA", "Neuropsych"),
                TestCatalogEntity("conners4", "Conners-4", "Behavioral"),
            )
        )
    }

    fun observePatientTests(patientId: String): Flow<List<PatientTestEntity>> = dao.observePatientTests(patientId)
    suspend fun assignTest(patientId: String, testId: String) {
        dao.insertPatientTest(
            PatientTestEntity(
                id = UUID.randomUUID().toString(),
                patientId = patientId,
                testId = testId,
                status = "assigned",
                plannedDate = System.currentTimeMillis(),
                resultSummary = ""
            )
        )
    }

    fun observeReminders(patientId: String): Flow<List<ReminderEntity>> = dao.observeReminders(patientId)
    suspend fun addReminder(patientId: String, title: String, type: String, scheduledAt: Long) {
        dao.insertReminder(
            ReminderEntity(
                id = UUID.randomUUID().toString(),
                patientId = patientId,
                title = title,
                type = type,
                scheduledAt = scheduledAt,
                status = "pending"
            )
        )
    }
}
