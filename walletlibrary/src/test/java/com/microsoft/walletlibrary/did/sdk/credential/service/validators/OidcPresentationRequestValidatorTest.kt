/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.credential.service.validators

import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.Registration
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.VpFormats
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.controlflow.InvalidResponseModeException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.InvalidResponseTypeException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.InvalidScopeException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.MissingInputInRequestException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SubjectIdentifierTypeNotSupported
import com.microsoft.walletlibrary.did.sdk.util.controlflow.VpFormatNotSupported
import com.microsoft.walletlibrary.identifier.HolderIdentifier
import com.microsoft.walletlibrary.identifier.IdentifierFactory
import com.microsoft.walletlibrary.requests.requirements.PresentationRequestCryptoRequirement
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.Date
import kotlin.test.assertFailsWith

class OidcPresentationRequestValidatorTest {

    private val mockedPresentationRequest: PresentationRequest = mockk()

    private val mockedJwsToken: JwsToken = mockk()

    private val mockedJwtValidator: JwtValidator = mockk()

    private val mockedOidcRequestContent: PresentationRequestContent = mockk()

    private val mockedIdentifier: Identifier = mockk()

    private val mockedRegistration: Registration = mockk()

    private val expectedSerializedToken: String = "token2364302"

    private val legacyHolder = holder("did:ion", "ES256K")
    private val fipsHolder = holder("did:jwk", "ES256")
    private val identifierFactory = IdentifierFactory(mutableListOf(legacyHolder))
    private val validator = OidcPresentationRequestValidator(mockedJwtValidator, identifierFactory)

    private val expectedSigningKeyRef: String = "sigKeyRef1243523"
    private val expectedDid: String = "did:test:2354543"
    private val expectedValidResponseType = "id_token"
    private val expectedValidResponseMode = "post"
    private val expectedValidScope = "openid"

    private val expectedSubjectSyntaxTypesSupported = "did:ion"
    private val expectedAlgorithmSupported = "ES256K"

    private val expectedInvalidResponseMode = "invalid_response_mode"
    private val expectedInvalidResponseType = "invalid_response_type"
    private val expectedInvalidScope = "invalid_scope"

    init {
        setUpPresentationRequest()
        setUpIdentifier()
        mockkObject(JwsToken)
    }

    private fun setUpPresentationRequest() {
        every { mockedPresentationRequest.content } returns mockedOidcRequestContent
    }

    private fun setUpIdentifier() {
        every { mockedIdentifier.signatureKeyReference } returns expectedSigningKeyRef
        every { mockedIdentifier.id } returns expectedDid
    }

    private fun setUpExpiration(offsetInSecond: Long) {
        val currentTimeInSeconds: Long = Date().time / Constants.MILLISECONDS_IN_A_SECOND
        val currentTimePlusOffsetInSeconds = currentTimeInSeconds + offsetInSecond
        every { mockedOidcRequestContent.expirationTime } returns currentTimePlusOffsetInSeconds
    }

    private fun setUpOidcRequestContentWithValidFields() {
        every { mockedOidcRequestContent.responseType } returns expectedValidResponseType
        every { mockedOidcRequestContent.responseMode } returns expectedValidResponseMode
        every { mockedOidcRequestContent.scope } returns expectedValidScope
    }

    private fun setUpOidcRequestContentWithInvalidResponseMode() {
        every { mockedOidcRequestContent.responseType } returns expectedValidResponseType
        every { mockedOidcRequestContent.responseMode } returns expectedInvalidResponseMode
        every { mockedOidcRequestContent.scope } returns expectedValidScope
    }

    private fun setUpOidcRequestContentWithInvalidResponseType() {
        every { mockedOidcRequestContent.responseType } returns expectedInvalidResponseType
        every { mockedOidcRequestContent.responseMode } returns expectedValidResponseMode
        every { mockedOidcRequestContent.scope } returns expectedValidScope
    }

    private fun setUpOidcRequestContentWithInvalidScope() {
        every { mockedOidcRequestContent.responseType } returns expectedValidResponseType
        every { mockedOidcRequestContent.responseMode } returns expectedValidResponseMode
        every { mockedOidcRequestContent.scope } returns expectedInvalidScope
    }

    @Test
    fun `valid signature is validated successfully`() {
        // Arrange
        setUpExpiration(86400)
        every { mockedPresentationRequest.getPresentationDefinitions() } returns
            listOf(mockk {
                    every { credentialPresentationInputDescriptors } returns listOf(mockk())
                })
        every { mockedPresentationRequest.content } returns mockedOidcRequestContent
        every { mockedOidcRequestContent.idTokenHint } returns null
        every { mockedOidcRequestContent.registration } returns mockedRegistration
        every { mockedOidcRequestContent.pinDetails } returns null
        every { mockedRegistration.subjectSyntaxTypesSupported } returns listOf(expectedSubjectSyntaxTypesSupported)
        every { mockedRegistration.vpFormats?.jwtVp } returns listOf(expectedAlgorithmSupported)
        every { mockedRegistration.vpFormats?.jwtVc } returns listOf(expectedAlgorithmSupported)
        setUpOidcRequestContentWithValidFields()
        every { JwsToken.deserialize(expectedSerializedToken) } returns mockedJwsToken
        coEvery { mockedJwtValidator.verifySignature(mockedJwsToken) } returns true
        runBlocking {
            // Act
            validator.validate(mockedPresentationRequest)
        }
        // Assert
    }

