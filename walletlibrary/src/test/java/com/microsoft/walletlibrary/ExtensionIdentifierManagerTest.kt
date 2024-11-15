package com.microsoft.walletlibrary

import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.identifier.HolderIdentifier
import com.microsoft.walletlibrary.identifier.IdentifierFactory
import com.microsoft.walletlibrary.mappings.identifier.toHolderIdentifier
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.verifiedid.OpenId4VciVerifiedId
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ExtensionIdentifierManagerTest {
    val payload = slot<String>()
    val subject = slot<HolderIdentifier>()
    val serializer = Json
    val libraryConfiguration = mockk<LibraryConfiguration>()
    val mockIdentifierFactory = mockk<IdentifierFactory>()
    val signedPayload = "expectedSignature${(0..20).random()}"

    val identifier: Identifier = Identifier(
        "did:example:mockabcdefghi1234567890",
        "sig",
        "enc",
        "sig",
        "sig",
        "mock"
    )
    private val holderIdentifier = identifier.toHolderIdentifier(mockk())

    init {
        every { libraryConfiguration.serializer } returns serializer
        every { libraryConfiguration.identifierFactory } returns mockIdentifierFactory
        every { mockIdentifierFactory.getIdentifier() } returns holderIdentifier
    }

    @Test
    fun createEphemeralSelfSignedVerifiedId_withIdentifier_createsVerifiedID() {
        // Arrange
        val claims = mapOf(
            "test" to (0..256).random().toString()
        )
        val types = Array(1) { "TestCredential" }
        val extensionIdentifierManager = spyk(ExtensionIdentifierManager(libraryConfiguration), recordPrivateCalls = true)
        every { extensionIdentifierManager["createAndSignToken"](capture(subject), any<String>()) } returns signedPayload

        // Act
        val vc = extensionIdentifierManager.createEphemeralSelfSignedVerifiedId(claims, types)
        // Assert
        assertTrue(vc is OpenId4VciVerifiedId)
        assertEquals(vc.issuerName, "Self")
        assertEquals(vc.raw.raw, signedPayload)
        assertEquals(subject.captured, holderIdentifier)
        val vcClaims = vc.getClaims()
        assertEquals(vcClaims.count(), claims.count())
        assertEquals(vcClaims.first().id, "test")
        assertEquals(vcClaims.first().value as String, claims["test"])
        assertEquals(vc.types.count(), 2)
        assertTrue(vc.types.contains("TestCredential"))
    }
}