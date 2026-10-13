
package gt.uvg.laboratorio9.viewmodel

import gt.uvg.laboratorio9.network.dto.OrderDto

sealed interface OrderSendState {

    data object Idle : OrderSendState

    data object Loading : OrderSendState

    data class Success(
        val order: OrderDto
    ) : OrderSendState

    data class Error(
        val message: String
    ) : OrderSendState
}
