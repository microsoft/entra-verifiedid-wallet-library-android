package com.microsoft.walletlibrary.did.sdk.credential.service.protectors

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential as SDKVerifiableCredential
import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationResponse
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.CardDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.ClaimDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.Claims
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.PresentationResponseClaims
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.VpTokenInRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.PresentationDefinition
import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.Schema
import com.microsoft.walletlibrary.did.sdk.credential.service.models.verifiablePresentation.VerifiablePresentationContent
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.defaultTestSerializer
import com.microsoft.walletlibrary.mappings.presentation.addRequirements
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Before
import org.junit.Test
import java.util.Date

class PresentationResponseFormatterTest {
    private val mockedTokenSigner: TokenSigner = mockk()
    private val vpFormatter: VerifiablePresentationFormatter =
        VerifiablePresentationFormatter(defaultTestSerializer, mockedTokenSigner)
    private val formatter: PresentationResponseFormatter =
        PresentationResponseFormatter(defaultTestSerializer, vpFormatter, mockedTokenSigner)

    private val mockedIdentifier: Identifier = mockk()
    private val slot = slot<String>()
    private val signingKeyRef: String = "sigKeyRef1243523"
    private val expectedDid: String = "did:test:2354543"
    private val expectedAudience: String = "audience2432"
    private val expectedPresentationContext = listOf(Constants.VP_CONTEXT_URL)
    private val expectedPresentationType = listOf(Constants.VERIFIABLE_PRESENTATION_TYPE)
    private val expectedNonce = "1234"
    private val firstInputVPId = "first vp definition"
    private val firstInputDefinitionId = "first input definition"
    private val firstVpTokenType = "some credential"
    private val secondInputVPId = "second vp definition"
    private val secondInputDefinitionId = "second input definition"
    private val secondVpTokenType = "another credential"
    private lateinit var sdkCredentialOne: SDKVerifiableCredential
    private lateinit var walletCredentialOne: VerifiableCredential
    private val expectedVCJTIOne = "jti:test:123"
    private val expectedVCRawOne = "imagineIfYouWill.AVerifiableCredential.withJtiTest123"
    private lateinit var sdkCredentialTwo: SDKVerifiableCredential
    private lateinit var walletCredentialTwo: VerifiableCredential
    private val expectedVCJTITwo = "jti:test:456"
    private val expectedVCRawTwo = "nowImagineAgain.ADifferentCredential.withJtiTest456"

    init {
        every { mockedIdentifier.id } returns expectedDid
        every { mockedIdentifier.signatureKeyReference } returns signingKeyRef
        every { mockedTokenSigner.signWithIdentifier(capture(slot), eq(mockedIdentifier)) } answers { slot.captured }
    }

    @Before
    fun setup() {
        sdkCredentialOne = SDKVerifiableCredential(expectedVCJTIOne, expectedVCRawOne, VerifiableCredentialContent(
            jti = expectedVCJTIOne,
            vc = VerifiableCredentialDescriptor(
                context = listOf(Constants.VP_CONTEXT_URL),
                type = listOf(firstVpTokenType),
                credentialSubject = mapOf(
                    "test" to "Yes"
                )
            ),
            sub = expectedDid,
            iss = expectedDid,
            iat = Date().time / 1000L
        ))
        walletCredentialOne = VerifiableCredential(sdkCredentialOne,
            VerifiableCredentialContract(
            id = "Not important here",
            input = mockk(),
            display = DisplayContract(
                card = CardDescriptor(
                    title = "Test",
                    issuedBy = "Unit test",
                    backgroundColor = "#ff9999",
                    textColor = "#333333",
                    description = "A test credential"
                ),
                consent = mockk(),
                claims = mapOf(
                    "test" to ClaimDescriptor(
                        type = "string",
                        label = "Is this a test?"
                    )
                )
            )
        ))

        sdkCredentialTwo = SDKVerifiableCredential(expectedVCJTITwo, expectedVCRawTwo,
            VerifiableCredentialContent(
            jti = expectedVCJTITwo,
            vc = VerifiableCredentialDescriptor(
                context = listOf(Constants.VP_CONTEXT_URL),
                type = listOf(secondVpTokenType),
                credentialSubject = mapOf(
                    "pass" to "huge success"
                )
            ),
            sub = expectedDid,
            iss = expectedDid,
            iat = Date().time / 1000L
        ))
        walletCredentialTwo = VerifiableCredential(sdkCredentialTwo, VerifiableCredentialContract(
            id = "Not important here",
            input = mockk(),
            display = DisplayContract(
                card = CardDescriptor(
                    title = "Another test",
                    issuedBy = "Unit test",
                    backgroundColor = "#333333",
                    textColor = "#ff9999",
                    description = "A different credential"
                ),
                consent = mockk(),
                claims = mapOf(
                    "pass" to ClaimDescriptor(
                        type = "string",
                        label = "I'm making a note here"
                    )
                )
            )
        ))
    }

