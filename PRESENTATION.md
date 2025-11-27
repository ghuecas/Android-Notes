# NotesApp - Arquitectura Android
## Aplicación de Notas con Room Database

---

# Agenda

1. Visión General de la Arquitectura
2. Capas de la Aplicación
3. Capa de Datos (Room Database)
4. Capa de Dominio (Repository)
5. Capa de Presentación (ViewModel + UI)
6. Navegación
7. Decisiones de Diseño Importantes
8. Mejoras Implementadas

---

# 1. Visión General de la Arquitectura

## Patrón: MVVM + Repository Pattern

```
┌─────────────────────────────────────┐
│         UI Layer (Compose)          │
│  NoteListScreen | EditNoteScreen    │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         ViewModel Layer             │
│         NoteViewModel               │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│       Repository Layer              │
│        NoteRepository               │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Data Layer (Room)              │
│    NoteDao | NoteDatabase           │
└─────────────────────────────────────┘
```

---

# 2. Estructura del Proyecto

```
es.upm.ging.notas/
├── data/
│   ├── Note.kt              (Entity)
│   ├── NoteDao.kt           (DAO)
│   ├── NoteDatabase.kt      (Database)
│   └── NoteRepository.kt    (Repository)
├── ui/
│   ├── NoteViewModel.kt     (ViewModel)
│   ├── screens/
│   │   ├── NoteListScreen.kt
│   │   ├── EditNoteScreen.kt
│   │   └── HelpScreen.kt
│   └── theme/
├── AppNav.kt                (Navigation)
└── MainActivity.kt
```

---

# 3. Capa de Datos - Entity

## Note.kt

```kotlin
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0L,
    val title: String,
    val category: String,
    val content: String
)
```

**Conceptos clave:**
- `@Entity`: Marca la clase como tabla de base de datos
- `@PrimaryKey`: Define la clave primaria
- `autoGenerate = true`: IDs autoincrementales
- `data class`: Inmutabilidad y funciones automáticas

---

# 4. Capa de Datos - DAO

## NoteDao.kt

```kotlin
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAllNotes(): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Long): Flow<Note?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long
    
    @Update
    suspend fun update(note: Note)
    
    @Delete
    suspend fun delete(note: Note)
    
    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}
```

---

# 5. DAO - Conceptos Importantes

## Flow vs Suspend

**Flow<T>**: Observables reactivos
- Emite actualizaciones automáticamente
- Ideal para consultas que cambian (SELECT)
- No requiere `suspend`

**suspend**: Operaciones únicas
- Para operaciones de escritura (INSERT, UPDATE, DELETE)
- Ejecuta en corrutina
- Retorna cuando completa

---

# 6. OnConflictStrategy

```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insert(note: Note): Long
```

**Estrategias disponibles:**
- `REPLACE`: Reemplaza si existe
- `IGNORE`: Ignora si existe
- `ABORT`: Cancela la transacción (default)
- `FAIL`: Falla pero continúa
- `ROLLBACK`: Revierte toda la transacción

**Decisión:** Usamos `REPLACE` para evitar crashes por duplicados

---

# 7. Capa de Datos - Database

## NoteDatabase.kt

```kotlin
@Database(entities = [Note::class], version = 1)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    
    companion object {
        @Volatile 
        private var INSTANCE: NoteDatabase? = null
        
        fun getInstance(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "notes.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
```

---

# 8. Singleton Pattern

## ¿Por qué Singleton?

```kotlin
@Volatile private var INSTANCE: NoteDatabase? = null

fun getInstance(context: Context): NoteDatabase {
    return INSTANCE ?: synchronized(this) {
        INSTANCE ?: /* create */ .also { INSTANCE = it }
    }
}
```

**Razones:**
1. **Eficiencia**: Una sola instancia de BD
2. **Thread-safety**: `synchronized` + `@Volatile`
3. **Consistencia**: Todos usan la misma conexión
4. **Double-check locking**: Optimización de rendimiento

---

# 9. Repository Pattern

## NoteRepository.kt

```kotlin
class NoteRepository(private val dao: NoteDao) {
    fun getAllNotes(): Flow<List<Note>> = dao.getAllNotes()
    
    fun getNoteById(id: Long): Flow<Note?> = dao.getNoteById(id)
    
    suspend fun insert(note: Note): Long = dao.insert(note)
    
    suspend fun update(note: Note) = dao.update(note)
    
    suspend fun delete(note: Note) = dao.delete(note)
    
    suspend fun deleteAll() = dao.deleteAll()
}
```

**Ventajas:**
- Abstracción de la fuente de datos
- Facilita testing (mock repository)
- Punto único de acceso a datos

---

