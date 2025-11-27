package es.upm.ging.notas.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import es.upm.ging.notas.data.Note
import es.upm.ging.notas.data.NoteDatabase
import es.upm.ging.notas.data.NoteRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {
    val allNotes: Flow<List<Note>> = repository.getAllNotes()

    fun getNoteById(id: Long) = repository.getNoteById(id)

    fun insert(note: Note) {
        viewModelScope.launch {
            repository.insert(note)
        }
    }

    fun update(note: Note) {
        viewModelScope.launch {
            repository.update(note)
        }
    }

    fun delete(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
                val db = NoteDatabase.getInstance(context)
                val repository = NoteRepository(db.noteDao())

                @Suppress("UNCHECKED_CAST")
                return NoteViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
