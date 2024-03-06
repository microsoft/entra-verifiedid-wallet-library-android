package com.microsoft.walletlibrary.util.http.httpagent

abstract class IHttpAgent {

    open class HttpAgentException(val response: IResponse): Exception()
    class ClientException(response: IResponse): HttpAgentException(response)
    class ServerException(response: IResponse): HttpAgentException(response)

    abstract suspend fun get(url: String, headers: Map<String, String>): Result<IResponse>

    abstract suspend fun post(url: String, headers: Map<String, String>, payload: ByteArray): Result<IResponse>
}