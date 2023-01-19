package com.microsoft.walletlibrary

import com.microsoft.walletlibrary.requests.FlowType
import com.microsoft.walletlibrary.requests.IssuanceRequest
import kotlinx.coroutines.runBlocking
import org.junit.Test

class VerifiedIdFlowTest {
    val expectedIssuanceRequest = IssuanceRequest("")

//    @Test
//    fun `test initiate request`() {
//        val contractUrl = "https://verifiedid.did.msidentity.com/v1.0/tenants/9c59be8b-bd18-45d9-b9d9-082bc07c094f/verifiableCredentials/contracts/OWM1OWJlOGItYmQxOC00NWQ5LWI5ZDktMDgyYmMwN2MwOTRmdmVyaWZpZWQgYnVzaW5lc3MgY2FyZA/manifest"
//        runBlocking {
//            val actualIssuanceRequest = VerifiedIdFlow().initiate(contractUrl, FlowType.ISSUANCE)
//            println(actualIssuanceRequest?.requester)
//        }
//    }
}