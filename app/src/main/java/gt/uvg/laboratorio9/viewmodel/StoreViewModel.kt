
package gt.uvg.laboratorio9.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gt.uvg.laboratorio9.data.coffeeOrigins
import gt.uvg.laboratorio9.data.coffeePresentations
import gt.uvg.laboratorio9.data.coffeeRoasts
import gt.uvg.laboratorio9.data.products
import gt.uvg.laboratorio9.data.producers
import gt.uvg.laboratorio9.model.BillingType
import gt.uvg.laboratorio9.model.OrderItem
import gt.uvg.laboratorio9.model.OrderReceipt
import gt.uvg.laboratorio9.model.PaymentMethod
import gt.uvg.laboratorio9.model.Product
import gt.uvg.laboratorio9.model.Producer
import gt.uvg.laboratorio9.model.calculateOrderTotal
import gt.uvg.laboratorio9.network.RetrofitProvider
import gt.uvg.laboratorio9.network.dto.CreateOrderDto
import gt.uvg.laboratorio9.network.dto.OrderLineDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import kotlin.random.Random

class StoreViewModel : ViewModel() {

    private val generatedProducts = generateProducts()

    private val _uiState = MutableStateFlow(
        StoreUiState(
            products = generatedProducts,
            producers = producers
        )
    )

    val uiState: StateFlow<StoreUiState> = _uiState

    private val _checkoutUiState = MutableStateFlow(
        CheckoutUiState()
    )

    val checkoutUiState: StateFlow<CheckoutUiState> =
        _checkoutUiState

    private val _orderReceipt =
        MutableStateFlow<OrderReceipt?>(null)

    val orderReceipt: StateFlow<OrderReceipt?> =
        _orderReceipt

    // LAB 13 - Estado remoto del envio
    private val _orderSendState =
        MutableStateFlow<OrderSendState>(
            OrderSendState.Idle
        )

    val orderSendState: StateFlow<OrderSendState> =
        _orderSendState

    // -------------------------
    // CATALOGO
    // -------------------------

    private fun generateProducts(): List<Product> {

        val random = Random(12345)

        val newProducts = (4..500).map { id ->

            val origin = coffeeOrigins.random(random)
            val roast = coffeeRoasts.random(random)
            val presentation = coffeePresentations.random(random)

            Product(
                id = id,
                name = "Café $origin $roast $id",
                description = "Café de $origin, tueste $roast, presentación de $presentation.",
                price = random.nextInt(60, 121).toDouble(),
                producerId = if (id % 2 == 0) 1 else 2,
                stock = random.nextInt(0, 11),
                imageUrl = "https://picsum.photos/seed/product-$id/400/400"
            )
        }

        return products + newProducts
    }

    fun toggleFavorite(productId: Int) {

        val currentFavorites =
            _uiState.value.favoriteProductIds

        val newFavorites =
            if (productId in currentFavorites) {
                currentFavorites - productId
            } else {
                currentFavorites + productId
            }

        _uiState.value = _uiState.value.copy(
            favoriteProductIds = newFavorites
        )
    }

    fun getProductById(productId: Int): Product? {
        return _uiState.value.products.find {
            it.id == productId
        }
    }

    fun getProducerById(producerId: Int): Producer? {
        return _uiState.value.producers.find {
            it.id == producerId
        }
    }

    // -------------------------
    // PEDIDO
    // -------------------------

    fun addToOrder(
        productId: Int,
        quantity: Int = 1
    ) {

        val product = getProductById(productId)

        if (product == null) {
            _uiState.value = _uiState.value.copy(
                orderMessage = "Producto no encontrado"
            )
            return
        }

        if (quantity <= 0) {
            _uiState.value = _uiState.value.copy(
                orderMessage = "La cantidad debe ser mayor a cero"
            )
            return
        }

        val currentItem =
            _uiState.value.orderItems.find {
                it.productId == productId
            }

        val currentQuantity =
            currentItem?.quantity ?: 0

        val newQuantity =
            currentQuantity + quantity

        if (newQuantity > product.stock) {
            _uiState.value = _uiState.value.copy(
                orderMessage = "No hay suficiente stock"
            )
            return
        }

        val newItems =
            if (currentItem == null) {

                _uiState.value.orderItems +
                        OrderItem(
                            productId = productId,
                            quantity = quantity
                        )

            } else {

                _uiState.value.orderItems.map { item ->

                    if (item.productId == productId) {
                        item.copy(quantity = newQuantity)
                    } else {
                        item
                    }
                }
            }

        _uiState.value = _uiState.value.copy(
            orderItems = newItems,
            orderMessage = "Producto agregado al pedido"
        )
    }

