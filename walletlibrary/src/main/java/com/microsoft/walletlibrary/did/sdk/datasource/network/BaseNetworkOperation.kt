/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.datasource.network

import com.microsoft.walletlibrary.did.sdk.util.Constants.CORRELATION_VECTOR_HEADER
import com.microsoft.walletlibrary.did.sdk.util.Constants.REQUEST_ID_HEADER
import com.microsoft.walletlibrary.did.sdk.util.NetworkErrorParser
import com.microsoft.walletlibrary.did.sdk.util.controlflow.ClientException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.ForbiddenException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.LocalNetworkException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.NetworkException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.NotFoundException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.RedirectException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.ServiceUnreachableException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.UnauthorizedException
import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog
import com.microsoft.walletlibrary.did.sdk.util.logNetworkTime
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import java.io.IOException

/**
 * Base Network Operation class with default methods for all Network Operations.
 * S: The Response Body Type from the Service.
 * T: The Object transformed from the response body.
 * In default methods, S == T, for no transformation takes place.
 * fire method will just return Result.Success(responseBody: S)
 */
internal abstract class BaseNetworkOperation<T> {

    abstract val call: suspend () -> Result<IResponse>

    abstract suspend fun toResult(response: IResponse): Result<T>

    suspend inline fun fire(): Result<T> {
        try {
            logNetworkTime("${this::class.simpleName}") {
                call.invoke()
            }.onSuccess {
                return toResult(it)
            }.onFailure {
                return onFailure(it)
            }
        } catch (exception: IOException) {
            return Result.failure(LocalNetworkException("Failed to send request.", exception))
        }
        return Result.failure(SdkException("Failed to get a response"))
    }

    // TODO("what do we want our base to look like")
    open fun onFailure(exception: Throwable): Result<Nothing> {
        val response: IResponse = when (exception) {
            is IHttpAgent.ClientException -> {
                exception.response
            }
            is IHttpAgent.ServerException -> {
                exception.response
            }
            else -> return Result.failure(NetworkException("Unknown Status code", true))
        }
        val responseBody = response.body.decodeToString()
        val errorMessage = NetworkErrorParser.extractErrorMessage(responseBody) ?: responseBody
        val error = when (response.status) {
            301, 302, 308 -> RedirectException(errorMessage, false)
            401 -> UnauthorizedException(errorMessage, false)
            400, 402 -> ClientException(errorMessage, false)
            403 -> ForbiddenException(errorMessage, false)
            404 -> NotFoundException(errorMessage, false)
            500, 501, 502, 503 -> ServiceUnreachableException(errorMessage, true)
            else -> NetworkException("Unknown Status code", true)
        }
        error.errorCode = response.status.toString()
        error.correlationVector = response.headers[CORRELATION_VECTOR_HEADER]
        error.requestId = response.headers[REQUEST_ID_HEADER]
        error.errorBody = responseBody
        error.innerErrorCodes = NetworkErrorParser.extractInnerErrorsCodes(error.errorBody)
        SdkLog.i("HttpError: ${error.errorCode} body: ${error.errorBody} cv: ${error.correlationVector}", exception)
        return Result.failure(error)
    }

    fun <S> onRetry(): Result<S> {
        throw LocalNetworkException("Retry Not Supported.")
    }
}