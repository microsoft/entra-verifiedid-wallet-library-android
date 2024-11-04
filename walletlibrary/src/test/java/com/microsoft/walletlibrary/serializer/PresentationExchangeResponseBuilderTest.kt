package com.microsoft.walletlibrary.serializer

import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.InputContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.CardDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.ConsentDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.PresentationResponseClaims
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.TokenSigner
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.identifier.IdentifierManager
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirementOperator
import com.microsoft.walletlibrary.requests.requirements.PresentationExchangeVerifiedIdRequirement
import com.microsoft.walletlibrary.requests.serializer.PresentationExchangeResponseBuilder
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.verifiedid.StringVCSerializer
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
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
    val firstVerifiedId = makeVerifiedId(identifier.id)
    val secondVerifiedId = makeVerifiedId(identifier.id)
    val firstRequirement = PresentationExchangeVerifiedIdRequirement(
        id = "recOne",
        types = listOf("TestCredential"),
        inputDescriptorId = "1"
    )
    val secondRequirement = PresentationExchangeVerifiedIdRequirement(
        id = "recTwo",
        types = listOf("TestCredential"),
        inputDescriptorId = "2"
    )
    val unmetRequirement = PresentationExchangeVerifiedIdRequirement(
        id = "recThree",
        types = listOf("TestCredential"),
        inputDescriptorId = "3"
    )
    val secondIdentifier: Identifier = Identifier(
        "did:example:otherabcdefghi1234567890",
        "sig",
        "enc",
        "sig",
        "sig",
        "otherMock"
    )
    val otherVerifiedId = makeVerifiedId(secondIdentifier.id)
    val otherRequirement = PresentationExchangeVerifiedIdRequirement(
        id = "recFour",
        types = listOf("TestCredential"),
        inputDescriptorId = "4"
    )

    init {
        val mockIdentifierManager = mockk<IdentifierManager>()
        coEvery { mockIdentifierManager.getMasterIdentifier() } returns Result.Success(identifier)
        every { libraryConfiguration.serializer } returns serializer
        every { libraryConfiguration.tokenSigner } returns signer
        every { libraryConfiguration.identifierManager } returns mockIdentifierManager
        every { signer.signWithIdentifier( capture(payload), capture(subject) )} returns signedPayload
        assertTrue(firstRequirement.fulfill(firstVerifiedId).isSuccess)
        assertTrue(secondRequirement.fulfill(secondVerifiedId).isSuccess)
        assertTrue(otherRequirement.fulfill(otherVerifiedId).isSuccess)
    }

    @Test
    fun serialize_singleRequirement_traversesRequirement() {
        // Arrange
        val builder = PresentationExchangeResponseBuilder(libraryConfiguration)
        val vcSerializer = spyk(StringVCSerializer)
        // Assert (pre-test check)
        assertEquals(builder.buildVpTokens("foo", "bar"), emptyList())
        // Act
        runBlocking {
            builder.serialize(firstRequirement, vcSerializer)
        }
        // Assert
        verify { vcSerializer.serialize(firstVerifiedId) }
        val vps = builder.buildVpTokens("aud", "nonce")
        assertEquals(vps.count(), 1)
        assertEquals(vps.first(), signedPayload)
        runBlocking {
            val idToken = builder.buildIdToken("req", "client", "nonce")
            assertEquals(idToken, signedPayload)
            val token = serializer.decodeFromString(PresentationResponseClaims.serializer(), payload.captured)
            assertEquals(token.nonce, "nonce")
            assertEquals(token.subject, identifier.id)
            assertEquals(token.audience, "client")
            assertEquals(token.vpToken.count(), 1)
            val submission = token.vpToken.first()
            assertEquals(submission.presentationSubmission.definitionId, "req")
            assertEquals(submission.presentationSubmission.presentationSubmissionDescriptors.count(), 1)
            val vpSubmission = submission.presentationSubmission.presentationSubmissionDescriptors.first()
            assertEquals(vpSubmission.idFromPresentationRequest, firstRequirement.inputDescriptorId)
        }
    }

    @Test
    fun serialize_groupRequirement_traversesAllRequirements() {
        // Arrange
        val builder = PresentationExchangeResponseBuilder(libraryConfiguration)
        val vcSerializer = spyk(StringVCSerializer)
        // Assert (pre-test check)
        assertEquals(builder.buildVpTokens("foo", "bar"), emptyList())
        val requirement = GroupRequirement(
            true,
            mutableListOf(firstRequirement, secondRequirement),
            GroupRequirementOperator.ALL
        )
        // Act
        runBlocking {
            builder.serialize(requirement, vcSerializer)
        }
        // Assert
        verify { vcSerializer.serialize(firstVerifiedId) }
        verify { vcSerializer.serialize(secondVerifiedId) }
        val vps = builder.buildVpTokens("aud", "nonce")
        assertEquals(vps.count(), 1)
        assertEquals(vps.first(), signedPayload)
        runBlocking {
            val idToken = builder.buildIdToken("req", "client", "nonce")
            assertEquals(idToken, signedPayload)
            val token = serializer.decodeFromString(PresentationResponseClaims.serializer(), payload.captured)
            assertEquals(token.nonce, "nonce")
            assertEquals(token.subject, identifier.id)
            assertEquals(token.audience, "client")
            assertEquals(token.vpToken.count(), 1)
            val submission = token.vpToken.first()
            assertEquals(submission.presentationSubmission.definitionId, "req")
            assertEquals(submission.presentationSubmission.presentationSubmissionDescriptors.count(), 2)
            val vpSubmissionOne = submission.presentationSubmission.presentationSubmissionDescriptors[0]
            assertEquals(vpSubmissionOne.idFromPresentationRequest, firstRequirement.inputDescriptorId)
            val vpSubmissionTwo = submission.presentationSubmission.presentationSubmissionDescriptors[1]
            assertEquals(vpSubmissionTwo.idFromPresentationRequest, secondRequirement.inputDescriptorId)
            // these should be in the same submission
            assertEquals(vpSubmissionOne.path, "$[0]")
            assertEquals(vpSubmissionTwo.path, "$[0]")
            assertEquals(vpSubmissionOne.pathNested?.path, "$.verifiableCredential[0]")
            assertEquals(vpSubmissionTwo.pathNested?.path, "$.verifiableCredential[1]")
        }
    }

    @Test
    fun serialize_groupRequirement_traversesAnyRequirements() {
        // Arrange
        val builder = PresentationExchangeResponseBuilder(libraryConfiguration)
        val vcSerializer = spyk(StringVCSerializer)
        // Assert (pre-test check)
        assertEquals(builder.buildVpTokens("foo", "bar"), emptyList())
        val requirement = GroupRequirement(
            true,
            mutableListOf(unmetRequirement, firstRequirement),
            GroupRequirementOperator.ANY
        )
        // Act
        runBlocking {
            builder.serialize(requirement, vcSerializer)
        }
        // Assert
        verify { vcSerializer.serialize(firstVerifiedId) }
        val vps = builder.buildVpTokens("aud", "nonce")
        assertEquals(vps.count(), 1)
        assertEquals(vps.first(), signedPayload)
        runBlocking {
            val idToken = builder.buildIdToken("req", "client", "nonce")
            assertEquals(idToken, signedPayload)
            val token = serializer.decodeFromString(PresentationResponseClaims.serializer(), payload.captured)
            assertEquals(token.nonce, "nonce")
            assertEquals(token.subject, identifier.id)
            assertEquals(token.audience, "client")
            assertEquals(token.vpToken.count(), 1)
            val submission = token.vpToken.first()
            assertEquals(submission.presentationSubmission.definitionId, "req")
            assertEquals(submission.presentationSubmission.presentationSubmissionDescriptors.count(), 1)
            val vpSubmission = submission.presentationSubmission.presentationSubmissionDescriptors.first()
            assertEquals(vpSubmission.idFromPresentationRequest, firstRequirement.inputDescriptorId)
        }
    }

    @Test
    fun serialize_twoIdentifiersInGroupRequirement_createsTwoPresentations() {
        // Arrange
        val builder = PresentationExchangeResponseBuilder(libraryConfiguration)
        val vcSerializer = spyk(StringVCSerializer)
        // Assert (pre-test check)
        assertEquals(builder.buildVpTokens("foo", "bar"), emptyList())
        val requirement = GroupRequirement(
            true,
            mutableListOf(firstRequirement, otherRequirement),
            GroupRequirementOperator.ALL
        )
        // Act
        runBlocking {
            builder.serialize(requirement, vcSerializer)
        }
        // Assert
        verify { vcSerializer.serialize(firstVerifiedId) }
        verify { vcSerializer.serialize(otherVerifiedId) }
        val vps = builder.buildVpTokens("aud", "nonce")
        assertEquals(vps.count(), 2)
        assertEquals(vps.first(), signedPayload)
        runBlocking {
            val idToken = builder.buildIdToken("req", "client", "nonce")
            assertEquals(idToken, signedPayload)
            val token = serializer.decodeFromString(PresentationResponseClaims.serializer(), payload.captured)
            assertEquals(token.nonce, "nonce")
            assertEquals(token.subject, identifier.id)
            assertEquals(token.audience, "client")
            assertEquals(token.vpToken.count(), 1)
            val submission = token.vpToken.first()
            assertEquals(submission.presentationSubmission.definitionId, "req")
            assertEquals(submission.presentationSubmission.presentationSubmissionDescriptors.count(), 2)
            val vpSubmissionOne = submission.presentationSubmission.presentationSubmissionDescriptors[0]
            assertEquals(vpSubmissionOne.idFromPresentationRequest, firstRequirement.inputDescriptorId)
            val vpSubmissionTwo = submission.presentationSubmission.presentationSubmissionDescriptors[1]
            assertEquals(vpSubmissionTwo.idFromPresentationRequest, otherRequirement.inputDescriptorId)
            // these should be in the same submission
            assertEquals(vpSubmissionOne.path, "$[0]")
            assertEquals(vpSubmissionTwo.path, "$[1]")
            assertEquals(vpSubmissionOne.pathNested?.path, "$.verifiableCredential[0]")
            assertEquals(vpSubmissionTwo.pathNested?.path, "$.verifiableCredential[0]")
        }
    }
}