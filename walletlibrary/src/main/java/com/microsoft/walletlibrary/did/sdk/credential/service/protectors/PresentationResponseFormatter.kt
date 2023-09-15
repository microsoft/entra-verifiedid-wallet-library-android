/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.credential.service.protectors

import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential
import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationResponse
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.PresentationResponseClaims
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.VpTokenInResponse
import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.PresentationSubmission
import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.PresentationSubmissionDescriptor
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.Constants.DEFAULT_VP_EXPIRATION_IN_SECONDS
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PresentationResponseFormatter @Inject constructor(
    private val serializer: Json,
    private val verifiablePresentationFormatter: VerifiablePresentationFormatter,
    private val signer: TokenSigner
    ) {
    fun formatResponse(
        request: PresentationRequest,
        presentationResponse: PresentationResponse,
        responder: Identifier,
        expiryInSeconds: Int = Constants.DEFAULT_EXPIRATION_IN_SECONDS
    ): Pair<String, String> {
        val (id_token, vp_tokens) = this.formatResponses(
            request,
            listOf(presentationResponse),
            responder,
            expiryInSeconds
        )
        return Pair(id_token, vp_tokens.first())
    }

    fun formatResponses(
        request: PresentationRequest,
        presentationResponses: List<PresentationResponse>,
        responder: Identifier,
        expiryInSeconds: Int = Constants.DEFAULT_EXPIRATION_IN_SECONDS
    ): Pair<String, List<String>> {
        val (issuedTime, expiryTime) = createIssuedAndExpiryTime(expiryInSeconds)
        val multipleVPs = presentationResponses.size > 1
        val vpTokens = presentationResponses.mapIndexed {
            index, it ->
            if (multipleVPs) {
                createAttestationsAndPresentationSubmission(it, index)
            } else {
                createAttestationsAndPresentationSubmission(it)
            }
        }.map {
            VpTokenInResponse(it)
        }
        val vpClaims = PresentationResponseClaims(vpTokens)

        val oidcResponseClaims = vpClaims.apply {
            subject = responder.id
            audience = request.content.clientId
            nonce = request.content.nonce
            responseCreationTime = issuedTime
            responseExpirationTime = expiryTime
        }

        val attestationResponse = createPresentations(
            presentationResponses,
            request.content.clientId,
            responder,
            request.content.nonce
        )

        val idToken = signContents(oidcResponseClaims, responder)
        return Pair(idToken, attestationResponse)
    }

    private fun createAttestationsAndPresentationSubmission(presentationResponse: PresentationResponse, index: Int? = null): PresentationSubmission {
        presentationResponse.requestedVcPresentationSubmissionMap.entries.indices
        val credentialPresentationSubmissionDescriptors =
            presentationResponse.requestedVcPresentationSubmissionMap.map { pair ->
                PresentationSubmissionDescriptor(
                    pair.key.id,
                    Constants.VERIFIABLE_PRESENTATION_FORMAT,
                    if (index != null) { "$[$index]" } else { "$" },
                    PresentationSubmissionDescriptor(
                        pair.key.id,
                        Constants.VERIFIABLE_CREDENTIAL_FORMAT,
                        "${Constants.CREDENTIAL_PATH_IN_RESPONSE}${
                            presentationResponse.requestedVcPresentationSubmissionMap.toList().indexOf(Pair(pair.key, pair.value))
                        }]"
                    )
                )
            }
        val presentationSubmissionId = UUID.randomUUID().toString()
        return PresentationSubmission(
            presentationSubmissionId,
            presentationResponse.requestedVcPresentationDefinitionId,
            credentialPresentationSubmissionDescriptors
        )
    }

    private fun createPresentations(
        presentationResponses: List<PresentationResponse>,
        audience: String,
        responder: Identifier,
        nonce: String
    ): List<String> {
        return presentationResponses.map { response ->
            verifiablePresentationFormatter.createPresentation(
                response.requestedVcPresentationSubmissionMap.values.toList<VerifiableCredential>(),
                DEFAULT_VP_EXPIRATION_IN_SECONDS,
                audience,
                responder,
                nonce
            )
        }
    }

    private fun signContents(contents: PresentationResponseClaims, responder: Identifier): String {
        val serializedResponseContent = serializer.encodeToString(PresentationResponseClaims.serializer(), contents)
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }
}