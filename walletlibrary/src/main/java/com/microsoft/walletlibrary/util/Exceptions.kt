package com.microsoft.walletlibrary.util

open class WalletLibraryException(message: String? = null, cause: Throwable? = null, val retryable: Boolean = false) : Exception(message, cause)

class HandlerMissingException(message: String = "", cause: Throwable? = null, retryable: Boolean = false) : WalletLibraryException(message, null, retryable)

class UnSupportedResolverException(message: String = "", cause: Throwable? = null, retryable: Boolean = false) : WalletLibraryException(message, null, retryable)

class ResolverMissingException(message: String = "", cause: Throwable? = null, retryable: Boolean = false) : WalletLibraryException(message, null, retryable)

class UnSupportedInputException(message: String = "", cause: Throwable? = null, retryable: Boolean = false) : WalletLibraryException(message, null, retryable)