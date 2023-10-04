package com.microsoft.walletlibrary.did.sdk.credential.service.protectors

import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential
import com.microsoft.walletlibrary.did.sdk.credential.service.*
import com.microsoft.walletlibrary.did.sdk.credential.service.models.RevocationRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.IssuanceResponseClaims
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.PresentationResponseClaims
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.RevocationResponseClaims
import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.defaultTestSerializer
import com.nimbusds.jose.jwk.JWK
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals

class OidcResponseFormatterTest {

    // mocks for retrieving public key.
    private val mockedKeyStore: EncryptedKeyStore = mockk()

    private val mockedTokenSigner: TokenSigner = mockk()
    private val slot = slot<String>()
    private val mockedVerifiablePresentationFormatter: VerifiablePresentationFormatter = mockk()
    private val mockedVc: VerifiableCredential = mockk()
    private val mockedIdentifier: Identifier = mockk()

    private val issuanceResponseFormatter: IssuanceResponseFormatter
    private val presentationResponseFormatter: PresentationResponseFormatter
    private val revocationResponseFormatter: RevocationResponseFormatter

    private val signingKeyRef: String = "sigKeyRef1243523"
    private val expectedDid: String = "did:test:2354543"
    private val expectedValidityInterval: Int = 3600
    private val expectedContract = "http://testcontract.com"
    private val expectedResponseAudience: String = "audience2432"
    private val expectedPresentationAudience: String = "audience6237"
    private val expectedExpiry: Int = 42
    private val expectedJsonWebKey: JWK = JWK.parse(
        "{\"kty\":\"EC\"," +
            "\"crv\":\"secp256k1\",\"x\":\"WfY7Px6AgH6x-_dgAoRbg8weYRJA36ON-gQiFnETrqw\"," +
            "\"y\":\"IzFx3BUGztK0cyDStiunXbrZYYTtKbOUzx16SUK0sAY\"}"
    )
    private val expectedSub: String = expectedJsonWebKey.computeThumbprint().toString()
    private val expectedVerifiablePresentation = "expectedPresentation"
    private val expectedSelfAttestedField = "testField3423442"
    private val expectedIdTokenConfig = "testIdTokenConfig234"
    private val expectedCredentialType: String = "type235"

    private val revocationRequest: RevocationRequest = mockk()
    private val mockedVcRaw = "testRawVc"
    private val expectedRevocationAudience = "audience1234"
    private val expectedRevokedRps = listOf("did:ion:test")
    private val expectedRevocationReason = "testing revocation"
    private val mockedPresentationResponse: PresentationResponse = mockk()
    private val mockedNonce = "123456789876"
    private val mockedState = "mockedState"
    private val mockedPresentationDefinitionId = UUID.randomUUID().toString()
    private val requestedVchPresentationSubmissionMap = mutableMapOf<CredentialPresentationInputDescriptor, VerifiableCredential>()

    private val mockedIssuanceResponse: IssuanceResponse = mockk()
    private val expectedRawToken = "rawToken2343"
    private val requestedIdTokenMap = mapOf(expectedIdTokenConfig to expectedRawToken) as RequestedIdTokenMap
    private val expectedSelfAttestedClaimValue = "value5234"
    private val requestedSelfAttestedClaimsMap =
        mapOf(expectedSelfAttestedField to expectedSelfAttestedClaimValue) as RequestedSelfAttestedClaimMap
    private val mockedPresentationAttestation: PresentationAttestation = mockk()
    private val mockedRequestedVcMap: RequestedVcMap = mutableMapOf(mockedPresentationAttestation to mockedVc)
    private val mockedPresentationRequest: PresentationRequest = mockk()

    init {
        issuanceResponseFormatter = IssuanceResponseFormatter(
            defaultTestSerializer,
            mockedVerifiablePresentationFormatter,
            mockedTokenSigner,
            mockedKeyStore
        )
        presentationResponseFormatter = PresentationResponseFormatter(
            defaultTestSerializer,
            mockedVerifiablePresentationFormatter,
            mockedTokenSigner
        )
        revocationResponseFormatter = RevocationResponseFormatter(
            defaultTestSerializer,
            mockedTokenSigner,
            mockedKeyStore
        )
        setUpGetPublicKey()
        setUpExpectedPresentations()
        every { mockedKeyStore.getKey(signingKeyRef) } returns expectedJsonWebKey
        every { mockedTokenSigner.signWithIdentifier(capture(slot), mockedIdentifier) } answers { slot.captured }
        every {
            mockedVerifiablePresentationFormatter.createPresentation(
                mockedVc,
                expectedValidityInterval,
                any(),
                mockedIdentifier
            )
        } returns expectedVerifiablePresentation
        // handle empty VC
        every {
            mockedVerifiablePresentationFormatter.createPresentation(
                emptyList(),
                expectedValidityInterval,
                any(),
                mockedIdentifier,
                mockedNonce
            )
        } returns expectedVerifiablePresentation
        every { mockedPresentationRequest.content.nonce } returns mockedNonce
        every { mockedPresentationRequest.content.clientId } returns expectedPresentationAudience
        mockPresentationResponse()
        mockIssuanceResponseWithNoAttestations()
        mockPresentationAttestation()
    }

