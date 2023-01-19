package com.microsoft.walletlibrary

import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.walletlibrary.requests.FlowType
import com.microsoft.walletlibrary.requests.IssuanceRequest
import com.microsoft.walletlibrary.requests.requirements.ClaimRequirement
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.assertj.core.api.Assertions.assertThat

class VerifiedIdFlowTest {

    @Test
    fun testGetIssuanceRequest() {
        val expectedIssuanceRequest = IssuanceRequest("DID Team")
        val expectedSelfIssuedClaimRequirement = SelfAttestedClaimRequirement("", true, false, listOf(
            ClaimRequirement("name", true, ""), ClaimRequirement("company", true, "")
        ))
//        expectedIssuanceRequest.contracts.first().selfAttestedClaimRequirements = listOf(expectedSelfIssuedClaimRequirement)

        val contractUrl = "https://verifiedid.did.msidentity.com/v1.0/tenants/9c59be8b-bd18-45d9-b9d9-082bc07c094f/verifiableCredentials/contracts/OWM1OWJlOGItYmQxOC00NWQ5LWI5ZDktMDgyYmMwN2MwOTRmdmVyaWZpZWQgYnVzaW5lc3MgY2FyZA/manifest"
        val verifiedIdFlow = VerifiedIdFlow()
        verifiedIdFlow.init(InstrumentationRegistry.getInstrumentation().context)
        runBlocking {
            val actualIssuanceRequest = verifiedIdFlow.initiate(contractUrl, FlowType.ISSUANCE)
            assertThat(actualIssuanceRequest?.requester).isEqualTo(expectedIssuanceRequest.requester)
            assertThat(actualIssuanceRequest).isInstanceOf(IssuanceRequest::class.java)
            println((actualIssuanceRequest as IssuanceRequest).contracts.first().selfAttestedClaimRequirements.first().claim.flatMap { it.claim.toList() })
            assertThat(actualIssuanceRequest.contracts.first().selfAttestedClaimRequirements.flatMap { it.claim.toList() }).isEqualTo(expectedSelfIssuedClaimRequirement.claim.flatMap { it.claim.toList() })
        }
    }
}