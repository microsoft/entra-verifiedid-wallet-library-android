/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary

import com.microsoft.walletlibrary.requests.RequestHandlerFactory
import com.microsoft.walletlibrary.requests.RequestResolverFactory
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestInput
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
            val requestResolver = requestResolverFactory.getResolver(verifiedIdRequestInput)
            val rawRequest = requestResolver.resolve(verifiedIdRequestInput)
            val requestHandler = requestHandlerFactory.getHandler(requestResolver)
            Result.success(requestHandler.handleRequest(rawRequest))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    fun encode(verifiedId: VerifiedId): Result<String> {
        return try {
            Result.success(serializer.encodeToString(verifiedId))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    fun decodeVerifiedId(encodedVerifiedId: String): Result<VerifiedId> {
        return try {
            Result.success(serializer.decodeFromString(encodedVerifiedId))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}