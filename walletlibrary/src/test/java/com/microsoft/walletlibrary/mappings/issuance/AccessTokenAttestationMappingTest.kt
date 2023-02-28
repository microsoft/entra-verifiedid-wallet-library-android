package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.models.attestations.AccessTokenAttestation
import com.microsoft.did.sdk.credential.service.models.attestations.ClaimAttestation
import org.assertj.core.api.Assertions
import org.junit.Test

class AccessTokenAttestationMappingTest {
    private val claimAttestations = mutableListOf(ClaimAttestation("name", true, "string"))
    private val expectedConfiguration: String = "https://testconfiguration.com"
    private val expectedRedirectUri = "testRedirectUri"
    private val expectedResourceId = "testResourceId"
    private val expectedScope = "testScope"
    private lateinit var actualAccessTokenAttestation: AccessTokenAttestation

    init {
        setupInput(claimAttestations, required = false, encrypted = false)
    }

    private fun setupInput(
        claimAttestations: List<ClaimAttestation>,
        required: Boolean,
        encrypted: Boolean
    ) {
        actualAccessTokenAttestation = AccessTokenAttestation(
            claimAttestations,
            expectedConfiguration,
            expectedResourceId,
            required = required,
            expectedRedirectUri,
            expectedScope,
            encrypted = encrypted
        )
    }

    @Test
    fun accessTokenMapping_RequiredAndEncryptedFalse_Succeeds() {
        // Arrange
        val expectedClaimName = "name"

        // Act
        val actualAccessTokenRequirement = actualAccessTokenAttestation.toAccessTokenRequirement()

        // Assert
        Assertions.assertThat(actualAccessTokenRequirement.claims.first().claim).isEqualTo(expectedClaimName)
        Assertions.assertThat(actualAccessTokenRequirement.claims.first().required).isEqualTo(true)
        Assertions.assertThat(actualAccessTokenRequirement.required).isEqualTo(false)
        Assertions.assertThat(actualAccessTokenRequirement.encrypted).isEqualTo(false)
        Assertions.assertThat(actualAccessTokenRequirement.configuration).isEqualTo(expectedConfiguration)
        Assertions.assertThat(actualAccessTokenRequirement.redirectUri).isEqualTo(expectedRedirectUri)
        Assertions.assertThat(actualAccessTokenRequirement.resourceId).isEqualTo(expectedResourceId)
        Assertions.assertThat(actualAccessTokenRequirement.scope).isEqualTo(expectedScope)

    }

    @Test
    fun accessTokenMapping_RequiredAndEncryptedTrue_Succeeds() {
        // Arrange
        setupInput(claimAttestations, required = true, encrypted = true)
        val expectedClaimName = "name"

        // Act
        val actualAccessTokenRequirement = actualAccessTokenAttestation.toAccessTokenRequirement()

        // Assert
        Assertions.assertThat(actualAccessTokenRequirement.claims.first().claim).isEqualTo(expectedClaimName)
        Assertions.assertThat(actualAccessTokenRequirement.claims.first().required).isEqualTo(true)
        Assertions.assertThat(actualAccessTokenRequirement.required).isEqualTo(true)
        Assertions.assertThat(actualAccessTokenRequirement.encrypted).isEqualTo(true)
        Assertions.assertThat(actualAccessTokenRequirement.configuration).isEqualTo(expectedConfiguration)
        Assertions.assertThat(actualAccessTokenRequirement.redirectUri).isEqualTo(expectedRedirectUri)
        Assertions.assertThat(actualAccessTokenRequirement.resourceId).isEqualTo(expectedResourceId)
        Assertions.assertThat(actualAccessTokenRequirement.scope).isEqualTo(expectedScope)
    }

    @Test
    fun accessTokenMapping_ListOfClaims_Succeeds() {
        // Arrange
        claimAttestations.add(ClaimAttestation("company", true, "string"))
        setupInput(claimAttestations, required = true, encrypted = true)
        val expectedClaimNames = listOf("name", "company")

        // Act
        val actualAccessTokenRequirement = actualAccessTokenAttestation.toAccessTokenRequirement()

        // Assert
        Assertions.assertThat(actualAccessTokenRequirement.claims.map { it.claim }
            .containsAll(expectedClaimNames)).isEqualTo(true)
        Assertions.assertThat(actualAccessTokenRequirement.claims.first().required).isEqualTo(true)
        Assertions.assertThat(actualAccessTokenRequirement.required).isEqualTo(true)
        Assertions.assertThat(actualAccessTokenRequirement.encrypted).isEqualTo(true)
        Assertions.assertThat(actualAccessTokenRequirement.configuration).isEqualTo(expectedConfiguration)
        Assertions.assertThat(actualAccessTokenRequirement.redirectUri).isEqualTo(expectedRedirectUri)
        Assertions.assertThat(actualAccessTokenRequirement.resourceId).isEqualTo(expectedResourceId)
        Assertions.assertThat(actualAccessTokenRequirement.scope).isEqualTo(expectedScope)
    }
}