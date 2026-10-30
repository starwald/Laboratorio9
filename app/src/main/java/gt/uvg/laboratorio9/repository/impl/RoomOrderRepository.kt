package gt.uvg.laboratorio9.repository.impl

import gt.uvg.laboratorio9.data.local.OrderLineDao
import gt.uvg.laboratorio9.data.local.OrderLineEntity
import gt.uvg.laboratorio9.model.OrderItem
import gt.uvg.laboratorio9.repository.OrderRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomOrderRepository(
    private val orderLineDao: OrderLineDao
) : OrderRepository {

    override fun observeOrderItems(): Flow<List<OrderItem>> =
        orderLineDao.observeOrderLines().map { lines ->
            lines.map { line ->
                OrderItem(
                    productId = line.productId,
                    quantity = line.quantity
                )
            }
        }

    override suspend fun upsertItem(item: OrderItem): Result<Unit> {
        return try {
            orderLineDao.upsertOrderLine(
                OrderLineEntity(
                    productId = item.productId,
                    quantity = item.quantity
                )
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeItem(productId: Int): Result<Unit> {
        return try {
            orderLineDao.deleteOrderLine(productId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearOrder(): Result<Unit> {
        return try {
            orderLineDao.clearOrder()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}