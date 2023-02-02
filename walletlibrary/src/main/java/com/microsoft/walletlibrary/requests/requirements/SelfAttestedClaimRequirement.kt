package com.microsoft.walletlibrary.requests.requirements

class SelfAttestedClaimRequirement(
    internal val id: String,

    // Claim requested
    val claim: String,

    // Indicates if the requirement must be encrypted
    internal val encrypted: Boolean = false,

    // Indicates if the requirement is required or optional
    override val required: Boolean = false
): Requirement {
    // Validates the requirement and throws an exception if the requirement is invalid or not fulfilled
    override fun validate() {
        TODO("Not yet implemented")
    }

    // Fulfills the requirement in the request with specified value
    fun fulfill(seldAttestedClaimValue: String) {
        TODO("Not yet implemented")
    }
}