package es.upm.ging.notas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import es.upm.ging.notas.ui.NoteViewModel
import es.upm.ging.notas.ui.theme.NotasTheme
import es.upm.ging.notas.AppNav

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NotasTheme {
                val vm: NoteViewModel = viewModel(factory = NoteViewModel.Factory(applicationContext))
                AppNav(vm)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NotasTheme {

    }
}