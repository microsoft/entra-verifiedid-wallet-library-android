package com.microsoft.walletlibrary

import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.TokenSigner
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.identifier.IdentifierManager
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.verifiedid.OpenId4VciVerifiedId
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ExtensionIdentifierManagerTest {
    val signer = mockk<TokenSigner>()
    val payload = slot<String>()
    val subject = slot<Identifier>()
    val serializer = Json
    val libraryConfiguration = mockk<LibraryConfiguration>()
    val signedPayload = "expectedSignature${(0..20).random()}"

    val identifier: Identifier = Identifier(
        "did:example:mockabcdefghi1234567890",
        "sig",
        "enc",
        "sig",
        "sig",
        "mock"
    )

    init {
        val mockIdentifierManager = mockk<IdentifierManager>()
        coEvery { mockIdentifierManager.getMasterIdentifier() } returns Result.Success(identifier)
        every { libraryConfiguration.serializer } returns serializer
        every { libraryConfiguration.tokenSigner } returns signer
        every { libraryConfiguration.identifierManager } returns mockIdentifierManager
        every { signer.signWithIdentifier( capture(payload), capture(subject) )} returns signedPayload
    }
    @Test
    fun createEphemeralSelfSignedVerifiedId_withIdentifier_createsVerifiedID() {
        // Arrange
        val claims = mapOf(
            "test" to (0..256).random().toString()
        )
        val types = Array(1) { "TestCredential" }

        val extensionIdentifierManager = ExtensionIdentifierManager(libraryConfiguration)
        // Act
        val vc = extensionIdentifierManager.createEphemeralSelfSignedVerifiedId(claims, types)
        // Assert
        assertTrue(vc is OpenId4VciVerifiedId)
        assertEquals(vc.issuerName, "Self")
        assertEquals(vc.raw.raw, signedPayload)
        assertEquals(subject.captured, identifier)
        val vcClaims = vc.getClaims()
        assertEquals(vcClaims.count(), claims.count())
        assertEquals(vcClaims.first().id, "test")
        assertEquals(vcClaims.first().value as String, claims["test"])
        assertEquals(vc.types.count(), 2)
        assertTrue(vc.types.contains("TestCredential"))
    }
}