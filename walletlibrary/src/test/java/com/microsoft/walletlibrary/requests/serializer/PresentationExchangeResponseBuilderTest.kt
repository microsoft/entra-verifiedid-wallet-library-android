// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.requests.serializer

import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.InputContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.CardDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.ConsentDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.PresentationResponseClaims
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.identifier.HolderIdentifier
import com.microsoft.walletlibrary.identifier.IdentifierFactory
import com.microsoft.walletlibrary.mappings.identifier.toHolderIdentifier
import com.microsoft.walletlibrary.requests.handlers.RequestProcessorSerializer
import com.microsoft.walletlibrary.requests.requirements.PresentationExchangeVerifiedIdRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.PreviewFeatureFlags
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.util.WalletLibraryLogger
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.microsoft.walletlibrary.verifiedid.VerifiedIdSerializer
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.util.Date
import kotlin.test.Test
import kotlin.test.fail

internal class PresentationExchangeResponseBuilderTest {

    class StubVerifiedIdSerializer : VerifiedIdSerializer<String> {
        /**
         * Serialize the given verifiedID into the SerializedFormat
         */
        override fun serialize(verifiedId: VerifiedId): String {
            return "serializedVerifiedId:${verifiedId.id}"
        }
    }

    class UnknownRequirement(override val required: Boolean = true) : Requirement {
        override fun validate(): VerifiedIdResult<Unit> {
            return VerifiedIdResult.failure<Unit>(Exception("Should not be called."))
        }

        override suspend fun <T> serialize(
            protocolSerializer: RequestProcessorSerializer<T>,
            verifiedIdSerializer: VerifiedIdSerializer<T>
        ): T? {
            fail("Attempted to serialize a non-PE requirement.")
        }
    }

