package es.upm.ging.notas

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import es.upm.ging.notas.ui.screens.HelpScreen
import es.upm.ging.notas.ui.NoteViewModel
import es.upm.ging.notas.ui.screens.EditNoteScreen
import es.upm.ging.notas.ui.screens.NoteListScreen

object Routes {
    const val LIST = "list"
    const val EDIT = "EditaNota"
    const val HELP = "help"
    const val NEW_NOTE_ID= -1L
}

@Composable
fun AppNav(noteViewModel: NoteViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            NoteListScreen(
                vm = noteViewModel,
                onAddNote = { navController.navigate("${Routes.EDIT}/${Routes.NEW_NOTE_ID}")},
                onClearAll = { noteViewModel.deleteAll() },
                onHelp = { navController.navigate(Routes.HELP) },
                onEditNote = { id -> navController.navigate("${Routes.EDIT}/$id") }
            )
        }
        composable(
            route = "${Routes.EDIT}/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { backStack ->
            val noteId = backStack.arguments?.getLong("noteId") ?: 0L
            EditNoteScreen(
                noteId = if (noteId == Routes.NEW_NOTE_ID) null else noteId,
                viewModel = noteViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.HELP) {
            HelpScreen(onBack = { navController.popBackStack() })
        }
    }
}
