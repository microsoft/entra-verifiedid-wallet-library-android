/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.resolvers

import android.net.Uri
import com.microsoft.walletlibrary.networking.operations.FetchOpenID4VCIRequestNetworkOperation
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestInput
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.util.Constants
import com.microsoft.walletlibrary.util.Constants.OPENID4VCI_INTER_OP_PROFILE
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.OpenId4VciRequestException
import com.microsoft.walletlibrary.util.PreviewFeatureFlags
import com.microsoft.walletlibrary.util.RequestURIMissingException
import com.microsoft.walletlibrary.util.UnSupportedVerifiedIdRequestInputException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.wrapper.OpenIdResolver
import org.json.JSONObject

/**
 * Implementation of RequestResolver specific to OIDCRequestHandler and VerifiedIdRequestURL as RequestInput.
 * It can resolve a VerifiedIdRequestInput and return a OIDC raw request.
 */
internal class OpenIdURLRequestResolver(val libraryConfiguration: LibraryConfiguration, private var preferHeader: List<String>) :
    RequestResolver {

    // Indicates whether this resolver can resolve the provided input.
    override fun canResolve(verifiedIdRequestInput: VerifiedIdRequestInput): Boolean {
        if (verifiedIdRequestInput !is VerifiedIdRequestURL) return false
        if (verifiedIdRequestInput.url.scheme == Constants.OPENID_SCHEME) return true
        return false
    }

    // Resolves the provided input and returns a raw request.
    override suspend fun resolve(verifiedIdRequestInput: VerifiedIdRequestInput): Any {
        if (verifiedIdRequestInput !is VerifiedIdRequestURL) throw UnSupportedVerifiedIdRequestInputException(
            "Provided VerifiedIdRequestInput is not supported."
        )
        return resolveOpenId4VCIRequest(verifiedIdRequestInput)
    }

    private suspend fun resolveOpenId4VCIRequest(verifiedIdRequestInput: VerifiedIdRequestURL): Any {
        val requestUri = getRequestUri(verifiedIdRequestInput.url)
            ?: throw RequestURIMissingException("Request URI is not provided in ${verifiedIdRequestInput.url}.")
        fetchOpenID4VCIRequest(requestUri)
            .onSuccess { requestPayload ->
                return try {
                    // Checks if the decoded string is a valid json, If not, fallback to old issuance flow.
                    val requestPayloadString = requestPayload.decodeToString()
                    JSONObject(requestPayloadString)
                    requestPayloadString
                } catch (e: Exception) {
                    OpenIdResolver.validateSignedRequest(requestPayload.decodeToString())
                }
            }
            .onFailure {
                throw OpenId4VciRequestException(
                    "Request fetch failed because of ${it.message}.",
                    VerifiedIdExceptions.CREDENTIAL_OFFER_FETCH_EXCEPTION.value,
                    it as Exception
                )
            }
        throw OpenId4VciRequestException(
            "Request fetch failed.",
            VerifiedIdExceptions.CREDENTIAL_OFFER_FETCH_EXCEPTION.value
        )
    }

    private fun getRequestUri(uri: Uri): String? {
        var requestUriParameter = uri.getQueryParameter(Constants.REQUEST_URI)
        if (requestUriParameter == null)
            requestUriParameter = uri.getQueryParameter(Constants.CREDENTIAL_OFFER_URI)
        return requestUriParameter
    }

    private suspend fun fetchOpenID4VCIRequest(url: String): Result<ByteArray> {
        if (libraryConfiguration.isPreviewFeatureEnabled(PreviewFeatureFlags.FEATURE_FLAG_OPENID4VCI_ACCESS_TOKEN)
            || libraryConfiguration.isPreviewFeatureEnabled(PreviewFeatureFlags.FEATURE_FLAG_OPENID4VCI_PRE_AUTH)
        ) {
            val headers = preferHeader.toMutableList()
            headers.add(OPENID4VCI_INTER_OP_PROFILE)
            preferHeader = headers
        }
        return FetchOpenID4VCIRequestNetworkOperation(
            url,
            preferHeader,
            libraryConfiguration.httpAgentApiProvider
        ).fire()
    }
}