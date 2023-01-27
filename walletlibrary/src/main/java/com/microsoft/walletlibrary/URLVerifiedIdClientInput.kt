package com.microsoft.walletlibrary

class URLVerifiedIdClientInput(private val url: String) : VerifiedIdClientInput {

    override fun resolve(): String {
        return url
    }

}