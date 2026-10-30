package gt.uvg.laboratorio9.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gt.uvg.laboratorio9.viewmodel.ConcurrencyTestState

@Composable
fun ConcurrencyTestPanel(
    state: ConcurrencyTestState,
    onRunTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Prueba de concurrencia")

        Button(
            onClick = onRunTest,
            enabled = state !is ConcurrencyTestState.Running
        ) {
            Text("Ejecutar 20 reservas")
        }

        when (state) {
            ConcurrencyTestState.Idle -> { Text("Prueba pendiente") }

            ConcurrencyTestState.Running -> {
                CircularProgressIndicator()
                Text("Ejecutando reservas...")
            }

            is ConcurrencyTestState.Completed -> {
                Text("Intentos: ${state.attempts}")
                Text("Éxitos: ${state.successes}")
                Text("Rechazos: ${state.rejections}")
            }

            is ConcurrencyTestState.Error -> { Text(state.message) }
        }

        Spacer(Modifier.height(8.dp))
    }
}