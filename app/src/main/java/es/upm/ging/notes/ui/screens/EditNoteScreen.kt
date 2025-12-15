package es.upm.ging.notes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.upm.ging.notes.R
import es.upm.ging.notes.data.Note
import es.upm.ging.notes.ui.NoteViewModel
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
    val titleRequiredMessage = stringResource(id = R.string.title_required)


    LaunchedEffect(note) {
        if (note != null) {
            title = note.title
            category = note.category
            content = note.content
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
        ) {

            Text(text = if (noteId == null) stringResource(id = R.string.new_note) else stringResource(id = R.string.edit_note))

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(id = R.string.note_title_hint)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text(stringResource(id = R.string.note_category_hint)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text(stringResource(id = R.string.note_content_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 200.dp),
                maxLines = 8
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ){
                Button(
                    onClick = onBack
                ) {
                    Text(stringResource(id = R.string.back))
                }

                Spacer(Modifier.width(16.dp))

                Button(
                    // Guardar
                    onClick = {
                        if (title.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = titleRequiredMessage,
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Short
                                )
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
                    Text(stringResource(id = R.string.save))
                }
            }
        }
    }
}
