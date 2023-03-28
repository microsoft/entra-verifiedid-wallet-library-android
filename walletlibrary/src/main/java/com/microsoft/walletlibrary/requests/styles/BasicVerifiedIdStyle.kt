package com.microsoft.walletlibrary.requests.styles

class BasicVerifiedIdStyle(
    // Name of Verified Id.
    override val name: String,

    // Issuer of the Verified Id.
    val issuer: String,

    // The background color of the Verified Id in hex.
    val backgroundColor: String,

    // The color of the text written on Verified Id in hex.
    val textColor: String,

    // Description of the Verified Id.
    val description: String,

    // Logo that should be displayed on the Verified Id.
    val logo: Logo? = null
): VerifiedIdStyle(name)