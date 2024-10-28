/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.util.controlflow

open class SdkException internal constructor(message: String? = null, cause: Throwable? = null, val retryable: Boolean = false) : Exception(message, cause)

internal open class CryptoException(message: String, cause: Throwable? = null, retryable: Boolean = false) : SdkException(message, cause, retryable)

internal class KeyStoreException(message: String, cause: Throwable? = null) : CryptoException(message, cause)

internal class KeyException(message: String, cause: Throwable? = null) : CryptoException(message, cause)

internal class AlgorithmException(message: String, cause: Throwable? = null) : CryptoException(message, cause)

internal open class BackupException(message: String, cause: Throwable? = null, retryable: Boolean = false) :
    SdkException(message, cause, retryable)

internal class UnknownBackupFormatException(message: String, cause: Throwable? = null) : BackupException(message, cause, false)

internal class UnknownProtectionMethodException(message: String, cause: Throwable? = null) : BackupException(message, cause, false)

internal class NoBackupException(message: String = "", retryable: Boolean = false) : BackupException(message, null, retryable)

internal open class MalformedBackupException(message: String, cause: Throwable? = null) : BackupException(message, cause, false)

internal class MalformedIdentityException(message: String, cause: Throwable? = null) : MalformedBackupException(message, cause)

internal class FailedDecryptException(message: String, cause: Throwable? = null, retryable: Boolean = false) :
    BackupException(message, cause, retryable)

internal class BadPasswordException(message: String, cause: Throwable? = null, retryable: Boolean = false) :
    BackupException(message, cause, retryable)

internal open class AuthenticationException(message: String, cause: Throwable? = null, retryable: Boolean = true) :
    SdkException(message, cause, retryable)

internal open class PresentationException(message: String, cause: Throwable? = null, retryable: Boolean = true) :
    AuthenticationException(message, cause, retryable)

internal open class IssuanceException(message: String, cause: Throwable? = null, retryable: Boolean = true) :
    AuthenticationException(message, cause, retryable)

internal open class RevocationException(message: String? = null, cause: Throwable? = null, retryable: Boolean = true) :
    SdkException(message, cause, retryable)

internal open class ValidatorException(message: String, cause: Throwable? = null, retryable: Boolean = false) :
    SdkException(message, cause, retryable)

internal class InvalidSignatureException(message: String) : ValidatorException(message)

internal class InvalidResponseTypeException(message: String) : ValidatorException(message)

internal class InvalidResponseModeException(message: String) : ValidatorException(message)

internal class InvalidScopeException(message: String) : ValidatorException(message)

internal class InvalidPinDetailsException(message: String) : ValidatorException(message)

internal class MissingInputInRequestException(message: String) : ValidatorException(message)

internal class DidInHeaderAndPayloadNotMatching(message: String) : ValidatorException(message)

internal class SubjectIdentifierTypeNotSupported(message: String) : ValidatorException(message)

internal class DidMethodNotSupported(message: String) : ValidatorException(message)

internal class VpFormatNotSupported(message: String) : ValidatorException(message)

internal open class ResolverException(message: String, cause: Throwable? = null) : SdkException(message, cause)

internal class LinkedDomainEndpointInUnknownFormatException(message: String, cause: Throwable? = null) : ResolverException(message, cause)

internal class RegistrarException(message: String, cause: Throwable? = null) : SdkException(message, cause)

internal open class LocalNetworkException(message: String, cause: Throwable? = null) : SdkException(message, cause, true)

internal open class NetworkException(message: String, retryable: Boolean) : SdkException(message, null, retryable) {
    var requestId: String? = null
    var correlationVector: String? = null
    var errorCode: String? = null
    var errorBody: String? = null
    var innerErrorCodes: String? = null
}

internal class ServiceUnreachableException(message: String, retryable: Boolean) : NetworkException(message, retryable)

internal class ClientException(message: String, retryable: Boolean) : NetworkException(message, retryable)

internal class ForbiddenException(message: String, retryable: Boolean) : NetworkException(message, retryable)

internal class NotFoundException(message: String, retryable: Boolean) : NetworkException(message, retryable)

internal class UnauthorizedException(message: String, retryable: Boolean) : NetworkException(message, retryable)

internal class RedirectException(message: String, retryable: Boolean) : NetworkException(message, retryable)

internal class ExpiredTokenException(message: String, retryable: Boolean) : NetworkException(message, retryable)

internal class InvalidPinException(message: String, retryable: Boolean) : NetworkException(message, retryable)

internal class RepositoryException(message: String, cause: Throwable? = null) : SdkException(message, cause)

internal class InvalidImageException(message: String, cause: Throwable? = null) : SdkException(message, cause)
