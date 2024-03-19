// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.networking.formatters

import android.util.Base64
import com.microsoft.walletlibrary.did.sdk.crypto.CryptoOperations
import com.microsoft.walletlibrary.did.sdk.crypto.DigestAlgorithm
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOffer
import com.microsoft.walletlibrary.networking.entities.openid4vci.request.OpenID4VCIJWTProof
import com.microsoft.walletlibrary.networking.entities.openid4vci.request.OpenID4VCIJWTProofClaims
import com.microsoft.walletlibrary.networking.entities.openid4vci.request.RawOpenID4VCIRequest
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.wrapper.IdentifierManager
import java.nio.charset.StandardCharsets

internal class OpenId4VciIssuanceRequestFormatter(private val libraryConfiguration: LibraryConfiguration) {
    suspend fun format(credentialOffer: CredentialOffer, credentialEndpoint: String, accessToken: String): RawOpenID4VCIRequest {
        val configurationId = credentialOffer.credential_configuration_ids.first()
        val jwtProof = formatProofAndSign(credentialOffer, credentialEndpoint, accessToken)
        val proof = OpenID4VCIJWTProof("jwt", jwt = jwtProof)
        return RawOpenID4VCIRequest(configurationId, credentialOffer.issuer_session, proof)
    }

    private suspend fun formatProofAndSign(credentialOffer: CredentialOffer, credentialEndpoint: String, accessToken: String): String {
        val identifier = IdentifierManager.getMasterIdentifier()
        val signingKeys = libraryConfiguration.keyStore.getKey(identifier.signatureKeyReference)
        val accessTokenHash =
            Base64.encodeToString(CryptoOperations.digest(accessToken.toByteArray(StandardCharsets.UTF_8), DigestAlgorithm.Sha256), Constants.BASE64_URL_SAFE)
        val claims = OpenID4VCIJWTProofClaims(
            aud = credentialEndpoint,
            iat = (System.currentTimeMillis()/1000).toString(),
            sub = identifier.id,
            at_hash = accessTokenHash
        )
        return signContents(claims, identifier)
    }

    private fun signContents(contents: OpenID4VCIJWTProofClaims, responder: Identifier): String {
        val serializedResponseContent = libraryConfiguration.serializer.encodeToString(OpenID4VCIJWTProofClaims.serializer(), contents)
        return libraryConfiguration.signer.signWithIdentifier(serializedResponseContent, responder, "openid4vci-proof+jwt")
    }
}