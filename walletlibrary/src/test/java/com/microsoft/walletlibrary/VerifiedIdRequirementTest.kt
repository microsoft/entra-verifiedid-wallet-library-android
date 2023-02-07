package com.microsoft.walletlibrary

import com.microsoft.did.sdk.credential.service.models.attestations.AcceptedIssuer
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.walletlibrary.mappings.toVerifiedIdRequirement
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VerifiedIdRequirementTest {
    private lateinit var actualPresentationAttestation: PresentationAttestation
    private val expectedCredentialType = "TestCredential"
    private val expectedAcceptedIssuers = listOf("TestIssuer1")
    private val expectedContracts = listOf("TestContract1")

    init {
        setupInput(expectedAcceptedIssuers, expectedContracts, required = false, encrypted = false)
    }

    private fun setupInput(
        issuers: List<String>,
        contracts: List<String>,
        required: Boolean,
        encrypted: Boolean
    ) {
        actualPresentationAttestation = PresentationAttestation(
            expectedCredentialType,
            issuers = issuers.map { AcceptedIssuer(it) },
            contracts,
            required = required,
            encrypted = encrypted,
            claims = emptyList()
        )
    }

    @Test
    fun `test mapping from vc sdk with required and encrypted as false`() {
        val expectedPurpose = "Testing"
        val expectedVerifiedIdRequirement = actualPresentationAttestation.toVerifiedIdRequirement()
        expectedVerifiedIdRequirement.purpose = expectedPurpose
        assertThat(expectedVerifiedIdRequirement.required).isEqualTo(false)
        assertThat(expectedVerifiedIdRequirement.encrypted).isEqualTo(false)
        assertThat(expectedVerifiedIdRequirement.types.contains(expectedCredentialType)).isEqualTo(
            true
        )
        assertThat(expectedVerifiedIdRequirement.acceptedIssuers.containsAll(expectedAcceptedIssuers)).isEqualTo(
            true
        )
        assertThat(
            expectedVerifiedIdRequirement.issuanceOptions?.credentialIssuerMetadata?.containsAll(
                expectedContracts
            )
        ).isEqualTo(true)
        assertThat(expectedVerifiedIdRequirement.purpose).isEqualTo(expectedPurpose)
    }

    @Test
    fun `test mapping from vc sdk with required and encrypted as true`() {
        setupInput(expectedAcceptedIssuers, expectedContracts, required = true, encrypted = true)
        val expectedPurpose = "Testing"
        val expectedVerifiedIdRequirement = actualPresentationAttestation.toVerifiedIdRequirement()
        expectedVerifiedIdRequirement.purpose = expectedPurpose
        assertThat(expectedVerifiedIdRequirement.required).isEqualTo(true)
        assertThat(expectedVerifiedIdRequirement.encrypted).isEqualTo(true)
        assertThat(expectedVerifiedIdRequirement.types.contains(expectedCredentialType)).isEqualTo(
            true
        )
        assertThat(expectedVerifiedIdRequirement.acceptedIssuers.containsAll(expectedAcceptedIssuers)).isEqualTo(
            true
        )
        assertThat(
            expectedVerifiedIdRequirement.issuanceOptions?.credentialIssuerMetadata?.containsAll(
                expectedContracts
            )
        ).isEqualTo(true)
        assertThat(expectedVerifiedIdRequirement.purpose).isEqualTo(expectedPurpose)
    }

    @Test
    fun `test mapping from vc sdk with empty contract list`() {
        setupInput(expectedAcceptedIssuers, emptyList(), required = true, encrypted = true)
        val expectedPurpose = "Testing"
        val expectedVerifiedIdRequirement = actualPresentationAttestation.toVerifiedIdRequirement()
        expectedVerifiedIdRequirement.purpose = expectedPurpose
        assertThat(expectedVerifiedIdRequirement.required).isEqualTo(true)
        assertThat(expectedVerifiedIdRequirement.encrypted).isEqualTo(true)
        assertThat(expectedVerifiedIdRequirement.types.contains(expectedCredentialType)).isEqualTo(
            true
        )
        assertThat(expectedVerifiedIdRequirement.acceptedIssuers.containsAll(expectedAcceptedIssuers)).isEqualTo(
            true
        )
        assertThat(
            expectedVerifiedIdRequirement.issuanceOptions?.credentialIssuerMetadata?.containsAll(
                expectedContracts
            )
        ).isEqualTo(false)
        assertThat(expectedVerifiedIdRequirement.purpose).isEqualTo(expectedPurpose)
    }

    @Test
    fun `test mapping from vc sdk with empty issuers list`() {
        setupInput(emptyList(), emptyList(), required = true, encrypted = true)
        val expectedPurpose = "Testing"
        val expectedVerifiedIdRequirement = actualPresentationAttestation.toVerifiedIdRequirement()
        expectedVerifiedIdRequirement.purpose = expectedPurpose
        assertThat(expectedVerifiedIdRequirement.required).isEqualTo(true)
        assertThat(expectedVerifiedIdRequirement.encrypted).isEqualTo(true)
        assertThat(expectedVerifiedIdRequirement.types.contains(expectedCredentialType)).isEqualTo(
            true
        )
        assertThat(expectedVerifiedIdRequirement.acceptedIssuers.containsAll(expectedAcceptedIssuers)).isEqualTo(
            false
        )
        assertThat(
            expectedVerifiedIdRequirement.issuanceOptions?.credentialIssuerMetadata?.containsAll(
                expectedContracts
            )
        ).isEqualTo(false)
        assertThat(expectedVerifiedIdRequirement.purpose).isEqualTo(expectedPurpose)
    }
}