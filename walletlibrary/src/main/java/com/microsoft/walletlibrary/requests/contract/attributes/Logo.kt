package com.microsoft.walletlibrary.requests.contract.attributes

data class Logo(
    // If image needs to be fetched, service will use this property.
    var uri: String? = null,

    // Else if image is in svg or base64 format, service will use this property.
    var image: String? = null,

    // Description used for alt text or voice over.
    val description: String
)