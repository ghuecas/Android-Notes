package es.upm.ging.notas.ui.screens

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import es.upm.ging.notas.data.Note
import es.upm.ging.notas.ui.NoteViewModel
import kotlinx.coroutines.launch

@Composable
fun EditNoteScreen(
    onBack: () -> Unit,
    viewModel: NoteViewModel,
    noteId: Long?
) {
    val note: Note? =
        if (noteId != null) {
            val flow = viewModel.getNoteById(noteId)
            val collected by flow.collectAsState(initial = null)
            collected
        } else {
            null
        }

    // ESTADO REAL que refleja lo que escribe el usuario
    var title by remember { mutableStateOf(note?.title ?: "") }
    var category by remember { mutableStateOf(note?.category ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(note) {
        if (note != null) {
            title = note.title
            category = note.category
            content = note.content
        }
    }

    // Detección de gesto "arrastrar a la derecha"
    val gestureModifier = Modifier.pointerInput(Unit) {
        detectHorizontalDragGestures { change, dragAmount ->
            if (dragAmount > 50) onBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.statusBarsPadding().navigationBarsPadding()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .then(gestureModifier)
        ) {

            Text(text = if (noteId == null) "Nueva nota" else "Editar nota")

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoría") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Contenido") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 200.dp),
                maxLines = 8
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Button(
                    onClick = onBack
                ) {
                    Text("Volver")
                }

                Spacer(Modifier.width(16.dp))

                Button(
                    // Guardar
                    onClick = {
                        if (title.isBlank()) {
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "El título es obligatorio",
                                    //actionLabel = "Ok",
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Short
                                )

                                // Si el usuario pulsa "Ok"
                                if (result == SnackbarResult.ActionPerformed) { // nada que hacer
                                }
                            }
                        } else {
                            val newNote = Note(
                                id = note?.id ?: 0,
                                title = title,
                                category = category,
                                content = content
                            )

                            if (noteId == null) {
                                viewModel.insert(newNote)
                            } else {
                                viewModel.update(newNote)
                            }

                            onBack()
                        }
                    }
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}

