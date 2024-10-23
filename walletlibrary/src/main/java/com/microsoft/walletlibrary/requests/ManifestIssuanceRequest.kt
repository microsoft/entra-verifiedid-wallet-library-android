/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.did.sdk.credential.service.models.issuancecallback.IssuanceCompletionResponse
import com.microsoft.walletlibrary.requests.rawrequests.RawManifest
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdStyle
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.VerifiedIdException
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.util.getResult
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.microsoft.walletlibrary.wrapper.VerifiedIdRequester

/**
 * Issuance request specific to Manifest/Contract.
 */
internal class ManifestIssuanceRequest(
    // Attributes describing the requester (eg. name, logo).
    override val requesterStyle: RequesterStyle,

    // Information describing the requirements needed to complete the flow.
    override val requirement: Requirement,

    // Root of trust of the requester (eg. linked domains).
    override val rootOfTrust: RootOfTrust,

    // Attributes describing the Verified ID (eg. name, issuer, logo, background and text colors).
    override val verifiedIdStyle: VerifiedIdStyle,

    val request: RawManifest,

    private var issuanceCallbackUrl: String? = null,

    private var requestState: String? = null,

    private val libraryConfiguration: LibraryConfiguration
) : VerifiedIdIssuanceRequest {
    // Completes the issuance request and returns a Result with VerifiedId if successful.
    override suspend fun complete(): VerifiedIdResult<VerifiedId> {
        val result = getResult {
            VerifiedIdRequester.sendIssuanceResponse(
                request.rawRequest,
                requirement,
                libraryConfiguration.serializer
            )
        }
        sendIssuanceCallbackIfRequestStateAndCallbackExists(requestState, result, issuanceCallbackUrl)
        return result
    }

    // Indicates whether issuance request is satisfied on client side.
    override fun isSatisfied(): Boolean {
        val validationResult = requirement.validate()
        //TODO("Add logging")
        return !validationResult.isFailure
    }

    override suspend fun cancel(message: String?): VerifiedIdResult<Unit> {
        return getResult {
            val issuanceCompletionResponse = requestState?.let {
                IssuanceCompletionResponse(
                    IssuanceCompletionResponse.IssuanceCompletionCode.ISSUANCE_FAILED,
                    it,
                    IssuanceCompletionResponse.IssuanceCompletionErrorDetails.USER_CANCELED
                )
            }
            VerifiedIdRequester.sendIssuanceCallback(
                issuanceCompletionResponse,
                issuanceCallbackUrl
            )
        }
    }

    private suspend fun sendIssuanceCallbackIfRequestStateAndCallbackExists(
        requestState: String?,
        result: VerifiedIdResult<VerifiedId>,
        issuanceCallbackUrl: String?
    ) {
        if (requestState == null || issuanceCallbackUrl == null) {
            return
        }
        var issuanceCompletionCode: IssuanceCompletionResponse.IssuanceCompletionCode =
            IssuanceCompletionResponse.IssuanceCompletionCode.ISSUANCE_FAILED
        var issuanceCompletionErrorDetails: IssuanceCompletionResponse.IssuanceCompletionErrorDetails? =
            null
        result.fold(
            onSuccess = {
                issuanceCompletionCode =
                    IssuanceCompletionResponse.IssuanceCompletionCode.ISSUANCE_SUCCESSFUL
            },
            onFailure = {
                issuanceCompletionCode =
                    IssuanceCompletionResponse.IssuanceCompletionCode.ISSUANCE_FAILED
                issuanceCompletionErrorDetails = when (it) {
                    is VerifiedIdException -> IssuanceCompletionResponse.IssuanceCompletionErrorDetails.ISSUANCE_SERVICE_ERROR
                    else -> IssuanceCompletionResponse.IssuanceCompletionErrorDetails.UNSPECIFIED_ERROR
                }
            }
        )
        val issuanceCompletionResponse = IssuanceCompletionResponse(
            issuanceCompletionCode,
            requestState,
            issuanceCompletionErrorDetails
        )
        VerifiedIdRequester.sendIssuanceCallback(
            issuanceCompletionResponse,
            issuanceCallbackUrl)
    }
}