# 10. ViewModel

## NoteViewModel.kt - Estructura

```kotlin
class NoteViewModel(
    private val repository: NoteRepository
) : ViewModel() {
    
    val allNotes: Flow<List<Note>> = repository.getAllNotes()
    
    fun insert(note: Note) {
        viewModelScope.launch {
            repository.insert(note)
        }
    }
    
    // ... más métodos
}
```

**Conceptos clave:**
- Inyección de dependencias (Repository)
- `viewModelScope`: Corrutinas vinculadas al ciclo de vida
- Separación de lógica de negocio y UI

---

# 11. ViewModel Factory

## ¿Por qué necesitamos Factory?

```kotlin
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
```

**Razón:** ViewModels con parámetros necesitan Factory

---

# 12. UI Layer - Jetpack Compose

## NoteListScreen.kt - Estructura

```kotlin
@Composable
fun NoteListScreen(
    vm: NoteViewModel,
    onAddNote: () -> Unit,
    onEditNote: (Long) -> Unit,
    // ...
) {
    val notes by vm.allNotes.collectAsState(initial = emptyList())
    
    Scaffold(topBar = { /* ... */ }) { padding ->
        if (notes.isEmpty()) {
            EmptyListMessage()
        } else {
            NotesList(notes, onEditNote)
        }
    }
}
```

---

# 13. State Management

## collectAsState

```kotlin
val notes by vm.allNotes.collectAsState(initial = emptyList())
```

**¿Qué hace?**
1. Convierte `Flow<List<Note>>` en `State<List<Note>>`
2. Recompone UI automáticamente cuando cambian los datos
3. `by`: Delegación de propiedades (acceso directo)
4. `initial`: Valor mientras carga

**Resultado:** UI reactiva sin código manual

---

# 14. Composables Reutilizables

```kotlin
@Composable
private fun NoteRow(
    note: Note,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(/* ... */) {
        Column(
            modifier = Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
        ) {
            Text(note.title, fontWeight = FontWeight.Bold)
            Text(note.category)
        }
    }
}
```

**Principio:** Componentes pequeños y reutilizables

---

# 15. Gestos de Usuario

## combinedClickable

```kotlin
modifier = Modifier.combinedClickable(
    onClick = { onEditNote(note.id) },
    onLongClick = { onDeleteNote(note) }
)
```

**Interacciones:**
- Click: Editar nota
- Long press: Mostrar diálogo de eliminación

**Alternativa considerada:** Swipe-to-delete (más complejo)

---

# 16. Diálogos de Confirmación

```kotlin
if (showDeleteAllDialog) {
    AlertDialog(
        onDismissRequest = { showDeleteAllDialog = false },
        title = { Text("¿Borrar todas las notas?") },
        text = { Text("Esta acción no se puede deshacer.") },
        confirmButton = {
            Button(onClick = {
                onClearAll()
                showDeleteAllDialog = false
            }) { Text("Eliminar todo") }
        },
        dismissButton = {
            Button(onClick = { showDeleteAllDialog = false }) {
                Text("Cancelar")
            }
        }
    )
}
```

---

# 17. Validación de Entrada

## EditNoteScreen - Snackbar

```kotlin
if (title.isBlank()) {
    scope.launch {
        snackbarHostState.showSnackbar(
            message = "El título es obligatorio",
            withDismissAction = true,
            duration = SnackbarDuration.Short
        )
    }
} else {
    // Guardar nota
}
```

**UX:** Feedback inmediato sin navegación

---

# 18. Navegación - Jetpack Navigation

## AppNav.kt

```kotlin
object Routes {
    const val LIST = "list"
    const val EDIT = "EditaNota"
    const val HELP = "help"
    const val NEW_NOTE_ID = -1L
}

@Composable
fun AppNav(noteViewModel: NoteViewModel) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) { /* ... */ }
        composable("${Routes.EDIT}/{noteId}") { /* ... */ }
        composable(Routes.HELP) { /* ... */ }
    }
}
```

---

# 19. Navegación con Argumentos

```kotlin
composable(
    route = "${Routes.EDIT}/{noteId}",
    arguments = listOf(
        navArgument("noteId") { type = NavType.LongType }
    )
) { backStack ->
    val noteId = backStack.arguments?.getLong("noteId") ?: 0L
    EditNoteScreen(
        noteId = if (noteId == Routes.NEW_NOTE_ID) null else noteId,
        viewModel = noteViewModel,
        onBack = { navController.popBackStack() }
    )
}
```

**Decisión:** `null` = nueva nota, `Long` = editar existente

---

# 20. Window Insets (Edge-to-Edge)

## Problema: Contenido tapado por barras del sistema

