
package gt.uvg.laboratorio9.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gt.uvg.laboratorio9.network.RetrofitProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

class OrdersViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow<OrdersUiState>(
            OrdersUiState.Idle
        )

    val uiState: StateFlow<OrdersUiState> =
        _uiState

    fun loadOrders() {

        if (_uiState.value is OrdersUiState.Loading) {
            return
        }

        _uiState.value = OrdersUiState.Loading

        viewModelScope.launch {
            try {

                val orders = RetrofitProvider.orderApi.getOrders(
                    sortBy = "createdAt",
                    order = "desc"
                )

                _uiState.value = if (orders.isEmpty()) {
                    OrdersUiState.Empty
                } else {
                    OrdersUiState.Success(orders)
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: IOException) {
                _uiState.value = OrdersUiState.Error(
                    "No pudimos conectar con el servidor. Revisa tu conexión."
                )

            } catch (e: HttpException) {
                _uiState.value = OrdersUiState.Error(
                    "No pudimos consultar los pedidos. Intenta nuevamente."
                )

            } catch (e: SerializationException) {
                _uiState.value = OrdersUiState.Error(
                    "Recibimos una respuesta inesperada del servidor."
                )
            }
        }
    }
}
