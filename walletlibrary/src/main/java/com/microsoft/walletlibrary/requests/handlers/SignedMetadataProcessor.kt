package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.JwaCryptoHelper
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.mappings.getJwk
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.SignedMetadataTokenClaims
import com.microsoft.walletlibrary.requests.RootOfTrust
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.OpenId4VciValidationException
import com.microsoft.walletlibrary.util.TokenValidationException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.wrapper.IdentifierDocumentResolver
import com.microsoft.walletlibrary.wrapper.RootOfTrustResolver
import com.nimbusds.jose.jwk.JWK

internal class SignedMetadataProcessor(private val libraryConfiguration: LibraryConfiguration) {
    internal suspend fun process(signedMetadata: String, credentialIssuer: String): RootOfTrust {
        val jwsToken = try {
            JwsToken.deserialize(signedMetadata)
        } catch (exception: Exception) {
            throw OpenId4VciValidationException(
                "Invalid signed metadata",
                VerifiedIdExceptions.MALFORMED_SIGNED_METADATA_EXCEPTION.value,
                exception
            )
        }

        val keyId = jwsToken.keyId ?: throw OpenId4VciValidationException(
            "JWS contains no key id",
            VerifiedIdExceptions.MALFORMED_SIGNED_METADATA_EXCEPTION.value
        )
        val did =
            JwaCryptoHelper.extractDidAndKeyId(keyId).first ?: throw OpenId4VciValidationException(
                "JWS contains no DID",
                VerifiedIdExceptions.MALFORMED_SIGNED_METADATA_EXCEPTION.value
            )

        IdentifierDocumentResolver.resolveIdentifierDocument(did)
            .onSuccess { document ->
                val jwk = document.getJwk(keyId)
                jwk?.let { validateSignedMetadata(jwsToken, it, credentialIssuer) }
                return RootOfTrustResolver.resolveRootOfTrust(document)
            }
            .onFailure {
                throw OpenId4VciValidationException(
                    "Failed to resolve identifier document ${it.message}}",
                    VerifiedIdExceptions.DOCUMENT_RESOLUTION_EXCEPTION.value
                )
            }
        return RootOfTrust("", false)
    }

    private fun validateSignedMetadata(jwsToken: JwsToken, jwk: JWK, credentialIssuer: String) {
        verifySignature(jwsToken, jwk)
        val signedMetadataTokenClaims = libraryConfiguration.serializer.decodeFromString(
            SignedMetadataTokenClaims.serializer(),
            jwsToken.content()
        )
        signedMetadataTokenClaims.validateSignedMetadataTokenClaims(credentialIssuer, jwk.keyID)
    }

    private fun verifySignature(jwsToken: JwsToken, jwk: JWK) {
        if (!jwsToken.verify(listOf(jwk))) {
            throw TokenValidationException(
                "Signature is invalid on Signed metadata",
                VerifiedIdExceptions.INVALID_SIGNATURE_EXCEPTION.value
            )
        }
    }


}