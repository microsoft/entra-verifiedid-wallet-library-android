/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationResponse
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.mappings.presentation.addRequirements
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.util.IdInVerifiedIdRequirementDoesNotMatchRequestException
import com.microsoft.walletlibrary.util.OpenIdResponseCompletionException
import com.microsoft.walletlibrary.util.WalletLibraryLogger

/**
 * Wrapper class to wrap the send presentation response to VC SDK.
 */
object OpenIdResponder {

    // sends the presentation response to VC SDK and returns nothing if successful.
    internal suspend fun sendPresentationResponse(
        presentationRequest: PresentationRequest,
        requirement: Requirement,
        additionalHeaders: Map<String, String>? = null
    ) {
        val presentationResponses = presentationRequest.getPresentationDefinitions().map { PresentationResponse(presentationRequest, it.id) }
        if (presentationResponses.size == 1) {
            presentationResponses.first().addRequirements(requirement)
        } else {
            // due to multi VP format. The outer most requirement may be grouping all requirements
            (requirement as? GroupRequirement)?.let {
                    groupRequirement ->
                groupRequirement.validate().getOrThrow()
                // Each requirement maps to at most one response
                val unmatchedPresentationResponses = mutableListOf<PresentationResponse>()
                unmatchedPresentationResponses.addAll(presentationResponses)
                groupRequirement.requirements.mapIndexed {
                    index, requirement ->
                    // find/remove the first matching presentation
                    val matchingPresentationResponse = unmatchedPresentationResponses.firstOrNull {
                        presentationResponse ->
                        try {
                            presentationResponse.addRequirements(requirement)
                            true
                        } catch (exception: IdInVerifiedIdRequirementDoesNotMatchRequestException) {
                            // Expected to throw for requirements in other responses
                            WalletLibraryLogger.i("requirement $index does not match " +
                                    presentationResponse.requestedVcPresentationDefinitionId
                            )
                            false
                        }
                    } ?: throw IdInVerifiedIdRequirementDoesNotMatchRequestException()
                    unmatchedPresentationResponses.remove(matchingPresentationResponse)
                }
            }
        }
        val presentationResponseResult =
            VerifiableCredentialSdk.presentationService.sendResponse(presentationRequest, presentationResponses, additionalHeaders)
        if (presentationResponseResult is Result.Failure) {
            throw OpenIdResponseCompletionException(
                "Unable to send presentation response",
                presentationResponseResult.payload
            )
        }
    }
}