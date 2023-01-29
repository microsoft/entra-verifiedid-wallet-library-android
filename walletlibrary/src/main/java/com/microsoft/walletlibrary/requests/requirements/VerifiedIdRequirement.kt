package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.verifiedid.VerifiedId

data class VerifiedIdRequirement(
    internal val id: String,

    // The types of Verified ID required
    val types: List<String>,

    // List of accepted issuers for the required Verified ID
    val acceptedIssuers: List<String>,

    // Indicates if the requirement must be encrypted
    internal val encrypted: Boolean = false,

    // Indicates if the requirement is required or optional
    override val required: Boolean = false,

    // Purpose of the requested Verified ID which could be displayed to user if needed
    var purpose: String = ""
): Requirement {
    // Information needed for issuance from presentation
    var credentialIssuanceParams: CredentialIssuanceParams? = null
    override fun isFulfilled(): Boolean {
        TODO("Not yet implemented")
    }

    fun fulfill(verifiedId: VerifiedId) {

    }

    fun getMatches(verifiedIds: List<VerifiedId>): List<VerifiedId> {
        return emptyList()
    }
}