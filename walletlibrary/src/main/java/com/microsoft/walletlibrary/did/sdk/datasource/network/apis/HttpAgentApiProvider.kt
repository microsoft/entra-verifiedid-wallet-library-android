package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class HttpAgentApiProvider @Inject constructor(agent: IHttpAgent, json: Json) {
    // maybe refactor these into different interface apis?
    val presentationApis = HttpAgentPresentationApis(agent)

    val issuanceApis = HttpAgentIssuanceApi(agent, json)

    val revocationApis = HttpAgentRevocationApi(agent, json)

    val linkedDomainsApis = HttpAgentLinkedDomainsApi(agent, json)

    val identifierApi = HttpAgentIdentifierApi(agent, json)
}