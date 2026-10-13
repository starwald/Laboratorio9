package gt.uvg.laboratorio9.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderLineDto(
    val productId: Int,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int
)