    fun decreaseOrderItem(productId: Int) {

        val currentItem =
            _uiState.value.orderItems.find {
                it.productId == productId
            } ?: return

        val newItems =
            if (currentItem.quantity <= 1) {

                _uiState.value.orderItems.filter {
                    it.productId != productId
                }

            } else {

                _uiState.value.orderItems.map { item ->

                    if (item.productId == productId) {
                        item.copy(
                            quantity = item.quantity - 1
                        )
                    } else {
                        item
                    }
                }
            }

        _uiState.value = _uiState.value.copy(
            orderItems = newItems
        )
    }

    fun removeFromOrder(productId: Int) {

        _uiState.value = _uiState.value.copy(
            orderItems = _uiState.value.orderItems.filter {
                it.productId != productId
            }
        )
    }

    fun clearOrderMessage() {

        _uiState.value = _uiState.value.copy(
            orderMessage = null
        )
    }

    // -------------------------
    // CHECKOUT
    // -------------------------

    fun onFullNameChange(value: String) {
        _checkoutUiState.value = _checkoutUiState.value.copy(
            fullName = value,
            fullNameTouched = true
        )
    }

    fun onPhoneChange(value: String) {
        _checkoutUiState.value = _checkoutUiState.value.copy(
            phone = value,
            phoneTouched = true
        )
    }

    fun onNitChange(value: String) {
        _checkoutUiState.value = _checkoutUiState.value.copy(
            nit = value,
            nitTouched = true
        )
    }

    fun onBusinessNameChange(value: String) {
        _checkoutUiState.value = _checkoutUiState.value.copy(
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

    // -------------------------
    // LAB 13 - ENVIO REMOTO
    // -------------------------

    fun sendOrder() {

        // Evitar doble envio.
        if (
            _orderSendState.value is OrderSendState.Loading ||
            _orderSendState.value is OrderSendState.Success
        ) {
            return
        }

        val checkout = _checkoutUiState.value
        val currentState = _uiState.value
        val orderItems = currentState.orderItems

        if (!checkout.isFormValid || orderItems.isEmpty()) {
            _orderSendState.value = OrderSendState.Error(
                "Completa los datos obligatorios y revisa tu pedido."
            )
            return
        }

        val productsById =
            currentState.products.associateBy { it.id }

        val validOrder = orderItems.all { item ->
            val product = productsById[item.productId]

            product != null &&
                    item.quantity > 0 &&
                    item.quantity <= product.stock
        }

        if (!validOrder) {
            _orderSendState.value = OrderSendState.Error(
                "El pedido contiene cantidades no disponibles."
            )
            return
        }

        val lines = orderItems.map { item ->

            val product =
                requireNotNull(productsById[item.productId])

            OrderLineDto(
                productId = product.id,
                productName = product.name,
                unitPrice = product.price,
                quantity = item.quantity
            )
        }

        val total = calculateOrderTotal(
            orderItems = orderItems,
            products = currentState.products
        )

        val request = CreateOrderDto(
            customerName = checkout.fullName.trim(),
            total = total,
            createdAt = Instant.now().toString(),
            lines = lines
        )

        _orderSendState.value = OrderSendState.Loading

        viewModelScope.launch {

            try {

                val savedOrder =
                    RetrofitProvider.orderApi.createOrder(
                        request
                    )

                // Guardar recibo del servidor.
                _orderReceipt.value = OrderReceipt(
                    folio = "#${savedOrder.id}",
                    customerName = savedOrder.customerName,
                    billingType = checkout.billingType,
                    paymentMethod = checkout.paymentMethod,
                    total = savedOrder.total
                )

                // Vaciar solamente tras el exito.
                _uiState.value = _uiState.value.copy(
                    orderItems = emptyList(),
                    orderMessage = null
                )

                resetCheckout()

                _orderSendState.value =
                    OrderSendState.Success(savedOrder)

            } catch (e: CancellationException) {
                throw e

            } catch (e: IOException) {

                _orderSendState.value = OrderSendState.Error(
                    "No pudimos conectar con el servidor. Revisa tu conexión."
                )

            } catch (e: HttpException) {

                _orderSendState.value = OrderSendState.Error(
                    when (e.code()) {
                        400 -> "No se pudo registrar el pedido."
                        404 -> "El servicio de pedidos no está disponible."
                        else -> "El servidor no pudo procesar el pedido."
                    }
                )

            } catch (e: SerializationException) {

                _orderSendState.value = OrderSendState.Error(
                    "Recibimos una respuesta inesperada del servidor."
                )
            }
        }
    }

    fun resetOrderSendState() {

        if (_orderSendState.value !is OrderSendState.Loading) {
            _orderSendState.value = OrderSendState.Idle
        }
    }

    private fun resetCheckout() {
        _checkoutUiState.value = CheckoutUiState()
    }
}
