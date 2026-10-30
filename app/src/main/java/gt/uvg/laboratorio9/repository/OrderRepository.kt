package gt.uvg.laboratorio9.repository

import gt.uvg.laboratorio9.model.OrderItem
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun observeOrderItems(): Flow<List<OrderItem>>
    suspend fun upsertItem(item: OrderItem): Result<Unit>
    suspend fun removeItem(productId: Int): Result<Unit>
    suspend fun clearOrder(): Result<Unit>
}