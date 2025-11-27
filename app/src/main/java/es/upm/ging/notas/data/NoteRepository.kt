package es.upm.ging.notas.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val dao: NoteDao) {
    fun getAllNotes(): Flow<List<Note>> = dao.getAllNotes()
    fun getNoteById(id: Long): Flow<Note?> = dao.getNoteById(id)
    suspend fun insert(note: Note): Long = dao.insert(note)
    suspend fun update(note: Note) = dao.update(note)
    suspend fun delete(note: Note) = dao.delete(note)
    suspend fun deleteAll() = dao.deleteAll()
}
