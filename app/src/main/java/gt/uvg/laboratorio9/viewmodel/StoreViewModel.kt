package gt.uvg.laboratorio9.viewmodel

import androidx.lifecycle.ViewModel
import gt.uvg.laboratorio9.data.coffeeOrigins
import gt.uvg.laboratorio9.data.coffeePresentations
import gt.uvg.laboratorio9.data.coffeeRoasts
import gt.uvg.laboratorio9.data.products
import gt.uvg.laboratorio9.data.producers
import gt.uvg.laboratorio9.model.OrderItem
import gt.uvg.laboratorio9.model.Product
import gt.uvg.laboratorio9.model.Producer
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

        val newFavorites = if (productId in currentFavorites) {

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

    fun decreaseOrderItem(
        productId: Int
    ) {

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

    fun removeFromOrder(
        productId: Int
    ) {

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
}