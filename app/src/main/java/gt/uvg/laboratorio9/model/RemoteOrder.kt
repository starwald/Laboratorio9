package gt.uvg.laboratorio9.model

data class RemoteOrderLine(
    val productId: Int,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int
)

data class RemoteOrder(
    val id: String,
    val customerName: String,
    val total: Double,
    val createdAt: String,
    val lines: List<RemoteOrderLine>
)

data class NewRemoteOrder(
    val customerName: String,
    val total: Double,
    val createdAt: String,
    val lines: List<RemoteOrderLine>
)