package gt.uvg.laboratorio9.ui.screens

import android.util.Log
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
import androidx.compose.runtime.DisposableEffect
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

        Text(
            text = "${products.size} productos",
            fontSize = 14.sp
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        products
            .chunked(2)
            .forEach { rowProducts ->

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    rowProducts.forEach { product ->

                        ProductCard(
                            product = product,
                            isFavorite = product.id in favoriteProductIds,
                            onProductClick = onProductClick,
                            onFavoriteClick = onFavoriteClick,
                            modifier = Modifier.weight(1f)
                        )

                    }

                    if (rowProducts.size == 1) {

                        Spacer(
                            modifier = Modifier.weight(1f)
                        )

                    }

                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp)
                )

            }

    }

}

@Composable
private fun ProductCard(
    product: Product,
    isFavorite: Boolean,
    onProductClick: (Int) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    DisposableEffect(product.id) {

        Log.d(
            "CatalogProbe",
            "ENTER product=${product.id}"
        )

        onDispose {

            Log.d(
                "CatalogProbe",
                "DISPOSE product=${product.id}"
            )

        }

    }

    Column(
        modifier = modifier.padding(8.dp)
    ) {

        Text(
            text = product.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = product.description,
            fontSize = 14.sp
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Q${product.price}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = if (product.stock == 0) {
                "Agotado"
            } else {
                "Stock: ${product.stock}"
            },
            fontSize = 14.sp
        )

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

}
