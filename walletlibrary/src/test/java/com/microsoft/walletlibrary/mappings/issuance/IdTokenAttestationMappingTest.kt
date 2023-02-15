package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.models.attestations.ClaimAttestation
import com.microsoft.did.sdk.credential.service.models.attestations.IdTokenAttestation
import org.assertj.core.api.Assertions
import org.junit.Test

class IdTokenAttestationMappingTest {
    private val claimAttestations = mutableListOf(ClaimAttestation("name", true, "string"))
    private val expectedConfiguration: String = "https://testconfiguration.com"
    private val expectedClientId = "testClientId"
    private val expectedRedirectUri = "testRedirectUri"
    private val expectedScope = "testScope"
    private lateinit var actualIdTokenAttestation: IdTokenAttestation

    init {
        setupInput(claimAttestations, required = false, encrypted = false)
    }

    private fun setupInput(
        claimAttestations: List<ClaimAttestation>,
        required: Boolean,
        encrypted: Boolean
    ) {
        actualIdTokenAttestation = IdTokenAttestation(
            claimAttestations,
            expectedConfiguration,
            expectedClientId,
            required = required,
            expectedRedirectUri,
            expectedScope,
            encrypted = encrypted
        )
    }

    @Test
    fun idTokenMapping_RequiredAndEncryptedFalse_Succeeds() {
        // Arrange
        val expectedClaimName = "name"

        // Act
        val actualIdTokenRequirement = actualIdTokenAttestation.toIdTokenRequirement()

        // Assert
        Assertions.assertThat(actualIdTokenRequirement.claims.first().claim).isEqualTo(expectedClaimName)
        Assertions.assertThat(actualIdTokenRequirement.claims.first().required).isEqualTo(true)
        Assertions.assertThat(actualIdTokenRequirement.required).isEqualTo(false)
        Assertions.assertThat(actualIdTokenRequirement.encrypted).isEqualTo(false)
        Assertions.assertThat(actualIdTokenRequirement.configuration).isEqualTo(expectedConfiguration)
        Assertions.assertThat(actualIdTokenRequirement.clientId).isEqualTo(expectedClientId)
        Assertions.assertThat(actualIdTokenRequirement.redirectUri).isEqualTo(expectedRedirectUri)
        Assertions.assertThat(actualIdTokenRequirement.scope).isEqualTo(expectedScope)

    }

    @Test
    fun idTokenMapping_RequiredAndEncryptedTrue_Succeeds() {
        // Arrange
        setupInput(claimAttestations, required = true, encrypted = true)
        val expectedClaimName = "name"

        // Act
        val actualIdTokenRequirement = actualIdTokenAttestation.toIdTokenRequirement()

        // Assert
        Assertions.assertThat(actualIdTokenRequirement.claims.first().claim).isEqualTo(expectedClaimName)
        Assertions.assertThat(actualIdTokenRequirement.claims.first().required).isEqualTo(true)
        Assertions.assertThat(actualIdTokenRequirement.required).isEqualTo(true)
        Assertions.assertThat(actualIdTokenRequirement.encrypted).isEqualTo(true)
        Assertions.assertThat(actualIdTokenRequirement.configuration).isEqualTo(expectedConfiguration)
        Assertions.assertThat(actualIdTokenRequirement.clientId).isEqualTo(expectedClientId)
        Assertions.assertThat(actualIdTokenRequirement.redirectUri).isEqualTo(expectedRedirectUri)
        Assertions.assertThat(actualIdTokenRequirement.scope).isEqualTo(expectedScope)
    }

    @Test
    fun idTokenMapping_ListOfClaims_Succeeds() {
        // Arrange
        claimAttestations.add(ClaimAttestation("company", true, "string"))
        setupInput(claimAttestations, required = true, encrypted = true)
        val expectedClaimNames = listOf("name", "company")

        // Act
        val actualIdTokenRequirement = actualIdTokenAttestation.toIdTokenRequirement()

        // Assert
        Assertions.assertThat(actualIdTokenRequirement.claims.map { it.claim }
            .containsAll(expectedClaimNames)).isEqualTo(true)
        Assertions.assertThat(actualIdTokenRequirement.claims.first().required).isEqualTo(true)
        Assertions.assertThat(actualIdTokenRequirement.required).isEqualTo(true)
        Assertions.assertThat(actualIdTokenRequirement.encrypted).isEqualTo(true)
        Assertions.assertThat(actualIdTokenRequirement.configuration).isEqualTo(expectedConfiguration)
        Assertions.assertThat(actualIdTokenRequirement.clientId).isEqualTo(expectedClientId)
        Assertions.assertThat(actualIdTokenRequirement.redirectUri).isEqualTo(expectedRedirectUri)
        Assertions.assertThat(actualIdTokenRequirement.scope).isEqualTo(expectedScope)
    }
}