    private fun setUpGetPublicKey() {
        every { mockedIdentifier.signatureKeyReference } returns signingKeyRef
        every { mockedIdentifier.id } returns expectedDid
    }

    private fun setUpExpectedPresentations() {
        every {
            mockedVerifiablePresentationFormatter.createPresentation(
                emptyList(),
                expectedValidityInterval,
                expectedDid,
                mockedIdentifier,
                mockedNonce
            )
        } returns expectedVerifiablePresentation
    }

    @Test
    fun `format presentation response with no attestations`() {
        // Arrange
        mockPresentationResponse()
        // Act
        val actualFormattedToken = presentationResponseFormatter.formatResponses(
            mockedPresentationRequest,
            listOf(mockedPresentationResponse),
            mockedIdentifier,
            expectedExpiry
        )
        val actualTokenContents = defaultTestSerializer.decodeFromString(PresentationResponseClaims.serializer(), actualFormattedToken.first)
        // Assert
        assertEquals(expectedPresentationAudience, actualTokenContents.audience)
        assertEquals(mockedNonce, actualTokenContents.nonce)
        assertEquals(expectedDid, actualTokenContents.subject)
        assertThat(actualTokenContents.vpToken.first().presentationSubmission.presentationSubmissionDescriptors.size).isEqualTo(0)
    }

    @Test
    fun `format issuance response with no attestations`() {
        // Arrange
        every { mockedIssuanceResponse.requestedIdTokenMap } returns mutableMapOf()
        every { mockedIssuanceResponse.requestedSelfAttestedClaimMap } returns mutableMapOf()
        every { mockedIssuanceResponse.requestedAccessTokenMap } returns mutableMapOf()
        // Act
        val actualFormattedToken = issuanceResponseFormatter.formatResponse(
            mutableMapOf(),
            mockedIssuanceResponse,
            mockedIdentifier,
            expectedExpiry
        )
        val actualTokenContents = defaultTestSerializer.decodeFromString(IssuanceResponseClaims.serializer(), actualFormattedToken)
        // Assert
        assertEquals(expectedResponseAudience, actualTokenContents.audience)
        assertEquals(expectedContract, actualTokenContents.contract)
        assertEquals(expectedDid, actualTokenContents.did)
        assertEquals(expectedSub, actualTokenContents.subject)
        assertEquals(expectedJsonWebKey, actualTokenContents.publicKeyJwk)
        assertThat(actualTokenContents.attestations.idTokens.size).isEqualTo(0)
        assertThat(actualTokenContents.attestations.presentations.size).isEqualTo(0)
        assertThat(actualTokenContents.attestations.selfIssued.size).isEqualTo(0)
    }

    @Test
    fun `format issuance response with id-token attestations`() {
        // Arrange
        every { mockedIssuanceResponse.requestedIdTokenMap } returns requestedIdTokenMap
        every { mockedIssuanceResponse.requestedSelfAttestedClaimMap } returns mutableMapOf()
        every { mockedIssuanceResponse.requestedAccessTokenMap } returns mutableMapOf()
        // Act
        val actualFormattedToken = issuanceResponseFormatter.formatResponse(
            mutableMapOf(),
            mockedIssuanceResponse,
            mockedIdentifier,
            expectedExpiry
        )
        val actualTokenContents = defaultTestSerializer.decodeFromString(IssuanceResponseClaims.serializer(), actualFormattedToken)
        // Assert
        assertEquals(expectedResponseAudience, actualTokenContents.audience)
        assertEquals(expectedContract, actualTokenContents.contract)
        assertEquals(expectedDid, actualTokenContents.did)
        assertEquals(expectedSub, actualTokenContents.subject)
        assertEquals(expectedJsonWebKey, actualTokenContents.publicKeyJwk)
        assertEquals(expectedRawToken, actualTokenContents.attestations.idTokens.entries.first().value)
        assertEquals(expectedIdTokenConfig, actualTokenContents.attestations.idTokens.entries.first().key)
        assertThat(actualTokenContents.attestations.selfIssued.size).isEqualTo(0)
        assertThat(actualTokenContents.attestations.presentations.size).isEqualTo(0)
    }

