package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.models.attestations.ClaimAttestation
import com.microsoft.did.sdk.credential.service.models.attestations.SelfIssuedAttestation
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirementOperator
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SelfIssuedAttestationMappingTest {
    private lateinit var actualSelfIssuedAttestation: SelfIssuedAttestation
    private val expectedClaimName = "name"
    private val claimAttestations =
        mutableListOf(ClaimAttestation(expectedClaimName, true, "string"))

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
    fun selfAttestedMapping_OneClaimRequiredTrue_ReturnsSelfAttestedRequirementRequiredTrue() {
        // Act
        val actualSelfAttestedRequirement = actualSelfIssuedAttestation.toRequirement()

        // Assert
        assertThat(actualSelfAttestedRequirement).isInstanceOf(SelfAttestedClaimRequirement::class.java)
        actualSelfAttestedRequirement as SelfAttestedClaimRequirement
        assertThat(actualSelfAttestedRequirement.claim).isEqualTo(expectedClaimName)
        assertThat(actualSelfAttestedRequirement.required).isEqualTo(true)
        assertThat(actualSelfAttestedRequirement.encrypted).isEqualTo(false)
    }

    @Test
    fun selfAttestedMapping_OneClaimRequiredFalse_ReturnsSelfAttestedRequirementRequiredFalse() {
        // Arrange
        val claimAttestation2 = ClaimAttestation(expectedClaimName, false, "string")
        setupInput(listOf(claimAttestation2), required = false, encrypted = true)
        val expectedClaimType = "string"

        // Act
        val actualSelfAttestedRequirement = actualSelfIssuedAttestation.toRequirement()

        // Assert
        assertThat(actualSelfAttestedRequirement).isInstanceOf(SelfAttestedClaimRequirement::class.java)
        actualSelfAttestedRequirement as SelfAttestedClaimRequirement
        assertThat(actualSelfAttestedRequirement.claim).isEqualTo(expectedClaimName)
        assertThat(actualSelfAttestedRequirement.required).isEqualTo(false)
        assertThat(actualSelfAttestedRequirement.encrypted).isEqualTo(true)
    }

    @Test
    fun selfAttestedMapping_ListOfClaims_ReturnsGroupRequirement() {
        // Arrange
        claimAttestations.add(ClaimAttestation("company", true, "string"))
        setupInput(claimAttestations, required = true, encrypted = true)
        val expectedClaimNames = listOf("name", "company")
        val expectedClaimType = "string"

        // Act
        val actualSelfAttestedRequirement = actualSelfIssuedAttestation.toRequirement()

        // Assert
        assertThat(actualSelfAttestedRequirement).isInstanceOf(GroupRequirement::class.java)
        actualSelfAttestedRequirement as GroupRequirement
        assertThat(actualSelfAttestedRequirement.requirements.size).isEqualTo(2)
        assertThat(actualSelfAttestedRequirement.requirements.map { it is SelfAttestedClaimRequirement }.size).isEqualTo(
            2
        )
        assertThat(actualSelfAttestedRequirement.requirements.map { requirement -> requirement as SelfAttestedClaimRequirement }
            .map { claim -> claim.claim }).containsAll(expectedClaimNames)
        assertThat(actualSelfAttestedRequirement.required).isEqualTo(true)
        assertThat(actualSelfAttestedRequirement.requirementOperator).isEqualTo(GroupRequirementOperator.ALL)
    }
}