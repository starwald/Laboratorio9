package gt.uvg.laboratorio9.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gt.uvg.laboratorio9.StoreApplication
import gt.uvg.laboratorio9.model.BillingType
import gt.uvg.laboratorio9.model.CatalogOrder
import gt.uvg.laboratorio9.model.NewRemoteOrder
import gt.uvg.laboratorio9.model.OrderItem
import gt.uvg.laboratorio9.model.OrderReceipt
import gt.uvg.laboratorio9.model.PaymentMethod
import gt.uvg.laboratorio9.model.Product
import gt.uvg.laboratorio9.model.Producer
import gt.uvg.laboratorio9.model.RemoteOrderLine
import gt.uvg.laboratorio9.model.calculateOrderTotal
import gt.uvg.laboratorio9.model.sortProducts
import gt.uvg.laboratorio9.repository.FavoriteRepository
import gt.uvg.laboratorio9.repository.OrderRepository
import gt.uvg.laboratorio9.repository.PreferencesRepository
import gt.uvg.laboratorio9.repository.ProducerRepository
import gt.uvg.laboratorio9.repository.ProductRepository
import gt.uvg.laboratorio9.repository.RemoteOrdersRepository
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import gt.uvg.laboratorio9.repository.StockRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.supervisorScope
import kotlin.collections.all

