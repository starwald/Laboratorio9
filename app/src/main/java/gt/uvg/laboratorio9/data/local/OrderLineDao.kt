package gt.uvg.laboratorio9.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import gt.uvg.laboratorio9.data.local.OrderLineEntity

@Dao
interface OrderLineDao {

    @Query("SELECT * FROM order_lines ORDER BY productId ASC")
    fun observeOrderLines(): Flow<List<OrderLineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrderLine(
        orderLine: OrderLineEntity
    )

    @Query("DELETE FROM order_lines WHERE productId = :productId")
    suspend fun deleteOrderLine(
        productId: Int
    )

    @Query("DELETE FROM order_lines")
    suspend fun clearOrder()
}