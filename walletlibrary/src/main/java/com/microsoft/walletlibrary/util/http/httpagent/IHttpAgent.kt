package com.microsoft.walletlibrary.util.http.httpagent

abstract class IHttpAgent {

    open class HttpAgentError(val response: IResponse): Error() {}
    class ClientError(response: IResponse): HttpAgentError(response) {}
    class ServerError(response: IResponse): HttpAgentError(response) {}

    abstract suspend fun get(url: String, headers: Map<String, String>): Result<IResponse>

    abstract suspend fun post(url: String, headers: Map<String, String>, payload: ByteArray): Result<IResponse>
}