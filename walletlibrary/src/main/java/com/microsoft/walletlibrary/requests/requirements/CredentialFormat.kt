package com.microsoft.walletlibrary.requests.requirements

internal data class CredentialFormat(
    internal val format: String,
    internal val types: List<String>
)