package es.upm.ging.notas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.upm.ging.notas.R
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun HelpScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    // Leer el fichero help.txt de res/raw
    val helpText = remember {
        val inputStream = context.resources.openRawResource(R.raw.ayuda)
        val reader = BufferedReader(InputStreamReader(inputStream))
        reader.use { it.readText() }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Cabecera personalizada
            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "Ayuda",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            // Contenido de ayuda
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(helpText)
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onBack) {
                        Text("Volver")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun HelpScreenPreview() {
    HelpScreen (onBack = {})
}