package gt.uvg.laboratorio9.viewmodel

import gt.uvg.laboratorio9.model.BillingType
import gt.uvg.laboratorio9.model.PaymentMethod
import gt.uvg.laboratorio9.model.validateBusinessName
import gt.uvg.laboratorio9.model.validateFullName
import gt.uvg.laboratorio9.model.validateNit
import gt.uvg.laboratorio9.model.validatePhoneNumber

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
) {

    // Errores reales (siempre calculados). En CF, los fiscales son null.
    val fullNameError: String?
        get() = validateFullName(fullName)

    val phoneError: String?
        get() = validatePhoneNumber(phone)

    val nitError: String?
        get() = if (billingType == BillingType.NIT) validateNit(nit) else null

    val businessNameError: String?
        get() = if (billingType == BillingType.NIT) validateBusinessName(businessName) else null

    // Errores visibles: solo si el campo fue tocado.
    val visibleFullNameError: String?
        get() = if (fullNameTouched) fullNameError else null

    val visiblePhoneError: String?
        get() = if (phoneTouched) phoneError else null

    val visibleNitError: String?
        get() = if (nitTouched) nitError else null

    val visibleBusinessNameError: String?
        get() = if (businessNameTouched) businessNameError else null

    // Validez: siempre se calcula, con campos tocados o no.
    val isFormValid: Boolean
        get() = fullNameError == null &&
                phoneError == null &&
                nitError == null &&
                businessNameError == null
}