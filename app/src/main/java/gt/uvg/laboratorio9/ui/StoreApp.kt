package gt.uvg.laboratorio9.ui

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import gt.uvg.laboratorio9.navigation.CatalogKey
import gt.uvg.laboratorio9.navigation.CheckoutKey
import gt.uvg.laboratorio9.navigation.DetailKey
import gt.uvg.laboratorio9.navigation.OrderKey
import gt.uvg.laboratorio9.navigation.ProfileKey
import gt.uvg.laboratorio9.ui.screens.CatalogScreen
import gt.uvg.laboratorio9.ui.screens.CheckoutScreen
import gt.uvg.laboratorio9.ui.screens.DetailScreen
import gt.uvg.laboratorio9.ui.screens.OrderScreen
import gt.uvg.laboratorio9.ui.screens.ProfileScreen
import gt.uvg.laboratorio9.viewmodel.StoreViewModel
import gt.uvg.laboratorio9.navigation.ConfirmationKey
import gt.uvg.laboratorio9.ui.screens.ConfirmationScreen
@Composable
fun StoreApp() {

    val storeViewModel: StoreViewModel = viewModel()

    val uiState by
    storeViewModel.uiState.collectAsStateWithLifecycle()

    val checkoutUiState by
    storeViewModel.checkoutUiState.collectAsStateWithLifecycle()

    val orderReceipt by storeViewModel.orderReceipt.collectAsStateWithLifecycle()

    val backStack = rememberNavBackStack(
        CatalogKey
    )

    val catalogGridState =
        rememberLazyGridState()

    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }

    val orderUnitCount =
        uiState.orderItems.sumOf { item ->
            item.quantity
        }

    NavDisplay(
        backStack = backStack,

        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },

        transitionSpec = {

            slideInHorizontally(
                initialOffsetX = { width ->
                    width
                }
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { width ->
                    -width
                }
            )
        },

        popTransitionSpec = {

            slideInHorizontally(
                initialOffsetX = { width ->
                    -width
                }
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { width ->
                    width
                }
            )
        },

        entryProvider = entryProvider {

            // CATALOGO

            entry<CatalogKey> {

                CatalogScreen(
                    products = uiState.products,
                    favoriteProductIds =
                        uiState.favoriteProductIds,
                    gridState = catalogGridState,
                    searchQuery = searchQuery,
                    orderUnitCount = orderUnitCount,

                    onSearchQueryChange = {
                        searchQuery = it
                    },

                    onOrderClick = {

                        backStack.add(
                            OrderKey
                        )
                    },

                    onProductClick = { productId ->

                        storeViewModel.clearOrderMessage()

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

            // DETALLE


            entry<DetailKey> { key ->

                val product =
                    storeViewModel.getProductById(
                        key.productId
                    )

                if (product != null) {

                    DetailScreen(
                        product = product,

                        isFavorite =
                            product.id in
                                    uiState.favoriteProductIds,

                        orderMessage =
                            uiState.orderMessage,

                        orderUnitCount =
                            orderUnitCount,

                        onFavoriteClick = {

                            storeViewModel.toggleFavorite(
                                product.id
                            )
                        },

                        onAddToOrder = {

                            storeViewModel.addToOrder(
                                productId = product.id
                            )
                        },

                        onOrderClick = {

                            backStack.add(
                                OrderKey
                            )
                        },

                        onProducerClick = { producerId ->

                            backStack.add(
                                ProfileKey(
                                    producerId =
                                        producerId
                                )
                            )
                        },

                        onBack = {

                            storeViewModel
                                .clearOrderMessage()

                            backStack
                                .removeLastOrNull()
                        }
                    )
                }
            }


            // PEDIDO

            entry<OrderKey> {

                OrderScreen(
                    orderItems =
                        uiState.orderItems,

                    products =
                        uiState.products,

                    orderMessage =
                        uiState.orderMessage,

                    onIncrease = { productId ->

                        storeViewModel.addToOrder(
                            productId = productId
                        )
                    },

                    onDecrease = { productId ->

                        storeViewModel
                            .decreaseOrderItem(
                                productId
                            )
                    },

                    onRemove = { productId ->

                        storeViewModel
                            .removeFromOrder(
                                productId
                            )
                    },

                    onCheckoutClick = {

                        backStack.add(
                            CheckoutKey
                        )
                    },

                    onBack = {

                        storeViewModel
                            .clearOrderMessage()

                        backStack
                            .removeLastOrNull()
                    }
                )
            }


            // CHECKOUT


            entry<CheckoutKey> {

                CheckoutScreen(
                    orderItems = uiState.orderItems,
                    products = uiState.products,
                    uiState = checkoutUiState,

                    onFullNameChange = storeViewModel::onFullNameChange,
                    onPhoneChange = storeViewModel::onPhoneChange,
                    onBillingTypeChange = storeViewModel::onBillingTypeChange,
                    onNitChange = storeViewModel::onNitChange,
                    onBusinessNameChange = storeViewModel::onBusinessNameChange,
                    onPaymentMethodChange = storeViewModel::onPaymentMethodChange,

                    onConfirmOrder = {
                        if (storeViewModel.confirmOrder()) {
                            backStack.add(ConfirmationKey)
                        }
                    },

                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<ConfirmationKey> {
                ConfirmationScreen(
                    receipt = orderReceipt,
                    onBackToCatalog = {
                        while (backStack.size > 1) {
                            backStack.removeLastOrNull()
                        }
                    }
                )
            }

            // PERFIL

            entry<ProfileKey> { key ->

                val producer =
                    storeViewModel
                        .getProducerById(
                            key.producerId
                        )

                if (producer != null) {

                    ProfileScreen(
                        producer = producer,

                        onBack = {

                            backStack
                                .removeLastOrNull()
                        }
                    )
                }
            }
        }
    )
}