package gt.uvg.laboratorio9.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gt.uvg.laboratorio9.model.Product
import kotlinx.coroutines.launch

@Composable
fun CatalogScreen(
    products: List<Product>,
    favoriteProductIds: Set<Int>,
    gridState: LazyGridState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onProductClick: (Int) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    val coroutineScope = rememberCoroutineScope()

    val cleanQuery = searchQuery.trim()

    val filteredProducts = products.filter { product ->

        product.name.contains(
            cleanQuery,
            ignoreCase = true
        )

    }

    val showBackToTop =
        gridState.firstVisibleItemIndex > 0

    Box(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
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
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = searchQuery,

                onValueChange = {
                    onSearchQueryChange(it)
                },

                label = {
                    Text("Buscar producto")
                },

                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "${filteredProducts.size} de ${products.size} productos",
                fontSize = 14.sp
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            if (filteredProducts.isEmpty()) {

                Text(
                    text = "No encontramos productos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Intenta con otro nombre de café."
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Button(
                    onClick = {
                        onSearchQueryChange("")
                    }
                ) {

                    Text("Limpiar búsqueda")

                }

            } else {

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    modifier = Modifier.fillMaxSize()
                ) {

                    items(
                        items = filteredProducts,

                        key = { product ->
                            product.id
                        }
                    ) { product ->

                        ProductCard(
                            product = product,

                            isFavorite =
                                product.id in favoriteProductIds,

                            onProductClick =
                                onProductClick,

                            onFavoriteClick =
                                onFavoriteClick
                        )

                    }

                }

            }

        }

        if (showBackToTop) {

            Button(
                onClick = {

                    coroutineScope.launch {

                        gridState.animateScrollToItem(0)

                    }

                },

                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {

                Text("↑ Volver arriba")

            }

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
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
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

                onProductClick(
                    product.id
                )

            }
        ) {

            Text("Ver producto")

        }

        TextButton(
            onClick = {

                onFavoriteClick(
                    product.id
                )

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