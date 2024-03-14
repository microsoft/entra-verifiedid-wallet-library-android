/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.util

open class WalletLibraryException(
    message: String? = null,
    cause: Throwable? = null,
    val retryable: Boolean = false
) : Exception(message, cause)

class HandlerMissingException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : WalletLibraryException(message, cause, retryable)

class UnSupportedRawRequestException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : WalletLibraryException(message, cause, retryable)


class ResolverMissingException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : WalletLibraryException(message, cause, retryable)

class UnSupportedInputException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : WalletLibraryException(message, cause, retryable)

class UnSupportedVerifiedIdRequestInputException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : WalletLibraryException(message, cause, retryable)

class RequestURIMissingException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : WalletLibraryException(message, cause, retryable)

class VerifiedIdRequestFetchException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : WalletLibraryException(message, cause, retryable)

class VerifiedIdResponseCompletionException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : WalletLibraryException(message, cause, retryable)

class OpenIdResponseCompletionException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : WalletLibraryException(message, cause, retryable)

class UnSupportedProtocolException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : WalletLibraryException(message, cause, retryable)

class UnSupportedRequirementException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : WalletLibraryException(message, cause, retryable)

class MissingInputDescriptorException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : WalletLibraryException(message, cause, retryable)

class MissingVerifiedIdTypeException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : WalletLibraryException(message, cause, retryable)

class RequirementCastingException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : WalletLibraryException(message, cause, retryable)

class InputCastingException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : WalletLibraryException(message, cause, retryable)

class MissingRequirementException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : WalletLibraryException(message, cause, retryable)

open class ParameterMissingException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : WalletLibraryException(message, cause, retryable)

class MissingRequestStateException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : ParameterMissingException(message, cause, retryable)

class MissingCallbackUrlException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : ParameterMissingException(message, cause, retryable)

open class RequirementValidationException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : WalletLibraryException(message, cause, retryable)

class MultipleRequirementsWithSameIdException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : RequirementValidationException(message, cause, retryable)

class NoMatchingRequirementInRequestException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : RequirementValidationException(message, cause, retryable)

class VerifiedIdTypeIsNotRequestedTypeException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : RequirementValidationException(message, cause, retryable)

class NoMatchForVcPathRegexConstraintException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : RequirementValidationException(message, cause, retryable)

class NoMatchForAnyConstraintsException(
    message: String = "",
    val exceptions: List<Throwable> = emptyList(),
    retryable: Boolean = false
) : RequirementValidationException(message, retryable = retryable)

class NoMatchForAtLeastOneConstraintException(
    message: String = "",
    val exceptions: List<Throwable> = emptyList(),
    retryable: Boolean = false
) : RequirementValidationException(message, retryable = retryable)

class VerifiedIdRequirementMissingIdException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : RequirementValidationException(message, cause, retryable)

class VerifiedIdRequirementIdConflictException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : RequirementValidationException(message, cause, retryable)

class IdInVerifiedIdRequirementDoesNotMatchRequestException(
    message: String = "",
    cause: Throwable? = null,
    retryable: Boolean = false
) : RequirementValidationException(message, cause, retryable)
