package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.did.sdk.util.controlflow.NetworkException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialConfiguration
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialMetadata
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOffer
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOfferGrant
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOfferPinDetails
import com.microsoft.walletlibrary.requests.RootOfTrust
import com.microsoft.walletlibrary.requests.openid4vci.OpenId4VciIssuanceRequest
import com.microsoft.walletlibrary.requests.requirements.AccessTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.OpenId4VCIPinRequirement
import com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdManifestIssuerStyle
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.OpenId4VciRequestException
import com.microsoft.walletlibrary.util.OpenId4VciValidationException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.defaultTestSerializer
import io.mockk.coEvery
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.Result as KotlinResult

class OpenId4VCIRequestHandlerTest {
    private val mockLibraryConfiguration = mockk<LibraryConfiguration>()
    private val mockSignedMetadataProcessor = mockk<SignedMetadataProcessor>()
    private val mockCredentialMetadata = mockk<CredentialMetadata>()
    private val mockCredentialOffer = mockk<CredentialOffer>()
    private val mockCredentialConfiguration = mockk<CredentialConfiguration>()
    private val mockCredentialOfferGrant = mockk<CredentialOfferGrant>()
    private val openId4VCIRequestHandler =
        spyk(OpenId4VCIRequestHandler(mockLibraryConfiguration, mockSignedMetadataProcessor))
    private val credentialIssuerEndpoint = "https://example.com/credential-issuer"
    private val tokenEndpoint = "https://example.com/token-endpoint"
    private val expectedCredentialOfferString = """
        {
            "credential_issuer": "$credentialIssuerEndpoint",
            "issuer_session": "request_state",
            "credential_configuration_ids": [
                "credential_id"
            ],
            "grants": {
                "authorization_code": {
                    "authorization_server": "authorization_server"
                }
            }
        }
    """.trimIndent()

    @Test
    fun canHandleTest_ValidCredentialOfferAsString_ReturnsTrue() {
        // Arrange
        every { mockLibraryConfiguration.serializer } returns defaultTestSerializer

        //Act
        val actualCanHandleResult =
            openId4VCIRequestHandler.canHandle(expectedCredentialOfferString)

        // Assert
        assertThat(actualCanHandleResult).isEqualTo(true)
    }

    @Test
    fun canHandleTest_InvalidCredentialOfferAsString_ReturnsFalse() {
        // Arrange
        every { mockLibraryConfiguration.serializer } returns defaultTestSerializer
        val invalidCredentialOfferString = "invalid_credential_offer"

        //Act
        val actualCanHandleResult = openId4VCIRequestHandler.canHandle(invalidCredentialOfferString)

        // Assert
        assertThat(actualCanHandleResult).isEqualTo(false)
    }

    @Test
    fun canHandleTest_EmptyStringAsRequest_ReturnsFalse() {
        // Arrange
        every { mockLibraryConfiguration.serializer } returns defaultTestSerializer
        val invalidCredentialOfferString = ""

        //Act
        val actualCanHandleResult = openId4VCIRequestHandler.canHandle(invalidCredentialOfferString)

        // Assert
        assertThat(actualCanHandleResult).isEqualTo(false)
    }

    @Test
    fun canHandleTest_AnyFailureWithSerializer_ReturnsFalse() {
        //Act
        val actualCanHandleResult =
            openId4VCIRequestHandler.canHandle(expectedCredentialOfferString)

        // Assert
        assertThat(actualCanHandleResult).isEqualTo(false)
    }

    @Test
    fun handleRequestTest_AnyFailureWithSerializer_ThrowsException() {
        runBlocking {
            //Act
            val actualHandleRequestResult = runCatching {
                openId4VCIRequestHandler.handleRequest(expectedCredentialOfferString)
            }
            // Assert
            assertThat(actualHandleRequestResult.isFailure).isTrue
            val actualException = actualHandleRequestResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
            assertThat(actualException?.message).contains("Failed to decode CredentialOffer")
            assertThat((actualException as OpenId4VciValidationException).innerError).isNotNull
        }
    }

