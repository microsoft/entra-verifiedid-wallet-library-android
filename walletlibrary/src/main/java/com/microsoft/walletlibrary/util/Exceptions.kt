/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.util

open class WalletLibraryException(message: String? = null, cause: Throwable? = null, val retryable: Boolean = false) : Exception(message, cause)

class HandlerMissingException(message: String = "", cause: Throwable? = null, retryable: Boolean = false) : WalletLibraryException(message, cause, retryable)

class UnSupportedResolverException(message: String = "", cause: Throwable? = null, retryable: Boolean = false) : WalletLibraryException(message, cause, retryable)

class ResolverMissingException(message: String = "", cause: Throwable? = null, retryable: Boolean = false) : WalletLibraryException(message, cause, retryable)

class UnSupportedInputException(message: String = "", cause: Throwable? = null, retryable: Boolean = false) : WalletLibraryException(message, cause, retryable)

class UnSupportedVerifiedIdRequestInputException(message: String = "", cause: Throwable? = null, retryable: Boolean = false) : WalletLibraryException(message, cause, retryable)

class VerifiedIdRequestFetchException(message: String = "", cause: Throwable? = null, retryable: Boolean = false) : WalletLibraryException(message, cause, retryable)

class UnSupportedProtocolException(message: String = "", cause: Throwable? = null, retryable: Boolean = false) : WalletLibraryException(message, cause, retryable)

class UnExpectedRequestTypeException(message: String = "", cause: Throwable? = null, retryable: Boolean = false) : WalletLibraryException(message, cause, retryable)

class MissingInputDescriptorException(message: String = "", cause: Throwable? = null, retryable: Boolean = false) : WalletLibraryException(message, cause, retryable)

class MissingVerifiedIdTypeException(message: String = "", cause: Throwable? = null, retryable: Boolean = false) : WalletLibraryException(message, cause, retryable)

class UnsupportedRequirementTypeException(message: String = "", cause: Throwable? = null, retryable: Boolean = false) : WalletLibraryException(message, cause, retryable)