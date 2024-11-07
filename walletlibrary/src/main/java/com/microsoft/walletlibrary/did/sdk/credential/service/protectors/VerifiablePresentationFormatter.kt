package com.microsoft.walletlibrary.did.sdk.credential.service.protectors

import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential
import com.microsoft.walletlibrary.did.sdk.credential.service.models.verifiablePresentation.VerifiablePresentationContent
import com.microsoft.walletlibrary.did.sdk.credential.service.models.verifiablePresentation.VerifiablePresentationDescriptor
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.identifier.HolderIdentifier
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class VerifiablePresentationFormatter @Inject constructor(
    private val serializer: Json
) {

    // only support one VC per VP
    fun createPresentation(
        verifiableCredential: VerifiableCredential,
        validityInterval: Int,
        audience: String,
        responder: HolderIdentifier
    ): String {
        val verifiablePresentation = VerifiablePresentationDescriptor(
            verifiableCredential = listOf(verifiableCredential.raw),
            context = listOf(Constants.VP_CONTEXT_URL),
            type = listOf(Constants.VERIFIABLE_PRESENTATION_TYPE)
        )

        val (issuedTime, expiryTime: Long) = createIssuedAndExpiryTime(validityInterval)
        val vpId = UUID.randomUUID().toString()
        val responderDid = responder.id
        val contents =
            VerifiablePresentationContent(
                vpId = vpId,
                verifiablePresentation = verifiablePresentation,
                issuerOfVp = responderDid,
                tokenIssuedTime = issuedTime,
                tokenNotValidBefore = issuedTime,
                tokenExpiryTime = expiryTime,
                audience = audience
            )
        val serializedContents = serializer.encodeToString(VerifiablePresentationContent.serializer(), contents)
        val jwsHeader = JwsHeaderFormatter.formatHeader(responder)
        val jwsToken = JwsToken(serializedContents, jwsHeader)
        return jwsToken.sign(responder)
    }

    // supports multiple VCs per VP
    fun createPresentation(
        verifiableCredentials: List<VerifiableCredential>,
        validityInterval: Int,
        audience: String,
        responder: HolderIdentifier,
        nonce: String
    ): String {
        val rawVerifiableCredentials = mutableListOf<String>()
        verifiableCredentials.forEach { rawVerifiableCredentials.add(it.raw) }
        val verifiablePresentation = VerifiablePresentationDescriptor(
            verifiableCredential = rawVerifiableCredentials,
            context = listOf(Constants.VP_CONTEXT_URL),
            type = listOf(Constants.VERIFIABLE_PRESENTATION_TYPE)
        )

        val (issuedTime, expiryTime: Long) = createIssuedAndExpiryTime(validityInterval)
        val vpId = UUID.randomUUID().toString()
        val responderDid = responder.id
        val contents =
            VerifiablePresentationContent(
                vpId = vpId,
                verifiablePresentation = verifiablePresentation,
                issuerOfVp = responderDid,
                tokenIssuedTime = issuedTime,
                tokenNotValidBefore = issuedTime,
                tokenExpiryTime = expiryTime,
                audience = audience,
                nonce = nonce
            )
        val serializedContents = serializer.encodeToString(VerifiablePresentationContent.serializer(), contents)
        val jwsHeader = JwsHeaderFormatter.formatHeader(responder)
        val jwsToken = JwsToken(serializedContents, jwsHeader)
        return jwsToken.sign(responder)
    }
}