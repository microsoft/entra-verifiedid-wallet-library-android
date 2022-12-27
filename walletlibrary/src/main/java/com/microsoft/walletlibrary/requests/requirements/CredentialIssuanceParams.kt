package com.microsoft.walletlibrary.requests.requirements

data class CredentialIssuanceParams(val credentialIssuerMetadata: List<String>) {
    val acceptedIssuers: List<String> = mutableListOf()
}