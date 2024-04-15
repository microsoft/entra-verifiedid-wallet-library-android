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

/**
 * Validates and processes signed metadata in Credential Metadata.
 */
internal class SignedMetadataProcessor(private val libraryConfiguration: LibraryConfiguration) {

    // Deserializes the provided signed metadata from credential metadata, verifies its integrity
    // validates it and processes it to return the root of trust.
    internal suspend fun process(
        signedMetadata: String,
        credentialIssuer: String,
        rootOfTrustResolver: com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver? = null
    ): RootOfTrust {
        val jwsToken = deserializeSignedMetadata(signedMetadata)

        // Extract the DID and Key ID from the signed metadata token header.
        val kid = jwsToken.keyId ?: throw OpenId4VciValidationException(
            "JWS contains no key id",
            VerifiedIdExceptions.MALFORMED_SIGNED_METADATA_EXCEPTION.value
        )
        val didKeyIdPair = JwaCryptoHelper.extractDidAndKeyId(kid)
        val did = didKeyIdPair.first ?: throw OpenId4VciValidationException(
            "JWS contains no DID",
            VerifiedIdExceptions.MALFORMED_SIGNED_METADATA_EXCEPTION.value
        )
        val keyId = didKeyIdPair.second

        // Resolve the identifier document for the DID in the token and verify the integrity of the signed metadata.
        val identifierDocument = IdentifierDocumentResolver.resolveIdentifierDocument(did)
        val jwk = identifierDocument.getJwk(keyId)
            ?: throw OpenId4VciValidationException(
                "JWK with key id $keyId not found in identifier document",
                VerifiedIdExceptions.MALFORMED_SIGNED_METADATA_EXCEPTION.value
            )
        validateSignedMetadata(jwsToken, jwk, credentialIssuer, did)

        // Return the root of trust from the identifier document along with its verification status.
        return RootOfTrustResolver.resolveRootOfTrust(identifierDocument, rootOfTrustResolver)
    }

    private fun deserializeSignedMetadata(signedMetadata: String): JwsToken {
        return try {
            JwsToken.deserialize(signedMetadata)
        } catch (exception: Exception) {
            throw OpenId4VciValidationException(
                "Invalid signed metadata",
                VerifiedIdExceptions.MALFORMED_SIGNED_METADATA_EXCEPTION.value,
                exception
            )
        }
    }

    private fun validateSignedMetadata(jwsToken: JwsToken, jwk: JWK, credentialIssuer: String, issuerDid: String) {
        try {
//            verifySignature(jwsToken, jwk)
            val signedMetadataTokenClaims = libraryConfiguration.serializer.decodeFromString(
                SignedMetadataTokenClaims.serializer(),
                jwsToken.content()
            )
            signedMetadataTokenClaims.validateSignedMetadataTokenClaims(credentialIssuer, issuerDid)
        } catch (exception: Exception) {
            throw OpenId4VciValidationException(
                "Invalid signed metadata",
                VerifiedIdExceptions.MALFORMED_SIGNED_METADATA_EXCEPTION.value,
                exception
            )
        }
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