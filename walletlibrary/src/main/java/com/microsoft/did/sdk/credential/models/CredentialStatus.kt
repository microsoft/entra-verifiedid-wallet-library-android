/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.models

import kotlinx.serialization.Serializable

@Serializable
internal data class CredentialStatus(val id: String, val status: String) {
    var reason: String? = null
}