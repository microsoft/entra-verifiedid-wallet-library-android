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
import com.microsoft.walletlibrary.util.OpenId4VciValidationException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.wrapper.IdentifierManager
import java.nio.charset.StandardCharsets

internal class OpenId4VciIssuanceRequestFormatter(private val libraryConfiguration: LibraryConfiguration) {
    suspend fun format(
        credentialOffer: CredentialOffer,
        credentialEndpoint: String,
        accessToken: String
    ): RawOpenID4VCIRequest {
        if (credentialOffer.credential_configuration_ids.isEmpty()) {
            throw OpenId4VciValidationException(
                "Credential id is not present in the credential offer.",
                VerifiedIdExceptions.REQUEST_CREATION_EXCEPTION.value
            )
        }
        val configurationId = credentialOffer.credential_configuration_ids.first()
        val jwtProof = formatProofAndSign(credentialEndpoint, accessToken)
        val proof = OpenID4VCIJWTProof("jwt", jwtProof)
        return RawOpenID4VCIRequest(configurationId, credentialOffer.issuer_session, proof)
    }

    private suspend fun formatProofAndSign(
        credentialEndpoint: String,
        accessToken: String
    ): String {
        val identifier = IdentifierManager.getMasterIdentifier()
        val accessTokenHash =
            Base64.encodeToString(
                CryptoOperations.digest(
                    accessToken.toByteArray(StandardCharsets.UTF_8),
                    DigestAlgorithm.Sha256
                ),
                Constants.BASE64_URL_SAFE
            )
        val claims = OpenID4VCIJWTProofClaims(
            aud = credentialEndpoint,
            iat = (System.currentTimeMillis() / 1000).toString(),
            sub = identifier.id,
            at_hash = accessTokenHash
        )
        return signContents(claims, identifier)
    }

    private fun signContents(contents: OpenID4VCIJWTProofClaims, responder: Identifier): String {
        val serializedResponseContent = libraryConfiguration.serializer.encodeToString(
            OpenID4VCIJWTProofClaims.serializer(),
            contents
        )
        return libraryConfiguration.signer.signWithIdentifier(
            serializedResponseContent,
            responder,
            com.microsoft.walletlibrary.util.Constants.OPENID4VCI_TYPE_HEADER
        )
    }
}