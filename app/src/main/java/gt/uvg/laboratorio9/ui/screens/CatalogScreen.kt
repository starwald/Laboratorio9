package gt.uvg.laboratorio9.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gt.uvg.laboratorio9.model.Product

@Composable
fun CatalogScreen(
    products: List<Product>,
    favoriteProductIds: Set<Int>,
    onProductClick: (Int) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Cafetería",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Cafés de Guatemala",
            fontSize = 16.sp
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        products.forEach { product ->
            val isFavorite = product.id in favoriteProductIds

            Text(
                text = product.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = product.description
            )

            Text(
                text = "Q${product.price}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        onProductClick(product.id)
                    }
                ) {
                    Text("Ver producto")
                }

                TextButton(
                    onClick = {
                        onFavoriteClick(product.id)
                    }
                ) {
                    Text(
                        text = if (isFavorite) {
                            "♥ Favorito"
                        } else {
                            "♡ Favorito"
                        }
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}
