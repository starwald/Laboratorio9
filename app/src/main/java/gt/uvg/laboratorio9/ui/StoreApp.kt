package gt.uvg.laboratorio9.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import gt.uvg.laboratorio9.navigation.CatalogKey
import gt.uvg.laboratorio9.navigation.DetailKey
import gt.uvg.laboratorio9.navigation.ProfileKey
import gt.uvg.laboratorio9.ui.screens.CatalogScreen
import gt.uvg.laboratorio9.ui.screens.DetailScreen
import gt.uvg.laboratorio9.ui.screens.ProfileScreen
import gt.uvg.laboratorio9.viewmodel.StoreViewModel

@Composable
fun StoreApp() {
    val storeViewModel: StoreViewModel = viewModel()
    val uiState by storeViewModel.uiState.collectAsState()
    val backStack = rememberNavBackStack(
        CatalogKey
    )

    NavDisplay(
        backStack = backStack,

        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },

        entryProvider = entryProvider{
            entry<CatalogKey> {
                CatalogScreen(
                    products = uiState.products,
                    favoriteProductIds = uiState.favoriteProductIds,

                    onProductClick = { productId ->
                        backStack.add(
                            DetailKey(
                                productId = productId
                            )
                        )
                    },

                    onFavoriteClick = { productId ->
                        storeViewModel.toggleFavorite(
                            productId
                        )
                    }
                )
            }

            entry<DetailKey> { key ->
                val product = storeViewModel.getProductById(
                    key.productId
                )

                if (product != null) {
                    DetailScreen(
                        product = product,
                        isFavorite = product.id in uiState.favoriteProductIds,
                        onFavoriteClick = {
                            storeViewModel.toggleFavorite(
                                product.id
                            )
                        },

                        onProducerClick = { producerId ->
                            backStack.add(ProfileKey(
                                producerId = producerId
                            ))
                        },

                        onBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
            }

            entry<ProfileKey> { key ->
                val producer = storeViewModel.getProducerById(
                    key.producerId
                )

                if (producer != null) {
                    ProfileScreen(
                        producer = producer,
                        onBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
            }
        }
    )
}