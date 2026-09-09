package gt.uvg.laboratorio9.viewmodel

import androidx.lifecycle.ViewModel
import gt.uvg.laboratorio9.data.products
import gt.uvg.laboratorio9.data.producers
import gt.uvg.laboratorio9.model.Product
import gt.uvg.laboratorio9.model.Producer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StoreViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        StoreUiState(
            products = products,
            producers = producers
        )
    )

    val uiState: StateFlow<StoreUiState> = _uiState

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
}