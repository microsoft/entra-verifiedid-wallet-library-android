package com.microsoft.walletlibrary.requests.requirements

data class PinRequirement(

    // Length of the pin
    val length: String,

    // Type of the pin (eg. alphanumeric, numeric)
    val type: String,

    // Indicates if pin is required or optional
    val required: Boolean = false
)