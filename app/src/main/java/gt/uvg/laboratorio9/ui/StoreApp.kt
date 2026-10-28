
package gt.uvg.laboratorio9.ui

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import gt.uvg.laboratorio9.navigation.ConfirmationKey
import gt.uvg.laboratorio9.navigation.DetailKey
import gt.uvg.laboratorio9.navigation.OrderDetailKey
import gt.uvg.laboratorio9.navigation.OrderKey
import gt.uvg.laboratorio9.navigation.OrdersKey
import gt.uvg.laboratorio9.navigation.ProfileKey
import gt.uvg.laboratorio9.ui.screens.CatalogScreen
import gt.uvg.laboratorio9.ui.screens.CheckoutScreen
import gt.uvg.laboratorio9.ui.screens.ConfirmationScreen
import gt.uvg.laboratorio9.ui.screens.DetailScreen
import gt.uvg.laboratorio9.ui.screens.OrderDetailScreen
import gt.uvg.laboratorio9.ui.screens.OrderScreen
import gt.uvg.laboratorio9.ui.screens.OrdersScreen
import gt.uvg.laboratorio9.ui.screens.ProfileScreen
import gt.uvg.laboratorio9.viewmodel.OrderDetailViewModel
import gt.uvg.laboratorio9.viewmodel.OrderSendState
import gt.uvg.laboratorio9.viewmodel.OrdersViewModel
import gt.uvg.laboratorio9.viewmodel.StoreViewModel

@Composable
fun StoreApp() {

    val storeViewModel: StoreViewModel = viewModel()
    val ordersViewModel: OrdersViewModel = viewModel()
    val orderDetailViewModel: OrderDetailViewModel = viewModel()

    val uiState by
    storeViewModel.uiState.collectAsStateWithLifecycle()

    val checkoutUiState by
    storeViewModel.checkoutUiState.collectAsStateWithLifecycle()

    val orderReceipt by
    storeViewModel.orderReceipt.collectAsStateWithLifecycle()

    val orderSendState by
    storeViewModel.orderSendState.collectAsStateWithLifecycle()

    val ordersUiState by
    ordersViewModel.uiState.collectAsStateWithLifecycle()

    val orderDetailUiState by
    orderDetailViewModel.uiState.collectAsStateWithLifecycle()

    val backStack = rememberNavBackStack(CatalogKey)

    val catalogGridState = rememberLazyGridState()

    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }

    val orderUnitCount =
        uiState.orderItems.sumOf { it.quantity }

    LaunchedEffect(orderSendState) {
        if (
            orderSendState is OrderSendState.Success &&
            backStack.lastOrNull() == CheckoutKey
        ) {
            backStack.add(ConfirmationKey)
        }
    }

    NavDisplay(
        backStack = backStack,

        onBack = {
            if (
                orderSendState !is OrderSendState.Loading &&
                backStack.size > 1
            ) {
                backStack.removeLastOrNull()
            }
        },

        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { width -> width }
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { width -> -width }
            )
        },

        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { width -> -width }
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { width -> width }
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
                    catalogOrder = uiState.catalogOrder,

                    onSearchQueryChange = {
                        searchQuery = it
                    },

                    onOrderClick = {
                        backStack.add(OrderKey)
                    },

                    onOrdersClick = {
                        ordersViewModel.loadOrders()
                        backStack.add(OrdersKey)
                    },

                    onProductClick = { productId ->
                        storeViewModel.clearOrderMessage()

                        backStack.add(
                            DetailKey(productId = productId)
                        )
                    },

                    onFavoriteClick = { productId ->
                        storeViewModel.toggleFavorite(productId)
                    },

                    onCatalogOrderChange =
                        storeViewModel::onCatalogOrderChange
                )
            }

            // DETALLE DEL PRODUCTO
            entry<DetailKey> { key ->

                val product =
                    storeViewModel.getProductById(key.productId)

                if (product != null) {

                    DetailScreen(
                        product = product,

                        isFavorite =
                            product.id in uiState.favoriteProductIds,

                        orderMessage = uiState.orderMessage,
                        orderUnitCount = orderUnitCount,

                        onFavoriteClick = {
                            storeViewModel.toggleFavorite(product.id)
                        },

                        onAddToOrder = {
                            storeViewModel.addToOrder(product.id)
                        },

                        onOrderClick = {
                            backStack.add(OrderKey)
                        },

                        onProducerClick = { producerId ->
                            backStack.add(
                                ProfileKey(producerId = producerId)
                            )
                        },

                        onBack = {
                            storeViewModel.clearOrderMessage()
                            backStack.removeLastOrNull()
                        }
                    )
                }
            }

            // PEDIDO ACTUAL
            entry<OrderKey> {

                OrderScreen(
                    orderItems = uiState.orderItems,
                    products = uiState.products,
                    orderMessage = uiState.orderMessage,

                    onIncrease = { productId ->
                        storeViewModel.addToOrder(productId)
                    },

                    onDecrease = { productId ->
                        storeViewModel.decreaseOrderItem(productId)
                    },

                    onRemove = { productId ->
                        storeViewModel.removeFromOrder(productId)
                    },

                    onCheckoutClick = {
                        storeViewModel.resetOrderSendState()
                        backStack.add(CheckoutKey)
                    },

                    onBack = {
                        storeViewModel.clearOrderMessage()
                        backStack.removeLastOrNull()
                    }
                )
            }

            // CHECKOUT
            entry<CheckoutKey> {

                CheckoutScreen(
                    orderItems = uiState.orderItems,
                    products = uiState.products,
                    uiState = checkoutUiState,
                    orderSendState = orderSendState,

                    onFullNameChange =
                        storeViewModel::onFullNameChange,

                    onPhoneChange =
                        storeViewModel::onPhoneChange,

                    onBillingTypeChange =
                        storeViewModel::onBillingTypeChange,

                    onNitChange =
                        storeViewModel::onNitChange,

                    onBusinessNameChange =
                        storeViewModel::onBusinessNameChange,

                    onPaymentMethodChange =
                        storeViewModel::onPaymentMethodChange,

                    onConfirmOrder = {
                        storeViewModel.sendOrder()
                    },

                    onBack = {
                        if (
                            orderSendState !is OrderSendState.Loading
                        ) {
                            storeViewModel.resetOrderSendState()
                            backStack.removeLastOrNull()
                        }
                    }
                )
            }

            // CONFIRMACION
            entry<ConfirmationKey> {

                ConfirmationScreen(
                    receipt = orderReceipt,

                    onBackToCatalog = {
                        storeViewModel.resetOrderSendState()

                        while (backStack.size > 1) {
                            backStack.removeLastOrNull()
                        }
                    }
                )
            }

            // MIS PEDIDOS
            entry<OrdersKey> {

                OrdersScreen(
                    uiState = ordersUiState,

                    onOrderClick = { orderId ->
                        orderDetailViewModel.loadOrder(orderId)

                        backStack.add(
                            OrderDetailKey(orderId = orderId)
                        )
                    },

                    onRetry = {
                        ordersViewModel.loadOrders()
                    },

                    onBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            // DETALLE DE PEDIDO REMOTO
            entry<OrderDetailKey> { key ->

                OrderDetailScreen(
                    uiState = orderDetailUiState,

                    onRetry = {
                        orderDetailViewModel.loadOrder(
                            orderId = key.orderId,
                            forceRefresh = true
                        )
                    },

                    onBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            // PERFIL DEL PRODUCTOR
            entry<ProfileKey> { key ->

                val producer =
                    storeViewModel.getProducerById(
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
