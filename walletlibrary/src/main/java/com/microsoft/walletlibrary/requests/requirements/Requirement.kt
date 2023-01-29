package com.microsoft.walletlibrary.requests.requirements

interface Requirement {
    // Indicates whether the requirement is required or optional
    val required: Boolean

    // Indicates whether the requirement is fulfilled or not
    fun isFulfilled(): Boolean
}