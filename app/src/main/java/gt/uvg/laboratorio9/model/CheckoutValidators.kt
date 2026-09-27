package gt.uvg.laboratorio9.model

fun validateFullName(value: String): String? {

    val cleanValue = value.trim()

    val letterCount = cleanValue.count { character ->
        character.isLetter()
    }

    val hasDigit = cleanValue.any { character ->
        character.isDigit()
    }

    return when {
        letterCount < 3 ->
            "Ingrese al menos 3 letras"

        hasDigit ->
            "El nombre no puede contener números"

        else ->
            null
    }
}

fun validatePhoneNumber(value: String): String? {

    val cleanValue = value.trim()

    return if (
        cleanValue.length == 8 &&
        cleanValue.all { character ->
            character.isDigit()
        }
    ) {
        null
    } else {
        "Ingrese exactamente 8 dígitos"
    }
}

fun validateNit(value: String): String? {

    val cleanValue = value.trim()

    return if (
        cleanValue.length >= 5 &&
        cleanValue.all { character ->
            character.isDigit()
        }
    ) {
        null
    } else {
        "Ingrese al menos 5 dígitos para este ejercicio"
    }
}

fun validateBusinessName(value: String): String? {

    val cleanValue = value.trim()

    return if (cleanValue.length >= 3) {
        null
    } else {
        "Ingrese al menos 3 caracteres"
    }
}