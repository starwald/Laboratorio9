package gt.uvg.laboratorio9.repository

import gt.uvg.laboratorio9.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeProducts(): Flow<Result<List<Product>>>
}