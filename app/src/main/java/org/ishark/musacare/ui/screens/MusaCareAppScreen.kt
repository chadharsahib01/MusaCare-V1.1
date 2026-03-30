package org.ishark.musacare.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ishark.musacare.model.PatientEntity
import org.ishark.musacare.ui.AppViewModel

@Composable
fun MusaCareAppScreen(vm: AppViewModel) {
    val auth by vm.auth.collectAsStateWithLifecycle()
    if (!auth.unlocked) {
        LockScreen(
            pin = auth.pinInput,
            error = auth.error,
            pinSet = auth.pinSet,
            onPin = vm::onPinChange,
            onSubmit = vm::submitPin
        )
        return
    }
    MainScreen(vm)
}

@Composable
private fun LockScreen(pin: String, error: String?, pinSet: Boolean, onPin: (String) -> Unit, onSubmit: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(if (pinSet) "Unlock MusaCare" else "Set 4-digit PIN") }) }) { pv ->
        Column(Modifier.padding(pv).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = pin, onValueChange = onPin, label = { Text("PIN") })
            Button(onClick = onSubmit) { Text(if (pinSet) "Unlock" else "Save PIN") }
            if (error != null) Text(error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(vm: AppViewModel) {
    var selectedPatientId by rememberSaveable { mutableStateOf<String?>(null) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val patients by vm.patients.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("MusaCare - Doctor Workspace") }) }) { pv ->
        Column(Modifier.fillMaxSize().padding(pv).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AddPatientForm(onAdd = vm::addPatient)
            PatientList(patients, onOpen = { selectedPatientId = it.id }, onArchive = vm::archivePatient)
            if (selectedPatientId != null) {
                vm.seedTests()
                TabRow(selectedTabIndex = tab) {
                    listOf("Sessions", "Notes", "Mood", "Tests", "Reminders").forEachIndexed { i, title ->
                        Tab(selected = tab == i, onClick = { tab = i }, text = { Text(title) })
                    }
                }
                when (tab) {
                    0 -> SessionPanel(vm, selectedPatientId!!)
                    1 -> NotesPanel(vm, selectedPatientId!!)
                    2 -> MoodPanel(vm, selectedPatientId!!)
                    3 -> TestsPanel(vm, selectedPatientId!!)
                    4 -> ReminderPanel(vm, selectedPatientId!!)
                }
            }
        }
    }
}

@Composable
private fun AddPatientForm(onAdd: (String, Int, String, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("25") }
    var gender by remember { mutableStateOf("Male") }
    var phone by remember { mutableStateOf("") }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Add Patient")
            OutlinedTextField(name, { name = it }, label = { Text("Name") })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(age, { age = it.filter(Char::isDigit) }, label = { Text("Age") }, modifier = Modifier.weight(1f))
                OutlinedTextField(gender, { gender = it }, label = { Text("Gender") }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(phone, { phone = it }, label = { Text("Phone") })
            Button(onClick = {
                onAdd(name, age.toIntOrNull() ?: 25, gender, phone.ifBlank { null })
                name = ""; phone = ""
            }) { Text("Save Patient") }
        }
    }
}

@Composable
private fun PatientList(items: List<PatientEntity>, onOpen: (PatientEntity) -> Unit, onArchive: (String) -> Unit) {
    Text("Patients")
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        items(items) { p ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(p.fullName)
                        Text("${p.age} • ${p.gender} • ${p.phone ?: "No phone"}")
                    }
                    Button(onClick = { onOpen(p) }) { Text("Open") }
                    Button(onClick = { onArchive(p.id) }) { Text("Archive") }
                }
            }
        }
    }
}

@Composable
private fun SessionPanel(vm: AppViewModel, patientId: String) {
    val list by vm.sessions(patientId).collectAsStateWithLifecycle()
    var type by remember { mutableStateOf("CBT") }
    var duration by remember { mutableStateOf("45") }
    var notes by remember { mutableStateOf("") }
    EditorCard("Add Session") {
        OutlinedTextField(type, { type = it }, label = { Text("Type") })
        OutlinedTextField(duration, { duration = it.filter(Char::isDigit) }, label = { Text("Duration minutes") })
        OutlinedTextField(notes, { notes = it }, label = { Text("Notes") })
        Button(onClick = { vm.addSession(patientId, type, duration.toIntOrNull() ?: 45, notes); notes = "" }) { Text("Save") }
    }
    ListCard("Session Logs", list.map { "${it.type} - ${it.duration} min" })
}

@Composable
private fun NotesPanel(vm: AppViewModel, patientId: String) {
    val list by vm.notes(patientId).collectAsStateWithLifecycle()
    var cat by remember { mutableStateOf("Progress") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    EditorCard("Add Note") {
        OutlinedTextField(cat, { cat = it }, label = { Text("Category") })
        OutlinedTextField(title, { title = it }, label = { Text("Title") })
        OutlinedTextField(content, { content = it }, label = { Text("Content") })
        Button(onClick = { vm.addNote(patientId, cat, title, content); title = ""; content = "" }) { Text("Save") }
    }
    ListCard("Notes", list.map { "${it.category}: ${it.title}" })
}

@Composable
private fun MoodPanel(vm: AppViewModel, patientId: String) {
    val list by vm.mood(patientId).collectAsStateWithLifecycle()
    var score by remember { mutableStateOf("5") }
    var comment by remember { mutableStateOf("") }
    EditorCard("Daily Mood") {
        OutlinedTextField(score, { score = it.filter(Char::isDigit) }, label = { Text("Score 1-10") })
        OutlinedTextField(comment, { comment = it }, label = { Text("Comment") })
        Button(onClick = { vm.addMood(patientId, score.toIntOrNull() ?: 5, comment); comment = "" }) { Text("Save Mood") }
    }
    ListCard("Mood History", list.map { "Score ${it.score}: ${it.comment}" })
}

@Composable
private fun TestsPanel(vm: AppViewModel, patientId: String) {
    val catalog by vm.catalog().collectAsStateWithLifecycle()
    val assigned by vm.patientTests(patientId).collectAsStateWithLifecycle()
    EditorCard("Assign Psychological Test") {
        catalog.forEach { test ->
            Button(onClick = { vm.assignTest(patientId, test.id) }, modifier = Modifier.fillMaxWidth()) {
                Text("Assign ${test.name} (${test.category})")
            }
        }
    }
    ListCard("Assigned Tests", assigned.map { "${it.testId} • ${it.status}" })
}

@Composable
private fun ReminderPanel(vm: AppViewModel, patientId: String) {
    val list by vm.reminders(patientId).collectAsStateWithLifecycle()
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("session") }
    EditorCard("Create Reminder") {
        OutlinedTextField(title, { title = it }, label = { Text("Title") })
        OutlinedTextField(type, { type = it }, label = { Text("Type") })
        Button(onClick = {
            vm.addReminder(patientId, title, type, System.currentTimeMillis() + 60_000)
            title = ""
        }) { Text("Set 1-min Reminder") }
    }
    ListCard("Reminders", list.map { "${it.type}: ${it.title} (${it.status})" })
}

@Composable
private fun EditorCard(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title)
            content()
        }
    }
}

@Composable
private fun ListCard(title: String, rows: List<String>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title)
            rows.take(8).forEach { Text("• $it") }
            if (rows.isEmpty()) Text("No records yet")
        }
    }
}
