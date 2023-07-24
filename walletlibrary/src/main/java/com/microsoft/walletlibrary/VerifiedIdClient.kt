/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary

import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.requests.RequestHandlerFactory
import com.microsoft.walletlibrary.requests.RequestResolverFactory
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequestSerializer
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestInput
import com.microsoft.walletlibrary.util.UnspecifiedVerifiedIdException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.util.WalletLibraryLogger
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * VerifiedIdClient is configured by builder and is used to create requests.
 */
class VerifiedIdClient(
    internal val requestResolverFactory: RequestResolverFactory,
    internal val requestHandlerFactory: RequestHandlerFactory,
    internal val logger: WalletLibraryLogger,
    private val serializer: Json
) {

    // Creates an issuance or presentation request based on the provided input.
    suspend fun createRequest(verifiedIdRequestInput: VerifiedIdRequestInput): Result<VerifiedIdRequest<*>> {
        return try {
            VerifiableCredentialSdk.correlationVectorService.startNewFlowAndSave()
            val requestResolver = requestResolverFactory.getResolver(verifiedIdRequestInput)
            val rawRequest = requestResolver.resolve(verifiedIdRequestInput)
            val requestHandler = requestHandlerFactory.getHandler(requestResolver)
            VerifiedIdResult.success(requestHandler.handleRequest(rawRequest))
        } catch (exception: Exception) {
            UnspecifiedVerifiedIdException("Unspecified Exception", VerifiedIdExceptions.UNSPECIFIED_EXCEPTION.value, exception).toVerifiedIdResult()
        }
    }

    fun encodeRequest(verifiedIdRequest: VerifiedIdRequest<*>): Result<String> {
        return try {
            Result.success(serializer.encodeToString(VerifiedIdRequestSerializer, verifiedIdRequest))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    fun decodeRequest(encodedVerifiedIdRequest: String): Result<VerifiedIdRequest<*>> {
        return try {
            Result.success(serializer.decodeFromString(VerifiedIdRequestSerializer, encodedVerifiedIdRequest))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    fun encode(verifiedId: VerifiedId): VerifiedIdResult<String> {
        return try {
            VerifiedIdResult.success(serializer.encodeToString(verifiedId))
        } catch (exception: Exception) {
            UnspecifiedVerifiedIdException("Unspecified Exception", VerifiedIdExceptions.UNSPECIFIED_EXCEPTION.value, exception).toVerifiedIdResult()
        }
    }

    fun decodeVerifiedId(encodedVerifiedId: String): Result<VerifiedId> {
        return try {
            VerifiedIdResult.success(serializer.decodeFromString(encodedVerifiedId))
        } catch (exception: Exception) {
            UnspecifiedVerifiedIdException("Unspecified Exception", VerifiedIdExceptions.UNSPECIFIED_EXCEPTION.value, exception).toVerifiedIdResult()
        }
    }
}