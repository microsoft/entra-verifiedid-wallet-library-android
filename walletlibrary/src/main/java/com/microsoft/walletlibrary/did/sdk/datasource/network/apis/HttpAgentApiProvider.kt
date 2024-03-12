package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import com.microsoft.walletlibrary.did.sdk.util.HttpAgentUtils
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
internal class HttpAgentApiProvider @Inject constructor(@Named("agent") agent: IHttpAgent, httpAgentUtils: HttpAgentUtils, json: Json) {
    // maybe refactor these into different interface apis?
    val presentationApis = HttpAgentPresentationApis(agent, httpAgentUtils)

    val issuanceApis = HttpAgentIssuanceApi(agent, httpAgentUtils, json)

    val revocationApis = HttpAgentRevocationApi(agent, httpAgentUtils, json)

    val linkedDomainsApis = HttpAgentLinkedDomainsApi(agent, httpAgentUtils, json)

    val identifierApi = HttpAgentIdentifierApi(agent, httpAgentUtils, json)

    val openId4VciApi = HttpAgentOpenId4VciApi(agent, httpAgentUtils, json)
}