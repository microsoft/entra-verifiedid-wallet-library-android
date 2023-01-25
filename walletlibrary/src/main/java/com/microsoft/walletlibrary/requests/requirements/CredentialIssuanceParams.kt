package com.microsoft.walletlibrary.requests.requirements

data class CredentialIssuanceParams(
    // Information like contract url which describes where to get the contract form
    val credentialIssuerMetadata: List<String>
)