    @Test
    fun formatResponses_shouldFormTokens_withSingleResponse() {
        // arrange
        val vcRequested = CredentialPresentationInputDescriptor(
            firstInputDefinitionId,
            listOf(Schema(firstVpTokenType))
        )

        val request = PresentationRequest(
            content = PresentationRequestContent(
                responseType = "id_token",
                responseMode = "post",
                clientId = expectedAudience,
                scope = "openid",
                nonce = expectedNonce,
                claims = Claims(
                    vpTokensInRequest = listOf(
                        VpTokenInRequest(
                            PresentationDefinition(
                                id = firstInputVPId,
                                listOf(vcRequested)
                            )
                        )
                    )
                )
            ),
            linkedDomainResult = LinkedDomainMissing
        )
        val response =
            PresentationResponse(
                request,
                firstInputVPId
            )
        response.addRequirements(VerifiedIdRequirement(
            firstInputDefinitionId,
            types = listOf(firstVpTokenType),
            _verifiedId = walletCredentialOne
        ))
        val responses = listOf(response)

        // act
        val tokens = formatter.formatResponses(
            request,
            responses,
            mockedIdentifier
        )

        // assert
        assertThat(tokens.first.length).isGreaterThan(0)
        assertThat(tokens.second.size).isGreaterThan(0)
        assertThat(tokens.second.first().length).isGreaterThan(0)
        // serializer can do some magic for us on array or object so search for
        // beginning of vp_token declaration. This should be an object.
        assertThat(tokens.first).contains("\"_vp_token\":{")
        val actualIdToken = defaultTestSerializer.decodeFromString(PresentationResponseClaims.serializer(), tokens.first)
        assertThat(actualIdToken.nonce).isEqualTo(expectedNonce)
        assertThat(actualIdToken.audience).isEqualTo(expectedAudience)
        assertThat(actualIdToken.subject).isEqualTo(expectedDid)
        assertThat(actualIdToken.vpToken.size).isEqualTo(1)
        val idTokenVpToken = actualIdToken.vpToken.first()
        assertThat(idTokenVpToken.presentationSubmission.definitionId).isEqualTo(firstInputVPId)
        assertThat(idTokenVpToken.presentationSubmission.presentationSubmissionDescriptors.size).isEqualTo(1)
        val presentationSubmission = idTokenVpToken.presentationSubmission.presentationSubmissionDescriptors.first()
        assertThat(presentationSubmission.path).isEqualTo("$")
        assertThat(presentationSubmission.idFromPresentationRequest).isEqualTo(firstInputDefinitionId)
        assertThat(presentationSubmission.pathNested?.path).isEqualTo("$.verifiableCredential[0]")

        val vpToken = defaultTestSerializer.decodeFromString(VerifiablePresentationContent.serializer(), tokens.second.first())
        assertThat(vpToken.audience).isEqualTo(expectedAudience)
        assertThat(vpToken.nonce).isEqualTo(expectedNonce)
        assertThat(vpToken.issuerOfVp).isEqualTo(expectedDid)
        assertThat(vpToken.verifiablePresentation.context).isEqualTo(expectedPresentationContext)
        assertThat(vpToken.verifiablePresentation.type).isEqualTo(expectedPresentationType)
        assertThat(vpToken.verifiablePresentation.verifiableCredential.size).isEqualTo(1)
        assertThat(vpToken.verifiablePresentation.verifiableCredential[0]).isEqualTo(expectedVCRawOne)
    }

