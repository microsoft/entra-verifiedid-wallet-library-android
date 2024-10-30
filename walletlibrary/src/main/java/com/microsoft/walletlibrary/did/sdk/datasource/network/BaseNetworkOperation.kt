/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.datasource.network

import com.microsoft.walletlibrary.did.sdk.util.controlflow.LocalNetworkException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog
import com.microsoft.walletlibrary.did.sdk.util.logNetworkTime
import com.microsoft.walletlibrary.util.NetworkingException
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import java.io.IOException

/**
 * Base Network Operation class with default methods for all Network Operations.
 * T: The Object transformed from the response body.
 * fire method will just return Result.Success(transformedResponseBody: T)
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
        (exception as? NetworkingException)?.let {
            error ->
            SdkLog.i("HttpError: ${error.code} body: ${error.errorBody} cv: ${error.correlationId}", exception)
        }
        return Result.failure(exception)
    }

    fun <S> onRetry(): Result<S> {
        throw LocalNetworkException("Retry Not Supported.")
    }
}