package gt.uvg.laboratorio9.viewmodel

import androidx.lifecycle.ViewModel
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
import gt.uvg.laboratorio9.model.validateBusinessName
import gt.uvg.laboratorio9.model.validateFullName
import gt.uvg.laboratorio9.model.validateNit
import gt.uvg.laboratorio9.model.validatePhoneNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private var nextOrderNumber = 1

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

                        item.copy(
                            quantity = newQuantity
                        )

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

        val newItems =
            _uiState.value.orderItems.filter {
                it.productId != productId
            }

        _uiState.value = _uiState.value.copy(
            orderItems = newItems
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
    fun onBillingTypeChange(
        billingType: BillingType
    ) {

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

    fun confirmOrder(): Boolean {

        val checkout =
            _checkoutUiState.value

        val orderItems =
            _uiState.value.orderItems

        if (
            !isCheckoutFormValid() ||
            orderItems.isEmpty()
        ) {
            return false
        }

        val total =
            calculateOrderTotal(
                orderItems = orderItems,
                products = _uiState.value.products
            )

        val folio =
            "#ORD-" +
                    nextOrderNumber
                        .toString()
                        .padStart(
                            length = 5,
                            padChar = '0'
                        )

        nextOrderNumber++

        _orderReceipt.value =
            OrderReceipt(
                folio = folio,

                customerName =
                    checkout.fullName.trim(),

                billingType =
                    checkout.billingType,

                paymentMethod =
                    checkout.paymentMethod,

                total = total
            )

        _uiState.value =
            _uiState.value.copy(
                orderItems = emptyList(),
                orderMessage = null
            )

        resetCheckout()

        return true
    }

    private fun resetCheckout() {

        _checkoutUiState.value =
            CheckoutUiState()
    }
}