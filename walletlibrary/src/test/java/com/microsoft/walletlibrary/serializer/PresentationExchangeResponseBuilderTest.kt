package com.microsoft.walletlibrary.serializer

import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.InputContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.CardDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.ConsentDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.TokenSigner
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.requests.requirements.PresentationExchangeVerifiedIdRequirement
import com.microsoft.walletlibrary.requests.serializer.PresentationExchangeResponseBuilder
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.serialization.json.Json
import java.util.Date
import kotlin.test.assertTrue


internal class PresentationExchangeResponseBuilderTest {
    val signer = mockk<TokenSigner>()
    val payload = slot<String>()
    val subject = slot<Identifier>()
    val serializer = Json
    val libraryConfiguration = mockk<LibraryConfiguration>()
    val signedPayload = "expectedSignature${(0..20).random()}"

    fun makeVerifiedId (sub: String): VerifiableCredential {
        return VerifiableCredential(
            com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential(
                "",
                "",
                VerifiableCredentialContent(
                    "",
                    VerifiableCredentialDescriptor(emptyList(), listOf("TestCredential"), emptyMap()),
                    sub,
                    sub,
                    Date().time
                )
            ), VerifiableCredentialContract(
                sub,
                InputContract(
                    credentialIssuer = sub,
                    issuer = sub,
                ),
                DisplayContract(
                    card = CardDescriptor("", "", "", "", null, ""),
                    consent = ConsentDescriptor(instructions = ""),
                    claims = emptyMap()
                )
            )
        )
    }

    val identifier: Identifier = Identifier(
        "did:example:mockabcdefghi1234567890",
        "sig",
        "enc",
        "sig",
        "sig",
        "mock"
    )
    val correctVerifiedId = makeVerifiedId(identifier.id)
    val defaultRequirement = PresentationExchangeVerifiedIdRequirement(
        id = "recOne",
        types = listOf("TestCredential"),
        inputDescriptorId = "1"
    )

    init {
        every { libraryConfiguration.serializer } returns serializer
        every { libraryConfiguration.tokenSigner } returns signer
        every { signer.signWithIdentifier( capture(payload), capture(subject) )} returns signedPayload
        assertTrue(defaultRequirement.fulfill(correctVerifiedId).isSuccess)
    }

    fun serialize_singleRequirement_traversesRequirement() {
        // Arrange
        val builder = PresentationExchangeResponseBuilder(libraryConfiguration)

    }
}