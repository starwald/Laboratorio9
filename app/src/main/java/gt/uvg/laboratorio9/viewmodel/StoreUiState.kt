package gt.uvg.laboratorio9.viewmodel

import gt.uvg.laboratorio9.model.Product
import gt.uvg.laboratorio9.model.Producer

data class StoreUiState(
    val products: List<Product> = emptyList(),
    val producers: List<Producer> = emptyList(),
    val favoriteProductIds: Set<Int> = emptySet()
)