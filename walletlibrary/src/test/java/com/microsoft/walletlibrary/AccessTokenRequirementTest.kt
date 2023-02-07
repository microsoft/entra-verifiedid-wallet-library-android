package com.microsoft.walletlibrary

import com.microsoft.did.sdk.credential.service.models.attestations.AccessTokenAttestation
import com.microsoft.did.sdk.credential.service.models.attestations.ClaimAttestation
import com.microsoft.walletlibrary.mappings.toAccessTokenRequirement
import org.assertj.core.api.Assertions
import org.junit.Test

class AccessTokenRequirementTest {
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
    fun `test mapping from vc sdk with required and encrypted as false`() {
        val expectedAccessTokenRequirement = actualAccessTokenAttestation.toAccessTokenRequirement()
        val expectedClaimName = "name"
        Assertions.assertThat(expectedAccessTokenRequirement.claims.first().claim).isEqualTo(expectedClaimName)
        Assertions.assertThat(expectedAccessTokenRequirement.claims.first().required).isEqualTo(true)
        Assertions.assertThat(expectedAccessTokenRequirement.required).isEqualTo(false)
        Assertions.assertThat(expectedAccessTokenRequirement.encrypted).isEqualTo(false)
        Assertions.assertThat(expectedAccessTokenRequirement.configuration).isEqualTo(expectedConfiguration)
        Assertions.assertThat(expectedAccessTokenRequirement.redirectUri).isEqualTo(expectedRedirectUri)
        Assertions.assertThat(expectedAccessTokenRequirement.resourceId).isEqualTo(expectedResourceId)
        Assertions.assertThat(expectedAccessTokenRequirement.scope).isEqualTo(expectedScope)

    }

    @Test
    fun `test mapping from vc sdk with required and encrypted as true`() {
        setupInput(claimAttestations, required = true, encrypted = true)
        val expectedAccessTokenRequirement = actualAccessTokenAttestation.toAccessTokenRequirement()
        val expectedClaimName = "name"
        Assertions.assertThat(expectedAccessTokenRequirement.claims.first().claim).isEqualTo(expectedClaimName)
        Assertions.assertThat(expectedAccessTokenRequirement.claims.first().required).isEqualTo(true)
        Assertions.assertThat(expectedAccessTokenRequirement.required).isEqualTo(true)
        Assertions.assertThat(expectedAccessTokenRequirement.encrypted).isEqualTo(true)
        Assertions.assertThat(expectedAccessTokenRequirement.configuration).isEqualTo(expectedConfiguration)
        Assertions.assertThat(expectedAccessTokenRequirement.redirectUri).isEqualTo(expectedRedirectUri)
        Assertions.assertThat(expectedAccessTokenRequirement.resourceId).isEqualTo(expectedResourceId)
        Assertions.assertThat(expectedAccessTokenRequirement.scope).isEqualTo(expectedScope)
    }

    @Test
    fun `test mapping from vc sdk with list of claims`() {
        claimAttestations.add(ClaimAttestation("company", true, "string"))
        setupInput(claimAttestations, required = true, encrypted = true)
        val expectedAccessTokenRequirement = actualAccessTokenAttestation.toAccessTokenRequirement()
        val expectedClaimNames = listOf("name", "company")
        Assertions.assertThat(expectedAccessTokenRequirement.claims.map { it.claim }
            .containsAll(expectedClaimNames)).isEqualTo(true)
        Assertions.assertThat(expectedAccessTokenRequirement.claims.first().required).isEqualTo(true)
        Assertions.assertThat(expectedAccessTokenRequirement.required).isEqualTo(true)
        Assertions.assertThat(expectedAccessTokenRequirement.encrypted).isEqualTo(true)
        Assertions.assertThat(expectedAccessTokenRequirement.configuration).isEqualTo(expectedConfiguration)
        Assertions.assertThat(expectedAccessTokenRequirement.redirectUri).isEqualTo(expectedRedirectUri)
        Assertions.assertThat(expectedAccessTokenRequirement.resourceId).isEqualTo(expectedResourceId)
        Assertions.assertThat(expectedAccessTokenRequirement.scope).isEqualTo(expectedScope)
    }
}