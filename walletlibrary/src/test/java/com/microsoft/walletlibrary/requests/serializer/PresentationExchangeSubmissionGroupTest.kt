package com.microsoft.walletlibrary.requests.serializer

import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.InputContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.CardDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.ConsentDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.verifiablePresentation.VerifiablePresentationContent
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.TokenSigner
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.requests.requirements.PresentationExchangeVerifiedIdRequirement
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.serialization.json.Json
import org.junit.Test
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class PresentationExchangeSubmissionGroupTest {

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
        ))
    }

    // Arrange
    val identifier: Identifier = Identifier(
        "did:example:mockabcdefghi1234567890",
        "sig",
        "enc",
        "sig",
        "sig",
        "mock"
    )

    val correctVerifiedId = makeVerifiedId(identifier.id)

    val incorrectVerifiedId = makeVerifiedId("did:example:notTheRightIdentifier")

    val defaultRequirement = PresentationExchangeVerifiedIdRequirement(
        id = "recOne",
        types = listOf("TestCredential"),
        inputDescriptorId = "1"
    )

    val canIncludeRequirement = PresentationExchangeVerifiedIdRequirement(
        id = "recTwo",
        types = listOf("TestCredential"),
        inputDescriptorId = "2"
    )

    val exclusiveRequirement = PresentationExchangeVerifiedIdRequirement(
        id = "recThree",
        types = listOf("TestCredential"),
        inputDescriptorId = "3",
        exclusivePresentationWith = listOf("1", "2")
    )

    val cannotIncludeRequirement = PresentationExchangeVerifiedIdRequirement(
        id = "recFour",
        types = listOf("TestCredential"),
        inputDescriptorId = "4"
    )

    init {
        assertTrue(defaultRequirement.fulfill(correctVerifiedId).isSuccess)
        assertTrue(cannotIncludeRequirement.fulfill(incorrectVerifiedId).isSuccess)
    }

    @Test
    fun canIncludeInGroup_withNoGroups_returnsTrue() {
        // Arrange
        val group = PresentationExchangeSubmissionGroup(identifier)
        // Act
        val actual = group.canIncludeInGroup(defaultRequirement)
        // Assert
        assertTrue(actual)
    }

    @Test
    fun canIncludeInGroup_withCompatibleRequirements_returnsTrue() {
        // Arrange
        val group = PresentationExchangeSubmissionGroup(identifier)
        group.include(defaultRequirement, defaultRequirement.inputDescriptorId)
        // Act
        val actual = group.canIncludeInGroup(canIncludeRequirement)
        // Assert
        assertTrue(actual)
    }

    @Test
    fun canIncludeInGroup_withExclusiveRequirements_returnsFalse() {
        // Arrange
        val group = PresentationExchangeSubmissionGroup(identifier)
        group.include(defaultRequirement, defaultRequirement.inputDescriptorId)
        // Act
        val actual = group.canIncludeInGroup(exclusiveRequirement)
        // Assert
        assertTrue(!actual)
    }

    @Test
    fun canIncludeInGroup_withOtherIdentifier_returnsFalse() {
        // Arrange
        val group = PresentationExchangeSubmissionGroup(identifier)
        group.include(defaultRequirement, defaultRequirement.inputDescriptorId)
        // Act
        val actual = group.canIncludeInGroup(cannotIncludeRequirement)
        // Assert
        assertTrue(!actual)
    }

    @Test
    fun include_addsCredential() {
        // Arrange
        val group = PresentationExchangeSubmissionGroup(identifier)
        // Assert
        assertTrue(group.getPresentationSubmissionMap(0).isEmpty())
        // Act
        group.include(defaultRequirement, "expected")
        val submissionDescriptors = group.getPresentationSubmissionMap(0)
        // Assert
        assertEquals(submissionDescriptors.count(), 1)
        assertEquals(submissionDescriptors[0].idFromPresentationRequest, defaultRequirement.inputDescriptorId)
    }

    @Test
    fun getPresentationSubmissionMap_correctlyMapsRequirements() {
        // Arrange
        val group = PresentationExchangeSubmissionGroup(identifier)
        group.include(defaultRequirement, "expected")
        // Act
        val submissionIndex = (0..20).random()
        val submissionDescriptors = group.getPresentationSubmissionMap(submissionIndex)
        // Assert
        assertEquals(submissionDescriptors.count(), 1)
        val submission = submissionDescriptors.first()
        assertEquals(submission.idFromPresentationRequest, defaultRequirement.inputDescriptorId)
        assertEquals(submission.format, "jwt_vp")
        assertEquals(submission.path, "$[$submissionIndex]")
        assertEquals(submission.pathNested?.idFromPresentationRequest, defaultRequirement.inputDescriptorId)
        assertEquals(submission.pathNested?.format, defaultRequirement.format.name)
        assertEquals(submission.pathNested?.path, "$.verifiableCredential[0]")
    }

    @Test
    fun getVerifiablePresentation_createsVP() {
        // Arrange
        val group = PresentationExchangeSubmissionGroup(identifier)
        val signer = mockk<TokenSigner>()
        val serializer = Json
        val validityInterval = 1
        val audience = "did:example:testcase"
        val nonce = (0..256).random().toString()
        val payload = slot<String>()
        val subject = slot<Identifier>()
        every { signer.signWithIdentifier( capture(payload), capture(subject) )} returns "expected"
        group.include(defaultRequirement, "foobar")

        // Act/Assert
        assertEquals(group.getVerifiablePresentation(
            signer,
            serializer,
            validityInterval,
            audience,
            nonce
        ), "expected")

        assertEquals(subject.captured, identifier)
        assertTrue(payload.isCaptured)

        val deserialized = serializer.decodeFromString(VerifiablePresentationContent.serializer(), payload.captured)
        assertEquals(deserialized.nonce, nonce)
        assertEquals(deserialized.audience, audience)
        assertEquals(deserialized.issuerOfVp, identifier.id)
        assertEquals(deserialized.verifiablePresentation.verifiableCredential.count(), 1)
        assertEquals(deserialized.verifiablePresentation.verifiableCredential.first(), "foobar")

    }
}