package gt.uvg.laboratorio9.model

enum class CatalogOrder {
    NAME,
    PRICE
}

fun sortProducts(products: List<Product>, order: CatalogOrder): List<Product> {
    return when (order) {
        CatalogOrder.NAME -> products.sortedBy { it.name }
        CatalogOrder.PRICE -> products.sortedBy { it.price }
    }
}