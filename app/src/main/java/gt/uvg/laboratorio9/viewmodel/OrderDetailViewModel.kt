
package gt.uvg.laboratorio9.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gt.uvg.laboratorio9.network.RetrofitProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

class OrderDetailViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow<OrderDetailUiState>(
            OrderDetailUiState.Idle
        )

    val uiState: StateFlow<OrderDetailUiState> =
        _uiState

    private var loadedOrderId: String? = null
    private var loadJob: Job? = null

    fun loadOrder(
        orderId: String,
        forceRefresh: Boolean = false
    ) {
        if (
            !forceRefresh &&
            loadedOrderId == orderId &&
            _uiState.value !is OrderDetailUiState.Idle
        ) {
            return
        }

        loadJob?.cancel()
        loadedOrderId = orderId

        _uiState.value = OrderDetailUiState.Loading

        loadJob = viewModelScope.launch {
            try {
                val order =
                    RetrofitProvider.orderApi.getOrderById(orderId)

                _uiState.value =
                    OrderDetailUiState.Success(order)

            } catch (e: CancellationException) {
                throw e

            } catch (e: HttpException) {
                _uiState.value =
                    if (e.code() == 404) {
                        OrderDetailUiState.NotFound
                    } else {
                        OrderDetailUiState.Error(
                            "El servidor no pudo consultar este pedido."
                        )
                    }

            } catch (e: IOException) {
                _uiState.value = OrderDetailUiState.Error(
                    "No pudimos conectar con el servidor. Revisa tu conexión."
                )

            } catch (e: SerializationException) {
                _uiState.value = OrderDetailUiState.Error(
                    "Recibimos una respuesta inesperada del servidor."
                )
            }
        }
    }
}
