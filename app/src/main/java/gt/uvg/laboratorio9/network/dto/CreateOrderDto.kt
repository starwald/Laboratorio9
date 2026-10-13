package gt.uvg.laboratorio9.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderDto(
    val customerName: String,
    val total: Double,
    val createdAt: String,
    val lines: List<OrderLineDto>
)