class StoreViewModel(
    private val productRepository: ProductRepository,
    private val producerRepository: ProducerRepository,
    private val favoriteRepository: FavoriteRepository,
    private val orderRepository: OrderRepository,
    private val preferencesRepository: PreferencesRepository,
    private val remoteOrdersRepository: RemoteOrdersRepository,
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        StoreUiState(isLoading = true)
    )
    val uiState = _uiState.asStateFlow()

    private val _checkoutUiState = MutableStateFlow(CheckoutUiState())
    val checkoutUiState = _checkoutUiState.asStateFlow()

    private val _orderReceipt = MutableStateFlow<OrderReceipt?>(null)
    val orderReceipt = _orderReceipt.asStateFlow()

    private val _orderSendState =
        MutableStateFlow<OrderSendState>(OrderSendState.Idle)
    val orderSendState = _orderSendState.asStateFlow()

    private val orderMutex = Mutex()
    private val favoriteMutex = Mutex()

    private var catalogJob: Job? = null
    private var catalogTimeoutJob: Job? = null

    init {
        observeLocalData()
        retryCatalog()
    }

    private val _isReserving = MutableStateFlow(false)
    val isReserving: StateFlow<Boolean> = _isReserving

    private fun showOrderMessage(message: String?) {
        _uiState.update { it.copy(orderMessage = message) }
    }

    fun clearOrderMessage() {
        _uiState.update { it.copy(orderMessage = null) }
    }

    private fun observeLocalData() {
        viewModelScope.launch {
            producerRepository.observeProducers()
                .catch {
                    showOrderMessage("No se pudieron cargar los productores.")
                }
                .collect { producers ->
                    _uiState.update { it.copy(producers = producers) }
                }
        }

        viewModelScope.launch {
            favoriteRepository.observeFavoriteIds()
                .catch {
                    showOrderMessage("No se pudieron cargar los favoritos.")
                }
                .collect { favoriteIds ->
                    _uiState.update {
                        it.copy(favoriteProductIds = favoriteIds)
                    }
                }
        }

        viewModelScope.launch {
            orderRepository.observeOrderItems()
                .catch {
                    showOrderMessage("No se pudo cargar el pedido local.")
                }
                .collect { items ->
                    _uiState.update { it.copy(orderItems = items) }
                }
        }

        viewModelScope.launch {
            preferencesRepository.catalogOrder
                .catch {
                    showOrderMessage("No se pudo leer el orden del catálogo.")
                }
                .collect { order ->
                    _uiState.update {
                        it.copy(
                            catalogOrder = order,
                            products = sortProducts(it.products, order)
                        )
                    }
                }
        }
    }

    fun retryCatalog() {
        catalogJob?.cancel()
        catalogTimeoutJob?.cancel()

        _uiState.update {
            it.copy(isLoading = true, errorMessage = null)
        }

        catalogTimeoutJob = viewModelScope.launch {
            delay(15_000)

            _uiState.update {
                if (it.isLoading) {
                    it.copy(
                        isLoading = false,
                        errorMessage =
                            "La carga está tardando demasiado. " +
                                    "Revisa tu conexión y vuelve a intentar."
                    )
                } else {
                    it
                }
            }
        }

        catalogJob = viewModelScope.launch {
            productRepository.observeProducts()
                .catch {
                    catalogTimeoutJob?.cancel()
                    showCatalogError()
                }
                .collect { result ->
                    catalogTimeoutJob?.cancel()

                    result.fold(
                        onSuccess = { products ->
                            _uiState.update {
                                it.copy(
                                    products = sortProducts(
                                        products,
                                        it.catalogOrder
                                    ),
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }
                        },
                        onFailure = {
                            showCatalogError()
                        }
                    )
                }
        }
    }

    private fun showCatalogError() {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage =
                    "No se pudo cargar el catálogo. " +
                            "Revisa tu conexión e intenta nuevamente."
            )
        }
    }

    fun getProductById(productId: Int): Product? =
        _uiState.value.products.find { it.id == productId }

    fun getProducerById(producerId: Int): Producer? =
        _uiState.value.producers.find { it.id == producerId }

    fun toggleFavorite(productId: Int) {
        viewModelScope.launch {
            favoriteMutex.withLock {
                try {
                    val favorites =
                        favoriteRepository.observeFavoriteIds().first()

                    val result = if (productId in favorites) {
                        favoriteRepository.removeFavorite(productId)
                    } else {
                        favoriteRepository.addFavorite(productId)
                    }

                    result.getOrThrow()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    showOrderMessage("No se pudo actualizar el favorito.")
                }
            }
        }
    }

    fun onCatalogOrderChange(order: CatalogOrder) {
        viewModelScope.launch {
            preferencesRepository.setCatalogOrder(order).onFailure {
                showOrderMessage("No se pudo guardar el orden del catálogo.")
            }
        }
    }

    private fun isOrderLocked(): Boolean =
        _orderSendState.value is OrderSendState.Loading ||
                _orderSendState.value is OrderSendState.Success

    private fun changeLocalOrder(
        action: suspend () -> Unit
    ) {
        if (isOrderLocked()) {
            showOrderMessage("Espera a que termine la confirmación del pedido.")
            return
        }

        viewModelScope.launch {
            orderMutex.withLock {
                if (isOrderLocked()) {
                    showOrderMessage(
                        "Espera a que termine la confirmación del pedido."
                    )
                    return@withLock
                }

                try {
                    action()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    showOrderMessage("No se pudo actualizar el pedido.")
                }
            }
        }
    }


    fun addToOrder(
        productId: Int,
        quantity: Int = 1
    ) {
        if (quantity <= 0) {
            showOrderMessage("Cantidad inválida")
            return
        }

        viewModelScope.launch {
            orderMutex.withLock {

                _isReserving.value = true

                try {
                    val product = getProductById(productId)

                    if (product == null) {
                        showOrderMessage(
                            "Producto no encontrado"
                        )
                        return@withLock
                    }

                    val currentItems =
                        orderRepository.observeOrderItems().first()

                    val currentQuantity =
                        currentItems.find {
                            it.productId == productId
                        }?.quantity ?: 0

                    var reserved = 0

                    for (attempt in 1..quantity) {

                        val result =
                            stockRepository.reserveUnit(productId)

                        if (result.isFailure) {
                            break
                        }

                        reserved++
                    }

                    if (reserved == 0) {
                        showOrderMessage(
                            "No hay existencias disponibles"
                        )
                        return@withLock
                    }

                    try {
                        orderRepository.upsertItem(
                            OrderItem(
                                productId = productId,
                                quantity = currentQuantity + reserved
                            )
                        )

                        showOrderMessage(
                            "$reserved unidad(es) reservada(s)"
                        )

                    } catch (e: Exception) {

                        repeat(reserved) {
                            stockRepository.releaseUnit(productId)
                        }

                        showOrderMessage(
                            "No se pudo guardar el pedido"
                        )
                    }

                } finally {
                    _isReserving.value = false
                }
            }
        }
    }

    fun decreaseOrderItem(productId: Int) {

        viewModelScope.launch {
            orderMutex.withLock {

                val current =
                    orderRepository.observeOrderItems()
                        .first()
                        .find { it.productId == productId }
                        ?: return@withLock

                val result =
                    stockRepository.releaseUnit(productId)

                if (result.isFailure) {
                    showOrderMessage(
                        "No se pudo devolver la unidad"
                    )
                    return@withLock
                }

                if (current.quantity <= 1) {
                    orderRepository.removeItem(productId)
                } else {
                    orderRepository.upsertItem(
                        current.copy(
                            quantity = current.quantity - 1
                        )
                    )
                }

                showOrderMessage(
                    "Unidad devuelta correctamente"
                )
            }
        }
    }

    fun removeFromOrder(productId: Int) {

        viewModelScope.launch {
            orderMutex.withLock {

                val current =
                    orderRepository.observeOrderItems()
                        .first()
                        .find { it.productId == productId }
                        ?: return@withLock

                var released = 0

                repeat(current.quantity) {
                    val result =
                        stockRepository.releaseUnit(productId)

                    if (result.isSuccess) {
                        released++
                    }
                }

                if (released == 0) {
                    showOrderMessage(
                        "No se pudieron devolver unidades"
                    )
                    return@withLock
                }

                val remaining =
                    current.quantity - released

                if (remaining == 0) {
                    orderRepository.removeItem(productId)
                } else {
                    orderRepository.upsertItem(
                        current.copy(quantity = remaining)
                    )
                }

                showOrderMessage(
                    "$released unidad(es) devuelta(s)"
                )
            }
        }
    }

    fun onFullNameChange(value: String) {
        _checkoutUiState.update {
            it.copy(fullName = value, fullNameTouched = true)
        }
    }

    fun onPhoneChange(value: String) {
        _checkoutUiState.update {
            it.copy(phone = value, phoneTouched = true)
        }
    }

    fun onNitChange(value: String) {
        _checkoutUiState.update {
            it.copy(nit = value, nitTouched = true)
        }
    }

    fun onBusinessNameChange(value: String) {
        _checkoutUiState.update {
            it.copy(businessName = value, businessNameTouched = true)
        }
    }

    fun onBillingTypeChange(billingType: BillingType) {
        _checkoutUiState.update {
            if (billingType == BillingType.CF) {
                it.copy(
                    billingType = BillingType.CF,
                    nit = "",
                    businessName = "",
                    nitTouched = false,
                    businessNameTouched = false
                )
            } else {
                it.copy(billingType = BillingType.NIT)
            }
        }
    }

    fun onPaymentMethodChange(paymentMethod: PaymentMethod) {
        _checkoutUiState.update {
            it.copy(paymentMethod = paymentMethod)
        }
    }

    fun isCheckoutFormValid(): Boolean =
        _checkoutUiState.value.isFormValid

    fun sendOrder() {
        if (isOrderLocked()) {
            return
        }

        val checkout = _checkoutUiState.value

        if (!checkout.isFormValid) {
            _orderSendState.value = OrderSendState.Error(
                "Completa los datos obligatorios."
            )
            return
        }

        _orderSendState.value = OrderSendState.Loading

        viewModelScope.launch {
            orderMutex.withLock {
                try {
                    val items = orderRepository.observeOrderItems().first()

                    if (items.isEmpty()) {
                        _orderSendState.value = OrderSendState.Error(
                            "El pedido está vacío."
                        )
                        return@withLock
                    }

                    val products = _uiState.value.products
                    val productsById = products.associateBy { it.id }


                    val valid = items.all { item ->
                        productsById[item.productId] != null &&
                                item.quantity > 0
                    }

                    if (!valid) {
                        _orderSendState.value =
                            OrderSendState.Error(
                                "El pedido contiene productos inválidos"
                            )
                        return@launch
                    }

                    val lines = items.map { item ->
                        val product = requireNotNull(
                            productsById[item.productId]
                        )

                        RemoteOrderLine(
                            productId = product.id,
                            productName = product.name,
                            unitPrice = product.price,
                            quantity = item.quantity
                        )
                    }

                    val request = NewRemoteOrder(
                        customerName = checkout.fullName.trim(),
                        total = calculateOrderTotal(
                            orderItems = items,
                            products = products
                        ),
                        createdAt = Instant.now().toString(),
                        lines = lines
                    )

                    val savedOrder = remoteOrdersRepository
                        .createOrder(request)
                        .getOrThrow()

                    _orderReceipt.value = OrderReceipt(
                        folio = "#${savedOrder.id}",
                        customerName = savedOrder.customerName,
                        billingType = checkout.billingType,
                        paymentMethod = checkout.paymentMethod,
                        total = savedOrder.total
                    )

                    orderRepository.clearOrder().getOrThrow()
                    clearOrderMessage()
                    resetCheckout()

                    _orderSendState.value =
                        OrderSendState.Success(savedOrder)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _orderSendState.value = OrderSendState.Error(
                        "No se pudo completar el proceso del pedido. " +
                                "Consulta Mis pedidos antes de volver a enviarlo."
                    )
                }
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    checkNotNull(this[APPLICATION_KEY]) as StoreApplication

                val container = application.container

                StoreViewModel(
                    productRepository = container.productRepository,
                    producerRepository = container.producerRepository,
                    favoriteRepository = container.favoriteRepository,
                    orderRepository = container.orderRepository,
                    preferencesRepository = container.preferencesRepository,
                    remoteOrdersRepository = container.remoteOrdersRepository,
                    stockRepository = container.stockRepository
                )
            }
        }
    }

    private val _concurrencyTestState =
        MutableStateFlow<ConcurrencyTestState>(
            ConcurrencyTestState.Idle
        )

    val concurrencyTestState:
            StateFlow<ConcurrencyTestState> =
        _concurrencyTestState

    fun onConcurrencyTest(
        productId: Int,
        attempts: Int = 20
    ) {
        if (_concurrencyTestState.value is ConcurrencyTestState.Running) {
            return
        }

        if (attempts <= 0) {
            _concurrencyTestState.value =
                ConcurrencyTestState.Error(
                    "La cantidad de intentos debe ser positiva"
                )
            return
        }

        _concurrencyTestState.value =
            ConcurrencyTestState.Running

        viewModelScope.launch {

            try {

                val results = supervisorScope {

                    List(attempts) {
                        async {
                            stockRepository.reserveUnit(productId)
                        }
                    }.awaitAll()
                }

                val successes =
                    results.count { it.isSuccess }

                _concurrencyTestState.value =
                    ConcurrencyTestState.Completed(
                        attempts = attempts,
                        successes = successes,
                        rejections = attempts - successes
                    )

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                _concurrencyTestState.value =
                    ConcurrencyTestState.Error(
                        "No se pudo completar la prueba"
                    )
            }
        }
    }
}