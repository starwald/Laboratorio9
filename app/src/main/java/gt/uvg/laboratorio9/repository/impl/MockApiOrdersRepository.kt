package gt.uvg.laboratorio9.repository.impl

import gt.uvg.laboratorio9.model.NewRemoteOrder
import gt.uvg.laboratorio9.model.RemoteOrder
import gt.uvg.laboratorio9.model.RemoteOrderLine
import gt.uvg.laboratorio9.network.OrderApi
import gt.uvg.laboratorio9.network.dto.CreateOrderDto
import gt.uvg.laboratorio9.network.dto.OrderDto
import gt.uvg.laboratorio9.network.dto.OrderLineDto
import gt.uvg.laboratorio9.repository.RemoteOrdersRepository
import kotlinx.coroutines.CancellationException
import gt.uvg.laboratorio9.model.OrderNotFoundException
import retrofit2.HttpException

class MockApiOrdersRepository(
    private val orderApi: OrderApi
) : RemoteOrdersRepository {

    override suspend fun createOrder(
        order: NewRemoteOrder
    ): Result<RemoteOrder> {
        return try {
            val request = CreateOrderDto(
                customerName = order.customerName,
                total = order.total,
                createdAt = order.createdAt,
                lines = order.lines.map { line ->
                    OrderLineDto(
                        productId = line.productId,
                        productName = line.productName,
                        unitPrice = line.unitPrice,
                        quantity = line.quantity
                    )
                }
            )

            Result.success(orderApi.createOrder(request).toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrders(): Result<List<RemoteOrder>> {
        return try {
            val orders = orderApi.getOrders(
                sortBy = "createdAt",
                order = "desc"
            )
            Result.success(orders.map { it.toDomain() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrderById(
        id: String
    ): Result<RemoteOrder> {
        return try {
            Result.success(orderApi.getOrderById(id).toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            if (e.code() == 404) {
                Result.failure(OrderNotFoundException())
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    private fun OrderDto.toDomain(): RemoteOrder {
        return RemoteOrder(
            id = id,
            customerName = customerName,
            total = total,
            createdAt = createdAt,
            lines = lines.map { line ->
                RemoteOrderLine(
                    productId = line.productId,
                    productName = line.productName,
                    unitPrice = line.unitPrice,
                    quantity = line.quantity
                )
            }
        )
    }
}