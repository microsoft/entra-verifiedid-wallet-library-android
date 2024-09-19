package com.microsoft.walletlibrary.requests.openid4vci

import android.util.Base64
import com.microsoft.walletlibrary.did.sdk.IdentifierService
import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.TokenSigner
import com.microsoft.walletlibrary.did.sdk.crypto.CryptoOperations
import com.microsoft.walletlibrary.did.sdk.crypto.DigestAlgorithm
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.networking.entities.openid4vci.RawOpenID4VCIResponse
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialConfiguration
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialMetadata
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOffer
import com.microsoft.walletlibrary.networking.entities.openid4vci.request.OpenID4VCIJWTProof
import com.microsoft.walletlibrary.networking.entities.openid4vci.request.RawOpenID4VCIRequest
import com.microsoft.walletlibrary.requests.RootOfTrust
import com.microsoft.walletlibrary.requests.requirements.AccessTokenRequirement
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdStyle
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.OpenId4VciRequestException
import com.microsoft.walletlibrary.util.OpenId4VciValidationException
import com.microsoft.walletlibrary.util.PreviewFeatureFlags
import com.microsoft.walletlibrary.util.UserCanceledException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.defaultTestSerializer
import com.microsoft.walletlibrary.verifiedid.OpenId4VciVerifiedId
import com.nimbusds.jose.jwk.JWK
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class OpenId4VciIssuanceRequestTest {
    private val mockRequesterStyle: RequesterStyle = mockk()
    private val mockRootOfTrust: RootOfTrust = mockk()
    private val mockVerifiedIdStyle: VerifiedIdStyle = mockk()
    private val mockCredentialOffer: CredentialOffer = mockk()
    private val mockCredentialMetadata: CredentialMetadata = mockk()
    private val mockCredentialConfiguration: CredentialConfiguration = mockk()
    private val mockHttpAgentApiProvider: HttpAgentApiProvider = mockk()
    private val mockPreviewFeatureFlags: PreviewFeatureFlags = mockk()
    private val mockedKeyStore: EncryptedKeyStore = mockk()
    private val mockedTokenSigner: TokenSigner = mockk()
    private val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    private val libraryConfiguration = LibraryConfiguration(
        mockPreviewFeatureFlags,
        mockHttpAgentApiProvider,
        defaultTestSerializer,
        mockedTokenSigner,
        clock
    )
    private val slot = slot<String>()
    private val mockedIdentifier: Identifier = mockk()
    private val mockIdentifierService: IdentifierService = mockk()
    private val signingKeyRef: String = "sigKeyRef1243523"
    private val expectedDid: String = "did:test:2354543"
    private val expectedJsonWebKey: JWK = JWK.parse(
        "{\"kty\":\"EC\"," +
                "\"crv\":\"secp256k1\",\"x\":\"WfY7Px6AgH6x-_dgAoRbg8weYRJA36ON-gQiFnETrqw\"," +
                "\"y\":\"IzFx3BUGztK0cyDStiunXbrZYYTtKbOUzx16SUK0sAY\"}"
    )

    // Arrange
    private val accessTokenRequirement = AccessTokenRequirement(
        "id",
        "name",
        resourceId = "resourceId",
        scope = "scope",
        claims = emptyList(),
        encrypted = false,
        required = true
    )
    private val openId4VciIssuanceRequest = spyk(
        OpenId4VciIssuanceRequest(
            mockRequesterStyle,
            accessTokenRequirement,
            mockRootOfTrust,
            mockVerifiedIdStyle,
            mockCredentialOffer,
            mockCredentialMetadata,
            mockCredentialConfiguration,
            libraryConfiguration
        )
    )
    private val mockCredentialEndpoint = "mockCredentialEndpoint"
    private val mockAccessToken = "testAccessToken"
    private val mockCredentialId = "mockCredentialId"
    private val mockIssuerSession = "mockIssuerSession"

    init {
        setUpGetPublicKey()
        mockCredentialOfferAndMetadata()
        mockVc()
        every { mockedKeyStore.getKey(signingKeyRef) } returns expectedJsonWebKey
        every {
            mockedTokenSigner.signWithIdentifier(
                capture(slot),
                mockedIdentifier,
                any()
            )
        } answers { slot.captured }
        mockkStatic(VerifiableCredentialSdk::class)
        every { VerifiableCredentialSdk.identifierService } returns mockIdentifierService
        coEvery { mockIdentifierService.getMasterIdentifier() } returns Result.Success(
            mockedIdentifier
        )
    }

    private fun mockVc() {
        val mockRequesterStyle = mockk<RequesterStyle>()
        every { mockRequesterStyle.name } returns "mock Issuer"
        every { mockCredentialMetadata.transformLocalizedIssuerDisplayDefinitionToRequesterStyle() } returns mockRequesterStyle
        val mockVc = mockk<com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential>()
        val mockVcContent = mockk<VerifiableCredentialContent>()
        every { mockVc.jti } returns "mockVcId"
        every { mockVc.contents } returns mockVcContent
        every { mockVcContent.iat } returns 10000L
        every { mockVcContent.exp } returns 10005L
        val mockVcDescriptor = mockk<VerifiableCredentialDescriptor>()
        every { mockVcContent.vc } returns mockVcDescriptor
        val mockType = "VerifiableCredential"
        every { mockVcDescriptor.type } returns listOf(mockType)
        every { mockCredentialConfiguration.getVerifiedIdStyleInPreferredLocale("mock Issuer") } returns mockk()
        coEvery { openId4VciIssuanceRequest["verifyAndUnWrapIssuanceResponse"](mockCredentialId) } returns mockVc
    }

    private fun mockCredentialOfferAndMetadata() {
        every { mockCredentialOffer.credential_configuration_ids } returns listOf(mockCredentialId)
        every { mockCredentialMetadata.credential_endpoint } returns mockCredentialEndpoint
        every { mockCredentialOffer.issuer_session } returns mockIssuerSession
    }

    private fun setUpGetPublicKey() {
        every { mockedIdentifier.signatureKeyReference } returns signingKeyRef
        every { mockedIdentifier.id } returns expectedDid
    }

    @Test
    fun isSatisfied_AccessTokenRequirementFulfilled_ReturnsTrue() {
        // Arrange
        accessTokenRequirement.fulfill("testAccessToken")

        // Act
        val actualResult = openId4VciIssuanceRequest.isSatisfied()

        // Assert
        assertThat(actualResult).isTrue
    }

    @Test
    fun isSatisfied_AccessTokenRequirementNotFulfilled_ReturnsFalse() {
        // Act
        val actualResult = openId4VciIssuanceRequest.isSatisfied()

        // Assert
        assertThat(actualResult).isFalse
    }

    @Test
    fun cancel_WithMessage_ThrowsExceptionWithProvidedMessage() {
        runBlocking {
            // Act
            val actual = openId4VciIssuanceRequest.cancel("test message")

            // Assert
            assertThat(actual.isFailure).isTrue
            val actualException = actual.exceptionOrNull()
            assertThat(actualException).isInstanceOf(UserCanceledException::class.java)
            assertThat(actualException?.message).isEqualTo("test message")
            assertThat((actualException as UserCanceledException).code).isEqualTo(
                VerifiedIdExceptions.USER_CANCELED_EXCEPTION.value
            )
        }
    }

    @Test
    fun cancel_WithNoMessage_ThrowsExceptionWithDefaultMessage() {
        runBlocking {
            // Act
            val actual = openId4VciIssuanceRequest.cancel(null)

            // Assert
            assertThat(actual.isFailure).isTrue
            val actualException = actual.exceptionOrNull()
            assertThat(actualException).isInstanceOf(UserCanceledException::class.java)
            assertThat(actualException?.message).isEqualTo("User Canceled")
            assertThat((actualException as UserCanceledException).code).isEqualTo(
                VerifiedIdExceptions.USER_CANCELED_EXCEPTION.value
            )
        }
    }

    @Test
    fun completeIssuance_NoAccessTokenInRequirement_ThrowsException() {
        runBlocking {
            // Act
            val actualResult = openId4VciIssuanceRequest.complete()

            // Assert
            assertThat(actualResult.isFailure).isTrue
            val actualException = actualResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
            assertThat(actualException?.message).isEqualTo("Access token is missing in requirement.")
            assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(
                VerifiedIdExceptions.REQUEST_CREATION_EXCEPTION.value
            )
        }
    }

    @Test
    fun completeIssuance_NoCredentialEndpointInMetadata_ThrowsException() {
        // Arrange
        accessTokenRequirement.fulfill("testAccessToken")
        every { mockCredentialMetadata.credential_endpoint } returns null
        runBlocking {
            // Act
            val actualResult = openId4VciIssuanceRequest.complete()

            // Assert
            assertThat(actualResult.isFailure).isTrue
            val actualException = actualResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
            assertThat(actualException?.message).isEqualTo("Credential endpoint is missing in credential metadata.")
            assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(
                VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
            )
        }
    }

    @Test
    fun completeIssuance_IssuanceAndConversionToVerifiedIdPasses_ReturnsOpenId4VciVerifiedId() {
        // Arrange
        val rawOpenID4VCIResponse = RawOpenID4VCIResponse(mockCredentialId, null)
        accessTokenRequirement.fulfill(mockAccessToken)
        val accessTokenHash = CryptoOperations.digest(
            mockAccessToken.toByteArray(StandardCharsets.US_ASCII),
            DigestAlgorithm.Sha256
        )
        val accessTokenPrefix = accessTokenHash.copyOfRange(0, 16)
        val encodedAccessToken = Base64.encodeToString(accessTokenPrefix, Constants.BASE64_URL_SAFE)
        val rawOpenID4VCIRequest = RawOpenID4VCIRequest(
            mockCredentialId,
            mockIssuerSession,
            OpenID4VCIJWTProof(
                "{\"aud\":\"$mockCredentialEndpoint\",\"iat\":\"${clock.millis() / 1000}\",\"sub\":\"$expectedDid\",\"at_hash\":\"$encodedAccessToken\"}",
            ),
        )
        runBlocking {
            delay(1000)
        }

        println("Expectation: $rawOpenID4VCIRequest")
        coEvery {
            openId4VciIssuanceRequest["sendIssuanceRequest"](
                mockCredentialEndpoint,
                rawOpenID4VCIRequest,
                mockAccessToken,
            )

            openId4VciIssuanceRequest["requestFormatter"]["format"]()
        } returns rawOpenID4VCIResponse

        runBlocking {
            // Act
            val actualResult = openId4VciIssuanceRequest.complete()

            // Assert
            assertThat(actualResult.isSuccess).isTrue
            val actualVerifiedId = actualResult.getOrNull()
            assertThat(actualVerifiedId).isInstanceOf(OpenId4VciVerifiedId::class.java)
            assertThat((actualVerifiedId as OpenId4VciVerifiedId).id).isEqualTo("mockVcId")
            assertThat(actualVerifiedId.types).contains("VerifiableCredential")
        }
    }

    @Test
    fun completeIssuance_IssuanceFails_ThrowsException() {
        // Arrange
        accessTokenRequirement.fulfill(mockAccessToken)
        coEvery {
            openId4VciIssuanceRequest["sendIssuanceRequest"](
                mockCredentialEndpoint,
                any<RawOpenID4VCIRequest>(),
                mockAccessToken
            )
        } throws OpenId4VciRequestException(
            "Failed to send issuance request.",
            VerifiedIdExceptions.REQUEST_SEND_EXCEPTION.value
        )
        runBlocking {
            // Act
            val actualResult = openId4VciIssuanceRequest.complete()

            // Assert
            assertThat(actualResult.isFailure).isTrue
            val actualException = actualResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciRequestException::class.java)
            assertThat(actualException?.message).containsIgnoringCase("Failed to send issuance request.")
            assertThat((actualException as OpenId4VciRequestException).code).isEqualTo(
                VerifiedIdExceptions.REQUEST_SEND_EXCEPTION.value
            )
        }
    }
}