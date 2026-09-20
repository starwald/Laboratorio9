package gt.uvg.laboratorio9.model

fun calculateSubtotal(
    product: Product,
    quantity: Int
): Double {

    return product.price * quantity
}

fun calculateOrderTotal(
    orderItems: List<OrderItem>,
    products: List<Product>
): Double {

    return orderItems.sumOf { item ->

        val product = products.find {
            it.id == item.productId
        }

        if (product != null) {

            calculateSubtotal(
                product = product,
                quantity = item.quantity
            )

        } else {

            0.0

        }
    }
}