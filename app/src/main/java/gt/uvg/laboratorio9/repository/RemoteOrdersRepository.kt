package gt.uvg.laboratorio9.repository

import gt.uvg.laboratorio9.model.NewRemoteOrder
import gt.uvg.laboratorio9.model.RemoteOrder

interface RemoteOrdersRepository {
    suspend fun createOrder(order: NewRemoteOrder): Result<RemoteOrder>
    suspend fun getOrders(): Result<List<RemoteOrder>>
    suspend fun getOrderById(id: String): Result<RemoteOrder>
}