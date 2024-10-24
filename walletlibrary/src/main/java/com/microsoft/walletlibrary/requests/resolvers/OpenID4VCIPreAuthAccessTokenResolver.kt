// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.requests.resolvers

import com.microsoft.walletlibrary.did.sdk.util.controlflow.ForbiddenException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.InvalidPinException
import com.microsoft.walletlibrary.networking.entities.openid4vci.request.OpenID4VCIPreAuthTokenRequest
import com.microsoft.walletlibrary.networking.operations.PostOpenID4VCIPreAuthNetworkOperation
import com.microsoft.walletlibrary.requests.requirements.OpenId4VCIPinRequirement
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.OpenId4VciValidationException
import com.microsoft.walletlibrary.util.RequirementValidationException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions

/**
 * Resolves and fulfills the access token for Pre Auth flow.
 */
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

                if (it is ForbiddenException) {
                    // Based on error message, determine if the error is retriable and how many more times.
                    it.errorBody?.let { errorBody ->
                        pinMismatchRegex.find(errorBody)?.let { match ->
                            match.groups[1]?.value?.toIntOrNull()?.let { attempts ->
                                innerException = InvalidPinException(
                                    "Entered PIN does not match expectations.",
                                    attempts > 0,
                                    attempts
                                )
                            }
                        }
                    }

                    // Even if no more attempts are possible, forbidden means a PIN error.
                    if (innerException !is InvalidPinException) {
                        innerException = InvalidPinException(
                            "Failed to validate PIN.",
                            false
                        )
                    }
                }

                throw RequirementValidationException(
                    "Failed to fetch access token for Pre Auth flow",
                    innerException,
                    (innerException as? InvalidPinException)?.retryable ?: false
                )
            }
    }
}