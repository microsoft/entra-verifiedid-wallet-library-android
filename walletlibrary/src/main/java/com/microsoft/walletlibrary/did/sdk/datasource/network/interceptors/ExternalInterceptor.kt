package com.microsoft.walletlibrary.did.sdk.datasource.network.interceptors

import com.microsoft.walletlibrary.interceptor.HttpInterceptor
import com.microsoft.walletlibrary.interceptor.HttpRequest
import com.microsoft.walletlibrary.interceptor.HttpResponse
import com.microsoft.walletlibrary.interceptor.MutableHttpHeaders
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.toImmutableMap

class ExternalInterceptor constructor(private val httpInterceptors: List<HttpInterceptor>) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // short circuit if no external interceptors
        if (httpInterceptors.isEmpty()) {
            return chain.proceed(chain.request())
        }

        // convert to abstract request
        val originalRequest = chain.request()
        val externalRequest = HttpRequest(
            uri = originalRequest.url.toString(),
            method = originalRequest.method,
            headers = this.convertHeaders(originalRequest.headers)
        )

        // allow each interceptor to mutate the headers
        httpInterceptors.forEach {
            it.beforeRequest(externalRequest)
        }
        // build a new request if required
        val newBuilder = originalRequest.newBuilder()
        var changed = false
        externalRequest.headers.forEach { (header, value) ->
            if (originalRequest.headers[header] != value) {
                changed = true
                newBuilder.header(header, value)
            }
        }

        // make the http call
        val response = if (changed) {
            val newRequest = newBuilder.build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }

        val externalResponse = HttpResponse(
            statusCode = response.code,
            statusMessage = response.message,
            headers = convertHeaders(response.headers).toImmutableMap()
        )

        // notify interceptors of response
        httpInterceptors.forEach {
            it.onResponse(externalResponse)
        }

        return response
    }

    private fun convertHeaders(headers: Headers): MutableHttpHeaders {
        val outputHeaders: MutableHttpHeaders = mutableMapOf()
        headers.names().forEach { header ->
            headers[header]?.let { value ->
                outputHeaders[header] = value
            }
        }
        return outputHeaders
    }
}