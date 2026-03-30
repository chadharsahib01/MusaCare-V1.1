package org.ishark.musacare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ishark.musacare.model.*
import org.ishark.musacare.repo.AppRepository

data class AuthState(
    val pinSet: Boolean = false,
    val pinInput: String = "",
    val unlocked: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val repo: AppRepository,
) : ViewModel() {
    private val _auth = MutableStateFlow(AuthState())
    val auth: StateFlow<AuthState> = _auth

    init {
        viewModelScope.launch {
            val isSet = repo.isPinSet()
            _auth.update { it.copy(pinSet = isSet) }
        }
    }

    val patients = repo.observePatients().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onPinChange(v: String) { if (v.length <= 4 && v.all(Char::isDigit)) _auth.update { it.copy(pinInput = v, error = null) } }

    fun submitPin() = viewModelScope.launch {
        val current = _auth.value.pinInput
        if (current.length != 4) {
            _auth.update { it.copy(error = "Enter 4 digits") }
            return@launch
        }
        if (!_auth.value.pinSet) {
            repo.savePin(current)
            _auth.update { it.copy(pinSet = true, pinInput = "", unlocked = true, error = null) }
        } else {
            val ok = repo.verifyPin(current)
            _auth.update { it.copy(pinInput = "", unlocked = ok, error = if (ok) null else "Incorrect PIN") }
        }
    }

    fun addPatient(name: String, age: Int, gender: String, phone: String?) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        repo.addPatient(name, age.coerceIn(1, 120), gender.ifBlank { "Unknown" }, phone)
    }

    fun archivePatient(id: String) = viewModelScope.launch { repo.archivePatient(id) }

    fun sessions(patientId: String) = repo.observeSessions(patientId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun addSession(patientId: String, type: String, duration: Int, notes: String) = viewModelScope.launch {
        repo.addSession(patientId, type.ifBlank { "Follow-up" }, duration.coerceAtLeast(1), notes)
    }

    fun notes(patientId: String) = repo.observeNotes(patientId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun addNote(patientId: String, category: String, title: String, content: String) = viewModelScope.launch {
        repo.addNote(patientId, category.ifBlank { "Progress" }, title, content)
    }

    fun mood(patientId: String) = repo.observeMood(patientId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun addMood(patientId: String, score: Int, comment: String) = viewModelScope.launch {
        repo.saveMood(patientId, score.coerceIn(1, 10), comment)
    }

    fun catalog() = repo.observeTestsCatalog().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun patientTests(patientId: String) = repo.observePatientTests(patientId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun seedTests() = viewModelScope.launch { repo.seedTestsIfNeeded() }
    fun assignTest(patientId: String, testId: String) = viewModelScope.launch { repo.assignTest(patientId, testId) }

    fun reminders(patientId: String) = repo.observeReminders(patientId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun addReminder(patientId: String, title: String, type: String, schedule: Long) = viewModelScope.launch {
        repo.addReminder(patientId, title, type.ifBlank { "session" }, schedule)
    }
}
