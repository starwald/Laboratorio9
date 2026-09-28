package gt.uvg.laboratorio9.viewmodel

import gt.uvg.laboratorio9.model.BillingType
import gt.uvg.laboratorio9.model.PaymentMethod

data class CheckoutUiState(
    val fullName: String = "",
    val phone: String = "",

    val billingType: BillingType = BillingType.CF,

    val nit: String = "",
    val businessName: String = "",

    val paymentMethod: PaymentMethod = PaymentMethod.CASH,

    val fullNameTouched: Boolean = false,
    val phoneTouched: Boolean = false,
    val nitTouched: Boolean = false,
    val businessNameTouched: Boolean = false
)