package com.microsoft.walletlibrary.requests.requirements

interface Requirement {
    // Indicates whether the requirement is required or optional
    val required: Boolean

    // Validates the requirement and throws an exception if the requirement is invalid or not fulfilled
    fun validate()
}