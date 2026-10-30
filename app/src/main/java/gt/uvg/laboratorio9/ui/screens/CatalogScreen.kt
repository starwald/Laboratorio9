package gt.uvg.laboratorio9.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gt.uvg.laboratorio9.model.CatalogOrder
import gt.uvg.laboratorio9.model.Product
import gt.uvg.laboratorio9.ui.components.ProductImage
import kotlinx.coroutines.launch

@Composable
fun CatalogScreen(
    products: List<Product>,
    favoriteProductIds: Set<Int>,
    gridState: LazyGridState,
    searchQuery: String,
    orderUnitCount: Int,
    catalogOrder: CatalogOrder,
    isLoading: Boolean,
    errorMessage: String?,
    actionMessage: String?,
    onRetry: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOrderClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onProductClick: (Int) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    onCatalogOrderChange: (CatalogOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val cleanQuery = searchQuery.trim()

    val filteredProducts = products.filter {
        it.name.contains(cleanQuery, ignoreCase = true)
    }

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
            Text("Cafés de Guatemala")

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onOrderClick) {
                    Text("Pedido ($orderUnitCount)")
                }
                OutlinedButton(onClick = onOrdersClick) {
                    Text("Mis pedidos")
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    onSearchQueryChange(query)
                    coroutineScope.launch {
                        gridState.scrollToItem(0)
                    }
                },
                label = { Text("Buscar producto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Ordenar por:",
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CatalogOrderButton(
                    label = "Nombre",
                    selected = catalogOrder == CatalogOrder.NAME,
                    onClick = {
                        onCatalogOrderChange(CatalogOrder.NAME)
                        coroutineScope.launch {
                            gridState.scrollToItem(0)
                        }
                    }
                )
                CatalogOrderButton(
                    label = "Precio",
                    selected = catalogOrder == CatalogOrder.PRICE,
                    onClick = {
                        onCatalogOrderChange(CatalogOrder.PRICE)
                        coroutineScope.launch {
                            gridState.scrollToItem(0)
                        }
                    }
                )
            }

            if (actionMessage != null) {
                Text(
                    text = actionMessage,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Cargando catálogo…")
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
                Button(
                    onClick = onRetry,
                    enabled = !isLoading
                ) {
                    Text("Reintentar")
                }
            }

            Spacer(Modifier.height(8.dp))

            if (products.isNotEmpty()) {
                Text(
                    text = "${filteredProducts.size} de " +
                            "${products.size} productos",
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
            }

            when {
                products.isEmpty() && !isLoading &&
                        errorMessage == null -> {
                    Text("No hay productos disponibles.")
                    Button(onClick = onRetry) {
                        Text("Volver a cargar")
                    }
                }

                products.isNotEmpty() && filteredProducts.isEmpty() -> {
                    Text(
                        text = "No encontramos productos",
                        fontWeight = FontWeight.Bold
                    )
                    Text("Intenta con otro nombre de café.")
                    Button(
                        onClick = {
                            onSearchQueryChange("")
                            coroutineScope.launch {
                                gridState.scrollToItem(0)
                            }
                        }
                    ) {
                        Text("Limpiar búsqueda")
                    }
                }

                filteredProducts.isNotEmpty() -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            items = filteredProducts,
                            key = { it.id }
                        ) { product ->
                            ProductCard(
                                product = product,
                                isFavorite =
                                    product.id in favoriteProductIds,
                                onProductClick = onProductClick,
                                onFavoriteClick = onFavoriteClick
                            )
                        }
                    }
                }
            }
        }

        if (
            filteredProducts.isNotEmpty() &&
            gridState.firstVisibleItemIndex > 0
        ) {
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
private fun CatalogOrderButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier) {
            Text(label)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) {
            Text(label)
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        ProductImage(
            imageUrl = product.imageUrl,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = product.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = product.description,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Q%.2f".format(product.price),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (product.stock <= 0) {
                "Agotado"
            } else {
                "Stock: ${product.stock}"
            }
        )

        Button(onClick = { onProductClick(product.id) }) {
            Text("Ver producto")
        }

        TextButton(onClick = { onFavoriteClick(product.id) }) {
            Text(
                if (isFavorite) "♥ Favorito" else "♡ Favorito"
            )
        }
    }
}