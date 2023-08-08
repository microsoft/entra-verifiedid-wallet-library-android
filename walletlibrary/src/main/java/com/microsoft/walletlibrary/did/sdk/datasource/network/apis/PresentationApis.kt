/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

internal interface PresentationApis {
    
    @Headers("prefer: JWT-interop-profile-0.0.1")
    @GET
    suspend fun getRequest(@Url overrideUrl: String): Response<String>

    @FormUrlEncoded
    @POST
    suspend fun sendResponse(
        @Url overrideUrl: String,
        @Field("id_token") token: String,
        @Field("vp_token") vpToken: String,
        @Field("state") state: String?
    ): Response<String>
}