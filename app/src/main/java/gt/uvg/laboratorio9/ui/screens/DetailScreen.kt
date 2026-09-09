package gt.uvg.laboratorio9.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gt.uvg.laboratorio9.model.Product

@Composable
fun DetailScreen(
    product: Product,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onProducerClick: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTechnicalInfo by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        TextButton(
            onClick = onBack
        ) {
            Text("← Regresar")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = product.description,
            fontSize = 16.sp
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "Q${product.price}",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Button(
            onClick = onFavoriteClick
        ) {
            Text(
                text = if (isFavorite) {
                    "♥ Quitar de favoritos"
                } else {
                    "♡ Agregar a favoritos"
                }
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Button(
            onClick = {
                showTechnicalInfo = !showTechnicalInfo
            }
        ) {
            Text(
                text = if (showTechnicalInfo) {
                    "Ocultar ficha técnica"
                } else {
                    "Ver ficha técnica"
                }
            )
        }

        if (showTechnicalInfo) {
            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "Ficha técnica",
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Origen: Guatemala"
            )

            Text(
                text = "Presentación: café tostado"
            )

            Text(
                text = "Contenido: 1 libra"
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Button(
            onClick = {
                onProducerClick(product.producerId)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Conocer productor")
        }
    }
}