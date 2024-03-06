/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.util.controlflow

import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog
import kotlinx.coroutines.CancellationException
import kotlin.Result as KotlinResult

typealias Success = Boolean

internal sealed class Result<out S> {
    class Success<out S>(val payload: S) : Result<S>()
    class Failure(val payload: SdkException) : Result<Nothing>()
}

internal fun <T> KotlinResult<T>.toSDK(): Result<T> {
    if (this.isSuccess) {
        this.getOrNull()?.let {
            return Result.Success<T>(it)
        }
    }
    this.exceptionOrNull()?.let {
        if (it is SdkException) {
            return Result.Failure(it)
        } else {
            return Result.Failure(SdkException("Could not cast failure to SDK", it.cause))
        }
    }
    return Result.Failure(SdkException("Unknown exception"))
}

internal fun <S> Result<S>.toNative(): KotlinResult<S> {
    return when (this) {
        is Result.Success<S> -> {
            KotlinResult.success(this.payload)
        }
        is Result.Failure -> {
            KotlinResult.failure(this.payload)
        }
    }
}

internal fun <U, T> Result<T>.map(transform: (T) -> U): Result<U> =
    when (this) {
        is Result.Success -> Result.Success(transform(payload))
        is Result.Failure -> this
    }

internal fun <T> Result<T>.mapError(transform: (SdkException) -> SdkException): Result<T> =
    when (this) {
        is Result.Success -> this
        is Result.Failure -> Result.Failure(transform(payload))
    }

internal fun <U, T> Result<T>.andThen(transform: (T) -> Result<U>): Result<U> =
    when (this) {
        is Result.Success -> transform(payload)
        is Result.Failure -> this
    }

internal suspend fun <T> runResultTry(block: suspend RunResultTryContext.() -> Result<T>): Result<T> =
    try {
        RunResultTryContext().block()
    } catch (ex: RunResultTryAbortion) {
        Result.Failure(ex.error as SdkException)
    } catch (ex: CancellationException) {
        throw ex
    } catch (ex: SdkException) {
        SdkLog.w("Internal Sdk Exception", ex)
        Result.Failure(ex)
    } catch (ex: Exception) {
        SdkLog.e("Unhandled Sdk Exception", ex)
        Result.Failure(SdkException("Unhandled Exception", ex))
    }

internal class RunResultTryContext {
    fun <T> Result<T>.abortOnError(): T =
        when (this) {
            is Result.Success -> payload
            is Result.Failure -> throw RunResultTryAbortion(payload as Any)
        }
}

private class RunResultTryAbortion(val error: Any) : SdkException()