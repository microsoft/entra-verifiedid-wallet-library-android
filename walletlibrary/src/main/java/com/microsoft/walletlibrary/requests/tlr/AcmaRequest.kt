// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.requests.tlr

import com.microsoft.walletlibrary.networking.operations.PostAcmaRequestNetworkOperation
import com.microsoft.walletlibrary.requests.Continuation
import com.microsoft.walletlibrary.util.AcmaRequestException
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.util.getResult

internal class AcmaRequest(
    private val continuation: Continuation,
    private val libraryConfiguration: LibraryConfiguration
) : BaseAcmaRequest {
    override suspend fun complete(): VerifiedIdResult<AcmaResponse> {
        return getResult {
            formatAndSendRequest()
        }
    }

    private suspend fun formatAndSendRequest(): AcmaResponse {
        PostAcmaRequestNetworkOperation(
            continuation.url,
            continuation.payload,
            libraryConfiguration.httpAgentApiProvider,
            libraryConfiguration.serializer
        ).fire()
            .onSuccess { response ->
                return AcmaResponse(
                    PresentationRequestUrl(response.presentationRequestUrl.uri, response.presentationRequestUrl.expiryTimestamp)
                )
            }
            .onFailure {
                throw AcmaRequestException(
                    "Failed to send ACMA request. ${it.message}",
                    VerifiedIdExceptions.REQUEST_SEND_EXCEPTION.value
                )
            }

        throw AcmaRequestException(
            "Failed to send ACMA request.",
            VerifiedIdExceptions.UNSPECIFIED_EXCEPTION.value
        )
    }
}