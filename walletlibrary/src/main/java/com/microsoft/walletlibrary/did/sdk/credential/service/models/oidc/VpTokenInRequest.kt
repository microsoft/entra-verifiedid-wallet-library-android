/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc

import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.PresentationDefinition
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class VpTokenInRequest(

    @SerialName("presentation_definition")
    val presentationDefinition: PresentationDefinition
)
