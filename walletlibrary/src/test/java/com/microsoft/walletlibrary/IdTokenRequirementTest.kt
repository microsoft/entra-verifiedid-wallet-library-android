package com.microsoft.walletlibrary

import com.microsoft.did.sdk.credential.service.models.attestations.ClaimAttestation
import com.microsoft.did.sdk.credential.service.models.attestations.IdTokenAttestation
import com.microsoft.walletlibrary.mappings.toIdTokenRequirement
import org.assertj.core.api.Assertions
import org.junit.Test

class IdTokenRequirementTest {
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
    fun `test mapping from vc sdk with required and encrypted as false`() {
        val expectedIdTokenRequirement = actualIdTokenAttestation.toIdTokenRequirement()
        val expectedClaimName = "name"
        Assertions.assertThat(expectedIdTokenRequirement.claims.first().claim).isEqualTo(expectedClaimName)
        Assertions.assertThat(expectedIdTokenRequirement.claims.first().required).isEqualTo(true)
        Assertions.assertThat(expectedIdTokenRequirement.required).isEqualTo(false)
        Assertions.assertThat(expectedIdTokenRequirement.encrypted).isEqualTo(false)
        Assertions.assertThat(expectedIdTokenRequirement.configuration).isEqualTo(expectedConfiguration)
        Assertions.assertThat(expectedIdTokenRequirement.clientId).isEqualTo(expectedClientId)
        Assertions.assertThat(expectedIdTokenRequirement.redirectUri).isEqualTo(expectedRedirectUri)
        Assertions.assertThat(expectedIdTokenRequirement.scope).isEqualTo(expectedScope)

    }

    @Test
    fun `test mapping from vc sdk with required and encrypted as true`() {
        setupInput(claimAttestations, required = true, encrypted = true)
        val expectedIdTokenRequirement = actualIdTokenAttestation.toIdTokenRequirement()
        val expectedClaimName = "name"
        Assertions.assertThat(expectedIdTokenRequirement.claims.first().claim).isEqualTo(expectedClaimName)
        Assertions.assertThat(expectedIdTokenRequirement.claims.first().required).isEqualTo(true)
        Assertions.assertThat(expectedIdTokenRequirement.required).isEqualTo(true)
        Assertions.assertThat(expectedIdTokenRequirement.encrypted).isEqualTo(true)
        Assertions.assertThat(expectedIdTokenRequirement.configuration).isEqualTo(expectedConfiguration)
        Assertions.assertThat(expectedIdTokenRequirement.clientId).isEqualTo(expectedClientId)
        Assertions.assertThat(expectedIdTokenRequirement.redirectUri).isEqualTo(expectedRedirectUri)
        Assertions.assertThat(expectedIdTokenRequirement.scope).isEqualTo(expectedScope)
    }

    @Test
    fun `test mapping from vc sdk with list of claims`() {
        claimAttestations.add(ClaimAttestation("company", true, "string"))
        setupInput(claimAttestations, required = true, encrypted = true)
        val expectedIdTokenRequirement = actualIdTokenAttestation.toIdTokenRequirement()
        val expectedClaimNames = listOf("name", "company")
        Assertions.assertThat(expectedIdTokenRequirement.claims.map { it.claim }
            .containsAll(expectedClaimNames)).isEqualTo(true)
        Assertions.assertThat(expectedIdTokenRequirement.claims.first().required).isEqualTo(true)
        Assertions.assertThat(expectedIdTokenRequirement.required).isEqualTo(true)
        Assertions.assertThat(expectedIdTokenRequirement.encrypted).isEqualTo(true)
        Assertions.assertThat(expectedIdTokenRequirement.configuration).isEqualTo(expectedConfiguration)
        Assertions.assertThat(expectedIdTokenRequirement.clientId).isEqualTo(expectedClientId)
        Assertions.assertThat(expectedIdTokenRequirement.redirectUri).isEqualTo(expectedRedirectUri)
        Assertions.assertThat(expectedIdTokenRequirement.scope).isEqualTo(expectedScope)
    }
}