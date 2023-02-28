package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.walletlibrary.verifiedid.VerifiedIdType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VerifiableCredentialMappingTest {
    private lateinit var verifiableCredentialDescriptor: VerifiableCredentialDescriptor
    private val expectedJti = "testvcjti"
    private val expectedRaw = "raw"
    private val expectedClaimName = "name"
    private val expectedClaimValue = "Test"
    private val expectedIssuedOn = 123456L
    private val expectedExpiry = 123480L
    private lateinit var verifiableCredentialContent: VerifiableCredentialContent
    private lateinit var verifiableCredential: VerifiableCredential

    init {
        setupInput()
    }

    private fun setupInput() {
        val expectedClaims = mutableMapOf<String, String>()
        expectedClaims[expectedClaimName] = expectedClaimValue
        verifiableCredentialDescriptor =
            VerifiableCredentialDescriptor(emptyList(), emptyList(), expectedClaims)
        verifiableCredentialContent = VerifiableCredentialContent(
            "testvccjti",
            verifiableCredentialDescriptor,
            "",
            "",
            expectedIssuedOn,
            expectedExpiry
        )
        verifiableCredential =
            VerifiableCredential(expectedJti, expectedRaw, verifiableCredentialContent)
    }

    @Test
    fun mapVerifiableCredential_ProvideVerifiableCredential_ReturnsVerifiedId() {
        val actualResult = verifiableCredential.toVerifiedId()
        assertThat(actualResult.id).isEqualTo(expectedJti)
        assertThat(actualResult.type).isEqualTo(VerifiedIdType.VERIFIABLE_CREDENTIAL)
        assertThat(actualResult.raw).isEqualTo(expectedRaw)
        assertThat(actualResult.claims.size).isEqualTo(1)
        assertThat(actualResult.claims.first().id).isEqualTo(expectedClaimName)
        assertThat(actualResult.claims.first().value).isEqualTo(expectedClaimValue)
        assertThat(actualResult.issuedOn).isEqualTo(expectedIssuedOn)
        assertThat(actualResult.expiresOn).isEqualTo(expectedExpiry)
    }

    @Test
    fun mapVerifiableCredential_ProvideVerifiableCredential_ThrowsException() {

        val expectedClaims = mutableMapOf<String, String>()
        expectedClaims[expectedClaimName] = expectedClaimValue
        verifiableCredentialDescriptor =
            VerifiableCredentialDescriptor(emptyList(), emptyList(), emptyMap())
        verifiableCredentialContent = VerifiableCredentialContent(
            "testvccjti",
            verifiableCredentialDescriptor,
            "",
            "",
            expectedIssuedOn,
            expectedExpiry
        )
        verifiableCredential =
            VerifiableCredential(expectedJti, expectedRaw, verifiableCredentialContent)

        val actualResult = verifiableCredential.toVerifiedId()
        assertThat(actualResult.id).isEqualTo(expectedJti)
        assertThat(actualResult.type).isEqualTo(VerifiedIdType.VERIFIABLE_CREDENTIAL)
        assertThat(actualResult.raw).isEqualTo(expectedRaw)
        assertThat(actualResult.claims.size).isEqualTo(0)
        assertThat(actualResult.issuedOn).isEqualTo(expectedIssuedOn)
        assertThat(actualResult.expiresOn).isEqualTo(expectedExpiry)
    }
}