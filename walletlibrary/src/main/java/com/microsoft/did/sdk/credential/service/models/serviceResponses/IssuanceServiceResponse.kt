/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.serviceResponses

import kotlinx.serialization.Serializable

@Serializable
internal data class IssuanceServiceResponse(
    val vc: String
)