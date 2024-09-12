/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary

import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog
import com.microsoft.walletlibrary.requests.RequestProcessorFactory
import com.microsoft.walletlibrary.requests.RequestResolverFactory
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequestSerializer
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestInput
import com.microsoft.walletlibrary.util.MalformedInputException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.util.WalletLibraryLogger
import com.microsoft.walletlibrary.util.getResult
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * VerifiedIdClient is configured by builder and is used to create requests.
 */
class VerifiedIdClient(
    internal val requestResolverFactory: RequestResolverFactory,
    internal val requestProcessorFactory: RequestProcessorFactory,
    internal val logger: WalletLibraryLogger,
    private val serializer: Json,
    private val rootOfTrustResolver: RootOfTrustResolver? = null
) {

    // Creates an issuance or presentation request based on the provided input.
    suspend fun createRequest(verifiedIdRequestInput: VerifiedIdRequestInput): VerifiedIdResult<VerifiedIdRequest<*>> {
        return getResult {
            VerifiableCredentialSdk.correlationVectorService.startNewFlowAndSave()
            val requestResolver = requestResolverFactory.getResolver(verifiedIdRequestInput)
            val rawRequest = requestResolver.resolve(verifiedIdRequestInput, rootOfTrustResolver)
            val requestHandler = requestProcessorFactory.getHandler(rawRequest)
            requestHandler.handleRequest(rawRequest, rootOfTrustResolver)
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
            MalformedInputException(
                "Malformed Input Exception",
                VerifiedIdExceptions.MALFORMED_INPUT_EXCEPTION.value,
                exception
            ).toVerifiedIdResult()
        }
    }

    fun decode(encodedVerifiedIdString: String): VerifiedIdResult<VerifiedId> {
        return try {
            when (val verifiedId: VerifiedId = serializer.decodeFromString(encodedVerifiedIdString)) {
                is VerifiableCredential -> VerifiedIdResult.success(verifiedId)
                else -> MalformedInputException(
                    "Malformed Input Exception",
                    VerifiedIdExceptions.MALFORMED_INPUT_EXCEPTION.value,
                    Exception("Unknown type: ${verifiedId.javaClass.name}")
                ).toVerifiedIdResult()
            }
        } catch (exception: SerializationException) {
            SdkLog.i("Decoding to verified ID failed with ${exception.javaClass.name}, so attempting to decode it to Verifiable Credential.")
            val verifiableCredential =
                serializer.decodeFromString<com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential>(
                    encodedVerifiedIdString
                )
            val vc = serializer.decodeFromString<com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential>(
                serializer.encodeToString(verifiableCredential)
            )
            VerifiedIdResult.success(VerifiableCredential(vc))
        }
    }

    fun decodeVerifiedId(encodedVerifiedId: String): VerifiedIdResult<VerifiedId> {
        return try {
            VerifiedIdResult.success(serializer.decodeFromString(encodedVerifiedId))
        } catch (exception: Exception) {
            MalformedInputException(
                "Malformed Input Exception",
                VerifiedIdExceptions.MALFORMED_INPUT_EXCEPTION.value,
                exception
            ).toVerifiedIdResult()
        }
    }
}