    @Test
    fun `format issuance response with self attested attestations`() {
        // Arrange
        every { mockedIssuanceResponse.requestedSelfAttestedClaimMap } returns requestedSelfAttestedClaimsMap
        every { mockedIssuanceResponse.requestedIdTokenMap } returns mutableMapOf()
        every { mockedIssuanceResponse.requestedAccessTokenMap } returns mutableMapOf()
        // Act
        val actualFormattedToken = issuanceResponseFormatter.formatResponse(
            mutableMapOf(),
            mockedIssuanceResponse,
            mockedIdentifier,
            expectedExpiry
        )
        val actualTokenContents = defaultTestSerializer.decodeFromString(IssuanceResponseClaims.serializer(), actualFormattedToken)
        // Assert
        assertEquals(expectedResponseAudience, actualTokenContents.audience)
        assertEquals(expectedContract, actualTokenContents.contract)
        assertEquals(expectedDid, actualTokenContents.did)
        assertEquals(expectedSub, actualTokenContents.subject)
        assertEquals(expectedJsonWebKey, actualTokenContents.publicKeyJwk)
        assertEquals(expectedSelfAttestedField, actualTokenContents.attestations.selfIssued.entries.first().key)
        assertEquals(expectedSelfAttestedClaimValue, actualTokenContents.attestations.selfIssued.entries.first().value)
        assertThat(actualTokenContents.attestations.idTokens.size).isEqualTo(0)
        assertThat(actualTokenContents.attestations.presentations.size).isEqualTo(0)
    }

    @Test
    fun `format issuance response with presentation attestations`() {
        // Arrange
        every { mockedIssuanceResponse.requestedVcMap } returns mockedRequestedVcMap
        every { mockedIssuanceResponse.requestedIdTokenMap } returns mutableMapOf()
        every { mockedIssuanceResponse.requestedSelfAttestedClaimMap } returns mutableMapOf()
        every { mockedIssuanceResponse.requestedAccessTokenMap } returns mutableMapOf()
        // Act
        val actualFormattedToken = issuanceResponseFormatter.formatResponse(
            mockedRequestedVcMap,
            mockedIssuanceResponse,
            mockedIdentifier,
            expectedExpiry
        )
        val actualTokenContents = defaultTestSerializer.decodeFromString(IssuanceResponseClaims.serializer(), actualFormattedToken)
        // Assert
        assertEquals(expectedResponseAudience, actualTokenContents.audience)
        assertEquals(expectedContract, actualTokenContents.contract)
        assertEquals(expectedDid, actualTokenContents.did)
        assertEquals(expectedSub, actualTokenContents.subject)
        assertEquals(expectedJsonWebKey, actualTokenContents.publicKeyJwk)
        assertEquals(mapOf(expectedCredentialType to expectedVerifiablePresentation), actualTokenContents.attestations.presentations)
        assertThat(actualTokenContents.attestations.idTokens.size).isEqualTo(0)
        assertThat(actualTokenContents.attestations.selfIssued.size).isEqualTo(0)
    }

    @Test
    fun `format issuance response with all attestations`() {
        // Arrange
        val expectedRawToken = "rawToken2343"
        every { mockedIssuanceResponse.requestedIdTokenMap } returns requestedIdTokenMap
        every { mockedIssuanceResponse.requestedAccessTokenMap } returns mutableMapOf()
        every { mockedIssuanceResponse.requestedSelfAttestedClaimMap } returns requestedSelfAttestedClaimsMap
        every { mockedIssuanceResponse.requestedVcMap } returns mockedRequestedVcMap
        every { mockedIssuanceResponse.request.entityIdentifier } returns expectedResponseAudience
        // Act
        val results = issuanceResponseFormatter.formatResponse(
            mockedRequestedVcMap,
            mockedIssuanceResponse,
            mockedIdentifier,
            expectedExpiry
        )
        val actualTokenContents = defaultTestSerializer.decodeFromString(IssuanceResponseClaims.serializer(), results)
        // Assert
        assertEquals(expectedResponseAudience, actualTokenContents.audience)
        assertEquals(expectedContract, actualTokenContents.contract)
        assertEquals(expectedDid, actualTokenContents.did)
        assertEquals(expectedSub, actualTokenContents.subject)
        assertEquals(expectedJsonWebKey, actualTokenContents.publicKeyJwk)
        assertEquals(mapOf(expectedCredentialType to expectedVerifiablePresentation), actualTokenContents.attestations.presentations)
        assertEquals(expectedSelfAttestedField, actualTokenContents.attestations.selfIssued.entries.first().key)
        assertEquals(expectedSelfAttestedClaimValue, actualTokenContents.attestations.selfIssued.entries.first().value)
        assertEquals(expectedRawToken, actualTokenContents.attestations.idTokens.entries.first().value)
        assertEquals(expectedIdTokenConfig, actualTokenContents.attestations.idTokens.entries.first().key)
    }

