
package gt.uvg.laboratorio9.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import gt.uvg.laboratorio9.data.coffeeOrigins
import gt.uvg.laboratorio9.data.coffeePresentations
import gt.uvg.laboratorio9.data.coffeeRoasts
import gt.uvg.laboratorio9.data.products
import gt.uvg.laboratorio9.data.producers
import gt.uvg.laboratorio9.data.local.CatalogPreferences
import gt.uvg.laboratorio9.data.local.FavoriteEntity
import gt.uvg.laboratorio9.data.local.OrderLineEntity
import gt.uvg.laboratorio9.data.local.StoreDatabaseProvider
import gt.uvg.laboratorio9.model.BillingType
import gt.uvg.laboratorio9.model.CatalogOrder
import gt.uvg.laboratorio9.model.OrderItem
import gt.uvg.laboratorio9.model.OrderReceipt
import gt.uvg.laboratorio9.model.PaymentMethod
import gt.uvg.laboratorio9.model.Product
import gt.uvg.laboratorio9.model.Producer
import gt.uvg.laboratorio9.model.calculateOrderTotal
import gt.uvg.laboratorio9.model.sortProducts
import gt.uvg.laboratorio9.network.RetrofitProvider
import gt.uvg.laboratorio9.network.dto.CreateOrderDto
import gt.uvg.laboratorio9.network.dto.OrderLineDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import kotlin.random.Random

private data class StoreInMemoryState(
    val products: List<Product> = emptyList(),
    val producers: List<Producer> = emptyList(),
    val orderMessage: String? = null
)

class StoreViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database =
        StoreDatabaseProvider.getDatabase(application)

    private val favoriteDao = database.favoriteDao()
    private val orderLineDao = database.orderLineDao()
    private val catalogPreferences = CatalogPreferences(application)

    private val generatedProducts = generateProducts()

    private val _inMemoryState = MutableStateFlow(
        StoreInMemoryState(
            products = generatedProducts,
            producers = producers
        )
    )

    val uiState: StateFlow<StoreUiState> = combine(
        _inMemoryState,
        favoriteDao.observeFavorites(),
        orderLineDao.observeOrderLines(),
        catalogPreferences.catalogOrder
    ) { inMemory, favorites, orderLines, catalogOrder ->

        StoreUiState(
            products = sortProducts(
                inMemory.products,
                catalogOrder
            ),
            producers = inMemory.producers,
            favoriteProductIds =
                favorites.map { it.productId }.toSet(),
            orderItems = orderLines.map { line ->
                OrderItem(
                    productId = line.productId,
                    quantity = line.quantity
                )
            },
            orderMessage = inMemory.orderMessage,
            catalogOrder = catalogOrder
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StoreUiState(
            products = generatedProducts,
            producers = producers
        )
    )

    private val _checkoutUiState =
        MutableStateFlow(CheckoutUiState())

    val checkoutUiState: StateFlow<CheckoutUiState> =
        _checkoutUiState

    private val _orderReceipt =
        MutableStateFlow<OrderReceipt?>(null)

    val orderReceipt: StateFlow<OrderReceipt?> =
        _orderReceipt

    private val _orderSendState =
        MutableStateFlow<OrderSendState>(
            OrderSendState.Idle
        )

    val orderSendState: StateFlow<OrderSendState> =
        _orderSendState

    private var nextOrderNumber = 1

    // CATALOGO

    private fun generateProducts(): List<Product> {

        val random = Random(12345)

        val newProducts = (4..500).map { id ->

            val origin = coffeeOrigins.random(random)
            val roast = coffeeRoasts.random(random)
            val presentation =
                coffeePresentations.random(random)

            Product(
                id = id,
                name = "Café $origin $roast $id",
                description =
                    "Café de $origin, tueste $roast, " +
                            "presentación de $presentation.",
                price = random.nextInt(60, 121).toDouble(),
                producerId = if (id % 2 == 0) 1 else 2,
                stock = random.nextInt(0, 11),
                imageUrl =
                    "https://picsum.photos/seed/product-$id/400/400"
            )
        }

        return products + newProducts
    }

    fun toggleFavorite(productId: Int) {
        viewModelScope.launch {

            val isFavorite =
                productId in uiState.value.favoriteProductIds

            if (isFavorite) {
                favoriteDao.deleteFavorite(productId)
            } else {
                favoriteDao.insertFavorite(
                    FavoriteEntity(productId)
                )
            }
        }
    }

    fun getProductById(productId: Int): Product? {
        return _inMemoryState.value.products.find {
            it.id == productId
        }
    }

    fun getProducerById(producerId: Int): Producer? {
        return _inMemoryState.value.producers.find {
            it.id == producerId
        }
    }

    fun onCatalogOrderChange(order: CatalogOrder) {
        viewModelScope.launch {
            catalogPreferences.setCatalogOrder(order)
        }
    }

    // PEDIDO LOCAL - ROOM

    fun addToOrder(
        productId: Int,
        quantity: Int = 1
    ) {

        val product = getProductById(productId)

        if (product == null) {
            showOrderMessage("Producto no encontrado")
            return
        }

        if (quantity <= 0) {
            showOrderMessage(
                "La cantidad debe ser mayor a cero"
            )
            return
        }

        viewModelScope.launch {

            val currentItems =
                orderLineDao.observeOrderLines().first()

            val currentQuantity =
                currentItems.find {
                    it.productId == productId
                }?.quantity ?: 0

            val newQuantity =
                currentQuantity + quantity

            if (newQuantity > product.stock) {
                showOrderMessage(
                    "No hay suficiente stock"
                )
                return@launch
            }

            orderLineDao.upsertOrderLine(
                OrderLineEntity(
                    productId = productId,
                    quantity = newQuantity
                )
            )

            showOrderMessage(
                "Producto agregado al pedido"
            )
        }
    }

    fun decreaseOrderItem(productId: Int) {
        viewModelScope.launch {

            val currentItem =
                orderLineDao.observeOrderLines()
                    .first()
                    .find { it.productId == productId }
                    ?: return@launch

            if (currentItem.quantity <= 1) {

                orderLineDao.deleteOrderLine(productId)

            } else {

                orderLineDao.upsertOrderLine(
                    OrderLineEntity(
                        productId = productId,
                        quantity = currentItem.quantity - 1
                    )
                )
            }
        }
    }

    fun removeFromOrder(productId: Int) {
        viewModelScope.launch {
            orderLineDao.deleteOrderLine(productId)
        }
    }

    private fun showOrderMessage(message: String) {
        _inMemoryState.value =
            _inMemoryState.value.copy(
                orderMessage = message
            )
    }

    fun clearOrderMessage() {
        _inMemoryState.value =
            _inMemoryState.value.copy(
                orderMessage = null
            )
    }

    // CHECKOUT

    fun onFullNameChange(value: String) {
        _checkoutUiState.value =
            _checkoutUiState.value.copy(
                fullName = value,
                fullNameTouched = true
            )
    }

    fun onPhoneChange(value: String) {
        _checkoutUiState.value =
            _checkoutUiState.value.copy(
                phone = value,
                phoneTouched = true
            )
    }

    fun onNitChange(value: String) {
        _checkoutUiState.value =
            _checkoutUiState.value.copy(
                nit = value,
                nitTouched = true
            )
    }

    fun onBusinessNameChange(value: String) {
        _checkoutUiState.value =
            _checkoutUiState.value.copy(
                businessName = value,
                businessNameTouched = true
            )
    }

    fun onBillingTypeChange(billingType: BillingType) {

        _checkoutUiState.value =
            if (billingType == BillingType.CF) {

                _checkoutUiState.value.copy(
                    billingType = BillingType.CF,
                    nit = "",
                    businessName = "",
                    nitTouched = false,
                    businessNameTouched = false
                )

            } else {

                _checkoutUiState.value.copy(
                    billingType = BillingType.NIT
                )
            }
    }

    fun onPaymentMethodChange(
        paymentMethod: PaymentMethod
    ) {
        _checkoutUiState.value =
            _checkoutUiState.value.copy(
                paymentMethod = paymentMethod
            )
    }

    fun isCheckoutFormValid(): Boolean =
        _checkoutUiState.value.isFormValid

    // LAB 13 - ENVIO A MOCKAPI

    fun sendOrder() {

        if (
            _orderSendState.value is OrderSendState.Loading ||
            _orderSendState.value is OrderSendState.Success
        ) {
            return
        }

        val checkout = _checkoutUiState.value

        if (!checkout.isFormValid) {
            _orderSendState.value =
                OrderSendState.Error(
                    "Completa los datos obligatorios."
                )
            return
        }

        viewModelScope.launch {

            try {

                val orderItems =
                    orderLineDao.observeOrderLines()
                        .first()
                        .map { line ->
                            OrderItem(
                                productId = line.productId,
                                quantity = line.quantity
                            )
                        }

                if (orderItems.isEmpty()) {
                    _orderSendState.value =
                        OrderSendState.Error(
                            "El pedido está vacío."
                        )
                    return@launch
                }

                val productsById =
                    _inMemoryState.value.products
                        .associateBy { it.id }

                val validOrder =
                    orderItems.all { item ->

                        val product =
                            productsById[item.productId]

                        product != null &&
                                item.quantity > 0 &&
                                item.quantity <= product.stock
                    }

                if (!validOrder) {
                    _orderSendState.value =
                        OrderSendState.Error(
                            "El pedido contiene cantidades no disponibles."
                        )
                    return@launch
                }

                val lines = orderItems.map { item ->

                    val product =
                        requireNotNull(
                            productsById[item.productId]
                        )

                    OrderLineDto(
                        productId = product.id,
                        productName = product.name,
                        unitPrice = product.price,
                        quantity = item.quantity
                    )
                }

                val total = calculateOrderTotal(
                    orderItems = orderItems,
                    products = _inMemoryState.value.products
                )

                val request = CreateOrderDto(
                    customerName =
                        checkout.fullName.trim(),
                    total = total,
                    createdAt = Instant.now().toString(),
                    lines = lines
                )

                _orderSendState.value =
                    OrderSendState.Loading

                val savedOrder =
                    RetrofitProvider.orderApi.createOrder(
                        request
                    )

                _orderReceipt.value = OrderReceipt(
                    folio = "#${savedOrder.id}",
                    customerName = savedOrder.customerName,
                    billingType = checkout.billingType,
                    paymentMethod = checkout.paymentMethod,
                    total = savedOrder.total
                )

                // El pedido se vacía solo después
                // de recibir éxito de MockAPI.
                orderLineDao.clearOrder()

                clearOrderMessage()
                resetCheckout()

                _orderSendState.value =
                    OrderSendState.Success(savedOrder)

            } catch (e: CancellationException) {
                throw e

            } catch (e: IOException) {

                _orderSendState.value =
                    OrderSendState.Error(
                        "No pudimos conectar con el servidor. " +
                                "Revisa tu conexión."
                    )

            } catch (e: HttpException) {

                _orderSendState.value =
                    OrderSendState.Error(
                        when (e.code()) {
                            400 -> "No se pudo registrar el pedido."
                            404 -> "El servicio no está disponible."
                            else -> "El servidor no pudo procesar el pedido."
                        }
                    )

            } catch (e: SerializationException) {

                _orderSendState.value =
                    OrderSendState.Error(
                        "Recibimos una respuesta inesperada."
                    )

            } catch (e: Exception) {

                _orderSendState.value =
                    OrderSendState.Error(
                        "No se pudo completar el pedido."
                    )
            }
        }
    }

    fun resetOrderSendState() {
        if (
            _orderSendState.value !is OrderSendState.Loading
        ) {
            _orderSendState.value =
                OrderSendState.Idle
        }
    }

    private fun resetCheckout() {
        _checkoutUiState.value =
            CheckoutUiState()
    }
}
