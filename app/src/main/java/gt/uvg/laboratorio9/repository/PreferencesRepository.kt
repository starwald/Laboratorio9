package gt.uvg.laboratorio9.repository

import gt.uvg.laboratorio9.model.CatalogOrder
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val catalogOrder: Flow<CatalogOrder>
    suspend fun setCatalogOrder(order: CatalogOrder): Result<Unit>
}
