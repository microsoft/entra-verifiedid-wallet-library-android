package com.microsoft.walletlibrary.controlflow

open class WalletLibraryException(
    message: String? = null,
    cause: Throwable? = null,
    val retryable: Boolean = false
) : Exception(message, cause)