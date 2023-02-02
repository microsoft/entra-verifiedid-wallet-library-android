package com.microsoft.walletlibrary.requests.requirements

class PinRequirement(
    // Length of the pin
    val length: String,

    // Type of the pin (eg. alphanumeric, numeric)
    val type: String,

    // Indicates if pin is required or optional
    override val required: Boolean = false
): Requirement {
    // Validates the requirement and throws an exception if the requirement is invalid or not fulfilled
    override fun validate() {
        TODO("Not yet implemented")
    }

    // Fulfills the requirement in the request with specified value
    fun fulfill(pin: String) {
        TODO("Not yet implemented")
    }
}