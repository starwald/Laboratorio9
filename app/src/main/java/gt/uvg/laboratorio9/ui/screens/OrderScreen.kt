package gt.uvg.laboratorio9.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gt.uvg.laboratorio9.model.OrderItem
import gt.uvg.laboratorio9.model.Product
import gt.uvg.laboratorio9.model.calculateOrderTotal
import gt.uvg.laboratorio9.model.calculateSubtotal

@Composable
fun OrderScreen(
    orderItems: List<OrderItem>,
    products: List<Product>,
    orderMessage: String?,
    onIncrease: (Int) -> Unit,
    onDecrease: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    val total = calculateOrderTotal(
        orderItems = orderItems,
        products = products
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {

        TextButton(
            onClick = onBack
        ) {
            Text("← Regresar")
        }

        Text(
            text = "Mi pedido",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        if (orderMessage != null) {

            Text(
                text = orderMessage,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }

        if (orderItems.isEmpty()) {

            Text(
                text = "No hay productos en tu pedido.",
                fontSize = 18.sp
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "Total: Q0.00",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

        } else {

            orderItems.forEach { item ->

                val product = products.find {
                    it.id == item.productId
                }

                if (product != null) {

                    val subtotal = calculateSubtotal(
                        product = product,
                        quantity = item.quantity
                    )

                    Text(
                        text = product.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Q%.2f por unidad".format(
                            product.price
                        )
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Button(
                            onClick = {
                                onDecrease(product.id)
                            }
                        ) {
                            Text("-")
                        }

                        Text(
                            text = "${item.quantity}",
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            ),
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = {
                                onIncrease(product.id)
                            }
                        ) {
                            Text("+")
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "Subtotal: Q%.2f".format(
                            subtotal
                        ),
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(
                        onClick = {
                            onRemove(product.id)
                        }
                    ) {
                        Text("Eliminar")
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(
                            vertical = 12.dp
                        )
                    )
                }
            }

            Text(
                text = "Total: Q%.2f".format(total),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}