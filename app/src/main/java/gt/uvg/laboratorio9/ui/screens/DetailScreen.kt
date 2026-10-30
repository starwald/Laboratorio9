
package gt.uvg.laboratorio9.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import gt.uvg.laboratorio9.ui.components.ProductImage
import gt.uvg.laboratorio9.ui.components.ConcurrencyTestPanel
import gt.uvg.laboratorio9.viewmodel.ConcurrencyTestState

@Composable
fun DetailScreen(
    product: Product,
    isFavorite: Boolean,
    orderMessage: String?,
    orderUnitCount: Int,
    onFavoriteClick: () -> Unit,
    onAddToOrder: () -> Unit,
    onOrderClick: () -> Unit,
    onProducerClick: (Int) -> Unit,
    onBack: () -> Unit,
    concurrencyTestState: ConcurrencyTestState,
    onConcurrencyTest: () -> Unit,
    modifier: Modifier = Modifier
) {

    var showTechnicalInfo by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        // BOTON REGRESAR

        TextButton(
            onClick = onBack
        ) {
            Text("← Regresar")
        }

        // NOMBRE DEL PRODUCTO

        Text(
            text = product.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // IMAGEN DEL PRODUCTO

        ProductImage(
            imageUrl = product.imageUrl,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // DESCRIPCION

        Text(
            text = product.description,
            fontSize = 16.sp
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // PRECIO

        Text(
            text = "Q%.2f".format(product.price),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        // EXISTENCIAS

        Text(
            text = if (product.stock == 0) {
                "Agotado"
            } else {
                "Stock: ${product.stock}"
            }
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // AGREGAR AL PEDIDO

        Button(
            onClick = onAddToOrder,
            enabled = product.stock > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (product.stock == 0) {
                    "Producto agotado"
                } else {
                    "Agregar al pedido"
                }
            )
        }

        // MENSAJE DEL PEDIDO

        if (orderMessage != null) {

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = orderMessage,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // VER PEDIDO

        Button(
            onClick = onOrderClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Ver pedido ($orderUnitCount)"
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // FAVORITOS

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

        // FICHA TECNICA

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

        // CONOCER PRODUCTOR

        Button(
            onClick = {
                onProducerClick(
                    product.producerId
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Conocer productor")
        }

        // LAB FINAL - PRUEBA DE CONCURRENCIA

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        ConcurrencyTestPanel(
            state = concurrencyTestState,
            onRunTest = onConcurrencyTest
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )
    }
}
