package gt.uvg.laboratorio9.repository.impl

import gt.uvg.laboratorio9.data.local.CatalogPreferences
import gt.uvg.laboratorio9.model.CatalogOrder
import gt.uvg.laboratorio9.repository.PreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class DataStorePreferencesRepository(
    private val catalogPreferences: CatalogPreferences
) : PreferencesRepository {

    override val catalogOrder: Flow<CatalogOrder> =
        catalogPreferences.catalogOrder

    override suspend fun setCatalogOrder(
        order: CatalogOrder
    ): Result<Unit> {
        return try {
            catalogPreferences.setCatalogOrder(order)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}