package com.microsoft.walletlibrary.requests.requirements

data class PinRequirement(
    // Length of the pin
    val length: String,

    // Type of the pin (eg. alphanumeric, numeric)
    val type: String,

    // Indicates if pin is required or optional
    override val required: Boolean = false
): Requirement {
    override fun isFulfilled(): Boolean {
        TODO("Not yet implemented")
    }

    fun fulfill(pin: String) {

    }
}