    @Test
    fun formatResponses_shouldFormTokens_withMultipleResponses() {
        // arrange
        val vcRequestOne = CredentialPresentationInputDescriptor(
            firstInputDefinitionId,
            listOf(Schema(firstVpTokenType))
        )
        val vcRequestTwo = CredentialPresentationInputDescriptor(
            secondInputDefinitionId,
            listOf(Schema(secondVpTokenType))
        )

        val request = PresentationRequest(
            content = PresentationRequestContent(
                responseType = "id_token",
                responseMode = "post",
                clientId = expectedAudience,
                scope = "openid",
                nonce = expectedNonce,
                claims = Claims(
                    vpTokensInRequest = listOf(
                        VpTokenInRequest(
                            PresentationDefinition(
                                id = firstInputVPId,
                                listOf(vcRequestOne)
                            )
                        ),
                        VpTokenInRequest(
                            PresentationDefinition(
                                id = secondInputVPId,
                                listOf(vcRequestTwo)
                            )
                        )
                    )
                )
            ),
            linkedDomainResult = LinkedDomainMissing
        )
        val responseOne =
            PresentationResponse(
                request,
                firstInputVPId
            )
        responseOne.addRequirements(VerifiedIdRequirement(
            firstInputDefinitionId,
            types = listOf(firstVpTokenType),
            _verifiedId = walletCredentialOne
        ))
        val responseTwo =
            PresentationResponse(
                request,
                secondInputVPId
            )
        responseTwo.addRequirements(VerifiedIdRequirement(
            secondInputDefinitionId,
            types = listOf(secondVpTokenType),
            _verifiedId = walletCredentialTwo
        ))
        val responses = listOf(responseOne, responseTwo)

        // act
        val tokens = formatter.formatResponses(
            request,
            responses,
            mockedIdentifier
        )

        // assert
        assertThat(tokens.first.length).isGreaterThan(0)
        assertThat(tokens.second.size).isGreaterThan(0)
        assertThat(tokens.second.first().length).isGreaterThan(0)
        // serializer can do some magic for us on array or object so search for
        // beginning of vp_token declaration. This should be an array of objects.
        assertThat(tokens.first).contains("\"_vp_token\":[{")
        val actualIdToken = defaultTestSerializer.decodeFromString(PresentationResponseClaims.serializer(), tokens.first)
        assertThat(actualIdToken.nonce).isEqualTo(expectedNonce)
        assertThat(actualIdToken.audience).isEqualTo(expectedAudience)
        assertThat(actualIdToken.subject).isEqualTo(expectedDid)
        assertThat(actualIdToken.vpToken.size).isEqualTo(2)
        val idTokenVpTokenOne = actualIdToken.vpToken[0]
        assertThat(idTokenVpTokenOne.presentationSubmission.definitionId).isEqualTo(firstInputVPId)
        assertThat(idTokenVpTokenOne.presentationSubmission.presentationSubmissionDescriptors.size).isEqualTo(1)
        val presentationSubmissionOne = idTokenVpTokenOne.presentationSubmission.presentationSubmissionDescriptors.first()
        assertThat(presentationSubmissionOne.path).isEqualTo("$[0]")
        assertThat(presentationSubmissionOne.idFromPresentationRequest).isEqualTo(firstInputDefinitionId)
        assertThat(presentationSubmissionOne.pathNested?.path).isEqualTo("$.verifiableCredential[0]")
        val idTokenVpTokenTwo = actualIdToken.vpToken[1]
        assertThat(idTokenVpTokenTwo.presentationSubmission.definitionId).isEqualTo(secondInputVPId)
        assertThat(idTokenVpTokenTwo.presentationSubmission.presentationSubmissionDescriptors.size).isEqualTo(1)
        val presentationSubmissionTwo = idTokenVpTokenTwo.presentationSubmission.presentationSubmissionDescriptors.first()
        assertThat(presentationSubmissionTwo.path).isEqualTo("$[1]")
        assertThat(presentationSubmissionTwo.idFromPresentationRequest).isEqualTo(secondInputDefinitionId)
        assertThat(presentationSubmissionTwo.pathNested?.path).isEqualTo("$.verifiableCredential[0]")

        val vpTokenOne = defaultTestSerializer.decodeFromString(VerifiablePresentationContent.serializer(), tokens.second[0])
        assertThat(vpTokenOne.audience).isEqualTo(expectedAudience)
        assertThat(vpTokenOne.nonce).isEqualTo(expectedNonce)
        assertThat(vpTokenOne.issuerOfVp).isEqualTo(expectedDid)
        assertThat(vpTokenOne.verifiablePresentation.context).isEqualTo(expectedPresentationContext)
        assertThat(vpTokenOne.verifiablePresentation.type).isEqualTo(expectedPresentationType)
        assertThat(vpTokenOne.verifiablePresentation.verifiableCredential.size).isEqualTo(1)
        assertThat(vpTokenOne.verifiablePresentation.verifiableCredential[0]).isEqualTo(expectedVCRawOne)

        val vpTokenTwo = defaultTestSerializer.decodeFromString(VerifiablePresentationContent.serializer(), tokens.second[1])
        assertThat(vpTokenTwo.audience).isEqualTo(expectedAudience)
        assertThat(vpTokenTwo.nonce).isEqualTo(expectedNonce)
        assertThat(vpTokenTwo.issuerOfVp).isEqualTo(expectedDid)
        assertThat(vpTokenTwo.verifiablePresentation.context).isEqualTo(expectedPresentationContext)
        assertThat(vpTokenTwo.verifiablePresentation.type).isEqualTo(expectedPresentationType)
        assertThat(vpTokenTwo.verifiablePresentation.verifiableCredential.size).isEqualTo(1)
        assertThat(vpTokenTwo.verifiablePresentation.verifiableCredential[0]).isEqualTo(expectedVCRawTwo)
    }

