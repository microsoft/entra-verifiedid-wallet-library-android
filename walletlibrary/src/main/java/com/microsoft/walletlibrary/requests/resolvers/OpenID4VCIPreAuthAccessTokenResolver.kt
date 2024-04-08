// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.requests.resolvers

import com.microsoft.walletlibrary.networking.entities.openid4vci.request.OpenID4VCIPreAuthTokenRequest
import com.microsoft.walletlibrary.networking.operations.PostOpenID4VCIPreAuthNetworkOperation
import com.microsoft.walletlibrary.requests.requirements.OpenId4VCIPinRequirement
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.OpenId4VciRequestException
import com.microsoft.walletlibrary.util.OpenId4VciValidationException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions

internal class OpenID4VCIPreAuthAccessTokenResolver(val libraryConfiguration: LibraryConfiguration) {
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
                throw OpenId4VciRequestException(
                    "Failed to fetch access token for Pre Auth flow",
                    VerifiedIdExceptions.REQUEST_CREATION_EXCEPTION.value,
                    it as Exception
                )
            }
    }
}