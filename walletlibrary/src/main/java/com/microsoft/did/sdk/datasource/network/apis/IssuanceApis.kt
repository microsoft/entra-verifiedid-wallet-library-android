/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network.apis

import com.microsoft.did.sdk.credential.service.models.serviceResponses.ContractServiceResponse
import com.microsoft.did.sdk.credential.service.models.serviceResponses.IssuanceServiceResponse
import retrofit2.Response
import retrofit2.http.*

internal interface IssuanceApis {

    @Headers("x-ms-sign-contract: true")
    @GET
    suspend fun getContract(@Url overrideUrl: String): Response<ContractServiceResponse>

    @POST
    suspend fun sendResponse(@Url overrideUrl: String, @Body body: String): Response<IssuanceServiceResponse>

    @Headers("Content-Type: application/json")
    @POST
    suspend fun sendCompletionResponse(@Url overrideUrl: String, @Body body: String): Response<Unit>
}