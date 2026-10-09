package gt.uvg.laboratorio9.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "order_lines")
data class OrderLineEntity(
    @PrimaryKey
    val productId: Int,
    val quantity: Int
)