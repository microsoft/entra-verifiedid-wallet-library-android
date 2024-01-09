/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.handlers

import android.content.Context
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.TokenSigner
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.mappings.issuance.toVerifiedIdStyle
import com.microsoft.walletlibrary.requests.ManifestIssuanceRequest
import com.microsoft.walletlibrary.requests.OpenIdPresentationRequest
import com.microsoft.walletlibrary.requests.PresentationRequestContent
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.requests.rawrequests.RawManifest
import com.microsoft.walletlibrary.requests.rawrequests.RawRequest
import com.microsoft.walletlibrary.requests.rawrequests.RequestType
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.util.InputCastingException
import com.microsoft.walletlibrary.util.RequirementCastingException
import com.microsoft.walletlibrary.util.UnSupportedProtocolException
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.microsoft.walletlibrary.wrapper.ManifestResolver
import kotlinx.serialization.json.Json

/**
 * OIDC protocol specific implementation of RequestHandler. It can handle OpenID raw request and returns a VerifiedIdRequest.
 */
internal class OpenIdRequestHandler (context: Context, private val json: Json) : RequestHandler {

    private val signer = TokenSigner(EncryptedKeyStore(context))

    override suspend fun handleRequest(rawRequest: RawRequest, rootOfTrustResolver: RootOfTrustResolver?): VerifiedIdRequest<*> {
        if (rawRequest !is OpenIdRawRequest)
            throw UnSupportedProtocolException("Received a raw request of unsupported protocol")
        val presentationRequestContent = rawRequest.mapToPresentationRequestContent()
        addSelfSignToVerifiedIdRequirements(presentationRequestContent.requirement)
        if (rawRequest.requestType == RequestType.ISSUANCE)
            return handleIssuanceRequest(presentationRequestContent, rootOfTrustResolver)
        return handlePresentationRequest(presentationRequestContent, rawRequest)
    }

    private fun handlePresentationRequest(
        presentationRequestContent: PresentationRequestContent,
        rawRequest: OpenIdRawRequest
    ): VerifiedIdRequest<Unit> {
        return OpenIdPresentationRequest(
            presentationRequestContent.requesterStyle,
            presentationRequestContent.requirement,
            presentationRequestContent.rootOfTrust,
            rawRequest
        )
    }

    private suspend fun handleIssuanceRequest(
        presentationRequestContent: PresentationRequestContent,
        rootOfTrustResolver: RootOfTrustResolver? = null
    ): VerifiedIdRequest<VerifiedId> {
        validateRequirement(presentationRequestContent)
        val contractUrl =
            ((presentationRequestContent.requirement as VerifiedIdRequirement).issuanceOptions.first() as VerifiedIdRequestURL).url
        val rawManifest = getIssuanceRequest(
            contractUrl.toString(),
            presentationRequestContent.requestState,
            presentationRequestContent.issuanceCallbackUrl,
            rootOfTrustResolver
        )
        val issuanceRequestContent = rawManifest.mapToIssuanceRequestContent()
        presentationRequestContent.injectedIdToken?.let {
            issuanceRequestContent.addRequirementsForIdTokenHint(
                it
            )
        }
        return ManifestIssuanceRequest(
            issuanceRequestContent.requesterStyle,
            issuanceRequestContent.requirement,
            issuanceRequestContent.rootOfTrust,
            rawManifest.rawRequest.contract.display.toVerifiedIdStyle(),
            rawManifest,
            presentationRequestContent.issuanceCallbackUrl,
            presentationRequestContent.requestState
        )
    }

    private fun validateRequirement(requestContent: PresentationRequestContent) {
        if (requestContent.requirement !is VerifiedIdRequirement)
            throw RequirementCastingException("Requirement is not the expected VerifiedId Requirement")
        if ((requestContent.requirement as VerifiedIdRequirement).issuanceOptions.first() !is VerifiedIdRequestURL)
            throw InputCastingException("VerifiedId Input is not the expected VerifiedIdRequestURL type")
    }

    private suspend fun getIssuanceRequest(
        contractUrl: String,
        requestState: String?,
        issuanceCallbackUrl: String?,
        rootOfTrustResolver: RootOfTrustResolver? = null
    ): RawManifest {
        return ManifestResolver.getIssuanceRequest(contractUrl, requestState, issuanceCallbackUrl, rootOfTrustResolver)
    }

    // mutates VerifiedId Requirement to use add signer and json serializers
    private fun addSelfSignToVerifiedIdRequirements(requirement: Requirement) {
        when (requirement) {
            is GroupRequirement -> {
                requirement.requirements.forEach { childRequirement ->
                    addSelfSignToVerifiedIdRequirements(childRequirement)
                }
            }
            is VerifiedIdRequirement -> {
                requirement.signer = signer
                requirement.json = json
            }
        }
    }
}