    private fun mockPresentationResponse() {
        //every { mockedPresentationResponse.request.entityIdentifier } returns expectedDid
        every { mockedPresentationResponse.audience } returns expectedPresentationAudience
        every { mockedPresentationResponse.request.content.clientId } returns expectedDid
        every { mockedPresentationResponse.request.content.nonce } returns mockedNonce
        every { mockedPresentationResponse.request.content.state } returns mockedState
        every { mockedPresentationResponse.requestedVcPresentationSubmissionMap } returns requestedVchPresentationSubmissionMap
        every { mockedPresentationResponse.requestedVcPresentationDefinitionId } returns mockedPresentationDefinitionId
    }

    private fun mockIssuanceResponseWithNoAttestations() {
        every { mockedIssuanceResponse.request.entityIdentifier } returns expectedDid
        every { mockedIssuanceResponse.audience } returns expectedResponseAudience
        every { mockedIssuanceResponse.issuancePin } returns null
        every { mockedIssuanceResponse.request.contractUrl } returns expectedContract
    }

    private fun mockPresentationAttestation() {
        every { mockedPresentationAttestation.credentialType } returns expectedCredentialType
        every { mockedPresentationAttestation.validityInterval } returns expectedValidityInterval
    }

    @Test
    fun `format revocation request with revoked RPs and reason for revocation`() {
        // Arrange
        every { revocationRequest.audience } returns expectedRevocationAudience
        every { revocationRequest.reason } returns expectedRevocationReason
        every { revocationRequest.rpList } returns expectedRevokedRps
        every { revocationRequest.owner } returns mockedIdentifier
        every { revocationRequest.verifiableCredential } returns mockedVc
        every { mockedVc.raw } returns mockedVcRaw
        // Act
        val results = revocationResponseFormatter.formatResponse(revocationRequest, Constants.DEFAULT_EXPIRATION_IN_SECONDS)
        val actualTokenContents = defaultTestSerializer.decodeFromString(RevocationResponseClaims.serializer(), results)
        // Assert
        assertThat(actualTokenContents.did).isEqualTo(expectedDid)
        assertThat(actualTokenContents.vc).isEqualTo(mockedVcRaw)
        assertThat(actualTokenContents.audience).isEqualTo(expectedRevocationAudience)
        assertThat(actualTokenContents.rpList).isEqualTo(expectedRevokedRps)
        assertThat(actualTokenContents.reason).isEqualTo(expectedRevocationReason)
    }

    @Test
    fun `format revocation request with no reason and RP list`() {
        // Arrange
        every { revocationRequest.audience } returns expectedRevocationAudience
        every { revocationRequest.verifiableCredential } returns mockedVc
        every { revocationRequest.owner } returns mockedIdentifier
        every { revocationRequest.rpList } returns emptyList()
        every { revocationRequest.reason } returns ""
        every { mockedVc.raw } returns mockedVcRaw
        // Act
        val results = revocationResponseFormatter.formatResponse(revocationRequest, Constants.DEFAULT_EXPIRATION_IN_SECONDS)
        val actualTokenContents = defaultTestSerializer.decodeFromString(RevocationResponseClaims.serializer(), results)
        // Assert
        assertThat(actualTokenContents.did).isEqualTo(expectedDid)
        assertThat(actualTokenContents.vc).isEqualTo(mockedVcRaw)
        assertThat(actualTokenContents.audience).isEqualTo(expectedRevocationAudience)
        assertThat(actualTokenContents.rpList).isEmpty()
        assertThat(actualTokenContents.reason).isEmpty()
    }
}