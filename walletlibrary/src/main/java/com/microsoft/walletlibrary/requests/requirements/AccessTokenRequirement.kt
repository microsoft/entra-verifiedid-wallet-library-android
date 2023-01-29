package com.microsoft.walletlibrary.requests.requirements

data class AccessTokenRequirement(
    internal val id: String,

    // Properties used by developers to get access token (configuration, clientId, resourceId, scope)
    val configuration: String,
    val clientId: String,
    val resourceId: String,
    val scope: String,

    // Specific claims requested from access token
    internal val claims: List<RequestedClaim>,

    // Indicates whether the requirement must be encrypted
    internal val encrypted: Boolean = false,

    // Indicates whether the requirement is required or optional
    override val required: Boolean = false
): Requirement {
    override fun isFulfilled(): Boolean {
        TODO("Not yet implemented")
    }

    fun fulfill() {}
}