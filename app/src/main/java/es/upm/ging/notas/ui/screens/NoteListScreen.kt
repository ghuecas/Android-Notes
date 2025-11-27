package es.upm.ging.notas.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.upm.ging.notas.ui.NoteViewModel
import es.upm.ging.notas.data.Note

@Composable
fun NoteListScreen(
    vm: NoteViewModel,
    onAddNote: () -> Unit,
    onClearAll: () -> Unit,
    onHelp: () -> Unit,
    onEditNote: (Long) -> Unit
) {
    val notes by vm.allNotes.collectAsState(initial = emptyList())
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Notas",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Row {
                        IconButton(onClick = onAddNote) {Icon(Icons.Filled.Add, contentDescription = "Añadir") }
                        IconButton(onClick = onHelp) { Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Ayuda") }
                        IconButton(
                            onClick = { showDeleteAllDialog = true },
                            enabled = !notes.isEmpty()
                        ) { Icon(Icons.Filled.DeleteSweep, contentDescription = "Vaciar") }
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (notes.isEmpty()) {
                EmptyListMessage()
            } else {
                NotesList(
                    notes = notes,
                    onEditNote = onEditNote,
                    onDeleteNote = { note -> noteToDelete = note}
                )
            }

            // Diálogo de confirmación al borrar
            noteToDelete?.let { note ->
                AlertDialog(
                    onDismissRequest = { noteToDelete = null },
                    title = { Text("¿Eliminar nota?") },
                    text = { Text("¿Seguro que deseas borrar \"${note.title}\"? Esta acción no se puede deshacer.") },
                    confirmButton = {
                        Button(onClick = {
                            vm.delete(note)
                            noteToDelete = null
                        }) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { noteToDelete = null }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Diálogo de confirmación para borrar todas las notas
            if (showDeleteAllDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteAllDialog = false },
                    title = { Text("¿Borrar todas las notas?") },
                    text = { Text("Esta acción no se puede deshacer.") },
                    confirmButton = {
                        Button(onClick = {
                            onClearAll()
                            showDeleteAllDialog = false
                        }) {
                            Text("Eliminar todo")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDeleteAllDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyListMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("No hay notas guardadas.")
        Spacer(Modifier.height(8.dp))
        Text("Usa el botón “Añadir” del menú para crear tu primera nota.")
    }
}

@Composable
private fun NotesList(
    notes: List<Note>,
    onEditNote: (Long) -> Unit,
    onDeleteNote: (Note) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            NoteRow(
                note = note,
                onClick = { onEditNote(note.id) },
                onLongClick = { onDeleteNote(note)}
            )
        }
    }
}

@Composable
private fun NoteRow(
    note: Note,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable (
                    onClick= onClick,
                    onLongClick= onLongClick
                )
                .padding(12.dp)
        ) {
            Text(text = note.title, fontWeight = FontWeight.Bold)
            Text(text = note.category, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
