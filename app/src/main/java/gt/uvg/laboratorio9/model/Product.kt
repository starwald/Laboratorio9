package gt.uvg.laboratorio9.model

data class Product(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val producerId: Int
)