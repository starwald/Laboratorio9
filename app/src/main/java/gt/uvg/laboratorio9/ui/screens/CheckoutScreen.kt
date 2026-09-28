package gt.uvg.laboratorio9.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gt.uvg.laboratorio9.model.OrderItem
import gt.uvg.laboratorio9.model.Product
import gt.uvg.laboratorio9.model.calculateOrderTotal
import gt.uvg.laboratorio9.model.calculateSubtotal


@Composable
fun CheckoutScreen(
    orderItems: List<OrderItem>,
    products: List<Product>,
    fullName: String,
    phone: String,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    val focusManager = LocalFocusManager.current

    val total = calculateOrderTotal(
        orderItems = orderItems,
        products = products
    )

    val unitCount = orderItems.sumOf { item ->
        item.quantity
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(16.dp)
    ) {

        TextButton(
            onClick = onBack
        ) {
            Text("← Regresar al pedido")
        }

        Text(
            text = "Checkout",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Completa tus datos para continuar con el pedido."
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "Datos de contacto",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = {
                Text("Nombre completo")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(
                        FocusDirection.Down
                    )
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = {
                Text("Teléfono")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Resumen del pedido",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "$unitCount unidades"
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

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
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${item.quantity} x Q%.2f".format(
                        product.price
                    )
                )

                Text(
                    text = "Subtotal: Q%.2f".format(
                        subtotal
                    )
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }
        }

        Text(
            text = "Total: Q%.2f".format(total),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )
    }
}