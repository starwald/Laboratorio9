package gt.uvg.laboratorio9.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import gt.uvg.laboratorio9.model.CatalogOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.catalogDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "catalog_preferences"
)

private val CATALOG_ORDER_KEY = stringPreferencesKey("catalog_order")

class CatalogPreferences(private val context: Context) {

    val catalogOrder: Flow<CatalogOrder> =
        context.catalogDataStore.data.map { preferences ->
            when (preferences[CATALOG_ORDER_KEY]) {
                CatalogOrder.PRICE.name -> CatalogOrder.PRICE
                else -> CatalogOrder.NAME
            }
        }

    suspend fun setCatalogOrder(order: CatalogOrder) {
        context.catalogDataStore.edit { preferences ->
            preferences[CATALOG_ORDER_KEY] = order.name
        }
    }
}