    @Test
    fun `throws when request has invalid response mode`() {
        // Arrange
        setUpExpiration(86400)
        every { mockedPresentationRequest.getPresentationDefinitions() } returns
                listOf(mockk {
                    every { credentialPresentationInputDescriptors } returns listOf(mockk())
                })
        every { mockedPresentationRequest.content } returns mockedOidcRequestContent
        setUpOidcRequestContentWithInvalidResponseMode()
        runBlocking {
            try {
                // Act
                validator.validate(mockedPresentationRequest)
            } catch (exception: Exception) {
                // Assert
                assertThat(exception).isInstanceOf(InvalidResponseModeException::class.java)
            }
        }
    }

    @Test
    fun `throws when request has invalid response type`() {
        // Arrange
        setUpExpiration(86400)
        every { mockedPresentationRequest.getPresentationDefinitions() } returns
                listOf(mockk {
                    every { credentialPresentationInputDescriptors } returns listOf(mockk())
                })
        every { mockedPresentationRequest.content } returns mockedOidcRequestContent
        setUpOidcRequestContentWithInvalidResponseType()
        runBlocking {
            try {
                // Act
                validator.validate(mockedPresentationRequest)
            } catch (exception: Exception) {
                // Assert
                assertThat(exception).isInstanceOf(InvalidResponseTypeException::class.java)
            }
        }
    }

    @Test
    fun `throws when request has invalid scope`() {
        // Arrange
        setUpExpiration(86400)
        every { mockedPresentationRequest.getPresentationDefinitions() } returns
                listOf(mockk {
                    every { credentialPresentationInputDescriptors } returns listOf(mockk())
                })
        every { mockedPresentationRequest.content } returns mockedOidcRequestContent
        setUpOidcRequestContentWithInvalidScope()
        runBlocking {
            try {
                // Act
                validator.validate(mockedPresentationRequest)
            } catch (exception: Exception) {
                // Assert
                assertThat(exception).isInstanceOf(InvalidScopeException::class.java)
            }
        }
    }

    @Test
    fun `throws when request has missing input`() {
        // Arrange
        setUpExpiration(86400)
        every { mockedPresentationRequest.getPresentationDefinitions() } returns
            listOf(mockk {
                every { credentialPresentationInputDescriptors } returns emptyList()
            })
        every { mockedPresentationRequest.content } returns mockedOidcRequestContent
        setUpOidcRequestContentWithValidFields()
        runBlocking {
            try {
                // Act
                validator.validate(mockedPresentationRequest)
            } catch (exception: Exception) {
                // Assert
                assertThat(exception).isInstanceOf(MissingInputInRequestException::class.java)
            }
        }
    }

    private fun holder(method: String, algorithm: String): HolderIdentifier = mockk {
        every { this@mockk.method } returns method
        every { this@mockk.algorithm } returns algorithm
        every { id } returns "$method:holder"
    }

    private fun registration(
        methods: List<String> = listOf("did:jwk"),
        vp: List<String> = listOf("ES256"),
        vc: List<String> = listOf("ES256")
    ) = Registration(subjectSyntaxTypesSupported = methods, vpFormats = VpFormats(vp, vc))

    private fun validateRegistration(registration: Registration, holders: List<HolderIdentifier>) {
        identifierFactory.identifiers.clear()
        identifierFactory.identifiers.addAll(holders)
        setUpOidcRequestContentWithValidFields()
        every { mockedPresentationRequest.getPresentationDefinitions() } returns listOf(mockk {
            every { credentialPresentationInputDescriptors } returns listOf(mockk())
        })
        every { mockedOidcRequestContent.registration } returns registration
        every { mockedOidcRequestContent.pinDetails } returns null
        runBlocking { validator.validate(mockedPresentationRequest) }
    }

    @Test
    fun `JWK holder accepts ESTS FIPS algorithms without ES256K`() {
        val algorithms = listOf("ES256", "ES384", "ES512", "EdDSA")
        validateRegistration(registration(vp = algorithms, vc = algorithms), listOf(fipsHolder))
    }

    @Test
    fun `legacy only registration and holder remain supported`() {
        validateRegistration(registration(listOf("did:ion"), listOf("ES256K"), listOf("ES256K")), listOf(legacyHolder))
    }

    @Test
    fun `mixed legacy metadata preserves existing default holder selection`() {
        val registration = registration(listOf("did:ion", "did:jwk"), listOf("ES256K"), listOf("ES256K"))
        validateRegistration(registration, listOf(fipsHolder, legacyHolder))
        assertThat(PresentationRequestCryptoRequirement(registration).getIdentifier(identifierFactory)).isSameAs(fipsHolder)
    }

