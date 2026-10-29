package gt.uvg.laboratorio9.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gt.uvg.laboratorio9.StoreApplication
import gt.uvg.laboratorio9.model.OrderNotFoundException
import gt.uvg.laboratorio9.repository.RemoteOrdersRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderDetailViewModel(
    private val repository: RemoteOrdersRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Idle)

    val uiState = _uiState.asStateFlow()

    private var loadedOrderId: String? = null
    private var loadJob: Job? = null

    fun loadOrder(
        orderId: String,
        forceRefresh: Boolean = false
    ) {
        if (
            !forceRefresh &&
            loadedOrderId == orderId &&
            (
                    _uiState.value is OrderDetailUiState.Loading ||
                            _uiState.value is OrderDetailUiState.Success
                    )
        ) {
            return
        }

        loadJob?.cancel()
        loadedOrderId = orderId
        _uiState.value = OrderDetailUiState.Loading

        loadJob = viewModelScope.launch {
            repository.getOrderById(orderId).fold(
                onSuccess = { order ->
                    _uiState.value = OrderDetailUiState.Success(order)
                },
                onFailure = { error ->
                    _uiState.value = if (error is OrderNotFoundException) {
                        OrderDetailUiState.NotFound
                    } else {
                        OrderDetailUiState.Error(
                            "No pudimos cargar el pedido. " +
                                    "Revisa tu conexión e intenta nuevamente."
                        )
                    }
                }
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    checkNotNull(this[APPLICATION_KEY]) as StoreApplication

                OrderDetailViewModel(
                    repository = application.container.remoteOrdersRepository
                )
            }
        }
    }
}