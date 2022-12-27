package com.microsoft.walletlibrary.requests.requirements

data class VerifiedIdRequirement(
    val id: String,
    val encrypted: Boolean,
    val required: Boolean,
    val types: List<String>,
    val acceptedIssuers: List<String>
) {
    var purpose: String? = null
    var credentialIssuanceParams: CredentialIssuanceParams? = null

    fun getMatches(verifiedIds: List<String>): List<String> {
        return emptyList()
    }
}