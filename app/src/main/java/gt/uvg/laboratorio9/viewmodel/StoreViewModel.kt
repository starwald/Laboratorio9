package gt.uvg.laboratorio9.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import gt.uvg.laboratorio9.data.coffeeOrigins
import gt.uvg.laboratorio9.data.coffeePresentations
import gt.uvg.laboratorio9.data.coffeeRoasts
import gt.uvg.laboratorio9.data.local.CatalogPreferences
import gt.uvg.laboratorio9.data.local.FavoriteEntity
import gt.uvg.laboratorio9.data.local.OrderLineEntity
import gt.uvg.laboratorio9.data.local.StoreDatabaseProvider
import gt.uvg.laboratorio9.data.producers
import gt.uvg.laboratorio9.data.products
import gt.uvg.laboratorio9.model.BillingType
import gt.uvg.laboratorio9.model.CatalogOrder
import gt.uvg.laboratorio9.model.OrderItem
import gt.uvg.laboratorio9.model.OrderReceipt
import gt.uvg.laboratorio9.model.PaymentMethod
import gt.uvg.laboratorio9.model.Producer
import gt.uvg.laboratorio9.model.Product
import gt.uvg.laboratorio9.model.calculateOrderTotal
import gt.uvg.laboratorio9.model.sortProducts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

// Estado que solo vive en memoria: no sobrevive al cierre de la app.
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
            products = sortProducts(inMemory.products, catalogOrder),
            producers = inMemory.producers,
            favoriteProductIds = favorites.map { it.productId }.toSet(),
            orderItems = orderLines.map { line ->
                OrderItem(productId = line.productId, quantity = line.quantity)
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

        viewModelScope.launch {

            val isFavorite =
                productId in uiState.value.favoriteProductIds

            if (isFavorite) {
                favoriteDao.deleteFavorite(productId)
            } else {
                favoriteDao.insertFavorite(FavoriteEntity(productId))
            }
        }
    }

    fun getProductById(productId: Int): Product? {
        return _inMemoryState.value.products.find { it.id == productId }
    }

    fun getProducerById(producerId: Int): Producer? {
        return _inMemoryState.value.producers.find { it.id == producerId }
    }

    fun onCatalogOrderChange(order: CatalogOrder) {
        viewModelScope.launch {
            catalogPreferences.setCatalogOrder(order)
        }
    }

    fun addToOrder(
        productId: Int,
        quantity: Int = 1
    ) {

        val product = getProductById(productId)

        if (product == null) {
            _inMemoryState.value = _inMemoryState.value.copy(
                orderMessage = "Producto no encontrado"
            )
            return
        }

        if (quantity <= 0) {
            _inMemoryState.value = _inMemoryState.value.copy(
                orderMessage = "La cantidad debe ser mayor a cero"
            )
            return
        }

        val currentItem =
            uiState.value.orderItems.find { it.productId == productId }

        val newQuantity = (currentItem?.quantity ?: 0) + quantity

        if (newQuantity > product.stock) {
            _inMemoryState.value = _inMemoryState.value.copy(
                orderMessage = "No hay suficiente stock"
            )
            return
        }

        viewModelScope.launch {
            orderLineDao.upsertOrderLine(
                OrderLineEntity(productId = productId, quantity = newQuantity)
            )
        }

        _inMemoryState.value = _inMemoryState.value.copy(
            orderMessage = "Producto agregado al pedido"
        )
    }

    fun decreaseOrderItem(productId: Int) {

        val currentItem =
            uiState.value.orderItems.find { it.productId == productId } ?: return

        viewModelScope.launch {
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

    fun clearOrderMessage() {
        _inMemoryState.value = _inMemoryState.value.copy(orderMessage = null)
    }



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
                _checkoutUiState.value.copy(billingType = BillingType.NIT)
            }
    }

    fun onPaymentMethodChange(paymentMethod: PaymentMethod) {
        _checkoutUiState.value = _checkoutUiState.value.copy(
            paymentMethod = paymentMethod
        )
    }

    fun isCheckoutFormValid(): Boolean =
        _checkoutUiState.value.isFormValid

    fun confirmOrder(): Boolean {

        val checkout = _checkoutUiState.value
        val orderItems = uiState.value.orderItems

        if (!isCheckoutFormValid() || orderItems.isEmpty()) {
            return false
        }

        val total = calculateOrderTotal(
            orderItems = orderItems,
            products = uiState.value.products
        )

        val folio = "#ORD-" + nextOrderNumber.toString().padStart(5, '0')
        nextOrderNumber++

        _orderReceipt.value = OrderReceipt(
            folio = folio,
            customerName = checkout.fullName.trim(),
            billingType = checkout.billingType,
            paymentMethod = checkout.paymentMethod,
            total = total
        )

        viewModelScope.launch {
            orderLineDao.clearOrder()
        }

        _inMemoryState.value = _inMemoryState.value.copy(orderMessage = null)

        resetCheckout()

        return true
    }

    private fun resetCheckout() {
        _checkoutUiState.value = CheckoutUiState()
    }
}