    @Test
    fun formatResponses_shouldFormTokens_withMultipleCredentials() {
        // arrange
        val vcRequestOne = CredentialPresentationInputDescriptor(
            firstInputDefinitionId,
            listOf(Schema(firstVpTokenType))
        )
        val vcRequestTwo = CredentialPresentationInputDescriptor(
            secondInputDefinitionId,
            listOf(Schema(secondVpTokenType))
        )

        val request = PresentationRequest(
            content = PresentationRequestContent(
                responseType = "id_token",
                responseMode = "post",
                clientId = expectedAudience,
                scope = "openid",
                nonce = expectedNonce,
                claims = Claims(
                    vpTokensInRequest = listOf(
                        VpTokenInRequest(
                            PresentationDefinition(
                                id = firstInputVPId,
                                listOf(vcRequestOne, vcRequestTwo)
                            )
                        )
                    )
                )
            ),
            linkedDomainResult = LinkedDomainMissing
        )
        val responseOne =
            PresentationResponse(
                request,
                firstInputVPId
            )
        responseOne.addRequirements(VerifiedIdRequirement(
            firstInputDefinitionId,
            types = listOf(firstVpTokenType),
            _verifiedId = walletCredentialOne
        ))
        responseOne.addRequirements(VerifiedIdRequirement(
            secondInputDefinitionId,
            types = listOf(secondVpTokenType),
            _verifiedId = walletCredentialTwo
        ))
        val responses = listOf(responseOne)

        // act
        val tokens = formatter.formatResponses(
            request,
            responses,
            mockedIdentifier,
        )

        // assert
        assertThat(tokens.first.length).isGreaterThan(0)
        assertThat(tokens.second.size).isGreaterThan(0)
        assertThat(tokens.second.first().length).isGreaterThan(0)
        // serializer can do some magic for us on array or object so search for
        // beginning of vp_token declaration. This should be an object.
        assertThat(tokens.first).contains("\"_vp_token\":{")
        val actualIdToken = defaultTestSerializer.decodeFromString(PresentationResponseClaims.serializer(), tokens.first)
        assertThat(actualIdToken.nonce).isEqualTo(expectedNonce)
        assertThat(actualIdToken.audience).isEqualTo(expectedAudience)
        assertThat(actualIdToken.subject).isEqualTo(expectedDid)
        assertThat(actualIdToken.vpToken.size).isEqualTo(1)
        val idTokenVpTokenOne = actualIdToken.vpToken[0]
        assertThat(idTokenVpTokenOne.presentationSubmission.definitionId).isEqualTo(firstInputVPId)
        assertThat(idTokenVpTokenOne.presentationSubmission.presentationSubmissionDescriptors.size).isEqualTo(2)
        val presentationSubmissionOne = idTokenVpTokenOne.presentationSubmission.presentationSubmissionDescriptors[0]
        assertThat(presentationSubmissionOne.path).isEqualTo("$")
        assertThat(presentationSubmissionOne.idFromPresentationRequest).isEqualTo(firstInputDefinitionId)
        assertThat(presentationSubmissionOne.pathNested?.path).isEqualTo("$.verifiableCredential[0]")
        val presentationSubmissionTwo = idTokenVpTokenOne.presentationSubmission.presentationSubmissionDescriptors[1]
        assertThat(presentationSubmissionTwo.path).isEqualTo("$")
        assertThat(presentationSubmissionTwo.idFromPresentationRequest).isEqualTo(secondInputDefinitionId)
        assertThat(presentationSubmissionTwo.pathNested?.path).isEqualTo("$.verifiableCredential[1]")

        val vpTokenOne = defaultTestSerializer.decodeFromString(VerifiablePresentationContent.serializer(), tokens.second[0])
        assertThat(vpTokenOne.audience).isEqualTo(expectedAudience)
        assertThat(vpTokenOne.nonce).isEqualTo(expectedNonce)
        assertThat(vpTokenOne.issuerOfVp).isEqualTo(expectedDid)
        assertThat(vpTokenOne.verifiablePresentation.context).isEqualTo(expectedPresentationContext)
        assertThat(vpTokenOne.verifiablePresentation.type).isEqualTo(expectedPresentationType)
        assertThat(vpTokenOne.verifiablePresentation.verifiableCredential.size).isEqualTo(2)
        assertThat(vpTokenOne.verifiablePresentation.verifiableCredential[0]).isEqualTo(expectedVCRawOne)
        assertThat(vpTokenOne.verifiablePresentation.verifiableCredential[1]).isEqualTo(expectedVCRawTwo)
    }
}