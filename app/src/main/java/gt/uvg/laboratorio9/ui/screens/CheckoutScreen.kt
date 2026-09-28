package gt.uvg.laboratorio9.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gt.uvg.laboratorio9.model.BillingType
import gt.uvg.laboratorio9.model.OrderItem
import gt.uvg.laboratorio9.model.PaymentMethod
import gt.uvg.laboratorio9.model.Product
import gt.uvg.laboratorio9.model.calculateOrderTotal
import gt.uvg.laboratorio9.model.calculateSubtotal
import gt.uvg.laboratorio9.viewmodel.CheckoutUiState

@Composable
fun CheckoutScreen(
    orderItems: List<OrderItem>,
    products: List<Product>,
    uiState: CheckoutUiState,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onBillingTypeChange: (BillingType) -> Unit,
    onNitChange: (String) -> Unit,
    onBusinessNameChange: (String) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onConfirmOrder: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val nitFocusRequester = remember { FocusRequester() }
    val businessFocusRequester = remember { FocusRequester() }

    val total = calculateOrderTotal(orderItems = orderItems, products = products)
    val unitCount = orderItems.sumOf { it.quantity }

    // Botón reactivo con derivedStateOf.
    // rememberUpdatedState entrega un State que siempre tiene el valor más reciente.
    val uiStateHolder = rememberUpdatedState(uiState)
    val unitCountHolder = rememberUpdatedState(unitCount)
    val isConfirmEnabled by remember {
        derivedStateOf {
            uiStateHolder.value.isFormValid && unitCountHolder.value > 0
        }
    }

    val hideKeyboard = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Regresar al pedido")
        }

        Text(text = "Checkout", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(text = "Total: Q%.2f".format(total), fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(16.dp))

        //Resumen
        Text("Resumen del pedido", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("$unitCount unidades")
        Spacer(Modifier.height(8.dp))

        orderItems.forEach { item ->
            val product = products.find { it.id == item.productId }
            if (product != null) {
                val subtotal = calculateSubtotal(product = product, quantity = item.quantity)
                Text("${product.name} (x${item.quantity}) · Subtotal: Q%.2f".format(subtotal))
            }
        }

        Spacer(Modifier.height(20.dp))

        //Datos de contacto
        OutlinedTextField(
            value = uiState.fullName,
            onValueChange = onFullNameChange,
            label = { Text("Nombre completo *") },
            placeholder = { Text("Ej. María Morales") },
            singleLine = true,
            isError = uiState.visibleFullNameError != null,
            supportingText = uiState.visibleFullNameError?.let { message -> { Text(message) } },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.phone,
            onValueChange = onPhoneChange,
            label = { Text("Teléfono / WhatsApp *") },
            placeholder = { Text("Ej. 55123456") },
            singleLine = true,
            isError = uiState.visiblePhoneError != null,
            supportingText = uiState.visiblePhoneError?.let { message -> { Text(message) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = if (uiState.billingType == BillingType.NIT) {
                    ImeAction.Next
                } else {
                    ImeAction.Done
                }
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    if (uiState.billingType == BillingType.NIT) {
                        nitFocusRequester.requestFocus()
                    }
                },
                onDone = { hideKeyboard() }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        //Facturación
        Text("Facturación *", fontWeight = FontWeight.Bold)
        Column(Modifier.selectableGroup()) {
            listOf(BillingType.CF, BillingType.NIT).forEach { type ->
                RadioOptionRow(
                    label = if (type == BillingType.CF) "Consumidor Final (CF)" else "Factura con NIT",
                    selected = uiState.billingType == type,
                    onClick = {
                        hideKeyboard()
                        onBillingTypeChange(type)
                    }
                )
            }
        }

        AnimatedVisibility(visible = uiState.billingType == BillingType.NIT) {
            Column {
                Text(
                    "DATOS DE FACTURACIÓN FISCAL",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = uiState.nit,
                    onValueChange = onNitChange,
                    label = { Text("NIT *") },
                    singleLine = true,
                    isError = uiState.visibleNitError != null,
                    supportingText = uiState.visibleNitError?.let { message -> { Text(message) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { businessFocusRequester.requestFocus() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(nitFocusRequester)
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.businessName,
                    onValueChange = onBusinessNameChange,
                    label = { Text("Razón Social / Nombre fiscal *") },
                    placeholder = { Text("Ej. Guzmán Inversiones S.A.") },
                    singleLine = true,
                    isError = uiState.visibleBusinessNameError != null,
                    supportingText = uiState.visibleBusinessNameError?.let { message -> { Text(message) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { hideKeyboard() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(businessFocusRequester)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        //Método de pago
        Text("Método de Pago *", fontWeight = FontWeight.Bold)
        Column(Modifier.selectableGroup()) {
            PaymentMethod.entries.forEach { method ->
                RadioOptionRow(
                    label = if (method == PaymentMethod.CASH) {
                        "Efectivo contra entrega"
                    } else {
                        "Transferencia bancaria"
                    },
                    selected = uiState.paymentMethod == method,
                    onClick = {
                        hideKeyboard()
                        onPaymentMethodChange(method)
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        //Confirmar
        Button(
            onClick = onConfirmOrder,
            enabled = isConfirmEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirmar pedido (Total Q%.2f)".format(total))
        }

        if (!isConfirmEnabled) {
            Text(
                text = "Completa los campos obligatorios para continuar.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun RadioOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}