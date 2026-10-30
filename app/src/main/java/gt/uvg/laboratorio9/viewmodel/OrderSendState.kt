package gt.uvg.laboratorio9.viewmodel

import gt.uvg.laboratorio9.model.RemoteOrder

sealed interface OrderSendState {

    data object Idle : OrderSendState

    data object Loading : OrderSendState

    data class Success(
        val order: RemoteOrder
    ) : OrderSendState

    data class Error(
        val message: String
    ) : OrderSendState
}