package com.microsoft.walletlibrary.did.sdk.credential.service.protectors

import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential
import com.microsoft.walletlibrary.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.walletlibrary.did.sdk.credential.service.models.verifiablePresentation.VerifiablePresentationContent
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.identifier.HolderIdentifier
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class VerifiablePresentationFormatterTest {

    private val mockedVerifiableCredential: VerifiableCredential = mockk()
    private val mockedPresentationAttestation: PresentationAttestation = mockk()
    private val mockedIdentifier: HolderIdentifier = mockk()
    private val slot = slot<ByteArray>()
    private val serializer: Json = Json

    private val formatter: VerifiablePresentationFormatter = VerifiablePresentationFormatter(serializer)

    private val signingKeyRef: String = "sigKeyRef1243523"
    private val expectedDid: String = "did:test:2354543"
    private val expectedAudience: String = "audience2432"
    private val expectedRawVerifiableCredential: String = "raw24237"
    private val expectedPresentationContext = listOf(Constants.VP_CONTEXT_URL)
    private val expectedPresentationType = listOf(Constants.VERIFIABLE_PRESENTATION_TYPE)

    init {
        every { mockedIdentifier.id } returns expectedDid
        every { mockedIdentifier.keyReference } returns signingKeyRef
        every { mockedIdentifier.algorithm } returns "ES256K"
        every { mockedIdentifier.sign(capture(slot)) } answers { slot.captured }
        every { mockedVerifiableCredential.raw } returns expectedRawVerifiableCredential
    }

    @Test
    fun `create presentation`() {
        val expectedValidityInterval = 2343
        every { mockedPresentationAttestation.validityInterval } returns expectedValidityInterval
        val results = formatter.createPresentation(mockedVerifiableCredential, expectedValidityInterval, expectedAudience, mockedIdentifier)
        val contents = serializer.decodeFromString(VerifiablePresentationContent.serializer(), JwsToken.deserialize(results).content())
        assertEquals(expectedAudience, contents.audience)
        assertEquals(expectedDid, contents.issuerOfVp)
        assertEquals(expectedPresentationContext, contents.verifiablePresentation.context)
        assertEquals(expectedPresentationType, contents.verifiablePresentation.type)
        assertEquals(listOf(expectedRawVerifiableCredential), contents.verifiablePresentation.verifiableCredential)
    }
}