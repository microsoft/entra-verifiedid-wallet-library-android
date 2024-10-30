// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.requests.resolvers

import com.microsoft.walletlibrary.did.sdk.util.controlflow.ForbiddenException
import com.microsoft.walletlibrary.networking.entities.openid4vci.request.OpenID4VCIPreAuthTokenRequest
import com.microsoft.walletlibrary.networking.operations.PostOpenID4VCIPreAuthNetworkOperation
import com.microsoft.walletlibrary.requests.requirements.OpenId4VCIPinRequirement
import com.microsoft.walletlibrary.util.InvalidPinAttemptException
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.OpenId4VciValidationException
import com.microsoft.walletlibrary.util.RequirementNotMetException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions

internal class OpenID4VCIPreAuthAccessTokenResolver(val libraryConfiguration: LibraryConfiguration) {
    companion object {
        private val pinMismatchRegex = "Invalid PIN\\. You can try ([0-9]+) more times\\.".toRegex()
    }

    suspend fun resolve(
        preAuthorizedCode: String?,
        openId4VCIPinRequirement: OpenId4VCIPinRequirement,
        accessTokenEndpoint: String
    ) {
        if (preAuthorizedCode == null) {
            throw OpenId4VciValidationException(
                "pre authorization code is not set.",
                VerifiedIdExceptions.INVALID_PROPERTY_EXCEPTION.value
            )
        }
        PostOpenID4VCIPreAuthNetworkOperation(
            accessTokenEndpoint,
            OpenID4VCIPreAuthTokenRequest(
                "urn:ietf:params:oauth:grant-type:pre-authorized_code",
                preAuthorizedCode,
                openId4VCIPinRequirement.pin
            ),
            libraryConfiguration.httpAgentApiProvider,
            libraryConfiguration.serializer
        ).fire()
            .onSuccess { openID4VCIPreAuthTokenResponse ->
                openID4VCIPreAuthTokenResponse.access_token?.let {
                    openId4VCIPinRequirement.fulfillAccessToken(
                        it
                    )
                } ?: throw OpenId4VciValidationException(
                    "Access token retrieval failed for Pre Auth flow.",
                    VerifiedIdExceptions.INVALID_PROPERTY_EXCEPTION.value
                )
            }
            .onFailure {
                var innerException = it as Exception

                // PIN related errors are 403.
                if (it is ForbiddenException) {
                    // Based on error message, determine if the error is retriable and how many more times.
                    it.errorBody?.let { errorBody ->
                        pinMismatchRegex.find(errorBody)?.let { match ->
                            match.groups[1]?.value?.toIntOrNull()?.let { attempts ->
                                innerException = InvalidPinAttemptException(
                                    "Entered PIN does not match expectations.",
                                    it,
                                    attempts > 0,
                                    attempts
                                )
                            }
                        }
                    }

                    // Even if no more attempts are possible, forbidden means a PIN error.
                    if (innerException !is InvalidPinAttemptException) {
                        innerException = InvalidPinAttemptException(
                            "Failed to validate PIN.",
                            it
                        )
                    }
                }

                throw RequirementNotMetException(
                    "Failed to fetch access token for Pre Auth flow",
                    VerifiedIdExceptions.REQUIREMENT_NOT_MET_EXCEPTION.value,
                    listOf(innerException)
                )
            }
    }
}