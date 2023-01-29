package com.microsoft.walletlibrary.requests.requirements

data class RequestedClaim(
    internal val indexed: Boolean,

    // Value of the claim
    val claim: String,

    // Indicated if claim is required or optional
    override val required: Boolean = false,
): Requirement {
    override fun isFulfilled(): Boolean {
        TODO("Not yet implemented")
    }

}