```kotlin
Scaffold(
    modifier = Modifier
        .statusBarsPadding()      // Barra superior
        .navigationBarsPadding()  // Barra inferior
) { padding ->
    Column(modifier = Modifier.padding(padding)) {
        // Contenido
    }
}
```

**Resultado:** UI respeta áreas seguras del sistema

---

# 21. LaunchedEffect

## Actualización de Estado Asíncrono

```kotlin
LaunchedEffect(note) {
    if (note != null) {
        title = note.title
        category = note.category
        content = note.content
    }
}
```

**¿Cuándo se ejecuta?**
- Al crear el composable
- Cuando `note` cambia (key)

**Uso:** Sincronizar estado con datos cargados

---

# 22. Decisiones de Diseño - Resumen

| Decisión | Razón |
|----------|-------|
| Room Database | Abstracción SQL, type-safe |
| Flow | Reactividad automática |
| Repository Pattern | Testabilidad, abstracción |
| MVVM | Separación UI/lógica |
| Jetpack Compose | UI declarativa moderna |
| Navigation Component | Navegación type-safe |
| Coroutines | Operaciones asíncronas |
| OnConflictStrategy.REPLACE | Evitar crashes |

---

# 23. Mejoras Implementadas

## Refactorización de Código

1. ✅ **Eliminado Context del ViewModel**
   - Antes: `NoteViewModel(context: Context)`
   - Después: `NoteViewModel(repository: NoteRepository)`
   - Razón: Evitar memory leaks

2. ✅ **Inyección de Repository**
   - Mejor testabilidad
   - Separación de responsabilidades

---

# 24. Mejoras Implementadas (cont.)

3. ✅ **OnConflictStrategy en DAO**
   ```kotlin
   @Insert(onConflict = OnConflictStrategy.REPLACE)
   ```
   - Manejo robusto de duplicados

4. ✅ **Constantes en lugar de números mágicos**
   ```kotlin
   const val NEW_NOTE_ID = -1L
   ```
   - Código autodocumentado

5. ✅ **Consistencia en nombres de paquetes**
   - Todo bajo `es.upm.ging.notas`

---

# 25. Flujo de Datos Completo

## Insertar Nueva Nota

```
1. Usuario escribe en EditNoteScreen
2. Click en "Guardar"
3. EditNoteScreen llama viewModel.insert(note)
4. ViewModel lanza corrutina
5. Repository.insert(note)
6. DAO.insert(note) → Room escribe en SQLite
7. Flow<List<Note>> emite nueva lista
8. NoteListScreen recompone automáticamente
9. Nueva nota aparece en la lista
```

---

# 26. Ciclo de Vida y Corrutinas

```kotlin
fun insert(note: Note) {
    viewModelScope.launch {
        repository.insert(note)
    }
}
```

**viewModelScope:**
- Se cancela automáticamente cuando ViewModel se destruye
- Evita memory leaks
- No necesita cancelación manual

---

# 27. Testing Considerations

## Arquitectura Testeable

```kotlin
// Unit test del Repository
class NoteRepositoryTest {
    @Test
    fun `insert note returns id`() = runTest {
        val mockDao = mock<NoteDao>()
        val repository = NoteRepository(mockDao)
        
        whenever(mockDao.insert(any())).thenReturn(1L)
        
        val result = repository.insert(testNote)
        assertEquals(1L, result)
    }
}
```

**Ventaja:** Repository pattern facilita mocking

---

# 28. Mejores Prácticas Aplicadas

1. **Single Responsibility Principle**
   - Cada clase tiene una responsabilidad clara

2. **Dependency Injection**
   - Repository inyectado en ViewModel

3. **Immutability**
   - `data class` con `val`

4. **Reactive Programming**
   - Flow para datos reactivos

5. **Separation of Concerns**
   - UI, ViewModel, Repository, DAO separados

---

# 29. Posibles Extensiones

## Mejoras Futuras

1. **Hilt/Dagger** para DI automática
2. **StateFlow** para mejor gestión de estado UI
3. **Error handling** con sealed classes
4. **Paging 3** para listas grandes
5. **WorkManager** para sincronización
6. **DataStore** para preferencias
7. **Testing** completo (Unit + UI)

---

# 30. Recursos y Referencias

## Documentación Oficial

- [Room Database](https://developer.android.com/training/data-storage/room)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Navigation Component](https://developer.android.com/guide/navigation)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [MVVM Architecture](https://developer.android.com/topic/architecture)

---

# ¡Gracias!

## Preguntas

**Repositorio:** NotesApp  
**Arquitectura:** MVVM + Repository Pattern  
**Stack:** Kotlin, Jetpack Compose, Room, Coroutines

