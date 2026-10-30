package gt.uvg.laboratorio9.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gt.uvg.laboratorio9.StoreApplication
import gt.uvg.laboratorio9.repository.RemoteOrdersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val repository: RemoteOrdersRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<OrdersUiState>(OrdersUiState.Idle)

    val uiState = _uiState.asStateFlow()

    fun loadOrders() {
        if (_uiState.value is OrdersUiState.Loading) {
            return
        }

        _uiState.value = OrdersUiState.Loading

        viewModelScope.launch {
            repository.getOrders().fold(
                onSuccess = { orders ->
                    _uiState.value = if (orders.isEmpty()) {
                        OrdersUiState.Empty
                    } else {
                        OrdersUiState.Success(orders)
                    }
                },
                onFailure = {
                    _uiState.value = OrdersUiState.Error(
                        "No pudimos cargar los pedidos. " +
                                "Revisa tu conexión e intenta nuevamente."
                    )
                }
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    checkNotNull(this[APPLICATION_KEY]) as StoreApplication

                OrdersViewModel(
                    repository = application.container.remoteOrdersRepository
                )
            }
        }
    }
}