    fun makeVerifiedId(sub: String): VerifiableCredential {
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
                    issuer = sub
                ),
                DisplayContract(
                    card = CardDescriptor("", "", "", "", null, ""),
                    consent = ConsentDescriptor(instructions = ""),
                    claims = emptyMap()
                )
            )
        )
    }

    val identifierOne = Identifier(
        "did:example:mockabcdefghi1234567890",
        "sig",
        "enc",
        "sig",
        "sig",
        "mock1"
    ).toHolderIdentifier(mockk())

    val credentialOne = makeVerifiedId(identifierOne.id)

    val identifierTwo = Identifier(
        "did:example:secondmockabcdefghi1234567890",
        "sig",
        "enc",
        "sig",
        "sig",
        "mock2"
    ).toHolderIdentifier(mockk())

    val credentialTwo = makeVerifiedId(identifierTwo.id)

    val serializerMock = StubVerifiedIdSerializer()

    val loggerMock = mockk<WalletLibraryLogger>(relaxed = true)

    val configuration = LibraryConfiguration(
        previewFeatureFlags = PreviewFeatureFlags(),
        httpAgentApiProvider = mockk(),
        serializer = Json.Default,
        rootOfTrustResolver = mockk(),
        logger = loggerMock,
        identifierFactory = IdentifierFactory(mutableListOf(identifierOne, identifierTwo))
    )

    @Test
    fun serialize_withUnknownRequirement_logsError() {
        val peBuilder = PresentationExchangeResponseBuilder(configuration)
        val unknown = UnknownRequirement()
        val loggedMessage = slot<String>()
        every { loggerMock.w(capture(loggedMessage)) } returns Unit
        runBlocking {
            peBuilder.serialize(unknown, serializerMock)
            assert(loggedMessage.isCaptured)
            assert(loggedMessage.captured.contains("Unknown credential type"))
            assert(peBuilder.vpTokens.size == 0)
        }
    }

    @Test
    fun serialize_withNoGroups_createsGroup() {
        val peBuilder = PresentationExchangeResponseBuilder(configuration)

        assert(peBuilder.vpTokens.size == 0) { "Expected no VP token groups before serialization" }
        val requirement = PresentationExchangeVerifiedIdRequirement("foo",
            credentialOne.types,
            inputDescriptorId = "foo")
        requirement.fulfill(credentialOne)
        runBlocking {
            peBuilder.serialize(requirement, serializerMock)
            assert(peBuilder.vpTokens.size == 1) { "Expected a VP token group for requirement" }
        }
    }

    @Test
    fun serialize_withGroup_addsToGroup() {
        val peBuilder = PresentationExchangeResponseBuilder(configuration)

        assert(peBuilder.vpTokens.size == 0) { "Expected no VP token groups before serialization" }
        val requirementOne = PresentationExchangeVerifiedIdRequirement("foo",
            credentialOne.types,
            inputDescriptorId = "foo")
        requirementOne.fulfill(credentialOne)
        val requirementTwo = PresentationExchangeVerifiedIdRequirement("bar",
            credentialOne.types,
            inputDescriptorId = "bar")
        requirementTwo.fulfill(credentialOne)
        runBlocking {
            peBuilder.serialize(requirementOne, serializerMock)
            peBuilder.serialize(requirementTwo, serializerMock)
            assert(peBuilder.vpTokens.size == 1) { "Expected a single VP token group for requirement" }
        }
    }

    @Test
    fun serialize_withExclusiveGroup_createsGroup() {
        val peBuilder = PresentationExchangeResponseBuilder(configuration)

        assert(peBuilder.vpTokens.size == 0) { "Expected no VP token groups before serialization" }
        val requirementOne = PresentationExchangeVerifiedIdRequirement("foo",
            credentialOne.types,
            inputDescriptorId = "foo")
        requirementOne.fulfill(credentialOne)
        val requirementTwo = PresentationExchangeVerifiedIdRequirement("bar",
            credentialOne.types,
            inputDescriptorId = "bar",
            exclusivePresentationWith = listOf("foo"))
        requirementTwo.fulfill(credentialOne)
        runBlocking {
            peBuilder.serialize(requirementOne, serializerMock)
            peBuilder.serialize(requirementTwo, serializerMock)
            assert(peBuilder.vpTokens.size == 2) { "Expected 2 VP token groups" }
        }
    }

    @Test
    fun serialize_withDifferentGroupSubject_createsGroup() {
        val peBuilder = PresentationExchangeResponseBuilder(configuration)

        assert(peBuilder.vpTokens.size == 0) { "Expected no VP token groups before serialization" }
        val requirementOne = PresentationExchangeVerifiedIdRequirement("foo",
            credentialOne.types,
            inputDescriptorId = "foo")
        requirementOne.fulfill(credentialOne)
        val requirementTwo = PresentationExchangeVerifiedIdRequirement("bar",
            credentialTwo.types,
            inputDescriptorId = "bar")
        requirementTwo.fulfill(credentialTwo)
        runBlocking {
            peBuilder.serialize(requirementOne, serializerMock)
            peBuilder.serialize(requirementTwo, serializerMock)
            assert(peBuilder.vpTokens.size == 2) { "Expected 2 VP token groups" }
        }
    }

    @Test
    fun buildVpTokens_withNoVPs_returnsEmptyList() {
        val peBuilder = PresentationExchangeResponseBuilder(configuration)

        val actual = peBuilder.buildVpTokens("aud", "nonce")
        assert(actual.isEmpty())
    }

    @Test
    fun buildVpTokens_withVPs_returnsListOfVPs() {
        val mockVP = mockk<PresentationExchangeSubmissionGroup>()
        val expectedVP = "fooBarBaz"
        every { mockVP.getVerifiablePresentation(any(), any(), "aud", "nonce") } returns expectedVP

        val peBuilder = PresentationExchangeResponseBuilder(configuration)
        peBuilder.vpTokens.add(mockVP)
        val actual = peBuilder.buildVpTokens("aud", "nonce")
        assert(actual == listOf(expectedVP)) { "Expected VP returned to be '$expectedVP'" }
    }

    @Test
    fun buildIdToken_buildsIdToken() {
        // need to substitute actual key-signing
        class PresentationExchangeResponseBuilderWithoutCrypto(configuration: LibraryConfiguration, private val payloadCapture: CapturingSlot<String>) : PresentationExchangeResponseBuilder(configuration) {
                override fun createAndSignToken(identifier: HolderIdentifier, jsonContent: String): String {
                    payloadCapture.captured = jsonContent
                    return ""
                }
        }

        val payloadCaptured = slot<String>()
        val expectedDefinition = "foobar-id"
        val expectedClient = "https://microsoft.com/"
        val expectedNonce = "totally-random-value"

        val peBuilder = PresentationExchangeResponseBuilderWithoutCrypto(configuration, payloadCaptured)

        runBlocking {
            peBuilder.buildIdToken(expectedDefinition, expectedClient, expectedNonce)
            assert(payloadCaptured.isCaptured) { "expected to capture unsigned payload" }
            val result = configuration.serializer.decodeFromString(PresentationResponseClaims.serializer(), payloadCaptured.captured)
            assert(result.subject == identifierOne.id) { "sub not first identifier." }
            assert(result.audience == expectedClient) { "aud not client id." }
            assert(result.nonce == expectedNonce) { "nonce value mismatched." }
            assert(result.vpToken.size == 1) { "expected 1 submission" }
            val peSubmission = result.vpToken[0]
            assert(peSubmission.presentationSubmission.definitionId == expectedDefinition) { "pe definition mismatched." }
            assert(peSubmission.presentationSubmission.presentationSubmissionDescriptors.isEmpty()) { "No presentation descriptors are expected. " }
        }
    }
}