    @Test
    fun `mixed legacy metadata remains accepted with FIPS only or legacy only inventory`() {
        val registration = registration(listOf("did:ion", "did:jwk"), listOf("ES256K"), listOf("ES256K"))
        for (holder in listOf(fipsHolder, legacyHolder)) {
            validateRegistration(registration, listOf(holder))
            assertThat(PresentationRequestCryptoRequirement(registration).getIdentifier(identifierFactory)).isSameAs(holder)
        }
    }

    @Test
    fun `legacy metadata retains preexisting validation without holder negotiation`() {
        validateRegistration(registration(listOf("did:ion"), listOf("ES256K"), listOf("ES256K")), listOf(fipsHolder))
        validateRegistration(registration(listOf("did:ion"), listOf("ES256K"), listOf("ES256K")), emptyList())
    }

    @Test
    fun `JWK registration selects compatible holder instead of default legacy holder`() {
        validateRegistration(registration(), listOf(legacyHolder, fipsHolder))
        assertThat(PresentationRequestCryptoRequirement(registration()).getIdentifier(identifierFactory)).isSameAs(fipsHolder)
    }

    @Test
    fun `mixed metadata accepts FIPS only holder when ES256 is advertised`() {
        validateRegistration(
            registration(listOf("did:ion", "did:jwk"), listOf("ES256K", "ES256"), listOf("ES256K")),
            listOf(fipsHolder)
        )
    }

    @Test
    fun `issuer credential algorithm need not equal holder signing algorithm`() {
        validateRegistration(registration(vc = listOf("ES384")), listOf(fipsHolder))
    }

    @Test
    fun `unsupported or empty subject methods are rejected`() {
        for (methods in listOf(emptyList(), listOf("did:key"), listOf("did:jwk:unexpected"))) {
            assertFailsWith<SubjectIdentifierTypeNotSupported> {
                validateRegistration(registration(methods), listOf(fipsHolder))
            }
        }
    }

    @Test
    fun `no configured holder is rejected`() {
        assertFailsWith<SubjectIdentifierTypeNotSupported> {
            validateRegistration(registration(), emptyList())
        }
    }

    @Test
    fun `VP algorithm requires intersection with the same holder as the method`() {
        assertFailsWith<VpFormatNotSupported> {
            validateRegistration(registration(vp = listOf("ES256K")), listOf(fipsHolder, legacyHolder))
        }
    }

    @Test
    fun `unsupported or empty VP and VC algorithms are rejected`() {
        for (algorithms in listOf(emptyList(), listOf("RS256"), listOf("none"))) {
            assertFailsWith<VpFormatNotSupported> {
                validateRegistration(registration(vp = algorithms), listOf(fipsHolder))
            }
            assertFailsWith<VpFormatNotSupported> {
                validateRegistration(registration(vc = algorithms), listOf(fipsHolder))
            }
        }
    }

    @Test
    fun `advertised EdDSA does not enable an unsupported holder signer`() {
        assertFailsWith<VpFormatNotSupported> {
            validateRegistration(registration(vp = listOf("EdDSA")), listOf(holder("did:jwk", "EdDSA")))
        }
    }

    @Test
    fun `omitted formats retain legacy optional behavior`() {
        validateRegistration(Registration(subjectSyntaxTypesSupported = listOf("did:jwk")), listOf(fipsHolder))
        validateRegistration(Registration(subjectSyntaxTypesSupported = listOf("did:ion")), listOf(legacyHolder))
    }

    @Test
    fun `holder selection does not switch away from the credential subject`() {
        val registration = registration(listOf("did:ion", "did:jwk"), listOf("ES256", "ES256K"), listOf("ES256"))
        val crypto = PresentationRequestCryptoRequirement(registration)
        val factory = IdentifierFactory(mutableListOf(fipsHolder, legacyHolder))
        assertThat(crypto.getIdentifier(factory, legacyHolder.id)).isSameAs(legacyHolder)
        assertFailsWith<SubjectIdentifierTypeNotSupported> { crypto.getIdentifier(factory, "did:ion:missing") }
        assertFailsWith<VpFormatNotSupported> {
            PresentationRequestCryptoRequirement(registration(vp = listOf("ES256K")))
                .getIdentifier(factory, fipsHolder.id)
        }
    }

    @Test
    fun `single holder response preserves legacy selection but binds new requests to credential subject`() {
        val factory = IdentifierFactory(mutableListOf(legacyHolder, fipsHolder))
        val legacy = PresentationRequestCryptoRequirement(
            registration(listOf("did:ion", "did:jwk"), listOf("ES256K"), listOf("ES256K"))
        )
        assertThat(legacy.getResponseIdentifier(factory, listOf(fipsHolder.id))).isSameAs(legacyHolder)

        val jwk = PresentationRequestCryptoRequirement(registration())
        assertThat(jwk.getResponseIdentifier(factory, listOf(fipsHolder.id, fipsHolder.id))).isSameAs(fipsHolder)
        assertFailsWith<SubjectIdentifierTypeNotSupported> {
            jwk.getResponseIdentifier(factory, listOf(legacyHolder.id))
        }
        assertFailsWith<SubjectIdentifierTypeNotSupported> {
            jwk.getResponseIdentifier(factory, listOf(legacyHolder.id, fipsHolder.id))
        }
    }
}
