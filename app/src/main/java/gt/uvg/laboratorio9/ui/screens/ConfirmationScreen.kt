package gt.uvg.laboratorio9.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gt.uvg.laboratorio9.model.BillingType
import gt.uvg.laboratorio9.model.OrderReceipt
import gt.uvg.laboratorio9.model.PaymentMethod

@Composable
fun ConfirmationScreen(
    receipt: OrderReceipt?,
    onBackToCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Atrás también lleva al catálogo, no al checkout.
    BackHandler(onBack = onBackToCatalog)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("✓", fontSize = 56.sp, color = MaterialTheme.colorScheme.primary)
        Text("¡Pedido confirmado!", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Orden registrada exitosamente en su tienda.")

        Spacer(Modifier.height(16.dp))

        if (receipt != null) {
            ReceiptRow("Folio:", receipt.folio)
            ReceiptRow("Cliente:", receipt.customerName)
            ReceiptRow(
                "Facturación:",
                if (receipt.billingType == BillingType.CF) "CF (Consumidor Final)"
                else "Factura con NIT"
            )
            ReceiptRow(
                "Método de pago:",
                if (receipt.paymentMethod == PaymentMethod.CASH) "Efectivo contra entrega"
                else "Transferencia bancaria"
            )
            ReceiptRow("Total del pedido:", "Q%.2f".format(receipt.total))
        }

        Spacer(Modifier.height(24.dp))

        Button(onClick = onBackToCatalog, modifier = Modifier.fillMaxWidth()) {
            Text("Volver al catálogo")
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(value, fontWeight = FontWeight.Bold)
    }
}