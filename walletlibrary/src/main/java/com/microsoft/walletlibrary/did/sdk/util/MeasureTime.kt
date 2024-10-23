// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.util

import com.microsoft.walletlibrary.did.sdk.util.MetricsConstants.DURATION
import com.microsoft.walletlibrary.did.sdk.util.MetricsConstants.NAME
import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog
import com.microsoft.walletlibrary.util.NetworkingException
import com.microsoft.walletlibrary.util.http.httpagent.IResponse

internal object MetricsConstants {
    const val NAME = "eventName"
    const val DURATION = "duration_ms"
}

internal inline fun <R> logTime(name: String, block: () -> R): R {
    val start = System.currentTimeMillis()
    val result = block()
    val elapsedTime = System.currentTimeMillis() - start
    SdkLog.event(
        "DIDPerformanceMetrics", mapOf(
            NAME to name,
            DURATION to "$elapsedTime"
        )
    )
    return result
}

internal inline fun logNetworkTime(name: String, block: () -> Result<IResponse>): Result<IResponse> {
    val start = System.currentTimeMillis()
    var code = 0
    var cvResponse = "none"
    var requestId = "none"
    val result = block().onSuccess {
        cvResponse = it.headers[Constants.CORRELATION_VECTOR_HEADER] ?: "none"
        requestId = it.headers[Constants.REQUEST_ID_HEADER] ?: "none"
        code = it.status
    }.onFailure {
        (it as? NetworkingException)?.let {
            error ->
            cvResponse = error.correlationId ?: cvResponse
            code = error.statusCode?.toInt() ?: code
        }
    }
    val elapsedTime = System.currentTimeMillis() - start

    SdkLog.event(
        "DIDNetworkMetrics", mapOf(
            NAME to name,
            DURATION to "$elapsedTime",
            "CV_response" to cvResponse,
            "request_Id" to requestId,
            "isSuccessful" to "${result.isSuccess}",
            "code" to "$code"
        )
    )
    return result
}
