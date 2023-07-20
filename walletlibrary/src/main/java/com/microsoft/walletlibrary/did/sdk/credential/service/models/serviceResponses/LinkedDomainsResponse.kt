/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.credential.service.models.serviceResponses

import com.microsoft.walletlibrary.did.sdk.util.Constants.CONTEXT
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class LinkedDomainsResponse(
    @SerialName(CONTEXT)
    val context: String,
    @SerialName("linked_dids")
    val linkedDids: List<String>
)