    @Test
    fun handleRequestTest_AnyFailureWithFetchingMetadata_ThrowsException() {
        // Arrange
        every { mockLibraryConfiguration.serializer } returns defaultTestSerializer
        coEvery { openId4VCIRequestHandler["fetchCredentialMetadata"](credentialIssuerEndpoint) } returns KotlinResult.failure<SdkException>(
            NetworkException("Failed to fetch metadata", false)
        )

        runBlocking {
            //Act
            val actualHandleRequestResult = runCatching {
                openId4VCIRequestHandler.handleRequest(expectedCredentialOfferString)
            }
            // Assert
            assertThat(actualHandleRequestResult.isFailure).isTrue
            val actualException = actualHandleRequestResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciRequestException::class.java)
            assertThat(actualException?.message).contains("Failed to fetch credential metadata")
            assertThat((actualException as OpenId4VciRequestException).innerError).isInstanceOf(
                NetworkException::class.java
            )
        }
    }

    @Test
    fun handleRequestTest_ValidateCredentialMetadataNoCredentialIssuer_ThrowsException() {
        // Arrange
        every { mockLibraryConfiguration.serializer } returns defaultTestSerializer
        mockFetchCredentialMetadata()
        mockFetchWellKnownConfiguration()
        every { mockCredentialMetadata.verifyIfCredentialIssuerExist() } throws OpenId4VciValidationException(
            "Credential metadata does not contain credential_issuer.",
            VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
        )

        runBlocking {
            //Act
            val actualHandleRequestResult =
                runCatching { openId4VCIRequestHandler.handleRequest(expectedCredentialOfferString) }

            // Assert
            assertThat(actualHandleRequestResult.isFailure).isTrue
            val actualException = actualHandleRequestResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
            assertThat(actualException?.message).contains("Credential metadata does not contain credential_issuer.")
            assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(
                VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
            )
        }
    }

    @Test
    fun handleRequestTest_ValidateCredentialMetadataNoSignedMetadata_ThrowsException() {
        // Arrange
        every { mockLibraryConfiguration.serializer } returns defaultTestSerializer
        mockFetchCredentialMetadata()
        mockFetchWellKnownConfiguration()
        justRun { mockCredentialMetadata.verifyIfCredentialIssuerExist() }
        every { mockCredentialMetadata.verifyIfSignedMetadataExist() } throws OpenId4VciValidationException(
            "Credential metadata does not contain signed_metadata.",
            VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
        )

        runBlocking {
            //Act
            val actualHandleRequestResult =
                runCatching { openId4VCIRequestHandler.handleRequest(expectedCredentialOfferString) }

            // Assert
            assertThat(actualHandleRequestResult.isFailure).isTrue
            val actualException = actualHandleRequestResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
            assertThat(actualException?.message).contains("Credential metadata does not contain signed_metadata.")
            assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(
                VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
            )
        }
    }

    @Test
    fun handleRequestTest_GetSupportedConfigurationIdsEmptyConfig_ThrowsException() {
        // Arrange
        every { mockLibraryConfiguration.serializer } returns defaultTestSerializer
        every { mockCredentialOffer.credential_issuer } returns credentialIssuerEndpoint
        mockFetchCredentialMetadata()
        mockFetchWellKnownConfiguration()
        every { openId4VCIRequestHandler["decodeCredentialOffer"](expectedCredentialOfferString) } returns mockCredentialOffer
        justRun { mockCredentialMetadata.verifyIfCredentialIssuerExist() }
        justRun { mockCredentialMetadata.verifyIfSignedMetadataExist() }
        justRun { mockCredentialMetadata.validateAuthorizationServers(mockCredentialOffer) }
        every { mockCredentialOffer.credential_configuration_ids } returns emptyList()
        every { mockCredentialMetadata.getSupportedCredentialConfigurations(emptyList()) } returns emptyList()

        runBlocking {
            //Act
            val actualHandleRequestResult =
                runCatching { openId4VCIRequestHandler.handleRequest(expectedCredentialOfferString) }

            // Assert
            assertThat(actualHandleRequestResult.isFailure).isTrue
            val actualException = actualHandleRequestResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
            assertThat(actualException?.message).contains("Request does not contain supported credential configuration.")
            assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(
                VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
            )
        }
    }

    @Test
    fun handleRequestTest_ValidVerifiedIdTransformationsWithAuthFlowGrant_ReturnsVerifiedIdRequest() {
        // Arrange
        every { mockLibraryConfiguration.serializer } returns defaultTestSerializer
        mockCredentialMetadata()
        mockCredentialOfferForAuthFlow()
        mockCredentialConfiguration()
        mockFetchCredentialMetadata()
        mockFetchWellKnownConfiguration()
        every { openId4VCIRequestHandler["decodeCredentialOffer"](expectedCredentialOfferString) } returns mockCredentialOffer
        coEvery {
            mockSignedMetadataProcessor.process(
                any(),
                credentialIssuerEndpoint
            )
        } returns RootOfTrust("root_of_trust", false)

        runBlocking {
            //Act
            val actualHandleRequestResult =
                runCatching { openId4VCIRequestHandler.handleRequest(expectedCredentialOfferString) }

            // Assert
            assertThat(actualHandleRequestResult.isSuccess).isTrue
            val actualVerifiedIdRequest = actualHandleRequestResult.getOrNull()
            assertThat(actualVerifiedIdRequest).isNotNull
            assertThat(actualVerifiedIdRequest).isInstanceOf(OpenId4VciIssuanceRequest::class.java)
            assertThat(actualVerifiedIdRequest?.requirement).isInstanceOf(AccessTokenRequirement::class.java)
            assertThat((actualVerifiedIdRequest?.requirement as AccessTokenRequirement).scope).isEqualTo(
                "scope/.default"
            )
            assertThat((actualVerifiedIdRequest.requirement as AccessTokenRequirement).resourceId).isEqualTo(
                "scope"
            )
            assertThat((actualVerifiedIdRequest.requirement as AccessTokenRequirement).configuration).isEqualTo(
                mockCredentialOfferGrant.authorizationServer
            )
        }
    }

    @Test
    fun handleRequestTest_ValidVerifiedIdTransformationsWithPreAuthFlowGrantAndNoPin_ReturnsVerifiedIdRequest() {
        // Arrange
        every { mockLibraryConfiguration.serializer } returns defaultTestSerializer
        mockCredentialMetadata()
        mockCredentialOfferForPreAuthFlow()
        mockCredentialConfiguration()
        mockFetchCredentialMetadata()
        mockFetchWellKnownConfiguration()
        every { openId4VCIRequestHandler["decodeCredentialOffer"](expectedCredentialOfferString) } returns mockCredentialOffer
        coEvery {
            mockSignedMetadataProcessor.process(
                any(),
                credentialIssuerEndpoint
            )
        } returns RootOfTrust("root_of_trust", false)
        coEvery {
            openId4VCIRequestHandler["preValidatePinRequirement"](any<OpenId4VCIPinRequirement>())
        } returns Unit

        runBlocking {
            //Act
            val actualHandleRequestResult =
                runCatching { openId4VCIRequestHandler.handleRequest(expectedCredentialOfferString) }

            // Assert
            assertThat(actualHandleRequestResult.isSuccess).isTrue
            val actualVerifiedIdRequest = actualHandleRequestResult.getOrNull()
            assertThat(actualVerifiedIdRequest).isNotNull
            assertThat(actualVerifiedIdRequest).isInstanceOf(OpenId4VciIssuanceRequest::class.java)
            assertThat(actualVerifiedIdRequest?.requirement).isInstanceOf(OpenId4VCIPinRequirement::class.java)
            val requirement = actualVerifiedIdRequest!!.requirement as OpenId4VCIPinRequirement
            assertThat(requirement.accessTokenEndpoint).isEqualTo(tokenEndpoint)
            assertThat(requirement.preAuthorizedCode).isNotNull()
            assertThat(requirement.preAuthorizedCode).isEqualTo(mockCredentialOfferGrant.preAuthorizedCode)
            assertThat(requirement.length).isNull()
            assertThat(requirement.type).isNull()
            assertThat(requirement.pinSet).isFalse()
        }
    }

    @Test
    fun handleRequestTest_ValidVerifiedIdTransformationsWithPreAuthFlowGrantAndPin_ReturnsVerifiedIdRequest() {
        // Arrange
        val pinType = "numeric"
        val pinLength = 6
        every { mockLibraryConfiguration.serializer } returns defaultTestSerializer
        mockCredentialMetadata()
        mockCredentialOfferForPreAuthFlow(CredentialOfferPinDetails(pinLength, pinType))
        mockCredentialConfiguration()
        mockFetchCredentialMetadata()
        mockFetchWellKnownConfiguration()
        every { openId4VCIRequestHandler["decodeCredentialOffer"](expectedCredentialOfferString) } returns mockCredentialOffer
        coEvery {
            mockSignedMetadataProcessor.process(
                any(),
                credentialIssuerEndpoint
            )
        } returns RootOfTrust("root_of_trust", false)

        runBlocking {
            //Act
            val actualHandleRequestResult =
                runCatching { openId4VCIRequestHandler.handleRequest(expectedCredentialOfferString) }

            // Assert
            assertThat(actualHandleRequestResult.isSuccess).isTrue
            val actualVerifiedIdRequest = actualHandleRequestResult.getOrNull()
            assertThat(actualVerifiedIdRequest).isNotNull
            assertThat(actualVerifiedIdRequest).isInstanceOf(OpenId4VciIssuanceRequest::class.java)
            assertThat(actualVerifiedIdRequest?.requirement).isInstanceOf(OpenId4VCIPinRequirement::class.java)
            val requirement = actualVerifiedIdRequest!!.requirement as OpenId4VCIPinRequirement
            assertThat(requirement.accessTokenEndpoint).isEqualTo(tokenEndpoint)
            assertThat(requirement.preAuthorizedCode).isNotNull()
            assertThat(requirement.preAuthorizedCode).isEqualTo(mockCredentialOfferGrant.preAuthorizedCode)
            assertThat(requirement.length).isEqualTo(pinLength)
            assertThat(requirement.type).isEqualTo(pinType)
            assertThat(requirement.pinSet).isTrue()
        }
    }

    @Test
    fun handleRequestTest_ValidVerifiedIdTransformationsWithBothGrants_ReturnsVerifiedIdRequest() {
        // Arrange
        every { mockLibraryConfiguration.serializer } returns defaultTestSerializer
        mockCredentialMetadata()
        mockCredentialOfferForBothGrants()
        mockCredentialConfiguration()
        mockFetchCredentialMetadata()
        mockFetchWellKnownConfiguration()
        every { openId4VCIRequestHandler["decodeCredentialOffer"](expectedCredentialOfferString) } returns mockCredentialOffer
        coEvery {
            mockSignedMetadataProcessor.process(
                any(),
                credentialIssuerEndpoint
            )
        } returns RootOfTrust("root_of_trust", false)
        coEvery {
            openId4VCIRequestHandler["preValidatePinRequirement"](any<OpenId4VCIPinRequirement>())
        } returns Unit

        runBlocking {
            //Act
            val actualHandleRequestResult =
                runCatching { openId4VCIRequestHandler.handleRequest(expectedCredentialOfferString) }

            // Assert
            assertThat(actualHandleRequestResult.isSuccess).isTrue
            val actualVerifiedIdRequest = actualHandleRequestResult.getOrNull()
            assertThat(actualVerifiedIdRequest).isNotNull
            assertThat(actualVerifiedIdRequest).isInstanceOf(OpenId4VciIssuanceRequest::class.java)
            assertThat(actualVerifiedIdRequest?.requirement).isInstanceOf(GroupRequirement::class.java)
            val requirement = actualVerifiedIdRequest!!.requirement as GroupRequirement
            assertThat(requirement.requirements).hasSize(2)
            val accessTokenRequirement = requirement.requirements.find { it is AccessTokenRequirement } as AccessTokenRequirement
            assertThat(accessTokenRequirement).isNotNull()
            assertThat(accessTokenRequirement.scope).isEqualTo("scope/.default")
            assertThat(accessTokenRequirement.resourceId).isEqualTo("scope")
            assertThat(accessTokenRequirement.configuration).isEqualTo(mockCredentialOfferGrant.authorizationServer)
            val openId4VCIPinRequirement = requirement.requirements.find { it is OpenId4VCIPinRequirement } as OpenId4VCIPinRequirement
            assertThat(openId4VCIPinRequirement).isNotNull()
            assertThat(openId4VCIPinRequirement.accessTokenEndpoint).isEqualTo(tokenEndpoint)
            assertThat(openId4VCIPinRequirement.preAuthorizedCode).isNotNull()
            assertThat(openId4VCIPinRequirement.preAuthorizedCode).isEqualTo(mockCredentialOfferGrant.preAuthorizedCode)
            assertThat(openId4VCIPinRequirement.length).isNull()
            assertThat(openId4VCIPinRequirement.type).isNull()
            assertThat(openId4VCIPinRequirement.pinSet).isFalse()
        }
    }

    @Test
    fun handleRequestTest_NoGrantsDefined_ThrowsException() {
        // Arrange
        every { mockLibraryConfiguration.serializer } returns defaultTestSerializer
        mockCredentialMetadata()
        mockCredentialOfferMissingGrant()
        mockCredentialConfiguration()
        mockFetchCredentialMetadata()
        mockFetchWellKnownConfiguration()
        every { openId4VCIRequestHandler["decodeCredentialOffer"](expectedCredentialOfferString) } returns mockCredentialOffer
        coEvery {
            mockSignedMetadataProcessor.process(
                any(),
                credentialIssuerEndpoint
            )
        } returns RootOfTrust("root_of_trust", false)

        runBlocking {
            //Act
            val actualHandleRequestResult =
                runCatching { openId4VCIRequestHandler.handleRequest(expectedCredentialOfferString) }

            // Assert
            assertThat(actualHandleRequestResult.isFailure).isTrue
            val actualException = actualHandleRequestResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
            assertThat(actualException?.message).contains("No grants defined in credential offer.")
            assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(
                VerifiedIdExceptions.REQUIREMENT_MISSING_EXCEPTION.value
            )
        }
    }

    @Test
    fun handleRequestTest_NullScope_ThrowsException() {
        // Arrange
        every { mockLibraryConfiguration.serializer } returns defaultTestSerializer
        mockCredentialMetadata()
        mockCredentialOfferForAuthFlow()
        mockCredentialConfigurationNullScope()
        mockFetchCredentialMetadata()
        mockFetchWellKnownConfiguration()
        every { openId4VCIRequestHandler["decodeCredentialOffer"](expectedCredentialOfferString) } returns mockCredentialOffer
        coEvery {
            mockSignedMetadataProcessor.process(
                any(),
                credentialIssuerEndpoint
            )
        } returns RootOfTrust("root_of_trust", false)

        runBlocking {
            //Act
            val actualHandleRequestResult =
                runCatching { openId4VCIRequestHandler.handleRequest(expectedCredentialOfferString) }

            // Assert
            assertThat(actualHandleRequestResult.isFailure).isTrue
            val actualException = actualHandleRequestResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
            assertThat(actualException?.message).contains("Credential configuration in credential metadata doesn't contain scope value.")
            assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(
                VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
            )
        }
    }

    private fun mockCredentialMetadata() {
        val mockRequesterStyle = mockk<VerifiedIdManifestIssuerStyle>()
        justRun { mockCredentialMetadata.verifyIfCredentialIssuerExist() }
        justRun { mockCredentialMetadata.verifyIfSignedMetadataExist() }
        justRun { mockCredentialMetadata.validateAuthorizationServers(mockCredentialOffer) }
        every { mockCredentialMetadata.signedMetadata } returns "signed_metadata"
        every { mockCredentialMetadata.credentialIssuer } returns credentialIssuerEndpoint
        every { mockCredentialMetadata.getSupportedCredentialConfigurations(listOf("credential_id")) } returns listOf(
            mockCredentialConfiguration
        )
        every { mockCredentialMetadata.transformLocalizedIssuerDisplayDefinitionToRequesterStyle() } returns mockRequesterStyle
        every { mockRequesterStyle.name } returns "Issuer Name"
    }

    private fun mockCredentialOfferForAuthFlow() {
        every { mockCredentialOffer.credential_issuer } returns credentialIssuerEndpoint
        every { mockCredentialOffer.credential_configuration_ids } returns listOf("credential_id")
        every { mockCredentialOffer.grants["authorization_code"] } returns mockCredentialOfferGrant
        every { mockCredentialOffer.grants["urn:ietf:params:oauth:grant-type:pre-authorized_code"] } returns null
        every { mockCredentialOfferGrant.authorizationServer } returns "authorization_server"
    }

    private fun mockCredentialOfferForPreAuthFlow(credentialOfferPinDetails: CredentialOfferPinDetails? = null) {
        every { mockCredentialOffer.credential_issuer } returns credentialIssuerEndpoint
        every { mockCredentialOffer.credential_configuration_ids } returns listOf("credential_id")
        every { mockCredentialOffer.grants["authorization_code"] } returns null
        every { mockCredentialOffer.grants["urn:ietf:params:oauth:grant-type:pre-authorized_code"] } returns mockCredentialOfferGrant
        every { mockCredentialOfferGrant.authorizationServer } returns "authorization_server"
        every { mockCredentialOfferGrant.txCode } returns credentialOfferPinDetails
        every { mockCredentialOfferGrant.preAuthorizedCode } returns "preAuthCode"
    }

    private fun mockCredentialOfferForBothGrants(credentialOfferPinDetails: CredentialOfferPinDetails? = null) {
        every { mockCredentialOffer.credential_issuer } returns credentialIssuerEndpoint
        every { mockCredentialOffer.credential_configuration_ids } returns listOf("credential_id")
        every { mockCredentialOffer.grants["authorization_code"] } returns mockCredentialOfferGrant
        every { mockCredentialOfferGrant.authorizationServer } returns "authorization_server"
        every { mockCredentialOffer.grants["urn:ietf:params:oauth:grant-type:pre-authorized_code"] } returns mockCredentialOfferGrant
        every { mockCredentialOfferGrant.txCode } returns credentialOfferPinDetails
        every { mockCredentialOfferGrant.preAuthorizedCode } returns "preAuthCode"
    }

    private fun mockCredentialOfferMissingGrant() {
        every { mockCredentialOffer.credential_issuer } returns credentialIssuerEndpoint
        every { mockCredentialOffer.credential_configuration_ids } returns listOf("credential_id")
        every { mockCredentialOffer.grants["authorization_code"] } returns null
        every { mockCredentialOffer.grants["urn:ietf:params:oauth:grant-type:pre-authorized_code"] } returns null
        every { mockCredentialOfferGrant.authorizationServer } returns "authorization_server"
    }

    private fun mockCredentialConfiguration() {
        val mockVerifiedIdStyle = mockk<BasicVerifiedIdStyle>()
        every { mockCredentialConfiguration.scope } returns "scope"
        every { mockCredentialConfiguration.getVerifiedIdStyleInPreferredLocale(any()) } returns mockVerifiedIdStyle
    }

    private fun mockCredentialConfigurationNullScope() {
        val mockVerifiedIdStyle = mockk<BasicVerifiedIdStyle>()
        every { mockCredentialConfiguration.scope } returns null
        every { mockCredentialConfiguration.getVerifiedIdStyleInPreferredLocale(any()) } returns mockVerifiedIdStyle
    }

    private fun mockFetchCredentialMetadata() {
        coEvery { openId4VCIRequestHandler["fetchCredentialMetadata"](credentialIssuerEndpoint) } returns KotlinResult.success(
            mockCredentialMetadata
        )
    }

    private fun mockFetchWellKnownConfiguration() {
        coEvery {
            openId4VCIRequestHandler["fetchAccessTokenEndpointFromOpenIdWellKnownConfig"](credentialIssuerEndpoint)
        } returns tokenEndpoint
    }
}