package gt.uvg.laboratorio9.model

enum class BillingType {
    CF,
    NIT
}

enum class PaymentMethod {
    CASH,
    BANK_TRANSFER
}

data class OrderReceipt(
    val folio: String,
    val customerName: String,
    val billingType: BillingType,
    val paymentMethod: PaymentMethod,
    val total: Double
)