/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.credential.service.protectors

import android.util.Base64
import com.microsoft.walletlibrary.did.sdk.credential.service.IssuanceResponse
import com.microsoft.walletlibrary.did.sdk.credential.service.RequestedAccessTokenMap
import com.microsoft.walletlibrary.did.sdk.credential.service.RequestedIdTokenMap
import com.microsoft.walletlibrary.did.sdk.credential.service.RequestedSelfAttestedClaimMap
import com.microsoft.walletlibrary.did.sdk.credential.service.RequestedVcMap
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.AttestationClaimModel
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.IssuanceResponseClaims
import com.microsoft.walletlibrary.did.sdk.crypto.CryptoOperations
import com.microsoft.walletlibrary.did.sdk.crypto.DigestAlgorithm
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.identifier.HolderIdentifier
import com.microsoft.walletlibrary.identifier.JWKRepresentation
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class IssuanceResponseFormatter @Inject constructor(
    private val serializer: Json,
    private val verifiablePresentationFormatter: VerifiablePresentationFormatter
) {

    fun formatResponse(
        requestedVcMap: RequestedVcMap = mutableMapOf(),
        issuanceResponse: IssuanceResponse,
        responder: HolderIdentifier,
        expiryInSeconds: Int
    ): String {
        val (issuedTime, expiryTime) = createIssuedAndExpiryTime(expiryInSeconds)
        val responseId = UUID.randomUUID().toString()
        val attestationResponse = this.createAttestationClaimModel(
            requestedVcMap,
            issuanceResponse.requestedIdTokenMap,
            issuanceResponse.requestedAccessTokenMap,
            issuanceResponse.requestedSelfAttestedClaimMap,
            issuanceResponse.request.entityIdentifier,
            responder
        )
        return createAndSignOidcResponseContent(issuanceResponse, responder, issuedTime, expiryTime, responseId, attestationResponse)
    }

    private fun createAndSignOidcResponseContent(
        issuanceResponse: IssuanceResponse,
        responder: HolderIdentifier,
        issuedTime: Long,
        expiryTime: Long,
        responseId: String,
        attestationResponse: AttestationClaimModel
    ): String {
        val publicKey = (responder as JWKRepresentation).getPublicKey()
        val contents = IssuanceResponseClaims(issuanceResponse.request.contractUrl, attestationResponse).apply {
            subject = publicKey.computeThumbprint().toString()
            audience = issuanceResponse.audience
            did = responder.id
            pin = hashIssuancePin(issuanceResponse)
            publicKeyJwk = publicKey
            responseCreationTime = issuedTime
            responseExpirationTime = expiryTime
            this.responseId = responseId
        }
        return signContents(contents, responder)
    }

    private fun signContents(contents: IssuanceResponseClaims, responder: HolderIdentifier): String {
        val serializedResponseContent = serializer.encodeToString(IssuanceResponseClaims.serializer(), contents)
        val jwsHeader = JwsHeaderFormatter.formatHeader(responder)
        val jwsToken = JwsToken(serializedResponseContent, jwsHeader)
        return jwsToken.sign(responder)
    }

    private fun createAttestationClaimModel(
        requestedVcMap: RequestedVcMap,
        requestedIdTokenMap: RequestedIdTokenMap,
        requestedAccessTokenMap: RequestedAccessTokenMap,
        requestedSelfAttestedClaimMap: RequestedSelfAttestedClaimMap,
        presentationsAudience: String,
        responder: HolderIdentifier
    ): AttestationClaimModel {
        if (areNoCollectedClaims(requestedVcMap, requestedIdTokenMap, requestedAccessTokenMap, requestedSelfAttestedClaimMap)) {
            return AttestationClaimModel()
        }
        val presentationAttestations = createPresentations(requestedVcMap, presentationsAudience, responder)
        return AttestationClaimModel(requestedSelfAttestedClaimMap, requestedIdTokenMap, requestedAccessTokenMap, presentationAttestations)
    }

    private fun createPresentations(requestedVcMap: RequestedVcMap, audience: String, responder: HolderIdentifier): Map<String, String> {
        return requestedVcMap.map { (inputDescriptor, vc) ->
            inputDescriptor.credentialType to verifiablePresentationFormatter.createPresentation(
                vc,
                inputDescriptor.validityInterval,
                audience,
                responder
            )
        }.toMap()
    }

    private fun areNoCollectedClaims(
        requestedVcMap: RequestedVcMap,
        requestedIdTokenMap: RequestedIdTokenMap,
        requestedAccessTokenMap: RequestedAccessTokenMap,
        requestedSelfAttestedClaimMap: RequestedSelfAttestedClaimMap
    ): Boolean {
        return (requestedVcMap.isNullOrEmpty() && requestedIdTokenMap.isNullOrEmpty()
            && requestedAccessTokenMap.isNullOrEmpty() && requestedSelfAttestedClaimMap.isNullOrEmpty())
    }

    private fun hashIssuancePin(response: IssuanceResponse): String? {
        val pinValueToHash = (response.issuancePin?.pinSalt ?: "") + (response.issuancePin?.pin ?: "")
        if (pinValueToHash.isEmpty()) return null
        return Base64.encodeToString(
            CryptoOperations.digest(pinValueToHash.toByteArray(), DigestAlgorithm.Sha256),
            Base64.NO_WRAP
        )
    }
}