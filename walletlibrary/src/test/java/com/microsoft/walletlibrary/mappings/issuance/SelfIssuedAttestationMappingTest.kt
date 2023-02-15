package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.models.attestations.ClaimAttestation
import com.microsoft.did.sdk.credential.service.models.attestations.SelfIssuedAttestation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SelfIssuedAttestationMappingTest {
    private lateinit var actualSelfIssuedAttestation: SelfIssuedAttestation
    private val claimAttestations = mutableListOf(ClaimAttestation("name", true, "string"))

    init {
        setupInput(claimAttestations, required = false, encrypted = false)
    }

    private fun setupInput(
        claimAttestations: List<ClaimAttestation>,
        required: Boolean,
        encrypted: Boolean
    ) {
        actualSelfIssuedAttestation = SelfIssuedAttestation(claimAttestations, required, encrypted)
    }

    @Test
    fun selfAttestedMapping_RequiredAndEncryptedFalse_Succeeds() {
        // Arrange
        val expectedClaimName = "name"
        val expectedClaimType = "string"

        // Act
        val actualSelfAttestedRequirement = actualSelfIssuedAttestation.toSelfAttestedClaimRequirement()

        // Assert
        assertThat(actualSelfAttestedRequirement.claim.first().claim).isEqualTo(expectedClaimName)
        assertThat(actualSelfAttestedRequirement.claim.first().type).isEqualTo(expectedClaimType)
        assertThat(actualSelfAttestedRequirement.claim.first().required).isEqualTo(true)
        assertThat(actualSelfAttestedRequirement.required).isEqualTo(false)
        assertThat(actualSelfAttestedRequirement.encrypted).isEqualTo(false)
    }

    @Test
    fun selfAttestedMapping_RequiredAndEncryptedTrue_Succeeds() {
        // Arrange
        setupInput(claimAttestations, required = true, encrypted = true)
        val expectedClaimName = "name"
        val expectedClaimType = "string"

        // Act
        val actualSelfAttestedRequirement = actualSelfIssuedAttestation.toSelfAttestedClaimRequirement()

        // Assert
        assertThat(actualSelfAttestedRequirement.claim.first().claim).isEqualTo(expectedClaimName)
        assertThat(actualSelfAttestedRequirement.claim.first().type).isEqualTo(expectedClaimType)
        assertThat(actualSelfAttestedRequirement.claim.first().required).isEqualTo(true)
        assertThat(actualSelfAttestedRequirement.required).isEqualTo(true)
        assertThat(actualSelfAttestedRequirement.encrypted).isEqualTo(true)
    }

    @Test
    fun selfAttestedMapping_ListOfClaims_Succeeds() {
        // Arrange
        claimAttestations.add(ClaimAttestation("company", true, "string"))
        setupInput(claimAttestations, required = true, encrypted = true)
        val expectedClaimNames = listOf("name", "company")
        val expectedClaimType = "string"

        // Act
        val actualSelfAttestedRequirement = actualSelfIssuedAttestation.toSelfAttestedClaimRequirement()

        // Assert
        assertThat(actualSelfAttestedRequirement.claim.map { it.claim }
            .containsAll(expectedClaimNames)).isEqualTo(true)
        assertThat(actualSelfAttestedRequirement.claim.map { it.type }
            .contains(expectedClaimType)).isEqualTo(true)
        assertThat(actualSelfAttestedRequirement.claim.first().required).isEqualTo(true)
        assertThat(actualSelfAttestedRequirement.required).isEqualTo(true)
        assertThat(actualSelfAttestedRequirement.encrypted).isEqualTo(true)
    }
}